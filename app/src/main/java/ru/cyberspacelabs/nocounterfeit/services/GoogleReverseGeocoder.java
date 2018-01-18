package ru.cyberspacelabs.nocounterfeit.services;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;
import android.widget.ImageView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.cyberspacelabs.nocounterfeit.contracts.geocoding.ImageryService;
import ru.cyberspacelabs.nocounterfeit.util.Util;

/**
 * Created by mike on 27.04.16.
 */

public class GoogleReverseGeocoder
		implements Callback, ru.cyberspacelabs.nocounterfeit.contracts.geocoding.GeocodingService, ImageryService {
	private static String URL_GEOCODING = "https://maps.googleapis.com/maps/api/geocode/json?key=${key}&latlng=";// https://maps.googleapis.com/maps/api/geocode/json?latlng=54.854567,83.047619
	private static String URL_IMAGERY = "center=${latitude},${longitude}&zoom=18&size=${width}x${height}&format=png&markers=color:red|${latitude},${longitude}&language=${language}&key=${key}";
	private OkHttpClient client;
	private Map<Request, Object> locks;
	private String lastDump;
	private String language;
	private String key;

	public GoogleReverseGeocoder() {
		client = new OkHttpClient();
		locks = new ConcurrentHashMap<>();
		lastDump = "";
	}

	public GoogleReverseGeocoder setLanguage(String language) {
		this.language = language;
		return this;
	}

	@Override
	public void setLocation(double latitude, double longitude) {
		Request request = new Request.Builder()
				.url(URL_GEOCODING.replace("${key}", key) + latitude + "," + longitude + "&language=" + language)
				.build();
		locks.put(request, new Object());
		synchronized (request) {
			client.newCall(request).enqueue(this);
			try {
				request.wait();
			} catch (InterruptedException e) {
				Log.w("Google", e);
			}
		}
		Response response = (Response) locks.get(request);
		synchronized (this) {
			lastDump = "";
			if (response != null && response.code() == 200) {
				String json = null;
				try {
					json = response.body().string();
					lastDump = json;
				} catch (IOException e) {
					Log.w("Google", e);
				}
			}
		}

	}

	@Override
	public String[] getLocationNames() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				String[] result = new String[2];
				result[1] = getDistrict(results);
				result[0] = getCity(results);
				return result;
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public void onFailure(Call call, IOException e) {
		synchronized (call.request()) {
			call.request().notify();
		}
	}

	@Override
	public void onResponse(Call call, Response response) throws IOException {
		synchronized (response.request()) {
			locks.put(response.request(), response);
			response.request().notify();
		}
	}

	private String getDistrict(JSONArray results) throws JSONException {
		return getNameOf("sublocality_level_2", results);
	}

	private String getCity(JSONArray results) throws JSONException {
		String city = getNameOf("locality", results);
		if (city.isEmpty()) {
			city = getNameOf("administrative_area_level_2", results);
		}
		return city;
	}

	@Override
	public String getAddress() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				for (int i = 0; i < results.length(); i++) {
					JSONObject result = results.getJSONObject(i);
					JSONArray types = result.getJSONArray("types");
					for (int j = 0; j < types.length(); j++) {
						String resultType = types.getString(j);
						if ("street_address".equals(resultType)) {
							return getStreet() + ", " + getBuildingNumber() + ", " + getCity() + ", " + getArea() + ", "
									+ getCountry() + ", " + getZIP();
						} else if ("route".equals(resultType)) {
							return getStreet() + ", " + getCity() + ", " + getArea() + ", " + getCountry() + ", "
									+ getZIP();
						}
					}
				}
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getZIP() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getNameOf("postal_code", results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getCountry() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getNameOf("country", results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getCity() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getCity(results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getDistrict() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getDistrict(results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getArea() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getNameOf("administrative_area_level_1", results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getStreet() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getNameOf("route", results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	@Override
	public String getBuildingNumber() {
		if (!lastDump.isEmpty()) {
			String json = lastDump;
			try {
				JSONObject data = new JSONObject(json);
				JSONArray results = data.getJSONArray("results");
				return getNameOf("street_number", results);
			} catch (JSONException e) {
				Log.w("Google", e);
			}
			return null;
		} else {
			return null;
		}
	}

	private String getNameOf(String locationType, JSONArray results) throws JSONException {
		int members = results.length();
		for (int i = 0; i < members; i++) {
			JSONObject result = results.getJSONObject(i);
			JSONArray address_components = result.getJSONArray("address_components");
			for (int j = 0; j < address_components.length(); j++) {
				JSONObject component = address_components.getJSONObject(j);
				JSONArray types = component.getJSONArray("types");
				for (int k = 0; k < types.length(); k++) {
					String type = types.getString(k);
					if (locationType.equalsIgnoreCase(type)) {
						return component.getString("long_name");
					}
				}
			}
		}
		return "";
	}

	@Override
	public String getPrettyAddress() {
		if (getLastDump().isEmpty()) {
			return "";
		}
		StringBuilder result = new StringBuilder();
		result.append(getCountry()).append(", ").append(getCity()).append(", ").append(getStreet());
		String building = getBuildingNumber();
		if (building != null && !building.isEmpty()) {
			result.append(", ").append(building);
		}
		return result.toString();
	}

	@Override
	public String getLastDump() {
		return lastDump;
	}

	public String getKey() {
		return key;
	}

	public GoogleReverseGeocoder setKey(String key) {
		this.key = key;
		return this;
	}

	@Override
	public void showOnMap(final double latitude, final double longitude, final ImageView target) {
		if (target != null) {
			final int vw = 800; // target.getLayoutParams().width;
			final int vh = 800; // target.getLayoutParams().height;
			final String url;
			final Activity ui = (Activity) target.getContext();
			try {
				url = "https://maps.googleapis.com/maps/api/staticmap?" + URLEncoder
						.encode(URL_IMAGERY.replace("${latitude}", Double.toString(latitude))
								.replace("${longitude}", Double.toString(longitude))
								.replace("${width}", Integer.toString(vw)).replace("${height}", Integer.toString(vh))
								.replace("${language}", language).replace("${key}", key), "UTF-8")
						.replace("%26", "&").replace("%3D", "=");
			} catch (UnsupportedEncodingException e) {
				Log.w("Imagery", e);
				target.setImageResource(android.R.color.transparent);
				return;
			}
			if (ui == null) {
				return;
			}
			Runnable imageryTask = new Runnable() {
				@Override
				public void run() {
					client.newCall(new Request.Builder().url(url).build()).enqueue(new Callback() {
						@Override
						public void onFailure(Call call, final IOException e) {
							ui.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									target.setImageResource(android.R.color.transparent);
									Log.w("Imagery", e);
								}
							});

						}

						@Override
						public void onResponse(Call call, Response response) throws IOException {
							final InputStream imageStream = response.body().byteStream();
							final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
							response.body().close();
							ui.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									target.setImageBitmap(bitmap);
								}
							});
						}
					});
				}
			};
			Util.runUiBackgroundTask(imageryTask, ui, null, null);
		}
	}
}
