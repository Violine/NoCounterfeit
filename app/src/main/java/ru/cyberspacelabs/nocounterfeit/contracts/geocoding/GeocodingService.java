package ru.cyberspacelabs.nocounterfeit.contracts.geocoding;

/**
 * Created by mike on 03.05.16.
 */
public interface GeocodingService {
	void setLocation(double latitude, double longitude);

	String[] getLocationNames();

	String getAddress();

	String getZIP();

	String getCountry();

	String getCity();

	String getDistrict();

	String getArea();

	String getStreet();

	String getBuildingNumber();

	String getPrettyAddress();

	String getLastDump();
}
