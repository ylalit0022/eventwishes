package com.ds.eventwishes.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/templates")
    Call<PaginatedResponse<Template>> getTemplates(
        @Query("page") int page,
        @Query("limit") int limit,
        @Query("category") String category
    );

    @GET("api/templates/{id}")
    Call<Template> getTemplate(@Path("id") String id);

    @POST("api/templates")
    Call<Template> createTemplate(@Body Template template);

    @PUT("api/templates/{id}")
    Call<Template> updateTemplate(@Path("id") String id, @Body Template template);

    @DELETE("api/templates/{id}")
    Call<Void> deleteTemplate(@Path("id") String id);

    @POST("api/share")
    Call<ShareResponse> createShareLink(@Body ShareRequest request);

    @GET("api/share/{shortCode}")
    Call<SharedWish> getSharedWish(@Path("shortCode") String shortCode);
}
