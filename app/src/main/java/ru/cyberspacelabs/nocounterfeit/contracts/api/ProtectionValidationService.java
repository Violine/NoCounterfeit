package ru.cyberspacelabs.nocounterfeit.contracts.api;

import java.io.IOException;
import java.net.MalformedURLException;

import ru.cyberspacelabs.nocounterfeit.dto.NearbyScans;
import ru.cyberspacelabs.nocounterfeit.dto.Protected;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionField;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionRegistration;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResult;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResults;

/**
 * Created by mike on 12.04.16.
 */
public interface ProtectionValidationService {
	ProtectionRegistration getProtectionRegitration(String barcode) throws IOException, MalformedURLException;

	NearbyScans getNearbyScans(ScanResult scanResult) throws IOException;

	ScanResults getRegisteredLocations(ScanResult scanResult) throws IOException;
}
