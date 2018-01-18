package ru.cyberspacelabs.nocounterfeit.services;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationService;
import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationServiceAsync;
import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationServiceCallback;
import ru.cyberspacelabs.nocounterfeit.async.Result;
import ru.cyberspacelabs.nocounterfeit.dto.NearbyScans;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionRegistration;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResult;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResults;
import ru.cyberspacelabs.nocounterfeit.json.LongIntegerJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.json.OptionalDateJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.network.TLSUtils;
import ru.cyberspacelabs.nocounterfeit.util.CryptoUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateEncodingException;
import javax.security.cert.X509Certificate;

/**
 * Created by mike on 12.04.16.
 */
public class ProtectionWebService implements ProtectionValidationServiceAsync, Callback, ProtectionValidationService {
	public static String DISPATCH_ID_GET_PROTECTION_REGISTRATION = "getProtectionRegistration";
	public static String DISPATCH_ID_NEARBY_SCANS = "getNearbyScans";
	public static String DISPATCH_ID_REGISTERED_LOCATIONS = "getRegisteredLocations";

	private static String BASE_URL = "https://gr.nocounterfeit.info";
	private OkHttpClient client;
	private Map<UUID, ProtectionValidationServiceCallback> callbacks;
	private Map<Request, Result> requests;

