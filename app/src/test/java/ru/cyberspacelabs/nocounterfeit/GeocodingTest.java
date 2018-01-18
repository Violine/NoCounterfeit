package ru.cyberspacelabs.nocounterfeit;

import org.junit.Test;

import ru.cyberspacelabs.nocounterfeit.contracts.geocoding.GeocodingService;
import ru.cyberspacelabs.nocounterfeit.services.GoogleReverseGeocoder;
import ru.cyberspacelabs.nocounterfeit.services.MapQuestReverseGeocoder;

/**
 * Created by mike on 03.05.16.
 */
public class GeocodingTest {
	@Test
	public void googleReverseGeocoding() {
		System.out.println("Google");
		System.out.println("--------------------------");
		GeocodingService geocoder = new GoogleReverseGeocoder();
		geocoder.setLocation(54.854567, 83.047619);
		String address = geocoder.getAddress();
		String zip = geocoder.getZIP();
		String country = geocoder.getCountry();
		String area = geocoder.getArea();
		String city = geocoder.getCity();
		String district = geocoder.getDistrict();
		String route = geocoder.getStreet();
		String building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(54.9067433, 82.9758575);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.0162481, 82.928048);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.0664951, 82.89924);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(54.7995721, 83.198912);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.138562, 82.921525);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		System.out.println("--------------------------");
	}

	@Test
	public void mapQuestReverseGeoCoding() {
		System.out.println("MapQuest");
		System.out.println("--------------------------");
		GeocodingService geocoder = new MapQuestReverseGeocoder().setApiKey("NFGnt2hRYqDYWT8H1dbB8SyyRo8V3CH6")
				.setLanguage("en_US");
		geocoder.setLocation(54.854567, 83.047619);
		String address = geocoder.getAddress();
		String zip = geocoder.getZIP();
		String country = geocoder.getCountry();
		String area = geocoder.getArea();
		String city = geocoder.getCity();
		String district = geocoder.getDistrict();
		String route = geocoder.getStreet();
		String building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(54.9067433, 82.9758575);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.0162481, 82.928048);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.0664951, 82.89924);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(54.7995721, 83.198912);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		// System.out.println(geocoder.getLastDump());
		System.out.println("--------------------------");

		geocoder.setLocation(55.138562, 82.921525);
		address = geocoder.getAddress();
		zip = geocoder.getZIP();
		country = geocoder.getCountry();
		area = geocoder.getArea();
		city = geocoder.getCity();
		district = geocoder.getDistrict();
		route = geocoder.getStreet();
		building = geocoder.getBuildingNumber();
		System.out.println(address);
		System.out.println(geocoder.getPrettyAddress());
		System.out.println("--------------------------");
	}
}
