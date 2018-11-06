package me.andrew.network;

import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ANetwork {
    static volatile ANetwork singleton = null;

    final HttpUrl mBaseUrl;
    final OkHttpClient mOkHttpClient;
    final Retrofit mRetrofit;

    final ThreadLocal<Map<Class<?>, Object>> mThreadLocal = new ThreadLocal<>();

    public ANetwork(HttpUrl baseUrl, OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        mBaseUrl = baseUrl;
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(new GsonBuilder().setLenient().create()))
                .client(mOkHttpClient)
                .build();
    }

    public static ANetwork with() {
        if (singleton == null) {
            synchronized (ANetwork.class) {
                if (singleton == null) {
                    singleton = new ANetwork.Builder().build();
                }
            }
        }
        return singleton;
    }

    public static void setSingletonInstance(ANetwork nioNetwork) {
        if (nioNetwork == null) {
            throw new IllegalArgumentException("ANetwork must not be null.");
        }
        synchronized (ANetwork.class) {
            if (singleton != null) {
                throw new IllegalStateException("Singleton instance already exists.");
            }
            singleton = nioNetwork;
        }
    }

    private String baseUrlJoin(String relativeUrl) {
        String url = mBaseUrl.newBuilder(relativeUrl).toString();
        return Uri.decode(url);
    }

    /**
     * Create an implementation of the API endpoints defined by the {@code service} interface.
     */
    public <T> T create(Class<T> service) {
        return mRetrofit.create(service);
    }

    public <T> T createIfAbsent(Class<T> service) {
        Map<Class<?>, Object> map = mThreadLocal.get();
        if (map == null) {
            map = new HashMap<>();
            mThreadLocal.set(map);
        }

        T instance = (T) map.get(service);
        if (instance == null) {
            instance = mRetrofit.create(service);
            map.put(service, instance);
        }

        return instance;
    }

    public void setDebug(boolean debug) {
        for (Interceptor interceptor : mOkHttpClient.interceptors()) {
            if (interceptor instanceof HttpLoggingInterceptor) {
                HttpLoggingInterceptor loggingInterceptor = (HttpLoggingInterceptor) interceptor;
                loggingInterceptor.setLevel(debug ? HttpLoggingInterceptor.Level.BODY
                        : HttpLoggingInterceptor.Level.NONE);
            }
        }
    }

    public Context getContext() {
        return ANetworkProvider.context;
    }

    public static class Builder {
        private OkHttpClient mOkHttpClient;
        private HttpUrl mBaseUrl;

        public ANetwork.Builder client(OkHttpClient client) {
            mOkHttpClient = client;
            return this;
        }

        public ANetwork.Builder baseUrl(String baseUrl) {
            if (baseUrl == null) {
                throw new NullPointerException("Base url must not be null");
            }
            HttpUrl httpUrl = HttpUrl.parse(baseUrl);
            if (httpUrl == null) {
                throw new IllegalArgumentException("Illegal URL: " + baseUrl);
            }
            return baseUrl(httpUrl);
        }

        public ANetwork.Builder baseUrl(HttpUrl baseUrl) {
            if (baseUrl == null) {
                throw new NullPointerException("Base url must not be null");
            }
            List<String> pathSegments = baseUrl.pathSegments();
            if (!"".equals(pathSegments.get(pathSegments.size() - 1))) {
                throw new IllegalArgumentException("baseUrl must end in /: " + baseUrl);
            }
            this.mBaseUrl = baseUrl;
            return this;
        }

        public ANetwork build() {
            if (mBaseUrl == null) {
                throw new IllegalStateException("Base URL required.");
            }

            if (mOkHttpClient == null) {
                mOkHttpClient = OkHttpClientProvider.getOkHttpClient();
            }

            return new ANetwork(mBaseUrl, mOkHttpClient);
        }
    }
}