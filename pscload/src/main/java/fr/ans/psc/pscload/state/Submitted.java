/*
 * Copyright A.N.S 2021
 */
package fr.ans.psc.pscload.state;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import fr.ans.psc.pscload.state.exception.DownloadException;
import fr.ans.psc.pscload.state.exception.LoadProcessException;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class Idle.
 */
@Slf4j
public class Submitted extends ProcessState {

	private boolean customSSLContext;

	private String certfile;

	private String keyfile;

	private String cafile;

	private String kspwd;

	private String filesDirectory;

	private String extractDownloadUrl;

	/**
	 * Instantiates a new Submitted.
	 */
	public Submitted() {
		super();
		this.customSSLContext = false;
	}

	/**
	 * Instantiates a new Submitted.
	 *
	 * @param extractDownloadUrl the extract download url
	 * @param filesDirectory     the files directory
	 */
	public Submitted(String extractDownloadUrl, String filesDirectory) {
		super();
		this.customSSLContext = false;
		this.extractDownloadUrl = extractDownloadUrl;
		this.filesDirectory = filesDirectory;

	}

	/**
	 * Instantiates a new Submitted.
	 *
	 * @param keyfile            the keyfile
	 * @param certfile           the certfile
	 * @param cafile             the cafile
	 * @param filesDirectory     the files directory
	 * @param extractDownloadUrl the extract download url
	 */
	public Submitted(String keyfile, String certfile, String cafile, String kspwd, String extractDownloadUrl,
					 String filesDirectory) {
		super();
		this.customSSLContext = true;
		this.certfile = certfile;
		this.keyfile = keyfile;
		this.cafile = cafile;
		this.kspwd = kspwd;
		this.extractDownloadUrl = extractDownloadUrl;
		this.filesDirectory = filesDirectory;
	}

	@Override
	public void nextStep() throws LoadProcessException {

		// downloads only if zip doesn't exist in our files directory
		String zipFile;
		try {
			if (customSSLContext) {
				initSSLContext(certfile, keyfile, cafile);
			}
			zipFile = downloadFile();
		} catch (GeneralSecurityException e) {
			log.error("SSL exception during downloading", e);
			throw new DownloadException("SSL exception during downloading", e);

		} catch (IOException e) {
			log.error("Exception during downloading", e);
			throw new DownloadException("Exception during downloading", e);
		}
		// If download is OK next step !
		if (zipFile != null) {
			process.setDownloadedFilename(zipFile);
		}

	}

	/**
	 * Inits the SSL context.
	 *
	 * @param certFile   the cert file
	 * @param keyFile    the key file
	 * @param caCertFile the ca cert file
	 * @throws GeneralSecurityException the general security exception
	 * @throws IOException              Signals that an I/O exception has occurred.
	 */
	public void initSSLContext(String certFile, String keyFile, String caCertFile)
			throws GeneralSecurityException, IOException {
		KeyStore keyStore = keyStoreFromPEM(certFile, keyFile);
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, kspwd.toCharArray());

		KeyStore trustStore = trustStoreFromPEM(caCertFile);
		TrustManagerFactory trustManagerFactory = TrustManagerFactory
				.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(trustStore);

