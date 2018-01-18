package ru.cyberspacelabs.nocounterfeit;

import android.util.Log;

import org.junit.Assert;
import org.junit.Test;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.cyberspacelabs.nocounterfeit.services.GoogleReverseGeocoder;
import ru.cyberspacelabs.nocounterfeit.util.Util;

/**
 * Created by mike on 23.04.16.
 */
public class DecoderTest {
	@Test
	public void testLatin() {
		String encoded = "b1e2z3_A";
		String decoded = "a1d2y3zA";
		Assert.assertEquals(decoded, Util.decode(encoded));
	}

	@Test
	public void testRussian() {
		String encoded = "к56ь11дгЮ";
		String decoded = "е56я11гвЮ";
		Assert.assertEquals(decoded, Util.decode(encoded));
	}

	@Test
	public void testDates() throws Exception {
		String ds = "2016-04-23";
		String dfin = "yyyy-MM-dd";
		String dfo = "dd MMMM yyyy";
		SimpleDateFormat sdfin = new SimpleDateFormat(dfin);
		SimpleDateFormat sdfo = new SimpleDateFormat(dfo);
		Date d = sdfin.parse(ds);
		String fo = sdfo.format(d);
		System.out.println(fo);
	}

	@Test
	public void testFormat() throws Exception {
		String ds = "2016-04-23 20:59:01 +0700";
		String dfin = "yyyy-MM-dd HH:mm:ss ZZZ";
		SimpleDateFormat sdfin = new SimpleDateFormat(dfin);
		SimpleDateFormat sdfo = new SimpleDateFormat(dfin);
		Date d = sdfin.parse(ds);
		String fo = sdfo.format(d);
		System.out.println(fo);
	}

	@Test
	public void testComplex() {
		String encoded = "rsdpefгбргбр         8812x";
		String decoded = "qrcodeвапвап         8812w";
		Assert.assertEquals(decoded, Util.decode(encoded));
	}

	@Test
	public void testUrlEncode() throws Exception {
		String url = "barcode=33300000003" + "&qr=qrcodeвапвап         8812w" + "&lat=54.854567"
				+ "&lon=83.047619" + "&dhop=2" + "&city=Новосибирск"
				+ "&district=Советский район";
		String url1251 = "/geo.php?" + URLEncoder.encode(url, "windows-1251").replace("%26", "&").replace("%3D", "=");
		String url8 = "/geo.php?" + URLEncoder.encode(url, "UTF-8");
		String urlasc = "/geo.php?" + URLEncoder.encode(url, "ASCII");
		System.out.println("1251: " + url1251);
		System.out.println("UTF: " + url8);
		System.out.println("ASCII: " + urlasc);
	}

	@Test
	public void encoding_reversible() throws Exception {
		String src = "абвгдежзиклмнопрстуф5273ш";
		String enc = Util.encode(src);
		String dec = Util.decode(enc);
		System.out.println(src + "\r\n" + enc + "\r\n" + dec);
		Assert.assertEquals(src, dec);
		src = "хцчшщъыьэюя          9150ш";
		enc = Util.encode(src);
		dec = Util.decode(enc);
		System.out.println(src + "\r\n" + enc + "\r\n" + dec);
		Assert.assertEquals(src, dec);
	}
}
