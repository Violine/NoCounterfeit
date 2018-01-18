package ru.cyberspacelabs.nocounterfeit.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import ru.cyberspacelabs.nocounterfeit.services.GoogleReverseGeocoder;

/**
 * Created by mike on 21.04.16.
 */
public class Util {
	public static final String PREFERENCE_KEY_TOS_CONFIRMED = "ToS.confirmed";
	public static final String PREFERENCE_KEY_LOCALE = "locale";
	public static final String PREFERENCE_KEY_GEOCODER = "geocoding";
	public static final String PREFERENCE_KEY_THEME = "theme";
	public static final String PREFERENCE_KEY_DEVICEID = "device.id";

	private static ExecutorService backgroundTaskThreadPool;
	private static String source = "абвгдежзиклмнопрстуфхцчшщъыьэюяz";
	private static String encoded = "бвгдекзаилмнопрстуфхцшэщчъыжюяь_";

	static {
		backgroundTaskThreadPool = new ThreadPoolExecutor(2, 10, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(25), new ThreadFactory() {
					private AtomicLong counter = new AtomicLong(0);

					@Override
					public Thread newThread(Runnable target) {
						Thread result = new Thread(target);
						result.setDaemon(true);
						result.setName("ru.cyberspacelabs.nocounterfeit-Worker-" + counter.incrementAndGet());
						return result;
					}
				});

	}

	public static boolean isLocationEnabled(LocationManager locationManager) {
		boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		return isGPSEnabled || isNetworkEnabled;
	}

	public static boolean isConnectivityEnabled(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	public static void startLocationSettings(Activity source) {
		Intent GPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		source.startActivity(GPSSettingIntent);
	}

	public static void startWirelessSettings(Activity source) {
		Intent wirelessSettingsIntent = new Intent(Settings.ACTION_SETTINGS);
		source.startActivity(wirelessSettingsIntent);
	}

	public static String encode(String clear) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < clear.length(); i++) {
			char c = clear.charAt(i);
			if (c >= 0x5000) {
				result.append(c);
				continue;
			}
			if (Character.isDigit(c) || c == ' ' || Character.isUpperCase(c)) {
				result.append(c);
				continue;
			}
			if (c >= 'a' && c < 'z') {
				result.append(Character.toChars(c + 1));
				continue;
			}
			int idx = source.indexOf(c);
			result.append(encoded.charAt(idx));
		}
		return result.toString();
	}

	public static String decode(String encodedString) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < encodedString.length(); i++) {
			char p = encodedString.charAt(i);
			if (p >= 0x5000) {
				// skipping characters not intended for encoding
				continue;
			}
			if (Character.isDigit(p) || p == ' ' || (Character.isUpperCase(p) && p != '_')) {
				result.append(p);
			} else {
				if (p >= 'b' && p <= 'z') {
					result.append(Character.toChars(p - 1));
				} else {
					char special = 0;
					try {
						int index = encoded.indexOf(p);
						if (index > -1) {
							special = source.charAt(index);
						}
					} catch (NullPointerException e) {
					}
					if (special != 0) {
						result.append(special);
					} else {
						result.append(Character.toChars(p - 1));
					}
				}
			}
		}
		return result.toString();
	}

	public static Date addDays(Date date, long expired) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, (int) expired);
		return c.getTime();
	}

	private static void appendFile(File file, byte[] content) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file, true);
			fos.write(content);
			fos.flush();
		} catch (Exception e) {
			Log.w("Util.appendFile", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	public static synchronized void appendLog(final Context context, final String logFile, final String text) {
		Runnable task = new Runnable() {
			@Override
			public void run() {
				try {
					File root = Environment.getExternalStorageDirectory();
					File appdir = new File(root, context.getPackageName());
					appdir.mkdirs();
					File log = new File(appdir, logFile);
					byte[] data = text.getBytes("UTF-8");
					appendFile(log, data);
				} catch (Exception e) {
					Log.w("Util.appendLog", e);
				}
			}
		};
		backgroundTaskThreadPool.submit(task);
	}

	public static void restartApplication(Activity context) {
		Context base = context.getBaseContext();
		Intent i = base.getPackageManager().getLaunchIntentForPackage(base.getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(i);
	}

	public static String getApplicationLocale(Activity context) {
		return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
				.getString(Util.PREFERENCE_KEY_LOCALE, Locale.getDefault().toString());
	}

	public static void openRuntimeSettings(Activity context, String toast) {
		Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
		ActivityCompat.requestPermissions(context,
				new String[] { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
						Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
						Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE },
				0xFF);
	}

	public static String normalizeBarcode(String barcode) {
		String bc = barcode;
		while (bc.startsWith("0")) {
			bc = bc.substring(1);
		}
		return bc.substring(1);
	}

	public static String getDeviceUUID(Context context) {
		String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
		return android_id;
	}

	public static String getRegisteredId(Context context) {
		String rt = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
		if (rt == null || rt.trim().isEmpty()) {
			rt = getDeviceUUID(context);
			Log.w("Util", "Device doesn't have IMEI/ESN; Assigning UUID " + rt);
		}
		SharedPreferences pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
		String reg = pref.getString(PREFERENCE_KEY_DEVICEID, "");
		if (!reg.isEmpty() && !reg.equals(rt)) {
			throw new IllegalStateException("Device ID " + rt + " doesn't match registered ID " + reg);
		}

		if (reg.isEmpty()) {
			pref.edit().putString(PREFERENCE_KEY_DEVICEID, rt).commit();
			reg = rt;
		}
		return reg;
	}

	public static void runBackgroundTask(Runnable task) {
		backgroundTaskThreadPool.submit(task);
	}

	public static void runUiBackgroundTask(final Runnable task, final Activity target, final Runnable completionTask,
			final Runnable exceptionTask) {
		if (task != null && target != null) {
			backgroundTaskThreadPool.submit(new Runnable() {
				@Override
				public void run() {
					try {
						backgroundTaskThreadPool.submit(task).get();
						if (completionTask != null) {
							target.runOnUiThread(completionTask);
						}
					} catch (InterruptedException e) {
						Log.w("Util", e);
						if (exceptionTask != null) {
							target.runOnUiThread(exceptionTask);
						}

					} catch (ExecutionException e) {
						Log.w("Util", e);
						if (exceptionTask != null) {
							target.runOnUiThread(exceptionTask);
						}
					}
				}
			});
		}
	}
}
