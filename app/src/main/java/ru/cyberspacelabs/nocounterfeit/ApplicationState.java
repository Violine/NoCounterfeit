package ru.cyberspacelabs.nocounterfeit;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.support.multidex.MultiDex;

import ru.cyberspacelabs.nocounterfeit.contracts.geocoding.GeocodingService;

/**
 * Created by mike on 07.07.16.
 */
public class ApplicationState extends Application {
	private Location location;
	private String city;
	private String district;
	private GeocodingService geoCoder;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void attachBaseContext(Context base) {
		MultiDex.install(base);
		super.attachBaseContext(base);
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public GeocodingService getGeoCoder() {
		return geoCoder;
	}

	public void setGeoCoder(GeocodingService geoCoder) {
		this.geoCoder = geoCoder;
	}
}
