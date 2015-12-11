package cybulskij.nazar.avoidpoints.network;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * Created by nazar on 15.10.15.
 */


public interface DirectionService {


    @Headers({
            "Sign-Request: true"
    })
    @GET("/json")
    void getDirection(@Query("origin") String from,
                      @Query("destination") String to,
                      @Query("mode") String mode,
                      @Query("key") String key,
                      @Query("sensor") Boolean sensor,
                      @Query("language") String language,
                      @Query("alternatives") Boolean alternatives,
                      Callback<Response> callback);


    @Headers({
            "Sign-Request: true"
    })
    @GET("/json")
    void getMatrixDirection(@Query("origins") String from,
                            @Query("destinations") String to,
                            @Query("mode") String mode,
                            @Query("key") String key,
                            @Query("sensor") Boolean sensor,
                            @Query("language") String language,
                            Callback<Response> callback);




}
