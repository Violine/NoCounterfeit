package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.security.KeyStore;

/**
 * Created by mike on 23.06.16.
 */
public interface KeyManagementService {
	KeyStore acquireKeyForId(String id) throws Exception;
}
