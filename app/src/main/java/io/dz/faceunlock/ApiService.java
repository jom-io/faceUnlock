package io.dz.faceunlock;


import java.util.List;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiService {

    @Multipart
    @POST
    Observable<Object> uploadFile(@Url String fileUrl, @Part List<MultipartBody.Part> partList);

}
