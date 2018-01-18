package ru.cyberspacelabs.nocounterfeit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 26.04.16.
 */
public class NearbyScans extends VersionedEntity {
	public static class LocationAwareScanResult extends ProtectionField {
		private String city;
		private String district;

		public LocationAwareScanResult() {
			super();
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
	}

	private List<LocationAwareScanResult> records;

	public NearbyScans() {
		super();
		records = new ArrayList<>();
	}

	public List<LocationAwareScanResult> getRecords() {
		return records;
	}

	public void setRecords(List<LocationAwareScanResult> records) {
		this.records = records;
	}
}
