package ru.toxuin.sellflip.restapi.spicerequests;

import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import ru.toxuin.sellflip.entities.SendFeedBackForm;
import ru.toxuin.sellflip.restapi.AppygramApiService;

public class SendFeedBackRequest extends RetrofitSpiceRequest<String, AppygramApiService> {
    private SendFeedBackForm feedBackForm;

    public SendFeedBackRequest(SendFeedBackForm feedBackForm) {
        super(String.class, AppygramApiService.class);
        this.feedBackForm = feedBackForm;
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        return getService().sendFeedBack(feedBackForm);
    }

}

