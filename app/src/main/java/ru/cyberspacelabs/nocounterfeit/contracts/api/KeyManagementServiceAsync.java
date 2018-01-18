package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.util.UUID;

import ru.cyberspacelabs.nocounterfeit.contracts.api.base.AsynchronousService;

/**
 * Created by mike on 23.06.16.
 */
public interface KeyManagementServiceAsync extends AsynchronousService<KeyManagementServiceCallback> {
	UUID acquireKeyForId(UUID clientID, String id);
}
