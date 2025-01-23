package com.ds.eventwishes.api;

import com.ds.eventwishes.model.Template;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @GET("api/templates")
    Call<ApiResponse<PaginatedResponse<Template>>> getTemplates(
        @Query("page") int page,
        @Query("limit") int limit,
        @Query("category") String category
    );

    @GET("api/templates/{id}")
    Call<ApiResponse<Template>> getTemplate(@Path("id") String id);

    @POST("api/templates")
    Call<ApiResponse<Template>> createTemplate(@Body Template template);

    @PUT("api/templates/{id}")
    Call<ApiResponse<Template>> updateTemplate(@Path("id") String id, @Body Template template);

    @DELETE("api/templates/{id}")
    Call<ApiResponse<Void>> deleteTemplate(@Path("id") String id);

    @POST("api/share")
    Call<ApiResponse<ShareResponse>> createShareLink(@Body ShareRequest request);

    @GET("api/share/{shortCode}")
    Call<ApiResponse<SharedWish>> getSharedWish(@Path("shortCode") String shortCode);
}
