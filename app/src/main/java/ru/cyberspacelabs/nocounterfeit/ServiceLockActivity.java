package ru.cyberspacelabs.nocounterfeit;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class ServiceLockActivity extends ConfigurableActivity {
	public static final String INTENT_EXTRA_LOCK_DETAILS = "LOCK_DETAILS";
	private TextView deviceId;
	private EditText lockCause;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_service_lock);
		resolveControls();
		processIntent();
	}

	private void processIntent() {
		deviceId.setText(Util.getRegisteredId(this));
		String intentText = getIntent().getStringExtra(INTENT_EXTRA_LOCK_DETAILS);
		lockCause.setText(intentText == null ? "Unknown cause" : intentText);
	}

	private void resolveControls() {
		deviceId = (TextView) findViewById(R.id.textLockedDeviceId);
		lockCause = (EditText) findViewById(R.id.editLockDescription);
	}
}
