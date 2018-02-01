package ru.cyberspacelabs.nocounterfeit;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class LicenseAgreementActivity extends ConfigurableActivity {

	private CheckBox checkBoxAccept;
	private Button buttonContinue;
	private EditText viewLicenseText;
	private Spinner dropdownLanguages;
	private Button buttonLanguage;
	private TextView languageText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_license);
		setTitle(R.string.activity_license);
		resolveControls();
		bindActions();
	}

	private void restoreState() {
		loadLicense();
		boolean agreement = getSharedPreferences(getPackageName(), MODE_PRIVATE)
				.getBoolean(Util.PREFERENCE_KEY_TOS_CONFIRMED, false);
		checkBoxAccept.setChecked(agreement);
	}

	private void loadLicense() {
		loadTextAsset("license_" + Util.getApplicationLocale(this).toLowerCase() + ".txt", viewLicenseText);
	}

	private void bindActions() {
		checkBoxAccept.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleButtonState();
			}
		});

		buttonContinue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				writeAgreementFlag();
				String locale = getSharedPreferences(getPackageName(), MODE_PRIVATE)
						.getString(Util.PREFERENCE_KEY_LOCALE, "");
				if (checkBoxAccept.isChecked() && !locale.isEmpty()) {
					startActivity(new Intent(LicenseAgreementActivity.this, OffGridActivity.class));
				} else if (checkBoxAccept.isChecked() && locale.isEmpty()) {
					startActivity(new Intent(LicenseAgreementActivity.this, MainActivity.class));
				}
			}
		});
		buttonLanguage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				restartActivitiy();
			}
		});
	}

	private void writeAgreementFlag() {
		getSharedPreferences(getPackageName(), MODE_PRIVATE).edit().putBoolean(Util.PREFERENCE_KEY_TOS_CONFIRMED, true)
				.commit();
	}

	private void toggleButtonState() {
		buttonContinue.setEnabled(checkBoxAccept.isChecked());
	}

	private void resolveControls() {
		checkBoxAccept = (CheckBox) findViewById(R.id.checkBoxAcceptToS);
		buttonContinue = (Button) findViewById(R.id.buttonAcceptToS);
		viewLicenseText = (EditText) findViewById(R.id.textLicense);
		buttonLanguage = (Button) findViewById(R.id.languageOK);
		languageText = (TextView) findViewById(R.id.language);
		languageText.setText(getResources().getString(R.string.language));
		dropdownLanguages = (Spinner) findViewById(R.id.dropdownLanguage);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(

				this, R.array.languages, R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		dropdownLanguages.setAdapter(adapter);
		dropdownLanguages.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos,
									   long id) {
				// TODO Auto-generated method stub
				((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
				((TextView) parent.getChildAt(0)).setTextSize(16);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});

	}

	private void loadState() {
		String localeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE,
				"ru_RU");
		dropdownLanguages.setSelection(localeId.equals("en_US") ? 0 : 1);
		if (ActivityCompat.checkSelfPermission(this,
				Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			Util.openRuntimeSettings(this, getResources().getString(R.string.toast_runtime_settings));
			return;
		}
	}

	private void saveState() {
		SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
		editor.putString(Util.PREFERENCE_KEY_LOCALE,
				dropdownLanguages.getSelectedItemPosition() == 0 ? "en_US" : "ru_RU");
		editor.commit();
	}

	public void restartActivitiy() {
		saveState();
		LicenseAgreementActivity.this.finish();
		Util.restartApplication(LicenseAgreementActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		restoreState();
		toggleButtonState();
		loadState();

		String locale = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE, "");
		if (!isExitPending()) {
			if (checkBoxAccept.isChecked() && !locale.isEmpty()) {
				startActivity(new Intent(LicenseAgreementActivity.this, OffGridActivity.class));
			} else if (checkBoxAccept.isChecked() && locale.isEmpty()) {
				startActivity(new Intent(this, ConfigActivity.class));
			}
		} else {
			setExitPending(false);
			finish();
		}
	}

}
