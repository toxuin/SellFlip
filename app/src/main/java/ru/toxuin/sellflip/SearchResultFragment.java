package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.toxuin.sellflip.library.SearchResultAdapter;

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

        return rootView;
    }
}
