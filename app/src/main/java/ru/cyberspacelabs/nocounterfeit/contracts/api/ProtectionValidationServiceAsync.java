package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.util.UUID;

import ru.cyberspacelabs.nocounterfeit.contracts.api.base.AsynchronousService;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionField;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResult;

/**
 * Created by mike on 12.04.16.
 */
public interface ProtectionValidationServiceAsync extends AsynchronousService<ProtectionValidationServiceCallback> {
	UUID getProtectionRegistration(UUID clientID, String barcode);

	UUID getNearbyScans(UUID clientID, ScanResult scanResult);

	UUID getRegisteredLocations(UUID clientID, ScanResult scanResult);
}
