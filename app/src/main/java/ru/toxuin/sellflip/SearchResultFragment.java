package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.SearchResultAdapter;

import java.util.LinkedList;

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

        SearchResultAdapter adapter = new SearchResultAdapter(getActivity(), new LinkedList<SingleAd>());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(adapter.searchResultsItemClickListener);
        listView.setOnScrollListener(adapter.searchResultsEndlessScrollListener);
        adapter.requestData(0);

        return rootView;
    }
}
