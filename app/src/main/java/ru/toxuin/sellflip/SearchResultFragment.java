package ru.toxuin.sellflip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.etsy.android.grid.StaggeredGridView;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.GridSearchAdapter;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.CategoryRequest;

import java.util.List;

public class SearchResultFragment extends SpiceFragment {
    private static final String TAG = "SEARCH_RESULT_UI";
    GridSearchAdapter searchAdapter;
    private StaggeredGridView gridView;
    private View rootView;

    List<Category> categories;
    CategoryListAdapter rightMenuAdapter;
    private static int totalServerItems = 0;

    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        gridView = (StaggeredGridView) rootView.findViewById(R.id.itemList);

        searchAdapter = new GridSearchAdapter(getActivity(), spiceManager);
        gridView.setAdapter(searchAdapter);

        gridView.setOnScrollListener(searchAdapter.searchResultsEndlessScrollListener);
        // DATA IS FETCHED IN onStart

        // RIGHT MENU STUFF
        spiceManager.execute(new CategoryRequest(), CategoryRequest.getCacheKey(), DurationInMillis.ONE_WEEK, new RequestListener<Category.List>() {
            @Override
            public void onRequestSuccess(Category.List cats) {
                categories = cats;
                drawRightMenu(null);
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
            }
        });


        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d(TAG, "!!!!! SCREEN WIDTH: " + dpWidth);


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

    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(totalItemsBroadcastReceiver, new IntentFilter(getActivity().getString(R.string.broadcast_intent_total_items)));
        searchAdapter.requestData(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(totalItemsBroadcastReceiver);
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    public static int getTotalServerItems() {
        return totalServerItems;
    }

    private BroadcastReceiver totalItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            totalServerItems = intent.getIntExtra("X-Total-Items", 0);
            Log.d("TOTAL-BROADCAST", "GOT TOTAL ITEMS ON SERVER: " + totalServerItems);
        }
    };
}
