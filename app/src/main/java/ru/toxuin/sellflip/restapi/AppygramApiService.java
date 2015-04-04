package ru.toxuin.sellflip.restapi;

import retrofit.http.Body;
import retrofit.http.POST;
import ru.toxuin.sellflip.entities.SendFeedBackForm;

public interface AppygramApiService {
    String APPYGRAM = "/appygrams";

    @POST(APPYGRAM) String sendFeedBack(@Body SendFeedBackForm feedBackForm);

}
