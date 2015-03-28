package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.SearchResultAdapter;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.util.List;

public class SearchResultFragment extends Fragment {
    private static final String TAG = "SEARCH_RESULT_UI";
    RecyclerView recyclerView;
    SearchResultAdapter searchAdapter;
    private View rootView;

    List<Category> categories;
    CategoryListAdapter rightMenuAdapter;

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.itemsList);

        searchAdapter = new SearchResultAdapter(getActivity());
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        RecyclerView.ItemAnimator animator = new DefaultItemAnimator();

        recyclerView.setItemAnimator(animator);
        recyclerView.setAdapter(searchAdapter);
        recyclerView.setLayoutManager(manager);
        searchAdapter.setLayoutManager(manager);
        recyclerView.setOnScrollListener(searchAdapter.searchResultsEndlessScrollListener);
        searchAdapter.requestData(0);

        // RIGHT MENU STUFF
        ApiConnector api = ApiConnector.getInstance(getActivity());
        api.requestCategories(new Callback<List<Category>>() {
            @Override
            public void success(List<Category> cats, Response response) {
                categories = cats;
                drawRightMenu(null);
            }

            @Override
            public void failure(RetrofitError error) {
                error.printStackTrace();
            }
        });

        return rootView;
    }

    private void drawRightMenu(Category root) {
        if (root != null) {
            if (root.hasSubcategories()) {
                rightMenuAdapter = new CategoryListAdapter(getActivity());
                rightMenuAdapter.setArrowButtonVisibitityMode(CategoryListAdapter.VisibilityMode.NAVIGATION);
                rightMenuAdapter.setRoot(root);
            }
            searchAdapter.clear();
            searchAdapter.setCategory(root.getId());
            searchAdapter.requestData(0);
        }
        else {
            rightMenuAdapter = new CategoryListAdapter(getActivity());
            rightMenuAdapter.addAll(categories);
        }
        BaseActivity.setRightMenuItemClickListener(categoryClickListener);
        BaseActivity.setRightMenuListAdapter(rightMenuAdapter);
    }

    private OnItemClickListener categoryClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Category selectedCat = rightMenuAdapter.getItem(position);
            if (selectedCat.equals(rightMenuAdapter.getRoot())) {
                Category papa = rightMenuAdapter.findParent(categories, selectedCat);
                drawRightMenu(papa);
                return;
            }
            drawRightMenu(selectedCat);
        }
    };


}
