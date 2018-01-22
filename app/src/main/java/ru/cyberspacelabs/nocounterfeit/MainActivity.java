package ru.cyberspacelabs.nocounterfeit;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.client.android.CaptureActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.cyberspacelabs.nocounterfeit.contracts.geocoding.GeocodingService;
import ru.cyberspacelabs.nocounterfeit.json.LongIntegerJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.json.OptionalDateJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.logging.GeoPosition;
import ru.cyberspacelabs.nocounterfeit.services.GoogleReverseGeocoder;
import ru.cyberspacelabs.nocounterfeit.services.MapQuestReverseGeocoder;
import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class MainActivity extends ConfigurableActivity implements LocationListener {
	private static int REQUEST_SCAN_INIT = 0x00000001;
	private static int REQUEST_SCAN_FINAL = 0x00000002;
	private static String FORMATS_BARCODE = "UPC_A,UPC_E,EAN_13,CODE_39,CODE_93,CODE_128,ITF,RSS_14,RSS_EXPANDED";
	private static String FORMATS_QRCODE = "QR_CODE";
	private static String FORMATS_ALL = "QR_CODE,UPC_A,UPC_E,EAN_13,CODE_39,CODE_93,CODE_128,ITF,RSS_14,RSS_EXPANDED";

	private static long MIN_DISTANCE_DELTA = 10; // 10 meters
	private static long MIN_TIME_DELTA = 15000; // 1 min
	private static final String LOG_LOCATION = "location.txt";

	private GoogleMap googleMap;
	private UiSettings mUiSettings;

	private Button buttonScan;
	private TextView locationStatus;
	private String qrCodeValue;
	private String barCodeValue;
	private LocationManager locationManager;
	private Gson gson;
	private SimpleDateFormat timestampFormat;


	private void resolveControls() {
		buttonScan = (Button) findViewById(R.id.button_scan);
		buttonScan.setEnabled(false);
		locationStatus = (TextView) findViewById(R.id.text_location);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	}

	/**
	 * Initialises the mapview
	 */
	private void createMapView(){
		/**
		 * Catch the null pointer exception that
		 * may be thrown when initialising the map
		 */
		try {
			if(null == googleMap){
				googleMap = ((MapFragment) getFragmentManager().findFragmentById(
						R.id.mapView)).getMap();
				googleMap.getUiSettings().setMyLocationButtonEnabled(true);
				if (ActivityCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
						|| ActivityCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
					return;
				}
				mUiSettings = googleMap.getUiSettings();
				googleMap.setMyLocationEnabled(true);
				mUiSettings.setZoomControlsEnabled(true);

				if (locationManager != null) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_DELTA,
							MIN_DISTANCE_DELTA, this);
					Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();
						googleMap.moveCamera(CameraUpdateFactory
								.newLatLngZoom(new LatLng(latitude, longitude), 17));
						if(null != googleMap) {
							googleMap.addMarker(new MarkerOptions()
									.position(new LatLng(latitude, longitude))
									.title("Marker")
									.draggable(true)
							);
						}
					}
				}
				if(null == googleMap) {
					Toast.makeText(getApplicationContext(),
							"Error creating map",Toast.LENGTH_SHORT).show();
				}
			}
		} catch (NullPointerException exception){
			Log.e("mapApp", exception.toString());
		}
	}

	/**
	 * Adds a marker to the map
	 */

	private void addMarker(){

		/** Make sure that the map has been initialised **/
		if(null != googleMap){
			googleMap.addMarker(new MarkerOptions()
					.position(new LatLng(0, 0))
					.title("Marker")
					.draggable(true)
			);
		}
	}

	private String formatLocation(Location location) {
		if (location == null)
			return "";
		return String.format(
				"Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
				location.getLatitude(), location.getLongitude(), new Date(
						location.getTime()));
	}

	private Intent createScanIntent(CharSequence prompt) {
		Intent intent = new Intent(this, CaptureActivity.class);
		intent.setAction("com.google.zxing.client.android.SCAN");
		intent.putExtra("SCAN_WIDTH", 1280);
		intent.putExtra("SCAN_HEIGHT", 720);
		if (prompt != null) {
			intent.putExtra("PROMPT_MESSAGE", prompt);
		}
		return intent;
	}

	private void bindActions() {
		buttonScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				qrCodeValue = "";
				barCodeValue = "";
				Intent intent = createScanIntent(getResources().getText(R.string.prompt_locate_protection_field));
				intent.putExtra("SCAN_FORMATS", FORMATS_ALL);
				startActivityForResult(intent, REQUEST_SCAN_INIT);
			}
		});
	}

	private String processScanResultIntent(Intent resultIntent) {
		String contents = resultIntent.getStringExtra("SCAN_RESULT");
		String format = resultIntent.getStringExtra("SCAN_RESULT_FORMAT");
		String nf = "";
		if (FORMATS_QRCODE.equalsIgnoreCase(format)) {
			qrCodeValue = contents;
			nf = FORMATS_BARCODE;
		} else {
			barCodeValue = contents;
			nf = FORMATS_QRCODE;
		}
		return nf;
	}

	private void startLocation() {
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		if (!isGPSEnabled && !isNetworkEnabled) {
			Util.appendLog(this, LOG_LOCATION, createTimestamp() + ": No location providers enabled");
		} else {
			if (ActivityCompat.checkSelfPermission(this,
					Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
					&& ActivityCompat.checkSelfPermission(this,
							Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				Util.appendLog(this, LOG_LOCATION,
						createTimestamp() + ": Runtime permissions for location has been revoked");
				Util.openRuntimeSettings(this, getResources().getString(R.string.toast_runtime_settings));
				return;
			}
			if (isNetworkEnabled) {
				Log.d("Location", "Coarse Enabled");
				if (locationManager != null) {
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_DELTA,
							MIN_DISTANCE_DELTA, this);
					Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (location != null) {
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();
						Log.i("Location", "Last lock-on: " + latitude + "," + longitude);
						getApplicationState().setLocation(location);
					}
				}
			}
			// if GPS Enabled get lat/long using GPS Services
			if (isGPSEnabled) {
				Log.d("Location", "GPS Enabled");
				if (locationManager != null) {
					locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_DELTA,
							MIN_DISTANCE_DELTA, this);
					Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					if (location != null) {
						double latitude = location.getLatitude();
						double longitude = location.getLongitude();
						Log.i("Location", "Last GPS fix: " + latitude + "," + longitude);
						getApplicationState().setLocation(location);
					}
				}

			}
		}
	}

	private String serializeGeoPosition(GeoPosition position) {
		return gson.toJson(position);
	}

	private String createTimestamp() {
		return timestampFormat.format(new Date());
	}

	private void resolveLocation() {
		getApplicationState().setCity("");
		getApplicationState().setDistrict("");
		buttonScan.setEnabled(false);
		locationStatus.setText(R.string.location_failure);
		Runnable task = new Runnable() {
			@Override
			public void run() {
				// lock-on established
				if (getApplicationState().getLocation() != null && getApplicationState().getGeoCoder() != null) {
					GeoPosition ap = new GeoPosition(getApplicationState().getLocation());
					getApplicationState().getGeoCoder().setLocation(getApplicationState().getLocation().getLatitude(),
							getApplicationState().getLocation().getLongitude());
					String address = getApplicationState().getGeoCoder().getPrettyAddress();
					ap.setGeoCoding(getApplicationState().getGeoCoder().getLastDump());
					// geocoding succeeded
					if (address != null && !address.isEmpty()) {
						String city = getApplicationState().getGeoCoder().getCity();
						String district = getApplicationState().getGeoCoder().getDistrict();
						// toponimic data not empty
						// setting located names for log
						ap.setCity(city);
						ap.setDistrict(district);
						if (!city.isEmpty() || !district.isEmpty()) {
							final String locationName = address;
							// and we are need no dump, reset it
							ap.setGeoCoding("");
							// and anyway location locked on and resolved
							getApplicationState().setCity(city);
							getApplicationState().setDistrict(district);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									locationStatus.setText(locationName);
									buttonScan.setEnabled(true);
								}
							});
						}
					}
					// finally write to log if we fail to locate
					if (!ap.getGeoCoding().isEmpty()) {
						Util.appendLog(MainActivity.this, LOG_LOCATION, createTimestamp() + ":\r\n"
								+ serializeGeoPosition(ap) + "\r\n---------------------------------------\r\n");
					}
				} else {
					Util.appendLog(MainActivity.this, LOG_LOCATION,
							createTimestamp() + ": Lock-on failed: location "
									+ (getApplicationState().getLocation() == null ? "null"
											: getApplicationState().getLocation().getLatitude() + ","
													+ getApplicationState().getLocation().getLongitude()));
				}
			}
		};
		Util.runBackgroundTask(task);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss ZZZ").create();
		timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
		resolveControls();
		bindActions();
		setTitle(R.string.activity_main_title);
		createGeocoder();

		createMapView();
		//addMarker();

		locationStatus.setText(R.string.location_in_progress);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
		case 1:
			if (resultCode == RESULT_OK) {

				String format = processScanResultIntent(intent);
				Intent finalIntent = createScanIntent(
						FORMATS_QRCODE.equalsIgnoreCase(format) ? getResources().getText(R.string.prompt_locate_qr)
								: getResources().getText(R.string.prompt_locate_barcode));
				finalIntent.putExtra("SCAN_FORMATS", format);
				startActivityForResult(finalIntent, REQUEST_SCAN_FINAL);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				Log.i("App", "Scan unsuccessful");
			}
			break;
		case 2:
			if (resultCode == RESULT_OK) {
				processScanResultIntent(intent);
				if (qrCodeValue == null || qrCodeValue.trim().isEmpty()) {
					Toast.makeText(this, R.string.error_qr_failed, Toast.LENGTH_SHORT).show();
					return;
				}
				if (barCodeValue == null || barCodeValue.trim().isEmpty()) {
					Toast.makeText(this, R.string.error_barcode_failed, Toast.LENGTH_SHORT).show();
					return;
				}
				Intent scanResultIntent = new Intent(this, ScanResultActivity.class);
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_BARCODE, Util.normalizeBarcode(barCodeValue));
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_QR_CODE, qrCodeValue);
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_CITY, getApplicationState().getCity());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_DISTRICT,
						getApplicationState().getDistrict());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_ZIP,
						getApplicationState().getGeoCoder().getZIP());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_COUNTRY,
						getApplicationState().getGeoCoder().getCountry());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_BUILDING,
						getApplicationState().getGeoCoder().getBuildingNumber());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_STREET,
						getApplicationState().getGeoCoder().getStreet());
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_LATITUDE,
						getApplicationState().getLocation() != null ? getApplicationState().getLocation().getLatitude()
								: -1);
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_LONGITUDE,
						getApplicationState().getLocation() != null ? getApplicationState().getLocation().getLongitude()
								: -1);
				scanResultIntent.putExtra(ScanResultActivity.INTENT_EXTRA_DOP,
						getApplicationState().getLocation() != null ? getApplicationState().getLocation().getAccuracy()
								: -1);
				startActivity(scanResultIntent);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
				Log.i("App", "Scan unsuccessful");
			}
			break;
		default:
			return;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		getApplicationState().setLocation(location);
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		Log.i("Location", "Updated: " + latitude + "," + longitude);
		this.locationStatus.setText(Double.toString(latitude) + "," + Double.toString(longitude));
		resolveLocation();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("Location", "Provider state changed: " + provider + ":" + status);
	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.i("Location", "Provider enabled: " + provider);
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.i("Location", "Provider disabled: " + provider);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!Util.isConnectivityEnabled(this) || !Util.isLocationEnabled(locationManager)) {
			startActivity(new Intent(this, OffGridActivity.class));
		}
		buttonScan.setEnabled(false);
		createGeocoder();
		startLocation();
	}

	private void createGeocoder() {
		if (getApplicationState().getGeoCoder() == null) {
			getApplicationState().setGeoCoder(new GoogleReverseGeocoder().setLanguage(Util.getApplicationLocale(this))
					.setKey("AIzaSyCx2xaHkO2Vc18jojwZhBKljWl_ZOHiItQ"));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Util.appendLog(this, LOG_LOCATION, createTimestamp() + ": Nothing to unsubscribe");
			return;
		}
		locationManager.removeUpdates(this);
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle(R.string.label_dialog_close_title).setMessage(R.string.label_dialog_close_text)
				.setPositiveButton(R.string.label_dialog_close_yes, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setExitPending(true);
						MainActivity.super.onBackPressed();

					}

				}).setNegativeButton(R.string.label_dialog_close_no, null).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.default_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Операции для выбранного пункта меню
		switch (item.getItemId()) {
		case R.id.menu_item_config:
			startActivity(new Intent(this, ConfigActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
