package com.ds.eventwishes.api;

import com.ds.eventwishes.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static ApiService instance;

    public static ApiService getInstance() {
        if (instance == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ? 
                HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            // Create OkHttp Client
            OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

            // Create Retrofit instance
            Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

            instance = retrofit.create(ApiService.class);
        }
        return instance;
    }
}
