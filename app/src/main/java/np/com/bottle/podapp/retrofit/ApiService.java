package np.com.bottle.podapp.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface ApiService {
    @FormUrlEncoded
    @POST
    Call<ResponseBody> postData(@Url String url,
                                @Field(value = "data", encoded = true) String data);
}
