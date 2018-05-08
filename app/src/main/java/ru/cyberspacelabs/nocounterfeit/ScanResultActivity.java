package ru.cyberspacelabs.nocounterfeit;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.cyberspacelabs.nocounterfeit.async.Result;
import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationServiceAsync;
import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationServiceCallback;
import ru.cyberspacelabs.nocounterfeit.dto.NearbyScans;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionRegistration;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResult;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResults;
import ru.cyberspacelabs.nocounterfeit.services.ProtectionWebService;
import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class ScanResultActivity extends ConfigurableActivity implements ProtectionValidationServiceCallback {
    public static final String INTENT_EXTRA_QR_CODE = "QR_CODE";
    public static final String INTENT_EXTRA_BARCODE = "BARCODE";
    public static final String INTENT_EXTRA_LATITUDE = "location.latitude";
    public static final String INTENT_EXTRA_LONGITUDE = "location.longitude";
    public static final String INTENT_EXTRA_DOP = "location.DOP";
    public static final String INTENT_EXTRA_CITY = "location.city";
    public static final String INTENT_EXTRA_DISTRICT = "location.district";
    public static final String INTENT_EXTRA_COUNTRY = "location.country";
    public static final String INTENT_EXTRA_ZIP = "location.zip";
    public static final String INTENT_EXTRA_STREET = "location.street";
    public static final String INTENT_EXTRA_BUILDING = "location.building";

    private ProtectionValidationServiceAsync validationService;
    private UUID clientID;
    private String qr;
    private String barcode;
    private TextView viewDescription;
    private TextView viewStatus;
    private TextView viewLocation;
    private TextView viewCounterfeitLabel;
    private TextView viewLabelShopAddress;
    private ImageView imageAccent;
    private LinearLayout containerStatus;
    private ProgressBar progressBarValidation;
    private AtomicInteger requests;
    private ProtectionRegistration protectionRegistration;
    private NearbyScans nearbyScans;
    private ScanResults registeredLocations;
    private String deviceId;
    private MapView mapView;
    private GoogleMap map;
    private SimpleDateFormat outDateFormat;
    private CardView cardView;
    private ImageView validationOkImage;
    private LinearLayout idResultLayout;
    private RelativeLayout vlaidationErrorLayout;
    private TextView errorText;
    private CardView errorCardView;
    private View mapFragment;
    private TextView scanInProcess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        outDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        deviceId = Util.getRegisteredId(this);
        clientID = UUID.randomUUID();
        ProtectionWebService protectionWebService = new ProtectionWebService();
        validationService = protectionWebService;
        validationService.addCallback(this);
        setTitle(R.string.activity_result_title);
        requests = new AtomicInteger(3);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        resolveControls();
        processIntent();
    }

    private ScanResult getScanResult() {
        ScanResult result = new ScanResult();
        Intent request = getIntent();

        result.setCity(request.getStringExtra(INTENT_EXTRA_CITY));
        result.setHdop(request.getFloatExtra(INTENT_EXTRA_DOP, -1));
        result.setBarcode(request.getStringExtra(INTENT_EXTRA_BARCODE));
        result.setLatitude(request.getDoubleExtra(INTENT_EXTRA_LATITUDE, -1));
        result.setQrText(Util.decode(request.getStringExtra(INTENT_EXTRA_QR_CODE)));
        result.setDistrict(request.getStringExtra(INTENT_EXTRA_DISTRICT));
        result.setLongitude(request.getDoubleExtra(INTENT_EXTRA_LONGITUDE, -1));
        result.setStreet(request.getStringExtra(INTENT_EXTRA_STREET));
        result.setBuilding(request.getStringExtra(INTENT_EXTRA_BUILDING));
        result.setCountry(request.getStringExtra(INTENT_EXTRA_COUNTRY));
        result.setZip(request.getStringExtra(INTENT_EXTRA_ZIP));
        result.setDeviceId(deviceId);
        return result;
    }

    private void processIntent() {
        Intent request = getIntent();
        qr = request.getStringExtra(INTENT_EXTRA_QR_CODE);
        barcode = request.getStringExtra(INTENT_EXTRA_BARCODE);
        viewDescription.setText("");
        viewStatus.setText("");
        validationService.getProtectionRegistration(getClientID(), barcode);
        validationService.getNearbyScans(getClientID(), getScanResult());
        validationService.getRegisteredLocations(getClientID(), getScanResult());
    }
