package ru.cyberspacelabs.nocounterfeit;

import android.os.Bundle;
import android.widget.EditText;

import ru.cyberspacelabs.nocounterfeit.ui.ConfigurableActivity;
import ru.cyberspacelabs.nocounterfeit.util.Util;

public class AboutAppActivity extends ConfigurableActivity {

	private EditText viewAbout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_app);
		setTitle(R.string.activity_about);
		viewAbout = (EditText) findViewById(R.id.textAbout);
		loadTextAsset("description_" + Util.getApplicationLocale(this).toLowerCase() + ".txt", viewAbout);
	}
}
