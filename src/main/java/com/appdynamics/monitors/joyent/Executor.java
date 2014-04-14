package com.appdynamics.monitors.joyent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;

public class Executor {

    private static final Logger LOG = Logger.getLogger(Executor.class);

    public String executeGetRequest(String url, String user, String keyName, String privateKeyPath) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing GET request " + url);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet req = new HttpGet(url);
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKeyPath, user, keyName, date);
            return executeRequest(httpClient, req);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOG.error("Exception while closing the client", e);
            }
        }
    }

    public String executePostRequest(String url, Map<String, String> parameters, String user, String keyName, String privateKeyPath) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing POST request " + url);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            URI uri = buildURI(url, parameters);
            HttpPost req = new HttpPost(uri);
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKeyPath, user, keyName, date);
            return executeRequest(httpClient, req);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOG.error("Exception while closing the client", e);
            }
        }
    }

    public String executeDeleteRequest(String deleteURL, String identity, String keyName, String privateKey) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing DELETE request " + deleteURL);
        }
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpDelete req = new HttpDelete(deleteURL);
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKey, identity, keyName, date);
            return executeRequest(httpClient, req);
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                LOG.error("Exception while closing the client", e);
            }
        }
    }

    private String executeRequest(CloseableHttpClient httpClient, HttpRequestBase req) throws HttpException {
        HttpResponse response = null;
        try {
            response = httpClient.execute(req);
            if (response.getStatusLine().getStatusCode() >= 300) {
                throw new HttpException(EntityUtils.toString(response.getEntity()));
            }
            if (response.getStatusLine().getStatusCode() != 204) {  //No Resp
                return EntityUtils.toString(response.getEntity());
            } else {
                return "";
            }
        } catch (IOException e) {
            LOG.error("Exception while executing request ", e);
            throw new HttpException("Exception while executing request ", e);
        }
    }

    private URI buildURI(String url, Map<String, String> parameters) throws HttpException {
        try {
            URIBuilder builder = new URIBuilder(url);
            for (Map.Entry<String, String> param : parameters.entrySet()) {
                builder.setParameter(param.getKey(), param.getValue());
            }
            return builder.build();
        } catch (URISyntaxException e) {
            LOG.error("Exception while build URI", e);
            throw new HttpException("Exception while build URI", e);
        }
    }

    private void addAuthHeader(HttpRequestBase req, String privateKeyPath, String user, String keyName, String date) {
        File file = new File(privateKeyPath);
        String digest = signSHA256withRSA(file, date);

        String authInfo = "Signature keyId=\"/" + user + "/keys/" + keyName
                + "\",algorithm=\"rsa-sha256\" " + digest;
        req.addHeader("Authorization", authInfo);
    }

    private void addDefaultHeaders(HttpRequestBase req, String date) {
        req.addHeader("Date", date);
        req.addHeader("Accept", "application/json");
        req.addHeader("X-Api-Version", "~6.5");
    }


    private String getCurrentTime() {
        SimpleDateFormat fmt = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
        fmt.setTimeZone(TimeZone.getTimeZone("GMT"));
        return fmt.format(new Date());
    }

    private String signSHA256withRSA(File privateKey, String data) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(getPrivateKey(privateKey));
            signature.update(data.getBytes());
            return Base64.encodeBase64String(signature.sign());
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Exception while signing ", e);
            throw new RuntimeException("Exception while signing ", e);
        } catch (InvalidKeyException e) {
            LOG.error("Exception while signing ", e);
            throw new RuntimeException("Exception while signing ", e);
        } catch (IOException e) {
            LOG.error("Exception while signing ", e);
            throw new RuntimeException("Exception while signing ", e);
        } catch (SignatureException e) {
            LOG.error("Exception while signing ", e);
            throw new RuntimeException("Exception while signing ", e);
        }
    }

    private PrivateKey getPrivateKey(File privateKey) throws IOException {

        FileReader fileReader = new FileReader(privateKey);
        PEMReader prmReader = new PEMReader(fileReader);
        try {
            return ((KeyPair) prmReader.readObject()).getPrivate();
        } catch (IOException ex) {
            LOG.error("The private key could not be decrypted", ex);
            throw new RuntimeException("The private key could not be decrypted", ex);
        } finally {
            prmReader.close();
            fileReader.close();
        }
    }


}