//	private void clearActivity(){
//		cardView.setVisibility(View.GONE); // убираем карточку
//		mapView.setVisibility(View.GONE); // убираем карту
//		validationOkImage.setVisibility(View.GONE);
//	}

    private void resolveControls() {
        vlaidationErrorLayout = (RelativeLayout) findViewById(R.id.vlaidationErrorLayout);

        cardView = (CardView) findViewById(R.id.card_view);
        errorCardView = (CardView) findViewById(R.id.error_CardView);

        idResultLayout = (LinearLayout) findViewById(R.id.resultLayout);
        validationOkImage = (ImageView) findViewById(R.id.validationOkImage);

        viewLabelShopAddress = (TextView) findViewById(R.id.labelRegisteredShop);
        viewLabelShopAddress.setTypeface(SingletonFonts.getInstance(this).getBoldFont());

        viewDescription = (TextView) findViewById(R.id.textDescription);
        viewDescription.setTypeface(SingletonFonts.getInstance(this).getNormalFont());

        viewStatus = (TextView) findViewById(R.id.textStatus);
        viewStatus.setTypeface(SingletonFonts.getInstance(this).getBoldFont());

        errorText = (TextView) findViewById(R.id.errorText);
        errorText.setTypeface(SingletonFonts.getInstance(this).getBoldFont());

        containerStatus = (LinearLayout) findViewById(R.id.containerValidationResult);
        progressBarValidation = (ProgressBar) findViewById(R.id.progressBarCodeValidation);
        viewLocation = (TextView) findViewById(R.id.textShopLocation);
        viewLocation.setTypeface(SingletonFonts.getInstance(this).getNormalFont());

        viewCounterfeitLabel = (TextView) findViewById(R.id.textLabelCounterfeit);
        viewCounterfeitLabel.setTypeface(SingletonFonts.getInstance(this).getNormalFont());

        imageAccent = (ImageView) findViewById(R.id.imageCounterfeitAccent);


        scanInProcess = (TextView) findViewById(R.id.scanInProcess);


        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
        MapsInitializer.initialize(this);

    }

    // Web service callback
    @Override
    public UUID getClientID() {
        return clientID;
    }

    @Override
    public void onProtectionRegistration(final Result<ProtectionRegistration> result) {
        int order = requests.decrementAndGet();
        protectionRegistration = result.getValue();
        if (order == 0) {
            processResult();
        }
    }

    @Override
    public void onNearbyScans(Result<NearbyScans> result) {
        int order = requests.decrementAndGet();
        nearbyScans = result.getValue();
        if (order == 0) {
            processResult();
        }
    }

    @Override
    public void onRegisteredLocations(Result<ScanResults> result) {
        int order = requests.decrementAndGet();
        registeredLocations = result.getValue();
        if (order == 0) {
            processResult();
        }
    }

    private void processResult() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarValidation.setVisibility(View.GONE);
                map.clear();
                // when label registered ...
                if (protectionRegistration != null && !protectionRegistration.getQrText().isEmpty()) {
                    // show the item name ...
                    viewDescription.setText(protectionRegistration.getProductName());
                    String decoded = Util.decode(qr);
                    // check that QRs are matched ...
                    if (protectionRegistration.getQrText().equalsIgnoreCase(decoded)) {
                        // if manager scanned label for activation yet ...
                        if (registeredLocations != null && !registeredLocations.getHistory().isEmpty()) {
                            if (protectionRegistration.getExpired().getTime() > System.currentTimeMillis()) {
                                displayShopAddress();
                                showLocationsOnMap();
                                validationOkImage.setVisibility(View.VISIBLE);
                                scanInProcess.setVisibility(View.GONE);
                                // ... and attract attention
                                viewStatus.setText(R.string.status_ok);
                                viewStatus.setTextColor(getResources().getColor(R.color.label_legal));
                                viewDescription.setText(protectionRegistration.getProductName() + "\r\n"
                                        + getResources().getString(R.string.label_validation_expiration) + " "
                                        + outDateFormat.format(protectionRegistration.getExpired()));
                            } else {
                                // product expired
                                warnForExpiration();
                            }
                        } else {
                            // ... if not activated yet
                            warnForInactive();
                        }

                    } else {
                        // ,,, not matched treated as counterfeit
                        warnForCounterfeit();
                        Util.appendLog(ScanResultActivity.this, "scan.txt", barcode + ":\"" + decoded
                                + "\" not paired: \"" + protectionRegistration.getQrText() + "\"\r\n");
                    }

                } else {
                    // purely absent
                    warnForCounterfeit();
                    Util.appendLog(ScanResultActivity.this, "scan.txt", barcode + " not protected\r\n");
                }

            }
        });
    }

    private void warnForExpiration() {

        vlaidationErrorLayout.setVisibility(View.VISIBLE);
        errorText.setText(R.string.status_product_expired + " " + outDateFormat.format(protectionRegistration.getExpired()));
        errorCardView.setCardBackgroundColor(Color.YELLOW);
        mapView.setVisibility(View.GONE);
        scanInProcess.setVisibility(View.GONE);
        viewLabelShopAddress.setVisibility(View.GONE);

//        viewStatus.setTextColor(Color.BLACK);
//        viewStatus.setBackgroundColor(getResources().getColor(R.color.background_not_activated));
//        viewLocation.setText("");
//        viewDescription.setTextColor(Color.BLACK);
//        viewDescription.setBackgroundColor(getResources().getColor(R.color.background_not_activated));
//        viewDescription.setText(outDateFormat.format(protectionRegistration.getExpired()));
//        containerStatus.setBackgroundColor(getResources().getColor(R.color.background_not_activated));
    }

    private void displayShopAddress() {
        viewLabelShopAddress.setVisibility(View.VISIBLE);
        scanInProcess.setVisibility(View.GONE);
        ScanResult result = registeredLocations.getHistory().get(0);
        // ,,, once - place the result into appropriated field
        if (registeredLocations.getHistory().size() == 1) {
            String shopAddress = new StringBuilder().append(result.getCountry()).append(", ").append(result.getCity())
                    .append(", ").append(result.getStreet()).append(", ").append(result.getBuilding()).toString();
            String dealer = result.getDealer();
            viewLocation.setText((dealer != null && !dealer.trim().isEmpty() ? dealer : shopAddress));
        }
    }

    private void showLocationsOnMap() {
        // ... otherwise pass all places
        LatLng lastCoord = null;
        for (ScanResult sr : registeredLocations.getHistory()) {
            LatLng latlon = new LatLng(sr.getLatitude(), sr.getLongitude());
            lastCoord = latlon;
            String address = new StringBuilder().append(sr.getCountry()).append(", ").append(sr.getCity()).append(", ")
                    .append(sr.getStreet()).append(", ").append(sr.getBuilding()).toString();
            map.addMarker(new MarkerOptions().position(latlon).title(address));
        }
        // ... and focus on last
        CameraPosition cameraPosition = new CameraPosition.Builder().target(lastCoord) // Sets the center of the map to
                // location user
                .zoom(15) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder
        // ... show map
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        mapView.setVisibility(View.VISIBLE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void warnForInactive() {

        mapView.setVisibility(View.GONE);
        scanInProcess.setVisibility(View.GONE);
        vlaidationErrorLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.label_notactive_background));
        // vlaidationErrorLayout.setBackground(getResources().getDrawable(R.drawable.label_notactive_background));
        errorCardView.setCardBackgroundColor(Color.CYAN);
        vlaidationErrorLayout.setVisibility(View.VISIBLE);

        errorText.setText(R.string.label_inactive);

//        viewStatus.setText(R.string.label_inactive);
//        viewStatus.setTextColor(Color.BLACK);
//        viewStatus.setBackgroundColor(getResources().getColor(R.color.background_not_activated));
//        viewLocation.setText("");
//        viewDescription.setText("");
//        viewLabelShopAddress.setVisibility(View.GONE);
//        containerStatus.setBackgroundColor(getResources().getColor(R.color.background_not_activated));
    }

    private void warnForCounterfeit() {

        mapView.setVisibility(View.GONE);
        scanInProcess.setVisibility(View.GONE);
        viewLabelShopAddress.setVisibility(View.GONE);
        vlaidationErrorLayout.setVisibility(View.VISIBLE);
        vlaidationErrorLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.validation_error_background));
        String str = getResources().getString(R.string.status_unregistered);
        errorText.setText(getResources().getString(R.string.status_unregistered));
        errorCardView.setCardBackgroundColor(Color.rgb(235, 135, 50));

//        clearActivity();
//        idResultLayout.setBackgroundColor(Color.RED);
//        imageAccent.setVisibility(View.VISIBLE);
//        viewCounterfeitLabel.setVisibility(View.VISIBLE);

//        viewStatus.setTextColor(Color.WHITE);
//        viewStatus.setBackgroundColor(Color.TRANSPARENT);
//        containerStatus.setBackgroundColor(Color.RED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        if (mapView != null) {
            mapView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mapView != null) {
            mapView.onPause();
        }
        super.onPause();
    }

}
