package retrofit;

import android.content.Context;
import android.util.Log;

import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class LocalJsonClient implements Client {
    private static final String TAG = "LocalJSONClient";
    private static final long MAXFILEAGE = 2678400000L; // MONTH!
    private Context context;
    private Provider fallbackClient;

    public LocalJsonClient(Context ctx) {
        this.context = ctx;
        this.fallbackClient = Platform.get().defaultClient();
    }

    @Override
    public Response execute(Request request) throws IOException {
        URL requestedUrl = new URL(request.getUrl());

        String fileName = (requestedUrl.getPath()).replace("/", "_").toLowerCase();

        File fwfolder = new File(context.getFilesDir(), "http_caches");
        fwfolder.mkdirs();
        File file = new File(fwfolder, fileName + ".json");

        if (!file.exists()) {
            Log.wtf(TAG, "Could not find " +  file.getAbsolutePath());
            Log.wtf(TAG, "FALLING BACK TO " + fallbackClient.get().getClass().getSimpleName());
            Response httpResponse = fallbackClient.get().execute(request);

            // SAVE TO FILE
            if (file.exists() && file.lastModified() + MAXFILEAGE < System.currentTimeMillis()) file.delete();
            if (!file.exists()) file.createNewFile();
            saveToFile(file, httpResponse.getBody().in());
            Log.d(TAG, "I'M A DISK CACHE, BABY ;-)");
            return httpResponse;
        }

        InputStream inputStream = new FileInputStream(file);
        String mimeType = URLConnection.guessContentTypeFromStream(inputStream);
        if (mimeType == null) {
            mimeType = "application/json";
        }

        TypedInput body = new TypedInputStream(mimeType, inputStream.available(), inputStream);
        return new Response(request.getUrl(), 200, "Content from res/raw/" + fileName, new ArrayList<Header>(), body);
    }

    private static class TypedInputStream implements TypedInput {
        private final String mimeType;
        private final long length;
        private final InputStream stream;

        private TypedInputStream(String mimeType, long length, InputStream stream) {
            this.mimeType = mimeType;
            this.length = length;
            this.stream = stream;
        }

        @Override
        public String mimeType() {
            return mimeType;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public InputStream in() throws IOException {
            return stream;
        }
    }

    private void saveToFile(File file, InputStream input) {
        FileOutputStream output = null;

        try {
            output = new FileOutputStream(file);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
