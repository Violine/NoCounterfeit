package ru.cyberspacelabs.nocounterfeit.services;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import ru.cyberspacelabs.nocounterfeit.async.Result;
import ru.cyberspacelabs.nocounterfeit.contracts.api.KeyManagementService;
import ru.cyberspacelabs.nocounterfeit.contracts.api.KeyManagementServiceAsync;
import ru.cyberspacelabs.nocounterfeit.contracts.api.KeyManagementServiceCallback;
import ru.cyberspacelabs.nocounterfeit.util.CryptoUtil;

/**
 * Created by mike on 23.06.16.
 */
public class KeyManagementWebService implements KeyManagementService, KeyManagementServiceAsync, Callback {
	public static String DISPATCH_ID_ACQUIRE_KEY = "acquireKeyForId";
	public static String DISPATCH_ID_DOWNLOAD_KEY = "downloadKey";
	private static String BASE_URL = "http://gr.nocounterfeit.info";
	private OkHttpClient client;
	private Map<UUID, KeyManagementServiceCallback> callbacks;
	private Map<Request, Result> requests;

	public KeyManagementWebService() {
		client = new OkHttpClient();
		callbacks = new ConcurrentHashMap<>();
		requests = new ConcurrentHashMap<>();
	}

	@Override
	public void addCallback(KeyManagementServiceCallback callback) {
		if (callback != null) {
			callbacks.put(callback.getClientID(), callback);
		}
	}

	@Override
	public void removeCallback(KeyManagementServiceCallback callback) {
		if (callback != null) {
			callbacks.remove(callback.getClientID());
		}
	}

	@Override
	public KeyStore acquireKeyForId(String id) throws Exception {
		Response redir = client.newCall(createRequest("/key.php?imei=" + id)).execute();
		String pkcs12addr = redir.body().string();
		Response pkcs12 = client.newCall(new Request.Builder().url(pkcs12addr).build()).execute();
		return CryptoUtil.decryptPKCS12(pkcs12.body().byteStream(), passwordForId(id));
	}

	@Override
	public UUID acquireKeyForId(UUID clientID, String id) {
		Result<String> result = new Result<String>(clientID, DISPATCH_ID_ACQUIRE_KEY) {
			@Override
			public Class<String> getEntityClass() {
				return String.class;
			}
		};
		result.setCorrelationID(id);
		return scheduleExecution(createRequest("/key.php?imei=" + id), result);
	}

	private Request createRequest(String relativeURL) {
		return new Request.Builder().url(BASE_URL + relativeURL).build();
	}

	private void dispatchResult(Result result) {
		KeyManagementServiceCallback callback = callbacks.get(result.getClientID());
		if (DISPATCH_ID_ACQUIRE_KEY.equalsIgnoreCase(result.getDispatchID())) {
			if (result.isSuccessful()) {
				downloadKey(result.getClientID(), result.getValue().toString(), result.getCorrelationID());
			} else {
				callback.onKeyAcquired(result);
			}
		}

		if (DISPATCH_ID_DOWNLOAD_KEY.equalsIgnoreCase(result.getDispatchID())) {
			if (result.isSuccessful()) {
				Result<KeyStore> unwrapped = new Result<KeyStore>(UUID.randomUUID(), DISPATCH_ID_ACQUIRE_KEY) {
					@Override
					public Class<KeyStore> getEntityClass() {
						return KeyStore.class;
					}
				};
				unwrapped.setCorrelationID(result.getCorrelationID());
				ByteBuffer bb = (ByteBuffer) result.getValue();
				try {
					unwrapped.setValue(CryptoUtil.decryptPKCS12(new ByteArrayInputStream(bb.array()),
							passwordForId(result.getCorrelationID())));
				} catch (Exception e) {
					unwrapped.setCause(e);
				}
				callback.onKeyAcquired(unwrapped);
			} else {
				callback.onKeyAcquired(result);
			}
		}
	}

	private void downloadKey(UUID clientID, String redirect, String id) {
		scheduleExecution(new Request.Builder().url(redirect).build(),
				new Result<ByteBuffer>(clientID, DISPATCH_ID_DOWNLOAD_KEY) {
					@Override
					public Class<ByteBuffer> getEntityClass() {
						return ByteBuffer.class;
					}
				});
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

	@Override
	public void onResponse(Call call, Response response) throws IOException {
		Result result = requests.get(response.request());
		try {
			if (result.getEntityClass() != Void.class) {
				if (result.getEntityClass().equals(ByteBuffer.class)) {
					result.setValue(ByteBuffer.wrap(response.body().bytes()));
				} else if (result.getEntityClass().equals(String.class)) {
					result.setValue(response.body().string());
				} else {
					throw new IOException("Unsupported serialization to " + result.getEntityClass().getName());
				}
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

	public static String passwordForId(String id) {
		return id + "gr.nocounterfeit.info";
	}
}
