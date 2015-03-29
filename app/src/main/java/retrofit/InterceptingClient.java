package retrofit;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import retrofit.client.Client;
import retrofit.client.Request;
import retrofit.client.Response;
import ru.toxuin.sellflip.R;

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
            context.sendBroadcast(new Intent(context.getString(R.string.broadcast_intent_auth)));
        }
        return httpResponse;
    }
}
