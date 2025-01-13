package com.ds.eventwishes.api;

import com.ds.eventwishes.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private static ApiService instance;

    public static ApiService getInstance() {
        if (instance == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ? 
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            // Create OkHttp Client with longer timeouts for development
            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

            // Create Retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            instance = retrofit.create(ApiService.class);
        }
        return instance;
    }
}
