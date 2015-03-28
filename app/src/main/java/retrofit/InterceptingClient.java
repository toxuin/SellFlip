package retrofit;

import android.content.Context;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.LogInFragment;

import java.io.IOException;

public class InterceptingClient implements Client {
    private static final String TAG = "InterceptingClient";
    private Context context;
    private Client fallbackClient;

    public InterceptingClient(Context ctx) {
        this.context = ctx;
        this.fallbackClient = Platform.get().defaultClient().get();
    }

    public InterceptingClient(Context ctx, Client fallback) {
        this.context = ctx;
        this.fallbackClient = fallback;
    }

    @Override
    public Response execute(Request request) throws IOException {
            Response httpResponse = fallbackClient.execute(request);
            int status = httpResponse.getStatus();
            if (status == 401 || status == 419) {
                BaseActivity.setContent(new LogInFragment());
            }
            return httpResponse;
    }
}
