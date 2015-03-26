package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.library.SearchResultAdapter;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.util.List;

public class SearchResultFragment extends Fragment {
    private static final String TAG = "SEARCH_RESULT_UI";
    RecyclerView recyclerView;
    private View rootView;

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.itemsList);

        // DO STUFF

        SearchResultAdapter adapter = new SearchResultAdapter(getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();

        recyclerView.setItemAnimator(animator);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(manager);
        adapter.setLayoutManager(manager);
        recyclerView.setOnScrollListener(adapter.searchResultsEndlessScrollListener);
        adapter.requestData(0);

        ApiConnector.getInstance(getActivity()).requestCategories(new Callback<List<Category>>() {
            @Override
            public void success(List<Category> categories, Response response) {
                Log.d(TAG, "SUCCESS! GOT CATEGORIES: " + categories.size());
                for (Category cat : categories) {
                    Log.d(TAG, cat.getName() + ", " + (cat.getSubcategories() == null? 0 : cat.getSubcategories().size()) + " SUBCATEGORIES");
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "FAILURE! ERROR: " + error.getMessage());
                error.printStackTrace();
            }
        });

        /*
        ApiConnector.getInstance().requestCategories(new Callback<JsonElement>() {
            @Override
            public void success(JsonElement element, Response response) {
                String json = element.toString();

                Log.d(TAG, "SUCCESS! GOT CATEGORIES: " + json);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "FAILURE! ERROR: " + error.getMessage());
                error.printStackTrace();
            }
        });
        */

        return rootView;
    }
}
