package ru.cyberspacelabs.nocounterfeit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mike on 12.04.16.
 */
public class Protected extends VersionedEntity {
	private List<ProtectionRegistration> records;

	public Protected() {
		super();
		records = new ArrayList<>();
	}

	public List<ProtectionRegistration> getRecords() {
		return records;
	}

	public void setRecords(List<ProtectionRegistration> records) {
		this.records = records;
	}
}
