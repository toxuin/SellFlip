package retrofit;

import android.content.Context;
import android.content.Intent;

import java.io.IOException;

import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import ru.toxuin.sellflip.R;

public class InterceptingClient implements Client {
    private static final String TAG = "InterceptingClient";
    private Context context;
    private Client fallbackClient;

    public InterceptingClient(Context ctx) {
        this.context = ctx.getApplicationContext();
        this.fallbackClient = Platform.get().defaultClient().get();
    }

    public InterceptingClient(Context ctx, Client fallback) {
        this.context = ctx.getApplicationContext();
        this.fallbackClient = fallback;
    }

    @Override
    public Response execute(Request request) throws IOException {
        Response httpResponse = fallbackClient.execute(request);

        // CHECK AUTHORIZATION FAIL
        int status = httpResponse.getStatus();
        if (status == 401 || status == 419) {
            context.sendBroadcast(new Intent(context.getString(R.string.broadcast_intent_auth)));
        }

        // CHECK TOTAL ITEMS
        for (Header header : httpResponse.getHeaders()) {
            try {
                if (header.getName().equals("X-Total-Items")) {
                    Intent intent = new Intent(context.getString(R.string.broadcast_intent_total_items));
                    intent.putExtra("X-Total-Items", Integer.parseInt(header.getValue()));
                    context.sendBroadcast(intent);
                }
            } catch (NullPointerException e) {
                // NO ACTION
            }
        }

        return httpResponse;
    }
}