	public ProtectionWebService() {
		List<ConnectionSpec> tls = new ArrayList<>();
		tls.add(ConnectionSpec.MODERN_TLS);
		try {
			client = new OkHttpClient.Builder().connectionSpecs(tls).hostnameVerifier(TLSUtils.getHostNameVerifier())
					.sslSocketFactory(TLSUtils.getSslSocketFactory()).build();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		callbacks = new ConcurrentHashMap<>();
		requests = new ConcurrentHashMap<>();
	}

	@Override
	public void addCallback(ProtectionValidationServiceCallback callback) {
		if (callback != null) {
			callbacks.put(callback.getClientID(), callback);
		}
	}

	@Override
	public void removeCallback(ProtectionValidationServiceCallback callback) {
		if (callback != null) {
			callbacks.remove(callback.getClientID());
		}
	}

	@Override
	public UUID getProtectionRegistration(UUID clientID, String barcode) {
		Request request = createRequest("/shtrih.php?sh=" + barcode);
		Result<ProtectionRegistration> result = new Result<ProtectionRegistration>(clientID,
				DISPATCH_ID_GET_PROTECTION_REGISTRATION) {
			@Override
			public Class<ProtectionRegistration> getEntityClass() {
				return ProtectionRegistration.class;
			}
		};
		return scheduleExecution(request, result);
	}

	@Override
	public UUID getNearbyScans(UUID clientID, ScanResult scanResult) {
		String url = "barcode=" + scanResult.getBarcode() + "&qr=" + scanResult.getQrText() + "&lat="
				+ scanResult.getLatitude() + "&lon=" + scanResult.getLongitude() + "&hdop=" + scanResult.getHdop()
				+ "&city=" + scanResult.getCity() + "&district=" + scanResult.getDistrict() + "&zip="
				+ scanResult.getZip() + "&street=" + scanResult.getStreet() + "&building=" + scanResult.getBuilding()
				+ "&country=" + scanResult.getCountry() + "&imei=" + scanResult.getDeviceId();

		try {
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.w("ProtectionWebService", e);
		}
		url = "/geo.php?" + url.replace("%26", "&").replace("%3D", "=");
		Request request = createRequest(url);
		Result<NearbyScans> result = new Result<NearbyScans>(clientID, DISPATCH_ID_NEARBY_SCANS) {
			@Override
			public Class<NearbyScans> getEntityClass() {
				return NearbyScans.class;
			}
		};
		return scheduleExecution(request, result);
	}

	@Override
	public UUID getRegisteredLocations(UUID clientID, ScanResult scanResult) {
		Request request = null;
		try {
			request = new Request.Builder().url(BASE_URL + "/location.php")
					.post(new FormBody.Builder().addEncoded("qr", URLEncoder.encode(scanResult.getQrText(), "UTF-8"))
							.add("barcode", scanResult.getBarcode()).build())
					.build();
			Result<ScanResults> result = new Result<ScanResults>(clientID, DISPATCH_ID_REGISTERED_LOCATIONS) {
				@Override
				public Class<ScanResults> getEntityClass() {
					return ScanResults.class;
				}
			};
			return scheduleExecution(request, result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private UUID scheduleExecution(Request request, Result result) {
		requests.put(request, result);
		client.newCall(request).enqueue(this);
		return result.getRequestID();
	}

	// okHttp Callbacks
	@Override
	public void onFailure(Call call, IOException e) {
		Result result = requests.get(call.request());
		result.setCause(e);
		dispatchResult(result);
	}

	private Gson createJsonEngine() {
		return new GsonBuilder().registerTypeAdapter(long.class, new LongIntegerJSONAdapter())
				.registerTypeAdapter(Long.class, new LongIntegerJSONAdapter())
				.registerTypeAdapter(Date.class, new OptionalDateJSONAdapter()).setPrettyPrinting()
				.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
	}

	@Override
	public void onResponse(Call call, Response response) throws IOException {
		Result result = requests.get(response.request());
		try {
			Gson gson = createJsonEngine();

			if (result.getEntityClass() != Void.class) {
				String json = response.body().string();
				result.setValue(gson.fromJson(json, result.getEntityClass()));
			} else {
				result.setValue(null);
			}
		} catch (NullPointerException np) {
			result.setValue(null);
		} catch (Exception e) {
			result.setCause(e);
		} finally {
			dispatchResult(result);
		}
	}

	private Request createRequest(String relativeURL) {
		return new Request.Builder().url(BASE_URL + relativeURL).build();
	}

	private void dispatchResult(Result result) {
		ProtectionValidationServiceCallback callback = callbacks.get(result.getClientID());
		if (DISPATCH_ID_GET_PROTECTION_REGISTRATION.equalsIgnoreCase(result.getDispatchID())) {
			callback.onProtectionRegistration(result);
		}

		if (DISPATCH_ID_NEARBY_SCANS.equalsIgnoreCase(result.getDispatchID())) {
			callback.onNearbyScans(result);
		}

		if (DISPATCH_ID_REGISTERED_LOCATIONS.equals(result.getDispatchID())) {
			callback.onRegisteredLocations(result);
		}
	}

	// synchronous version
	@Override
	public ProtectionRegistration getProtectionRegitration(String barcode) throws IOException, MalformedURLException {
		Request request = createRequest("/shtrih.php?sh=" + barcode);
		Response response = client.newCall(request).execute();
		Gson engine = createJsonEngine();
		String responseText = response.body().string();
		return engine.fromJson(responseText, ProtectionRegistration.class);
	}

	@Override
	public NearbyScans getNearbyScans(ScanResult scanResult) throws IOException {
		String url = "barcode=" + scanResult.getBarcode() + "&qr=" + scanResult.getQrText() + "&lat="
				+ scanResult.getLatitude() + "&lon=" + scanResult.getLongitude() + "&hdop=" + scanResult.getHdop()
				+ "&city=" + scanResult.getCity() + "&district=" + scanResult.getDistrict() + "&zip="
				+ scanResult.getZip() + "&street=" + scanResult.getStreet() + "&building=" + scanResult.getBuilding()
				+ "&country=" + scanResult.getCountry() + "&imei=" + scanResult.getDeviceId();

		try {
			url = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.w("ProtectionWebService", e);
		}
		url = "/geo.php?" + url.replace("%26", "&").replace("%3D", "=");
		Request request = createRequest(url);
		Response response = client.newCall(request).execute();
		Gson engine = createJsonEngine();
		String body = response.body().string();
		return engine.fromJson(body, NearbyScans.class);
	}

	@Override
	public ScanResults getRegisteredLocations(ScanResult scanResult) throws IOException {
		Request request = new Request.Builder().url(BASE_URL + "/location.php").post(new FormBody.Builder()
				.add("qr", scanResult.getQrText()).add("barcode", scanResult.getBarcode()).build()).build();
		Response response = client.newCall(request).execute();
		String json = response.body().string();
		Gson engine = createJsonEngine();
		return engine.fromJson(json, ScanResults.class);
	}

}
