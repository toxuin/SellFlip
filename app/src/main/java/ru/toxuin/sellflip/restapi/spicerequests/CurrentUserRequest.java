package ru.toxuin.sellflip.restapi.spicerequests;


import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import ru.toxuin.sellflip.entities.User;
import ru.toxuin.sellflip.restapi.ApiService;

public class CurrentUserRequest extends RetrofitSpiceRequest<User, ApiService> {

    public CurrentUserRequest() {
        super(User.class, ApiService.class);
    }

    @Override
    public User loadDataFromNetwork() throws Exception {
        return getService().getCurrentUser();
    }

}
