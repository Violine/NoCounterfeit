package ru.cyberspacelabs.nocounterfeit;

import android.content.Intent;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class OffGridActivity extends ConfigurableActivity {

	private LocationManager locationManager;
	private Button buttonEnableLocation;
	private Button buttonEnableConnectivity;
	private ImageView iconGps;
	private ImageView iconNetwork;
	private TextView labelNetwork;
	private TextView labelLocation;
	private boolean isLocationEnabled;
	private boolean isConnectivityEnabled;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_off_grid);
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		resolveControls();
		bindActions();
		setTitle(R.string.activity_offgrid_title);
	}

	private void bindActions() {
		buttonEnableConnectivity.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.startWirelessSettings(OffGridActivity.this);
			}
		});

		buttonEnableLocation.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Util.startLocationSettings(OffGridActivity.this);
			}
		});
	}

	private void resolveControls() {
		buttonEnableConnectivity = (Button) findViewById(R.id.buttonEnableNetwork);
		buttonEnableLocation = (Button) findViewById(R.id.buttonEnableGPS);
		iconGps = (ImageView) findViewById(R.id.iconGPSStatus);
		iconNetwork = (ImageView) findViewById(R.id.iconConnectionStatus);
		labelLocation = (TextView) findViewById(R.id.labelGPSOK);
		labelNetwork = (TextView) findViewById(R.id.labelInternetOK);
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkLocationEnabled();
		checkConnectivityEnabled();
		if (isLocationEnabled && isConnectivityEnabled && !isExitPending()) {
			startActivity(new Intent(this, MainActivity.class));
		} else if (isExitPending()) {
			finish();
		}
	}

	private void checkConnectivityEnabled() {
		isConnectivityEnabled = Util.isConnectivityEnabled(this);
		buttonEnableConnectivity.setVisibility(isConnectivityEnabled ? View.GONE : View.VISIBLE);
		labelNetwork.setVisibility(isConnectivityEnabled ? View.VISIBLE : View.GONE);
		iconNetwork.setImageResource(isConnectivityEnabled ? R.drawable.ic_network_cell_white_24dp
				: R.drawable.ic_signal_cellular_connected_no_internet_4_bar_white_24dp);
	}

	private void checkLocationEnabled() {
		isLocationEnabled = Util.isLocationEnabled(locationManager);
		buttonEnableLocation.setVisibility(isLocationEnabled ? View.GONE : View.VISIBLE);
		iconGps.setImageResource(
				isLocationEnabled ? R.drawable.ic_gps_fixed_white_24dp : R.drawable.ic_gps_off_white_24dp);
		labelLocation.setVisibility(isLocationEnabled ? View.VISIBLE : View.GONE);
	}
}
