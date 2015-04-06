package ru.toxuin.sellflip;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.Toast;

import com.bulletnoid.android.widget.StaggeredGridView.StaggeredGridView;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.GridSearchAdapter;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.CategoryRequest;
import ru.toxuin.sellflip.restapi.spicerequests.ListAdsRequest;
import ru.toxuin.sellflip.restapi.spicerequests.SingleAdRequest;

public class SearchResultFragment extends SpiceFragment {
    private static final String TAG = "SEARCH_RESULT_UI";

    private StaggeredGridView gridView;
    private View rootView;

    CategoryListAdapter rightMenuAdapter;
    GridSearchAdapter searchAdapter;

    private LinearLayout emptyPanel;
    private TextView emptyText;
    private ProgressDialog loading;
    List<Category> categories;
    private String category;

    private Mode mode = Mode.NORMAL;
    private String searchQuery;
    private boolean orderByLikes = false;

    // LOADING ITEMS
    private int page = 0;
    private static int totalServerItems = 0;
    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);
    private PendingRequestListener<Category.List> rightMenuSpiceListener;
    private PendingRequestListener<SingleAd.List> listRequestListener;
    private Object listCacheKey;
              // CACHE KEY - LISTENER
    private Map<Object, PendingRequestListener<SingleAd>> favAdsRequests = new HashMap<>();

    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);
        gridView = (StaggeredGridView) rootView.findViewById(R.id.itemList);
        emptyPanel = (LinearLayout) rootView.findViewById(R.id.nothing_panel);
        emptyText = (TextView) rootView.findViewById(R.id.nothing_to_show);

        searchAdapter = new GridSearchAdapter(getActivity(), spiceManager);
        if (mode == Mode.FAVS) {
            BaseActivity.setContentTitle("My favorites");
        } else if (mode == Mode.MY_ADS) {
            BaseActivity.setContentTitle("My Ads");
        }


        String savedSearchQuery = null;
        String savedCategory = null;
        boolean savedTrending = false;
        if (savedInstanceState != null) {
            savedCategory = savedInstanceState.getString("category", null);
            savedSearchQuery = savedInstanceState.getString("searchQuery", null);
            savedTrending = savedInstanceState.getBoolean("trending", false);
        }
        if (savedSearchQuery != null) {
            searchQuery = savedSearchQuery;
        }

        if (savedCategory != null) {
            category = savedCategory;
        }
        if (savedTrending || orderByLikes) {
            BaseActivity.setContentTitle("Trending");
        }

        gridView.setAdapter(searchAdapter);

        gridView.setOnLoadmoreListener(loadMoreListener);

        SharedPreferences prefs = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
        if (!prefs.getString("pref_key_search_result_columns", "0").equals("0")) {
            gridView.setColumnCount(Integer.parseInt(prefs.getString("pref_key_search_result_columns", "0")));
        }

        // DATA IS FETCHED IN onStart

        // RIGHT MENU STUFF

        rightMenuSpiceListener = new PendingRequestListener<Category.List>() {
            @Override
            public void onRequestNotFound() {
            }

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
            category = root.getId();
            requestData(0);
        } else {
            rightMenuAdapter = new CategoryListAdapter(getActivity());
            rightMenuAdapter.addAll(categories);
        }
        BaseActivity.setRightMenuItemClickListener(categoryClickListener);
        BaseActivity.setRightMenuListAdapter(rightMenuAdapter);
    }









    // ##### LIFECYCLE ###### //


    @Override
    public void onStart() {
        super.onStart();
        getActivity().registerReceiver(totalItemsBroadcastReceiver, new IntentFilter(getActivity().getString(R.string.broadcast_intent_total_items)));
        getActivity().registerReceiver(emptyReceiver, new IntentFilter(getActivity().getString(R.string.broadcast_intent_empty_result)));
        requestData(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getActivity().unregisterReceiver(totalItemsBroadcastReceiver);
            getActivity().unregisterReceiver(emptyReceiver);
        } catch (Exception e) {
            // ignore, just not registered.
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (searchAdapter == null) return;
        if (category != null && !category.isEmpty()) {
            outState.putString("category", category);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            outState.putString("searchQuery", searchQuery);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (spiceManager == null) return;
        spiceManager.addListenerIfPending(Category.List.class, CategoryRequest.getCacheKey(), rightMenuSpiceListener);
        spiceManager.addListenerIfPending(SingleAd.List.class, listCacheKey, listRequestListener);
        for (Object favCacheKey : favAdsRequests.keySet()) {
            spiceManager.addListenerIfPending(SingleAd.class, favCacheKey, favAdsRequests.get(favCacheKey));
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (loading != null) loading.dismiss();
    }






    // ###### MODES ###### //

    public SearchResultFragment favsMode() {
        this.mode = Mode.FAVS;
        return this;
    }

    public SearchResultFragment trending() {
        this.orderByLikes = true;
        return this;
    }

    public SearchResultFragment myAdsMode() {
        this.mode = Mode.MY_ADS;
        return this;
    }

    public SearchResultFragment setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
        return this;
    }






    // ###### LISTENERS ###### //

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

    private StaggeredGridView.OnLoadmoreListener loadMoreListener = new StaggeredGridView.OnLoadmoreListener() {
        @Override
        public void onLoadmore() {
            if (totalServerItems > 0 && searchAdapter.getCount() > 0 && searchAdapter.getCount() < totalServerItems) {
                page++;
                requestData(page);
            }
        }
    };

    private BroadcastReceiver emptyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            page = 0;
            String message = intent.getStringExtra("message");
            emptyPanel.setVisibility(View.VISIBLE);
            emptyText.setText(message);
        }
    };

    private BroadcastReceiver totalItemsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            totalServerItems = intent.getIntExtra("X-Total-Items", 0);
            Log.d("TOTAL-BROADCAST", "GOT TOTAL ITEMS ON SERVER: " + totalServerItems);
            emptyPanel.setVisibility(View.GONE);
        }
    };







    // #### LOADING DATA STUFF ##### //

    public void requestData(final int page) {
        if (mode == Mode.FAVS) {
            SharedPreferences spref = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
            Set<String> favs = spref.getStringSet("favoriteAds", new HashSet<String>());
            if (favs.isEmpty()) {
                Intent intent = new Intent(getActivity().getString(R.string.broadcast_intent_empty_result));
                intent.putExtra("message", "No favorites found!");
                getActivity().sendBroadcast(intent);
            } else {
                final boolean[] firstAd = {true};
                for (String id : favs) {
                    SingleAdRequest favAdRequest = new SingleAdRequest(id);
                    Object favCacheKey = favAdRequest.getCacheKey();
                    PendingRequestListener<SingleAd> favRequestListener = new PendingRequestListener<SingleAd>() {
                        @Override
                        public void onRequestSuccess(SingleAd ad) {
                            if (firstAd[0]) {
                                searchAdapter.clear();
                                firstAd[0] = false;
                            }
                            searchAdapter.add(ad);
                            Log.d(TAG, "FAV: ADDED " + ad.getTitle());
                        }

                        @Override
                        public void onRequestNotFound() {
                        }

                        @Override
                        public void onRequestFailure(SpiceException spiceException) {
                            spiceException.printStackTrace();
                        }
                    };
                    spiceManager.execute(favAdRequest, favCacheKey, DurationInMillis.ONE_HOUR, favRequestListener);
                    favAdsRequests.put(favCacheKey, favRequestListener);
                }
            }

        } else {
            if (loading != null) loading.dismiss();
            if (page == 0) {
                loading = new ProgressDialog(getActivity());

                loading.setTitle("Loading");
                loading.setIndeterminate(true);
                loading.setCancelable(false);
                loading.setMessage("Wait while loading...");
                loading.show();
            }
            String order = null;
            if (orderByLikes) order = "likes";
            ListAdsRequest listAdsRequest = new ListAdsRequest(category, searchQuery, order, page);
            if (mode == Mode.MY_ADS) {
                searchAdapter.myAdsMode();
                listAdsRequest.setOnlyMine(true);
            }
            listCacheKey = listAdsRequest.getCacheKey();
            listRequestListener = new PendingRequestListener<SingleAd.List>() {
                @Override
                public void onRequestNotFound() {
                    try {
                        loading.dismiss();
                    } catch (Exception e) {
                        // INGORE
                    }
                }

                @Override
                public void onRequestSuccess(SingleAd.List allAds) {
                    try {
                        loading.dismiss();
                    } catch (Exception e) {
                        // INGORE
                    }
                    if (allAds == null || allAds.isEmpty()) {
                        Intent intent = new Intent(getActivity().getString(R.string.broadcast_intent_empty_result));
                        intent.putExtra("message", "No results!");
                        getActivity().sendBroadcast(intent);
                        searchAdapter.clear();
                        searchAdapter.notifyDataSetChanged();
                        return;
                    }
                    if (page == 0) searchAdapter.clear();
                    searchAdapter.addAll(allAds);
                    Log.d(TAG, "GOT " + allAds.size() + " ITEMS!");
                }

                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    try {
                        loading.dismiss();
                        Intent intent = new Intent(getActivity().getString(R.string.broadcast_intent_empty_result));
                        intent.putExtra("message", "Error: " + spiceException.getMessage());
                        getActivity().sendBroadcast(intent);
                        searchAdapter.clear();
                        searchAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        // INGORE
                    }
                    Toast.makeText(getActivity(), "ERROR: " + spiceException.getMessage(), Toast.LENGTH_SHORT).show();
                    spiceException.printStackTrace();
                }
            };
            spiceManager.execute(listAdsRequest, listAdsRequest.getCacheKey(), DurationInMillis.ONE_MINUTE, listRequestListener);
        }
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }

    public static int getTotalServerItems() {
        return totalServerItems;
    }

    private enum Mode {
        FAVS, MY_ADS, NORMAL
    }
}
