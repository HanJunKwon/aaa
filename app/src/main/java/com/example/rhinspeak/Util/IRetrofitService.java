package com.example.rhinspeak.Util;

import com.example.rhinspeak.Model.Exist;
import com.example.rhinspeak.Model.ImageInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IRetrofitService {

    /**
     * img_id에 해당하는 이미지가 존재하는지 여부확인
     * @param img_id 이미지 번호
     * @return 이미지 존재 여부
     */
    @GET("isImageExist")
    Call<Exist> isImageExist(@Query("img_id") int img_id);

    /**
     * 이미지 정보를 가져온다
     * @param img_id 이미지 번호
     * @return 이미지 정보
     */
    @GET("getImageInfo")
    Call<ImageInfo> getImageInfo(@Query("img_id") int img_id);
}
