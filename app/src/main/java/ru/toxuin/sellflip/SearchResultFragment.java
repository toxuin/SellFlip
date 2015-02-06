package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.LoadingCallback;
import ru.toxuin.sellflip.library.SearchResultAdapter;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.util.List;

public class SearchResultFragment extends Fragment {
    private static final String TAG = "SEARCH_RESULT_UI";
    private View rootView;
    ListView listView;

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        listView = (ListView) rootView.findViewById(R.id.itemsList);

        // DO STUFF

        ApiConnector api = ApiConnector.getInstance();
        api.requestTopAds(new LoadingCallback<List<SingleAd>>(getActivity()) {
            @Override
            protected void onSuccess(List<SingleAd> allAds, Response response) {
                SearchResultAdapter adapter = new SearchResultAdapter(getActivity(), allAds);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(adapter.searchReslutsItemClickListener);
                //listView.setOnScrollListener(adapter.searchResultsEndlessScrollListener);
                Log.d(TAG, "GOT " + allAds.size() + " ITEMS!");
            }

            @Override
            protected void onFailure(RetrofitError error) {
                Toast.makeText(getActivity(), "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
