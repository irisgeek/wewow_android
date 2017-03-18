package com.wewow.utils;

import android.util.Log;
import android.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

/**
 * Created by suncjs on 2017/3/11.
 */

public class WebAPIHelper {

    private static WebAPIHelper instance = null;

    public static WebAPIHelper getWewowWebAPIHelper() {
        if (WebAPIHelper.instance == null) {
            WebAPIHelper.instance = new WebAPIHelper();
            WebAPIHelper.instance.addDefaultHeader("User-Agent", "Wewow/1.6");
        }
        return WebAPIHelper.instance;
    }

    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PUT = "PUT";
    private static final String HTTP_METHOD_DELETE = "DELETE";

    private static String TAG = "WebAPIHelper";

    private List<Pair<String, String>> defaultHeaders = new ArrayList<Pair<String, String>>();
    private List<Pair<String, String>> defaultParams = new ArrayList<Pair<String, String>>();
    private DefaultHttpClient client;
    private CookieStore ckstore = new BasicCookieStore();

    private StatusLine st = null;
    private HashMap<String, String> responseHeaders = null;

    private WebAPIHelper() {
        this.createClient();
    }

    public byte[] callWebAPI(String url) {
        return this.callWebAPI(url, HttpMethod.GET);
    }

    public byte[] callWebAPI(String url, HttpMethod method) {
        return this.callWebAPI(url, method, null);
    }

    public byte[] callWebAPI(String url, HttpMethod method, byte[] data) {
        return this.callWebAPI(url, method, data, null);
    }

    public byte[] callWebAPI(String url, HttpMethod method, byte[] data, List<Pair<String, String>> headers) {
        this.checkMethod(method, data);
        url = this.addDefaultUrlParamsInUrl(url);
        HttpUriRequest req = this.createRequest(url, method, data);
        this.addDefaultHeaders(req);
        this.addHeaders(req, headers);
        return this.runHttp(req);
    }

    private byte[] runHttp(HttpUriRequest req) {
        Log.d(TAG, String.format("start %s using %s", req.getURI().toString(), req.getMethod()));
        try {
            HttpResponse rsp = this.client.execute(req);
            this.st = rsp.getStatusLine();
            HttpEntity entity = rsp.getEntity();
            this.responseHeaders = new HashMap<String, String>();
            for (Header header : rsp.getAllHeaders()) {
                this.responseHeaders.put(header.getName(), header.getValue());
            }
            InputStream is = entity.getContent();
            int len = (int) entity.getContentLength();
            byte[] buf = new byte[len];
            ByteArrayOutputStream ops = new ByteArrayOutputStream();
            while (true) {
                int num = is.read(buf, 0, len);
                if (num == -1)
                    break;
                ops.write(buf, 0, num);
            }
            is.close();
            byte[] ret = ops.toByteArray();
            ops.close();
            Log.d(TAG, String.format("complete %s as %d", req.getURI().toString(), st.getStatusCode()));
            return ret;
        } catch (IOException ex) {
            Log.d(TAG, String.format("fail %s", req.getURI().toString()));
            return null;
        }
    }

    private void createClient() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        HttpParams params = new BasicHttpParams();
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        this.client = new DefaultHttpClient(cm, params);
        this.client.setCookieStore(this.ckstore);
    }

    private void checkMethod(HttpMethod method, byte[] data) {
        if ((data == null) && ((method == HttpMethod.POST) || (method == HttpMethod.PUT))) {
            throw new InvalidParameterException("http method needs data");
        }
    }

    private HttpUriRequest createRequest(String url, HttpMethod method, byte[] data) {
        HttpUriRequest req = null;
        switch (method) {
            case GET:
                req = new HttpGet(url);
                break;
            case POST:
                HttpPost post = new HttpPost(url);
                HttpEntity outEntity = new ByteArrayEntity(data);
                post.setEntity(outEntity);
                req = post;
                break;
            case PUT:
                HttpPut put = new HttpPut(url);
                HttpEntity putEntity = new ByteArrayEntity(data);
                put.setEntity(putEntity);
                req = put;
                break;
            case DELETE:
                HttpDelete hd = new HttpDelete(url);
                req = hd;
                break;
        }
        return req;
    }

    private void addDefaultHeaders(HttpUriRequest req) {
        for (Pair<String, String> p : this.defaultHeaders) {
            req.setHeader(p.first, p.second);
        }
    }

    private void addHeaders(HttpUriRequest req, List<Pair<String, String>> headers) {
        if (headers == null) {
            return;
        }
        for (Pair<String, String> p : headers) {
            req.setHeader(p.first, p.second);
        }
    }

    private String addDefaultUrlParamsInUrl(String url) {
        return WebAPIHelper.addUrlParams(url, this.defaultParams);
    }

    public void addDefaultHeader(String name, String value) {
        this.defaultHeaders.add(new Pair<String, String>(name, value));
    }

    public void addDefaultParameters(String name, String value) {
        this.defaultParams.add(new Pair<String, String>(name, value));
    }

    public int getResponseCode() {
        if (this.st == null) {
            throw new IllegalStateException("no response");
        } else {
            return this.st.getStatusCode();
        }
    }

    public String getResponseHeader(String name) {
        if (this.responseHeaders == null) {
            throw new IllegalStateException("no response");
        } else {
            if (this.responseHeaders.containsKey(name)) {
                return this.responseHeaders.get(name);
            } else {
                return null;
            }
        }
    }

    public static String addUrlParams(String url, List<Pair<String, String>> params) {
        if (params.size() == 0) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        int qmpos = url.indexOf("?");
        if (qmpos < 0) {
            sb.append(qmpos == url.length() - 1 ? "" : "?");
        } else {
            sb.append(url.charAt(url.length() - 1) == '&' ? "" : "&");
        }
        for (Pair<String, String> p : params) {
            try {
                sb.append(URLEncoder.encode(p.first, "UTF-8"))
                        .append("=")
                        .append(URLEncoder.encode(p.second, "UTF-8"))
                        .append("&");
            } catch (UnsupportedEncodingException ex) {
                Log.e(TAG, String.format("encoding eerror in addDefaultUrlParams, key:%s, value:%s", p.first, p.second));
            }
        }
        return sb.toString();
    }
}
