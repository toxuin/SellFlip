package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.LoadingCallback;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.util.List;

public class SearchResultFragment extends Fragment {
    private View rootView;
    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        // DO STUFF

        ApiConnector api = ApiConnector.getInstance();
        api.requestTopAds(new LoadingCallback<List<SingleAd>>(getActivity()) {
            @Override
            protected void onSuccess(List<SingleAd> singleAds, Response response) {

            }

            @Override
            protected void onFailure(RetrofitError error) {

            }
        });

        return rootView;
    }
}
