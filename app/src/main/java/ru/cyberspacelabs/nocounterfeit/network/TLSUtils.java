package ru.cyberspacelabs.nocounterfeit.network;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by mike on 07.08.17.
 */
public class TLSUtils {
	private static HostnameVerifier verifier;
	private static SSLSocketFactory socketFactory;
	private static SSLContext context;
	private static X509TrustManager trustManager;

	public static HostnameVerifier getHostNameVerifier() {
		if (verifier == null) {
			verifier = new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					if (!hostname.contains(".nocounterfeit.info")) {
						return false;
					}
					return true;
				}
			};
		}
		return verifier;
	}

	public static SSLSocketFactory getSslSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
		TrustManager[] tm = new TrustManager[] { getTrustManager() };
		if (context == null) {
			context = SSLContext.getInstance("TLSv1.2");
			context.init(null, tm, new SecureRandom());
		}
		if (socketFactory == null) {
			socketFactory = context.getSocketFactory();
		}
		return socketFactory;
	}

	public static X509TrustManager getTrustManager() throws NoSuchAlgorithmException {
		if (trustManager == null) {
			final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			// Initialise the TMF as you normally would, for example:
			try {
				tmf.init((KeyStore) null);
			} catch (KeyStoreException e) {
				e.printStackTrace();
			}

			trustManager = new X509TrustManager() {
				private final X509TrustManager origTrustmanager = (X509TrustManager) tmf.getTrustManagers()[0];

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					try {
						return origTrustmanager.getAcceptedIssuers();
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
					boolean result = false;

					for (java.security.cert.X509Certificate c : certs) {
						if (c.getSubjectDN().getName().contains(".nocounterfeit.info")) {
							result = true;
							break;
						}
					}

					if (!result) {
						throw new RuntimeException(new SSLPeerUnverifiedException("Peer not match to certificate"));
					}
				}
			};
		}
		return trustManager;
	}
}
