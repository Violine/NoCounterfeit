package ru.cyberspacelabs.nocounterfeit.logging;

import android.location.Location;

import java.util.Date;

/**
 * Created by mike on 29.04.16.
 */
public class GeoPosition {
	private Date timestamp;
	private double latitude;
	private double longitude;
	private double precision;
	private String city;
	private String district;
	private String geoCoding;

	public GeoPosition() {
		timestamp = new Date();
	}

	public GeoPosition(Location location) {
		this();
		latitude = location.getLatitude();
		longitude = location.getLongitude();
		precision = location.getAccuracy();
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
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

	public String getGeoCoding() {
		return geoCoding;
	}

	public void setGeoCoding(String geoCoding) {
		this.geoCoding = geoCoding;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}
}
