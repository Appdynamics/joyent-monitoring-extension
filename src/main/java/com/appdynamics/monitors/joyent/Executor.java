package com.appdynamics.monitors.joyent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMReader;

public class Executor {

    private static final Logger LOG = Logger.getLogger(Executor.class);

    static {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public String executeGetRequest(String url, String user, String keyName, String privateKeyPath) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing GET request " + url);
        }
        HttpClient client = new HttpClient();
        GetMethod req = new GetMethod(url);
        try {
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKeyPath, user, keyName, date);
            return executeRequest(client, req);
        } finally {
            req.releaseConnection();
        }
    }

    public String executePostRequest(String url, Map<String, String> parameters, String user, String keyName, String privateKeyPath) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing POST request " + url);
        }
        HttpClient client = new HttpClient();
        PostMethod req = new PostMethod(url);
        setQueryParameters(req, parameters);
        try {
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKeyPath, user, keyName, date);
            return executeRequest(client, req);
        } finally {
            req.releaseConnection();
        }
    }

    public String executeDeleteRequest(String deleteURL, String identity, String keyName, String privateKey) throws HttpException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing DELETE request " + deleteURL);
        }
        HttpClient client = new HttpClient();
        DeleteMethod req = new DeleteMethod(deleteURL);
        try {
            String date = getCurrentTime();
            addDefaultHeaders(req, date);
            addAuthHeader(req, privateKey, identity, keyName, date);
            return executeRequest(client, req);
        } finally {
            req.releaseConnection();
        }
    }

    private String executeRequest(HttpClient httpClient, HttpMethodBase req) throws HttpException {
        try {
            int statusCode = httpClient.executeMethod(req);
            if (statusCode >= 300) {
                throw new HttpException(req.getResponseBodyAsString());
            }
            if (statusCode == 204) { //No Resp 
                return "";
            }
            return req.getResponseBodyAsString();
        } catch (IOException e) {
            LOG.error("Exception while executing request ", e);
            throw new HttpException("Exception while executing request ", e);
        }
    }

    private void setQueryParameters(HttpMethodBase req, Map<String, String> parameters) throws HttpException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : parameters.entrySet()) {
            nameValuePairs.add(new NameValuePair(param.getKey(), param.getValue()));
        }
        req.setQueryString(nameValuePairs.toArray(new NameValuePair[parameters.size()]));
    }

    private void addAuthHeader(HttpMethodBase req, String privateKeyPath, String user, String keyName, String date) {
        File file = new File(privateKeyPath);
        String digest = signSHA256withRSA(file, date);

        String authInfo = "Signature keyId=\"/" + user + "/keys/" + keyName
                + "\",algorithm=\"rsa-sha256\" " + digest;
        req.addRequestHeader("Authorization", authInfo);
    }

    private void addDefaultHeaders(HttpMethodBase req, String date) {
        req.addRequestHeader("Date", date);
        req.addRequestHeader("Accept", "application/json");
        req.addRequestHeader("X-Api-Version", "~6.5");
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
            return new String(Base64.encodeBase64(signature.sign()));
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
