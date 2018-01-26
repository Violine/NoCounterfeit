package ru.cyberspacelabs.nocounterfeit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
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
					startActivity(new Intent(LicenseAgreementActivity.this, ConfigActivity.class));
				}
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
	}

	public void restartActivitiy() {
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	@Override
	protected void onResume() {
		super.onResume();
		restoreState();
		toggleButtonState();

		dropdownLanguages = (Spinner) findViewById(R.id.dropdownLanguage);
		String localeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE,
				"en_US");
		final SharedPreferences.Editor editor = getSharedPreferences(getPackageName(), MODE_PRIVATE).edit();
		dropdownLanguages.setSelection(localeId.equals("en_US") ? 0 : 1);
		String locale = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE, "");
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.languages, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dropdownLanguages.setAdapter(adapter);

		/*dropdownLanguages.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				editor.putString(Util.PREFERENCE_KEY_LOCALE,
						dropdownLanguages.getSelectedItemPosition() == 0 ? "en_US" : "ru_RU");
				editor.commit();
				restartActivitiy();
			}
		});*/

		/*OnItemSelectedListener itemSelectedListener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				editor.putString(Util.PREFERENCE_KEY_LOCALE,
						dropdownLanguages.getSelectedItemPosition() == 0 ? "en_US" : "ru_RU");
				editor.commit();
                loadLicense();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		};
		dropdownLanguages.setOnItemSelectedListener(itemSelectedListener);*/
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
