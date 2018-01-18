package ru.cyberspacelabs.nocounterfeit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import ru.cyberspacelabs.nocounterfeit.contracts.api.ProtectionValidationService;
import ru.cyberspacelabs.nocounterfeit.dto.NearbyScans;
import ru.cyberspacelabs.nocounterfeit.dto.Protected;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionField;
import ru.cyberspacelabs.nocounterfeit.dto.ProtectionRegistration;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResult;
import ru.cyberspacelabs.nocounterfeit.dto.ScanResults;
import ru.cyberspacelabs.nocounterfeit.json.LongIntegerJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.json.OptionalDateJSONAdapter;
import ru.cyberspacelabs.nocounterfeit.services.ProtectionWebService;

/**
 * Created by mike on 14.04.16.
 */

public class JSONTest {
	private static String protected_data = "{ \"version\" : \"1\", " + "\"records\": [ " + "{\"version\" : \"1\","
			+ "\"qrText\": \"Edit13we4r5tykjhg 1\"," + "\"barcode\": \"34567890\","
			+ "\"updated\": \"2016-03-01 11:24:04\"," + "\"hits\": \"\"" + "}," + "{\"version\" : \"1\","
			+ "\"qrText\": \"Edit1ftghjkl;' 1\"," + "\"barcode\": \"9999991\","
			+ "\"updated\": \"2016-03-01 11:28:30\"," + "\"hits\": \"\"} " + "] " + "}";

	private static String pf_data = "{\"version\" : \"1\",\"qrText\": \"Edit1wertyui 4\",\"barcode\": \"77777773\"}";
	private static String pr_data = "{\"version\" : \"1\",\"qrText\": \"Edit1wertyui 4\",\"barcode\": \"77777773\", \"expired\" : \"180\", \"productName\":\"h311 r@1d3r\", \"updated\": \"2016-04-16 11:23:48\"}";
	private static String las_data = "{ \"version\" : \"1\", \"records\": [ {\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"},{\"version\" : \"1\",\"qrText\": \"qrcode1\",\"barcode\": \"56675687681\",\"city\": \"novosibirsk\",\"district\": \"err\"}] }";

	private Gson engine;
	private ProtectionValidationService serviceClient;

	@Before
	public void prepare() {
		engine = new GsonBuilder().registerTypeAdapter(long.class, new LongIntegerJSONAdapter())
				.registerTypeAdapter(Long.class, new LongIntegerJSONAdapter())
				.registerTypeAdapter(Date.class, new OptionalDateJSONAdapter()).setPrettyPrinting()
				.setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		serviceClient = new ProtectionWebService();
	}

	@Test
	public void deserializeProtected() throws Exception {
		System.out.println("deserializeProtected");
		System.out.println("--------------------");
		Protected obj = engine.fromJson(protected_data, Protected.class);
		System.out.println(engine.toJson(obj));
		System.out.println("--------------------");
	}

	@Test
	public void deserializeProtectionField() throws Exception {
		System.out.println("deserializeProtectionField");
		System.out.println("--------------------");
		ProtectionField pf = engine.fromJson(pf_data, ProtectionField.class);
		System.out.println(engine.toJson(pf));
		System.out.println("--------------------");
	}

	@Test
	public void getQrByBarcode() throws Exception {
		System.out.println("getQrByBarcode");
		System.out.println("--------------------");
		ProtectionRegistration pr = serviceClient.getProtectionRegitration("34567890");
		System.out.println(engine.toJson(pr));
		System.out.println("--------------------");
		Assert.assertEquals("", pr.getQrText());
	}

	@Test
	public void deserializeProtectionRegistration() throws Exception {
		System.out.println("deserializeProtectionRegistration");
		System.out.println("--------------------");
		ProtectionRegistration pf = engine.fromJson(pr_data, ProtectionRegistration.class);
		System.out.println(engine.toJson(pf));
		System.out.println("--------------------");
	}

	@Test
	public void deserializeNearbyScans() throws Exception {
		System.out.println("deserializeNearbyScans");
		System.out.println("--------------------");
		NearbyScans obj = engine.fromJson(las_data, NearbyScans.class);
		System.out.println(engine.toJson(obj));
		System.out.println("--------------------");
	}

	@Test
	public void getNearbyScans() throws Exception {
		System.out.println("getNearbyScans");
		System.out.println("--------------------");
		ScanResult scanResult = new ScanResult();
		scanResult.setCity("Новосибирск");
		scanResult.setDistrict("Советский");
		scanResult.setLatitude(16.01);
		scanResult.setLongitude(54.01);
		scanResult.setHdop(2.0);
		scanResult.setQrText("qrcode1");
		scanResult.setBarcode("56675687681");
		scanResult.setDeviceId("000000000000000");
		scanResult.setCountry("Россия");
		scanResult.setZip("630058");
		scanResult.setBuilding("3");
		scanResult.setStreet("Шлюзовая");
		NearbyScans nr = serviceClient.getNearbyScans(scanResult);
		System.out.println(engine.toJson(nr));
		System.out.println("--------------------");
		Assert.assertNotNull(nr.getRecords().get(0).getCity());
		Assert.assertNotNull(nr.getRecords().get(0).getDistrict());
	}

	@Test
	public void deserializeEmptyScanResults() throws Exception {
		String json = "{\"history\": []}";
		System.out.println("deserializeEmptyScanResults");
		System.out.println("--------------------");
		ScanResults result = engine.fromJson(json, ScanResults.class);
		System.out.println(engine.toJson(result));
		System.out.println("--------------------");
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getHistory());
		Assert.assertEquals(0, result.getHistory().size());
	}

	@Test
	public void deserializeFilledScanResults() throws Exception {
		String json = "{\"history\": [{\"version\" : \"1\", \"qrText\": \"тест майорка 2090а\",\"barcode\": \"01000000000\",\"city\": \"Новосибирск\",\"district\": \"Советский район\",\"zip\": \"630117\",\"street\": \"улица Арбузова\",\"building\": \"1/1 корпус 14\",\"country\": \"Россия\",\"latitude\": \"54.8723\",\"longitude\": \"83.1031\"}]}";
		System.out.println("deserializeFilledScanResults");
		System.out.println("--------------------");
		ScanResults result = engine.fromJson(json, ScanResults.class);
		System.out.println(engine.toJson(result));
		System.out.println("--------------------");
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.getHistory());
		Assert.assertEquals(1, result.getHistory().size());
		Assert.assertNotNull(result.getHistory().get(0).getQrText());
		Assert.assertNotNull(result.getHistory().get(0).getBarcode());
		Assert.assertNotNull(result.getHistory().get(0).getLatitude());
		Assert.assertNotNull(result.getHistory().get(0).getLongitude());
	}
}
