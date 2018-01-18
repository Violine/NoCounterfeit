package ru.cyberspacelabs.nocounterfeit.services;

import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.cyberspacelabs.nocounterfeit.contracts.geocoding.GeocodingService;

/**
 * Created by mike on 03.05.16.
 */
public class MapQuestReverseGeocoder implements Callback, GeocodingService {
	private static String URL_GEOCODING = "http://www.mapquestapi.com/geocoding/v1/reverse?key=${key}&location=${latitude},${longitude}";
	private OkHttpClient client;
	private Map<Request, Object> locks;
	private String lastDump;
	private String key;
	private JSONObject geocodingResult;
	private String language;

	public MapQuestReverseGeocoder() {
		client = new OkHttpClient();
		locks = new ConcurrentHashMap<>();
		lastDump = "";
	}

	public MapQuestReverseGeocoder setApiKey(String key) {
		this.key = key;
		return this;
	}

	public MapQuestReverseGeocoder setLanguage(String language) {
		this.language = language;
		return this;
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
			if (response != null && response.code() == 200) {
				lastDump = response.body().string();
			} else {
				lastDump = "";
			}
			response.request().notify();
		}
	}

	@Override
	public void setLocation(double latitude, double longitude) {
		Request request = new Request.Builder().url(buildURL(latitude, longitude))
				.addHeader("Accept-Language", language).build();
		locks.put(request, new Object());
		synchronized (request) {
			client.newCall(request).enqueue(this);
			try {
				request.wait();
			} catch (InterruptedException e) {
				// Log.w("MapQuest", e);
			}
		}
		Response response = (Response) locks.get(request);
		synchronized (this) {
			geocodingResult = null;
			if (response != null && response.code() == 200 && !lastDump.isEmpty()) {
				try {
					geocodingResult = new JSONObject(lastDump);
				} catch (JSONException e) {
					// Log.w("MapQuest", e);
				}
			}
		}
	}

	private String buildURL(double latitude, double longitude) {
		return URL_GEOCODING.replace("${key}", key).replace("${latitude}", Double.toString(latitude))
				.replace("${longitude}", Double.toString(longitude));
	}

	private JSONArray getComponents() throws Exception {
		if (geocodingResult != null) {
			JSONObject info = geocodingResult.getJSONObject("info");
			if (info != null) {
				int status = info.getInt("statuscode");
				if (status == 0) {
					JSONArray results = geocodingResult.getJSONArray("results");
					if (results != null && results.length() > 0) {
						JSONObject target = results.getJSONObject(0);
						JSONArray locations = target.getJSONArray("locations");
						if (locations != null && locations.length() > 0) {
							return locations;
						} else {
							throw new NullPointerException("Response .results.locations block absent or empty");
						}
					} else {
						throw new NullPointerException("Response .results block absent or empty");
					}
				} else {
					throw new IllegalStateException("Status code from MapQuest is " + status);
				}
			} else {
				throw new NullPointerException("Response .info block absent");
			}
		} else {
			throw new NullPointerException("Response from MapQuest hasn't been achieved");
		}
	}

	@Override
	public String[] getLocationNames() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String quality = first.getString("geocodeQuality");
			if ("ADDRESS".equals(quality)) {
				return new String[] { first.getString("adminArea5"), "" };
			} else if ("STREET".equals(quality)) {
				return new String[] { first.getString("adminArea4"), first.getString("adminArea5") };
			}
			return null;
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getAddress() {
		return (getStreet().isEmpty() ? "" : getStreet() + ", ")
				+ (getBuildingNumber().isEmpty() ? "" : getBuildingNumber() + ", ") + getCity() + ", " + getArea()
				+ ", " + getCountry() + (getZIP().isEmpty() ? "" : ", " + getZIP());
	}

	@Override
	public String getZIP() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			return first.getString("postalCode");
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getCountry() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String c = first.getString("adminArea1");
			return new Locale("", c).getDisplayCountry();
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getCity() {
		JSONArray components = null;
		try {
			components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String quality = first.getString("geocodeQuality");
			if ("ADDRESS".equals(quality)) {
				return first.getString("adminArea5");
			} else if ("STREET".equals(quality)) {
				return first.getString("adminArea4");
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getDistrict() {
		JSONArray components = null;
		try {
			components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String quality = first.getString("geocodeQuality");
			if ("ADDRESS".equals(quality)) {
				return "";
			} else if ("STREET".equals(quality)) {
				return first.getString("adminArea5");
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String getArea() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			return first.getString("adminArea3");
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getStreet() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String quality = first.getString("geocodeQuality");
			if ("ADDRESS".equals(quality)) {
				String street = first.getString("street");
				int name = getFirstUppercaseIndex(street);
				String n = street.substring(name);
				int space = n.indexOf(" ");
				return n.substring(space + 1) + " " + n.substring(0, space).trim();
			} else if ("STREET".equals(quality)) {
				return first.getString("street");
			}
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getBuildingNumber() {
		try {
			JSONArray components = getComponents();
			JSONObject first = components.getJSONObject(0);
			String quality = first.getString("geocodeQuality");
			if ("ADDRESS".equals(quality)) {
				String street = first.getString("street");
				int name = getFirstUppercaseIndex(street);
				return street.substring(0, name).trim();
			} else if ("STREET".equals(quality)) {
				return "";
			}
		} catch (Exception e) {
			// Log.w("MapQuest", e);
		}
		return null;
	}

	@Override
	public String getPrettyAddress() {
		return getCountry() + ", " + getCity() + (getDistrict().isEmpty() ? "" : ", " + getDistrict())
				+ (getStreet().isEmpty() ? "" : ", " + getStreet())
				+ (getBuildingNumber().isEmpty() ? "" : ", " + getBuildingNumber());
	}

	@Override
	public String getLastDump() {
		return lastDump;
	}

	private int getFirstUppercaseIndex(String input) {
		int result = -1;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (Character.isUpperCase(c) && !Character.isDigit(c)) {
				result = i;
				break;
			}
		}
		return result;
	}
}
