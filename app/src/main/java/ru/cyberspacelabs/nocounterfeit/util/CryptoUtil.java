package ru.cyberspacelabs.nocounterfeit.util;

import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.x509.X509V3CertificateGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

/**
 * Created by mike on 28.05.16.
 */
public class CryptoUtil {
	public static final String KEYSTORE_APP = "application.jks";

	public static String packageToAlias(String packageName) {
		return packageName.replace(".", "_");
	}

	private static Date addDays(Date base, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(base);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}

	public static KeyStore initializeKeyStore(String packageName, String deviceId, File jks)
			throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException,
			InvalidAlgorithmParameterException, SignatureException, NoSuchProviderException, InvalidKeyException {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		String keyid = packageToAlias(packageName);
		String id = deviceId + ":" + packageName;
		Security.addProvider(new BouncyCastleProvider());
		if (jks.exists()) {
			ks.load(new FileInputStream(jks), packageName.toCharArray());
		} else {
			ks.load(null, null);
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
			KeyPair kp = kpg.generateKeyPair();

			// build a certificate generator
			X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();
			X500Principal dnName = new X500Principal("CN=" + id);

			certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
			certGen.setSubjectDN(new X509Name("DC=" + id));
			certGen.setIssuerDN(dnName); // use the same
			// yesterday
			certGen.setNotBefore(addDays(new Date(), -1));
			// in 5 year
			certGen.setNotAfter(addDays(new Date(), 1825));
			certGen.setPublicKey(kp.getPublic());
			certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
			certGen.addExtension(X509Extensions.ExtendedKeyUsage, true,
					new ExtendedKeyUsage(KeyPurposeId.anyExtendedKeyUsage));

			X509Certificate cert = certGen.generate(kp.getPrivate(), "BC");
			ks.setKeyEntry(keyid, kp.getPrivate(), packageName.toCharArray(), new Certificate[] { cert });
			ks.store(new FileOutputStream(jks), packageName.toCharArray());
		}
		return ks;
	}

	public static String SHA1Base64(byte[] data) {
		return new String(Base64.encode(data));
	}

	public static java.security.cert.X509Certificate loadCertificate(InputStream in) throws CertificateException {
		CertificateFactory fact = CertificateFactory.getInstance("X.509");
		java.security.cert.X509Certificate cer = (java.security.cert.X509Certificate) fact.generateCertificate(in);
		return cer;
	}

	public static KeyStore decryptPKCS12(InputStream in, String password) throws NoSuchProviderException,
			KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
		KeyStore store = KeyStore.getInstance("PKCS12", "SunJSSE");
		store.load(in, password.toCharArray());
		return store;
	}
}
