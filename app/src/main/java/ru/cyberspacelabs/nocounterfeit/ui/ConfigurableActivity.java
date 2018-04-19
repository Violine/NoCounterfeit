package ru.cyberspacelabs.nocounterfeit.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import ru.cyberspacelabs.nocounterfeit.ApplicationState;
import ru.cyberspacelabs.nocounterfeit.R;
import ru.cyberspacelabs.nocounterfeit.util.Util;

/**
 * Created by mike on 04.05.16.
 */
public class ConfigurableActivity extends AppCompatActivity {
	private static boolean exitPending;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String localeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getString(Util.PREFERENCE_KEY_LOCALE,
				"");
		int themeId = getSharedPreferences(getPackageName(), MODE_PRIVATE).getInt(Util.PREFERENCE_KEY_THEME, -1);
		if (!localeId.isEmpty()) {
			Locale locale = null;// new Locale(localeId);
			for (Locale l : Locale.getAvailableLocales()) {
				if (l.toString().equalsIgnoreCase(localeId)) {
					locale = l;
					break;
				}
			}
			if (locale != null) {
				Locale.setDefault(locale);
				Configuration config = getBaseContext().getResources().getConfiguration(); // new Configuration();
				config.locale = locale;
				getBaseContext().getResources().updateConfiguration(config,
						getBaseContext().getResources().getDisplayMetrics());
			}
		}

		/*switch (themeId) {
		case 0:
			setTheme(R.style.OriginalTheme);
			getApplicationContext().setTheme(R.style.OriginalTheme);
			break;
		case 1:
			setTheme(R.style.BlackTheme);
			getApplicationContext().setTheme(R.style.BlackTheme);
			break;
		case 2:
			setTheme(R.style.LightTheme);
			getApplicationContext().setTheme(R.style.LightTheme);
			break;
		default:
			setTheme(R.style.OriginalTheme);
			getApplicationContext().setTheme(R.style.OriginalTheme);
			break;
		}*/

	}

	protected void loadTextAsset(String name, EditText target) {
		try {
			InputStream is = getAssets().open(name);
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String text = new String(buffer);
			target.setText(text);
		} catch (IOException e) {
			Log.w("loadTextAsset", e);
			target.setText("");
		}
	}

	protected static void setExitPending(boolean value) {
		exitPending = value;
	}

	protected static boolean isExitPending() {
		return exitPending;
	}

	protected ApplicationState getApplicationState() {
		return (ApplicationState) getApplicationContext();
	}
}
