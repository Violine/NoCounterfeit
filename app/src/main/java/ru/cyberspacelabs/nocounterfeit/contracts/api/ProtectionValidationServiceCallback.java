package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.util.UUID;

import ru.cyberspacelabs.nocounterfeit.async.Result;
import ru.cyberspacelabs.nocounterfeit.contracts.api.base.AsynchronousResultCallback;
import ru.cyberspacelabs.nocounterfeit.dto.NearbyScans;
import ru.cyberspacelabs.nocounterfeit.dto.Protected;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionField;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionRegistration;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResults;

/**
 * Created by mike on 12.04.16.
 */
public interface ProtectionValidationServiceCallback extends AsynchronousResultCallback {
	void onProtectionRegistration(Result<ProtectionRegistration> result);

	void onNearbyScans(Result<NearbyScans> result);

	void onRegisteredLocations(Result<ScanResults> result);
}
