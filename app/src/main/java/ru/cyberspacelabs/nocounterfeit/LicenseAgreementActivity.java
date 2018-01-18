package ru.cyberspacelabs.nocounterfeit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class LicenseAgreementActivity extends ConfigurableActivity {

	private CheckBox checkBoxAccept;
	private Button buttonContinue;
	private EditText viewLicenseText;

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
		loadTextAsset("license.txt", viewLicenseText);
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

	@Override
	protected void onResume() {
		super.onResume();
		restoreState();
		toggleButtonState();
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
