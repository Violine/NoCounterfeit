package ru.cyberspacelabs.nocounterfeit;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class ConfigActivity extends ConfigurableActivity {
	private Spinner dropdownLanguages;
	private Spinner dropdownThemes;
	private Spinner dropdownGeocoding;
	private Button buttonSave;
	private Button buttonAbout;
	private TextView textDeviceId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
		setTitle(R.string.activity_config_title);
		resolveControls();
		bindActions();
	}

	private void bindActions() {
		buttonAbout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(ConfigActivity.this, AboutAppActivity.class));
			}
		});
		buttonSave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				saveState();
				ConfigActivity.this.finish();
				Util.restartApplication(ConfigActivity.this);
			}
		});
	}

	private void resolveControls() {
		dropdownGeocoding = (Spinner) findViewById(R.id.dropdownGeocoding);
		dropdownLanguages = (Spinner) findViewById(R.id.dropdownLanguage);
		dropdownThemes = (Spinner) findViewById(R.id.dropdownThemes);
		buttonAbout = (Button) findViewById(R.id.buttonAbout);
		buttonSave = (Button) findViewById(R.id.buttonSaveSettings);
		textDeviceId = (TextView) findViewById(R.id.labelDeviceID);
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadState();
	}

	private void loadState() {
		String localeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE,
				"en_US");
		int themeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getInt(Util.PREFERENCE_KEY_THEME, 0);
		int geocode = getSharedPreferences(getPackageName(), MODE_PRIVATE).getInt(Util.PREFERENCE_KEY_GEOCODER, 0);
		dropdownGeocoding.setSelection(geocode);
		dropdownThemes.setSelection(themeId);
		dropdownLanguages.setSelection(localeId.equals("en_US") ? 0 : 1);
		if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this,
						Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Util.openRuntimeSettings(this, getResources().getString(R.string.toast_runtime_settings));
			return;
		}
		textDeviceId.setText(Util.getRegisteredId(this));
	}

	private void saveState() {
		SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
		editor.putInt(Util.PREFERENCE_KEY_GEOCODER, dropdownGeocoding.getSelectedItemPosition());
		editor.putInt(Util.PREFERENCE_KEY_THEME, dropdownThemes.getSelectedItemPosition());
		editor.putString(Util.PREFERENCE_KEY_LOCALE,
				dropdownLanguages.getSelectedItemPosition() == 0 ? "en_US" : "ru_RU");
		editor.commit();
	}

}
