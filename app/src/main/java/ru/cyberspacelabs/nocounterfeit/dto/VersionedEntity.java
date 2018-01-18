package ru.cyberspacelabs.nocounterfeit.dto;

import java.io.Serializable;

/**
 * Created by mike on 12.04.16.
 */
public class VersionedEntity implements Serializable {
	private long version;

	public VersionedEntity(long version) {
		this.version = version;
	}

	public VersionedEntity() {
		this(1L);
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}
}
