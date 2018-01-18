package ru.cyberspacelabs.nocounterfeit;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

import ru.cyberspacelabs.nocounterfeit.services.KeyManagementWebService;
import ru.cyberspacelabs.nocounterfeit.util.CryptoUtil;

/**
 * Created by mike on 28.05.16.
 */
public class KeyStoreTest {
	private String imei = "896049353001447";
	private String packageId = "ru.cyberspacelabs.nocounterfeit";
	private String jks = "cryptest.jks";

	@Test
	public void generateKeyStore() throws Exception {
		KeyStore ks = CryptoUtil.initializeKeyStore(packageId, imei, new File(jks));
		Certificate cert = ks.getCertificate(CryptoUtil.packageToAlias(packageId));
		System.out.println(cert);
		new FileOutputStream(System.currentTimeMillis() + "_test.crt").write(cert.getEncoded());
		System.out.println(cert.getPublicKey());
	}

	@Test
	public void getKeyFromServer() throws Exception {
		String id = "0x" + Long.toHexString(System.currentTimeMillis()).toUpperCase();
		KeyStore ks = new KeyManagementWebService().acquireKeyForId(id);
		Enumeration<String> aliases = ks.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			System.out.print(alias + ": ");
			if (ks.isKeyEntry(alias)) {
				System.out.println(" Private key");
				Key k = ks.getKey(alias, KeyManagementWebService.passwordForId(id).toCharArray());
				System.out.println(k);
				Certificate c = ks.getCertificate(alias);
				System.out.println(c);
				Certificate[] chain = ks.getCertificateChain(alias);
				if (chain != null && chain.length > 0) {
					for (int i = 0; i < chain.length; i++) {
						System.out.println(chain[i]);
					}
				}
			} else {
				System.out.println(" Certificate");
				System.out.println(ks.getCertificate(alias));
			}
		}
	}
}
