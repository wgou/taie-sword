package io.renren.common.utils;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.*;

public class HttpUtils {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public HttpUtils() {
        client = new OkHttpClient.Builder()
                .callTimeout(20L, TimeUnit.SECONDS)
                .connectTimeout(20L, TimeUnit.SECONDS)
                .build();
    }
    
    public String get(String url) throws IOException {
    	return get(url,null,null);
    }

    // GET 请求 - 带 Headers 和 Query 参数
    public String get(String url, Map<String, String> headers, Map<String, String> queryParams) throws IOException {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(url).newBuilder();
        
        // 添加 Query 参数
        if (queryParams != null) {
            queryParams.forEach(httpUrlBuilder::addQueryParameter);
        }

        HttpUrl httpUrl = httpUrlBuilder.build();

        Request.Builder requestBuilder = new Request.Builder().url(httpUrl);

        // 添加 Headers
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }

    public String post(String url, String json) throws IOException {
    	return post(url,json,null);
    }
    // POST 请求 - 带 Headers 和 Body 参数
    public String post(String url, String json, Map<String, String> headers) throws IOException {
        RequestBody body = RequestBody.create( json,JSON);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        // 添加 Headers
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
    
    public String post(String url) throws IOException {
        RequestBody body = RequestBody.create("",null);
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);
        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }

    // POST 请求 - 带 Headers 和 Form 参数
    public String postForm(String url, Map<String, String> formParams, Map<String, String> headers) throws IOException {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        // 添加 Form 参数
        if (formParams != null) {
            formParams.forEach(formBodyBuilder::add);
        }

        RequestBody body = formBodyBuilder.build();

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(body);

        // 添加 Headers
        if (headers != null) {
            headers.forEach(requestBuilder::addHeader);
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            return response.body() != null ? response.body().string() : null;
        }
    }
     
}
