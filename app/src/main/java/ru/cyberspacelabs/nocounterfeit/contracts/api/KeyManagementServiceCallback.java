package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.security.KeyStore;

import ru.cyberspacelabs.nocounterfeit.async.Result;
import ru.cyberspacelabs.nocounterfeit.contracts.api.base.AsynchronousResultCallback;

/**
 * Created by mike on 23.06.16.
 */
public interface KeyManagementServiceCallback extends AsynchronousResultCallback {
	void onKeyAcquired(Result<KeyStore> result);
}