		final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
		SSLContext.setDefault(sslContext);
	}

	/**
	 * Download file.
	 *
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public String downloadFile() throws IOException {
		URL url = new URL(extractDownloadUrl);
		log.info("try to download file from " + extractDownloadUrl);
		HttpURLConnection httpConn;
		// Check if connection is https
		if ("https".equals(url.getProtocol())) {
			httpConn = (HttpsURLConnection) url.openConnection();
		} else {
			httpConn = (HttpURLConnection) url.openConnection();
		}

		int responseCode = httpConn.getResponseCode();

		// always check HTTP response code first
		if (responseCode == HttpURLConnection.HTTP_OK) {
			String fileName = "";
			String disposition = httpConn.getHeaderField("Content-Disposition");

			if (disposition != null) {
				// extracts file name from header field
				int index = disposition.indexOf("filename=");
				if (index > 0) {
					fileName = disposition.substring(disposition.lastIndexOf("=") + 1);
				}
			} else {
				// extracts file name from URL
				fileName = extractDownloadUrl.substring(extractDownloadUrl.lastIndexOf("/") + 1);
			}

			String zipFile = filesDirectory + File.separator + fileName;

			// Check if zip already exists before download
			File[] existingFiles = new File(filesDirectory).listFiles();
			String finalFileName = fileName;
			if (existingFiles != null
					&& Arrays.stream(existingFiles).anyMatch(f -> finalFileName.equals(f.getName()))) {
				log.info("{} already downloaded", fileName);
				httpConn.disconnect();
				return zipFile;
			}

			// opens input stream from the HTTP connection
			InputStream inputStream = httpConn.getInputStream();

			// opens an output stream to save into file
			FileOutputStream outputStream = new FileOutputStream(zipFile);

			int bytesRead;
			byte[] buffer = new byte[4096];
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			log.info("{} downloaded", fileName);

			outputStream.close();
			inputStream.close();
			httpConn.disconnect();

			return zipFile;
		}
		log.info("No files to download. Server replied with HTTP code: {}", responseCode);
		httpConn.disconnect();
		throw new DownloadException(Integer.toString(responseCode));
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeBoolean(customSSLContext);
		output.writeString(cafile);
		output.writeString(certfile);
		output.writeString(keyfile);
		output.writeString(kspwd);
		output.writeString(filesDirectory);
		output.writeString(extractDownloadUrl);
	}

	@Override
	public void read(Kryo kryo, Input input) {
		customSSLContext = input.readBoolean();
		cafile = input.readString();
		certfile = input.readString();
		keyfile = input.readString();
		kspwd = input.readString();
		filesDirectory = input.readString();
		extractDownloadUrl = input.readString();

	}

	private KeyStore trustStoreFromPEM(String caCertFile) throws IOException, GeneralSecurityException {

		PemReader caReader = new PemReader(new FileReader(caCertFile));
		PEMParser caParser = new PEMParser(caReader);

		KeyStore trustStore = KeyStore.getInstance("JKS");
		trustStore.load(null);

		int index = 1;
		Object pemCert;

		while ((pemCert = caParser.readObject()) != null) {
			X509Certificate caCert = new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider())
					.getCertificate((X509CertificateHolder) pemCert);
			trustStore.setCertificateEntry("ca-" + index, caCert);
			index++;
		}
		caParser.close();
		return trustStore;
	}

	private KeyStore keyStoreFromPEM(String certFile, String keyFile) throws IOException, GeneralSecurityException {
		String alias = "psc";

		// Private Key
		PemReader keyReader = new PemReader(new FileReader(keyFile));
		PemObject keyObject = keyReader.readPemObject();

		PrivateKeyInfo pkInfo = PrivateKeyInfo.getInstance(keyObject.getContent());
		PKCS8EncodedKeySpec pkSpec = new PKCS8EncodedKeySpec(pkInfo.getEncoded());
		PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(pkSpec);
		keyReader.close();

		// Certificate
		PemReader certReader = new PemReader(new FileReader(certFile));
		PemObject certObject = certReader.readPemObject();

		List<X509Certificate> certs = new ArrayList<>();
		X509CertificateHolder certHolder = new X509CertificateHolder(certObject.getContent());
		certs.add(new JcaX509CertificateConverter().setProvider(new BouncyCastleProvider()).getCertificate(certHolder));
		certReader.close();

		// Keystore
		KeyStore ks = KeyStore.getInstance("PKCS12");
		ks.load(null);

		for (int i = 0; i < certs.size(); i++) {
			ks.setCertificateEntry(alias + "_" + i, certs.get(i));
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(null);
		keyStore.setKeyEntry(alias, key, kspwd.toCharArray(), certs.toArray(new X509Certificate[certs.size()]));

		return keyStore;
	}

}
