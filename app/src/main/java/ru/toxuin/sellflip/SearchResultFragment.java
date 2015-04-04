package ru.toxuin.sellflip;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.etsy.android.grid.StaggeredGridViewSellFlip;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.GridSearchAdapter;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.CategoryRequest;

import java.util.List;
import java.util.Map;

public class SearchResultFragment extends SpiceFragment {
    private static final String TAG = "SEARCH_RESULT_UI";
    GridSearchAdapter searchAdapter;
    private StaggeredGridViewSellFlip gridView;
    private View rootView;

    List<Category> categories;
    CategoryListAdapter rightMenuAdapter;
    private static int totalServerItems = 0;
    private String searchQuery;

    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);
    private PendingRequestListener<Category.List> rightMenuSpiceListener;
    private boolean favsMode = false;
    private boolean trending = false;
    private LinearLayout emptyPanel;
    private TextView emptyText;

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        gridView = (StaggeredGridViewSellFlip) rootView.findViewById(R.id.itemList);
        emptyPanel = (LinearLayout) rootView.findViewById(R.id.nothing_panel);
        emptyText = (TextView) rootView.findViewById(R.id.nothing_to_show);

        searchAdapter = new GridSearchAdapter(getActivity(), spiceManager);
        if (favsMode) {
            BaseActivity.setContentTitle("My favorites");
            searchAdapter.favsMode();
        }

        String savedSearchQuery = null;
        String savedCategory = null;
        boolean savedTrending = false;
        if (savedInstanceState != null) {
            savedCategory = savedInstanceState.getString("category", null);
            savedSearchQuery = savedInstanceState.getString("searchQuery", null);
            savedTrending = savedInstanceState.getBoolean("trending", false);
        }
        if (searchQuery != null) {
            searchAdapter.setSearchQuery(searchQuery);
        } else if (savedSearchQuery != null) {
            searchAdapter.setSearchQuery(savedSearchQuery);
        }

        if (savedCategory != null) {
            searchAdapter.setCategory(savedCategory);
        }
        if (savedTrending || trending) {
            BaseActivity.setContentTitle("Trending");
            searchAdapter.setTrending(true);
        }

        gridView.setAdapter(searchAdapter);

        if (!favsMode) gridView.setOnScrollListener(searchAdapter.searchResultsEndlessScrollListener);

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
        if (!prefs.getString("pref_key_search_result_columns", "0").equals("0")) {
            gridView.setColumnCount(Integer.parseInt(prefs.getString("pref_key_search_result_columns", "0")), false);
        }

        // DATA IS FETCHED IN onStart

        // RIGHT MENU STUFF

        rightMenuSpiceListener = new PendingRequestListener<Category.List>() {
            @Override
            public void onRequestNotFound() {}

            @Override
            public void onRequestSuccess(Category.List cats) {
                categories = cats;
                drawRightMenu(null);
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
            }
        };

        spiceManager.execute(new CategoryRequest(), CategoryRequest.getCacheKey(), DurationInMillis.ONE_WEEK, rightMenuSpiceListener);

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
        getActivity().registerReceiver(emptyReciever, new IntentFilter(getActivity().getString(R.string.broadcast_intent_empty_result)));
        searchAdapter.requestData(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(totalItemsBroadcastReceiver);
            getActivity().unregisterReceiver(emptyReciever);
        } catch (Exception e) {
            // ignore, just not registered.
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchAdapter == null) return;
        if (searchAdapter.getCategory() != null && !searchAdapter.getCategory().isEmpty()) {
            outState.putString("category", searchAdapter.getCategory());
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            outState.putString("searchQuery", searchQuery);
        }
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

    private BroadcastReceiver emptyReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            emptyPanel.setVisibility(View.VISIBLE);
            emptyText.setText(message);
        }
    };

    public SearchResultFragment setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (spiceManager == null) return;
        spiceManager.addListenerIfPending(Category.List.class, CategoryRequest.getCacheKey(), rightMenuSpiceListener);
        if (searchAdapter == null) return;
        spiceManager.addListenerIfPending(SingleAd.List.class, searchAdapter.getCacheKey(), searchAdapter.getSpiceListener());
        Map<Object, PendingRequestListener<SingleAd>> favListeners = searchAdapter.getFavListeners();
        for (Object favCacheKey : favListeners.keySet()) {
            spiceManager.addListenerIfPending(SingleAd.class, favCacheKey, favListeners.get(favCacheKey));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (searchAdapter == null) return;
        searchAdapter.hideLoading();
    }

    public SearchResultFragment favsMode() {
        this.favsMode = true;
        return this;
    }

    public SearchResultFragment trending() {
        this.trending = true;
        return this;
    }
}
