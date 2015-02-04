package ru.toxuin.sellflip;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.AdsService;

public class SingleAdFragment extends Fragment {
    public static final String TAG = "SingleAdFragment";

    // TODO: move it probably to separate class
    public static final String API_ENDPOINT_URL = "http://appfrontend-mavd.rhcloud.com/api";
    AdsService adsService;
    RestAdapter restAdapter;

    private View rootView;

    public SingleAdFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_singlead, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        // Testing video
        VideoView videoView = (VideoView) rootView.findViewById(R.id.videoView);
        String path = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.small;
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();
        videoView.setMediaController(new MediaController(getActivity()));
        // videoView.start();

        /*
        * REST example
        * */
        initRest();
        adsService.listAds(new Callback<List<SingleAd>>() {
            @Override public void success(List<SingleAd> singleAds, Response response) {
                Log.i(TAG, singleAds.get(0).toString());
            }

            @Override public void failure(RetrofitError error) {
                Log.i(TAG, "Failure " + error);
            }
        });

        return rootView;
    }

    private void initRest() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Will complain about default JS format without it
                .create();

        restAdapter = new RestAdapter.Builder()
                .setEndpoint(API_ENDPOINT_URL)
                .setConverter(new GsonConverter(gson))
                .build();

        adsService = restAdapter.create(AdsService.class);
    }
}
