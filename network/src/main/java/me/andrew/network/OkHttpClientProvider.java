/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 * <p>
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

package me.andrew.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Helper class that provides the same OkHttpClient instance that will be used for all networking
 * requests.
 */
public final class OkHttpClientProvider {

    // Centralized OkHttpClient for all networking requests.
    private static
    OkHttpClient sClient;

    public static OkHttpClient getOkHttpClient() {
        if (sClient == null) {
            sClient = createCommonBuilder().build();
        }
        return sClient;
    }

    // okhttp3 OkHttpClient is immutable
    // This allows app to init an OkHttpClient with custom settings.
    public static void replaceOkHttpClient(OkHttpClient client) {
        sClient = client;
    }

    public static OkHttpClient.Builder createCommonBuilder() {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);
    }
}
