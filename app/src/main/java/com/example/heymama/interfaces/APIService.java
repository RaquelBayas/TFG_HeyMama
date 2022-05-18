package com.example.heymama.interfaces;

import com.example.heymama.MyResponse;
import com.example.heymama.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-type:application/json",
            "Authorization:key=AAAAPwxbCEg:APA91bEbRHTsIjjZPT7hZnRixSOP77kbTmysaV9J9cAXop4LeaXkLXM0_JhW5cU9gwZL7UvVtEbwXsAVxBP5KEdjODZ46oxvaCLaW1D8tS1W_XtB34EfZ8jLKdvHMC9bOntxFGVrMeRi"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
