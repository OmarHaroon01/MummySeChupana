package com.ttv.facerecog;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface UploadApis {
    @Multipart
    @POST("register")
    Call<ResponseBody> callMultipleUploadApi(@Part List<MultipartBody.Part> image);
}
