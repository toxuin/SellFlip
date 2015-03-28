package ru.toxuin.sellflip.library;

import android.app.ProgressDialog;
import android.content.Context;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Callback wrapper that handles loading spinner hide and show
 * error messaging and whatever else that you want.
 * @param <T>
 */
public abstract class LoadingCallback<T> implements Callback<T> {
    private static final String TAG = "LOADING_CALLBACK";
    private ProgressDialog loading;

    public LoadingCallback(Context context) {
        loading = new ProgressDialog(context);
        loading.setTitle("Loading");
        loading.setIndeterminate(true);
        loading.setMessage("Wait while loading...");
        loading.show();
    }

    protected abstract void onSuccess(T t, Response response);
    protected abstract void onFailure(RetrofitError error);

    public void success(T t, Response response) {
        hideLoading();
        onSuccess(t, response);
    }

    public void failure(RetrofitError error) {
        hideLoading();
        onFailure(error);
    }

    private void hideLoading() {
        loading.dismiss();
    }
}
