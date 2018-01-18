package ru.cyberspacelabs.nocounterfeit.dto;

import java.util.Date;

/**
 * Created by mike on 26.04.16.
 */
public class ProtectionRegistration extends ProtectionField {
	private String productName;
	private Date expired;
	private Date updated;
	private long hits;

	public ProtectionRegistration() {
		super();
	}

	public ProtectionRegistration(String qrText, String barcode) {
		super(qrText, barcode);
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Date getExpired() {
		return expired;
	}

	public void setExpired(Date expired) {
		this.expired = expired;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public long getHits() {
		return hits;
	}

	public void setHits(long hits) {
		this.hits = hits;
	}
}
