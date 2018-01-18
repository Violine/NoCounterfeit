package ru.cyberspacelabs.nocounterfeit.dto;

/**
 * Created by mike on 12.04.16.
 */
public class ProtectionField extends VersionedEntity {
	private String qrText;
	private String barcode;

	public ProtectionField() {
		super();
	}

	public ProtectionField(String qrText, String barcode) {
		super();
		this.qrText = qrText;
		this.barcode = barcode;
	}

	public String getQrText() {
		return qrText;
	}

	public void setQrText(String qrText) {
		this.qrText = qrText;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
}
