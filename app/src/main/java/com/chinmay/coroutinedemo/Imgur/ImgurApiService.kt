package com.chinmay.coroutinedemo.Imgur

import com.chinmay.coroutinedemo.model.ImageDetails
import com.chinmay.coroutinedemo.model.UploadImageDetails
import okhttp3.MultipartBody
import retrofit2.http.*

interface ImgurApiService {

    @Headers("Authorization:Client-ID 546c25a59c58ad7")
    @GET("image/{imageHash}")
    suspend fun getImages(@Path("imageHash") imageHash: String): ImageDetails


    @Headers("Authorization:Client-ID 546c25a59c58ad7")
    @Multipart
    @POST("image")
    suspend fun postImage(@Part image: MultipartBody.Part): UploadImageDetails
}