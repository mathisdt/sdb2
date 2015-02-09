package org.zephyrsoft.sdb2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Loads certificates from the class path and checks if the are in the system's key store already.
 * Any missing cert will be added (inside the running VM, not on disk) and the trust manager
 * for SSL connections will be reconfigured.
 */
public class CertificateManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(CertificateManager.class);
	
	private static final String KEY_STORE_PASSWORD = "changeit";
	private static final String CERTIFICATE_FACTORY_TYPE = "X.509";
	private static final String TRUST_MANAGER_FACTORY_TYPE = "X509";
	private static final String SSL_CONTEXT_PROTOCOL = "SSL";
	
	private static KeyStore keyStore;
	
	private CertificateManager() {
		// only static use intended
	}
	
	/**
	 * Ensures that "certificates-to-load/*.pem" (from the class path) are present in the trust store.
	 */
	public static void loadDefaultCerts() {
		loadCerts("certificates-to-load/*.pem");
	}
	
	/**
	 * Ensures that the certificates matched by the search string (from the class path) are present in the trust store.
	 * 
	 * @param classpathWildcardSearch
	 *            search string, e.g. "certificates/*.pem"
	 */
	public static void loadCerts(String classpathWildcardSearch) {
		File keyStoreLocation = getKeyStoreLocation();
		keyStore = loadKeyStore(keyStoreLocation);
		
		Resource[] resources = findResources(classpathWildcardSearch);
		if (resources != null) {
			for (Resource resource : resources) {
				handleCertificate(resource);
			}
			updateDefaultTrustManager();
		}
	}
	
	private static File getKeyStoreLocation() {
		File directory = new File(System.getProperty("java.home") + File.separatorChar
			+ "lib" + File.separatorChar + "security");
		File keyStoreFile = new File(directory, "jssecacerts");
		if (!keyStoreFile.exists() || !keyStoreFile.isFile()) {
			keyStoreFile = new File(directory, "cacerts");
		}
		return keyStoreFile;
	}
	
	private static KeyStore loadKeyStore(File keyStoreLocation) {
		LOG.debug("load key store {}", keyStoreLocation.getAbsolutePath());
		try (InputStream keyStoreInputStream = new FileInputStream(keyStoreLocation)) {
			KeyStore defaultKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			defaultKeyStore.load(keyStoreInputStream, KEY_STORE_PASSWORD.toCharArray());
			return defaultKeyStore;
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			String message = "key store " + keyStoreLocation.getAbsolutePath() + " could not be loaded";
			LOG.warn(message, e);
			throw new RuntimeException(message, e);
		}
	}
	
	private static Resource[] findResources(String classpathWildcardSearch) {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		Resource[] resources = null;
		try {
			resources = resolver.getResources("classpath:" + classpathWildcardSearch);
		} catch (IOException e) {
			LOG.warn("certificates could not be found");
		}
		return resources;
	}
	
	private static void handleCertificate(Resource resource) {
		String certificateName =
			resource.getDescription().replaceAll("^.*\\" + File.separator, "").replaceAll("]$", "");
		try {
			URL source = resource.getURL();
			Certificate certificate =
				CertificateFactory.getInstance(CERTIFICATE_FACTORY_TYPE).generateCertificate(source.openStream());
			String certificateAlias = keyStore.getCertificateAlias(certificate);
			if (certificateAlias == null) {
				LOG.info("certificate {} not found, adding it", certificateName);
				keyStore.setCertificateEntry(certificateName, certificate);
			}
		} catch (IOException | CertificateException | KeyStoreException e) {
			LOG.warn("certificate {} could not be processed", resource.getDescription());
		}
	}
	
	private static void updateDefaultTrustManager() {
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TRUST_MANAGER_FACTORY_TYPE);
			trustManagerFactory.init(keyStore);
			TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
			SSLContext sc = SSLContext.getInstance(SSL_CONTEXT_PROTOCOL);
			sc.init(null, trustManagers, null);
			SSLContext.setDefault(sc);
		} catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
			LOG.warn("key store could not be set as default");
		}
	}
	
}
