package ru.toxuin.sellflip;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;
import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.Tracking;
import net.hockeyapp.android.UpdateManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.toxuin.sellflip.entities.SideMenuItem;
import ru.toxuin.sellflip.entities.User;
import ru.toxuin.sellflip.fragments.PrefsFragment;
import ru.toxuin.sellflip.library.LeftMenuAdapter;
import ru.toxuin.sellflip.library.OnBackPressedListener;
import ru.toxuin.sellflip.library.SuggestionAdapter;
import ru.toxuin.sellflip.library.Utils;
import ru.toxuin.sellflip.restapi.ApiHeaders;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.AuthRequest;
import ru.toxuin.sellflip.restapi.spicerequests.CurrentUserRequest;


public class BaseActivity extends ActionBarActivity {
    public static final String TAG = "BaseActivity";

    private static final float MENU_FADE_DEGREE = 0.35f;
    private static final String ACTIVE_FRAGMENT_TAG = "activeFragmentTag";
    private static BaseActivity self;
    private static FragmentManager fragmentManager;
    private static AuthDialog authDialog;
    public static User currentUser;
    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);
    private SlidingMenu leftMenu;
    private SlidingMenu rightMenu;
    private ListView rightMenuList;
    private Fragment activeFragment;
    private UiLifecycleHelper uiLifecycleHelper;
    private OnBackPressedListener backPressedListener;
    private ProfilePictureView facebook_profile_pic;
    private TextView facebook_username;
    private LinearLayout facebook_container;
    private LeftMenuAdapter leftMenuAdapter;
    private Menu menu;
    private SearchView searchView;

    private SideMenuItem loginMenuItem;
    private SideMenuItem myAdsMenuItem;

    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override public void call(Session session, SessionState sessionState, Exception e) {
            onSessionStateChange(session, sessionState, e);
        }
    };

    /**
     * Call this to set contents.
     *
     * @param fragment Fragment to set as content.
     */
    public static void setContent(Fragment fragment, boolean addToBackStack) {
        String fragName = fragment.getClass().getName();
        if (self == null) return;
        if (self.leftMenu.isMenuShowing()) self.leftMenu.toggle();
        if (self.rightMenu.isMenuShowing()) self.rightMenu.toggle();
        if (self.menu != null) {
            MenuItem searchItem = self.menu.findItem(R.id.action_search);
            if (searchItem != null) {
                searchItem.getActionView().clearFocus();
                searchItem.collapseActionView();
            }
        }
        self.disableRightMenu();
        if (fragment instanceof SearchResultFragment) {
            self.enableRightMenu();
        } else if (fragment instanceof CaptureVideoFragment) {
            if (!Utils.checkCameraHardware(self)) {
                Toast.makeText(self, "Your device does not have a camera!", Toast.LENGTH_SHORT).show();
                return;
            }
            self.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (!(fragment instanceof CaptureVideoFragment) && !(fragment instanceof CategorySelectFragment) && !(fragment instanceof CreateAdFragment)) {
            Utils.removeTempFiles();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, fragment, ACTIVE_FRAGMENT_TAG);
        if (addToBackStack) {
            if (fragment instanceof CaptureVideoFragment || fragment instanceof CategorySelectFragment || fragment instanceof CreateAdFragment) {
                fragName = "CREATION";
            }
            transaction.addToBackStack(fragName);
        }
        transaction.commit();
        if (fragment instanceof SearchResultFragment && !addToBackStack) {
            fragmentManager.popBackStack("CREATION", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        self.activeFragment = fragment;
        self.backPressedListener = null;
    }

    public static void setContent(Fragment fragment) {
        setContent(fragment, true);
    }

    /**
     * Exposes the currently active fragment.
     *
     * @return Fragment that is currently occupying content view
     */
    public static Fragment getActiveFragment() {
        return self.activeFragment;
    }

    public static void setContentTitle(String title) {
        self.setTitle(title);
    }

    public static void registerBackPressedListener(OnBackPressedListener listener) {
        self.backPressedListener = listener;
    }

    public static void setRightMenuItemClickListener(AdapterView.OnItemClickListener listener) {
        self.rightMenuList.setOnItemClickListener(listener);
    }

    public static void setRightMenuListAdapter(ListAdapter adapter) {
        self.rightMenuList.setAdapter(adapter);
    }

    public static void showLogInDialog() {
        if (authDialog != null) return;
        authDialog = new AuthDialog();
        authDialog.show(fragmentManager, "Authenticate");
    }

    public static void hideLogInDialog() {
        if (authDialog == null) return;
        try {
            authDialog.dismiss();
        } catch (Exception ignored) {
        }
        authDialog = null;
    }

    public static void hideActionBar() {
        self.getSupportActionBar().hide();
    }

    public static void showActionBar() {
        self.getSupportActionBar().show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CrashManager.register(this, "775e53fa038441cc6ee48a8c1091c69d");
        // TODO: REMOVE THIS BEFORE PUBLISHING TO STORE!!!
        UpdateManager.register(this, "775e53fa038441cc6ee48a8c1091c69d");
        AppEventsLogger.activateApp(this);
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiLifecycleHelper.onResume();
        spiceManager.start(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppEventsLogger.deactivateApp(this);
        spiceManager.shouldStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "GOT SEARCH QUERY : " + query);
            searchView.setQuery(query, false);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tracking.startUsage(this);
        self = this;
        setContentView(R.layout.activity_base);

        leftMenu = new SlidingMenu(this);
        leftMenu.setMode(SlidingMenu.LEFT);
        leftMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        leftMenu.setShadowDrawable(R.drawable.shadow);
        leftMenu.setShadowWidthRes(R.dimen.menu_shadow_offset);
        leftMenu.setFadeDegree(MENU_FADE_DEGREE);
        leftMenu.setBehindWidthRes(R.dimen.menu_behind_offset);
        leftMenu.setTouchmodeMarginThreshold((int) getResources().getDimension(R.dimen.menu_swipe_threshold));
        leftMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        leftMenu.setMenu(R.layout.left_main_menu);

        rightMenu = new SlidingMenu(this);
        rightMenu.setMode(SlidingMenu.RIGHT);
        rightMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
        rightMenu.setShadowDrawable(R.drawable.shadow_rtl);
        rightMenu.setShadowWidthRes(R.dimen.menu_shadow_offset);
        rightMenu.setFadeDegree(MENU_FADE_DEGREE);
        rightMenu.setBehindWidthRes(R.dimen.menu_behind_offset);
        rightMenu.setTouchmodeMarginThreshold((int) getResources().getDimension(R.dimen.menu_swipe_threshold));
        rightMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        rightMenu.setMenu(R.layout.right_main_menu);
        rightMenu.setSlidingEnabled(false);
        // MAKE HOME BUTTON IN ACTIONBAR SEXY
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        // CONTENT MANAGEMENT STUFF
        fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                for (Fragment frag : fragmentManager.getFragments()) {
                    if (frag != null && frag.isVisible()) {
                        activeFragment = frag;
                    }
                }
                if (activeFragment instanceof SearchResultFragment) enableRightMenu();
                else disableRightMenu();
            }
        });

        if (savedInstanceState == null) {
            setContent(new SearchResultFragment());
        } else {
            activeFragment = fragmentManager.findFragmentByTag(ACTIVE_FRAGMENT_TAG);
            if (activeFragment instanceof SearchResultFragment) {
                enableRightMenu(); // TODO: SAME IS DONE IN setContent()
            }
        }

        loginMenuItem = new SideMenuItem("Login", "fa-sign-in", new View.OnClickListener() {
            @Override public void onClick(View v) {
                sendBroadcast(new Intent(getString(R.string.broadcast_intent_auth)));
            }
        });

        myAdsMenuItem = new SideMenuItem("My Ads", "fa-adn", new View.OnClickListener() {
            @Override public void onClick(View v) {
                BaseActivity.setContent(new SearchResultFragment().myAdsMode());
            }
        });

        // ADD ITEMS TO LEFT MENU
        ListView leftMenuList = (ListView) leftMenu.getMenu().findViewById(R.id.left_menu_list);

        leftMenuAdapter = new LeftMenuAdapter(this);

        leftMenuAdapter.add(new SideMenuItem("Recent ads", "fa-random", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.setContent(new SearchResultFragment());
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("Trending", "fa-line-chart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.setContent(new SearchResultFragment().trending());
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("Create ad", "fa-plus", new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (ApiHeaders.isTokenEmpty()) {
                    showLogInDialog();
                    return;
                }
                BaseActivity.setContent(new CaptureVideoFragment());
            }
        }));
        leftMenuAdapter.add(new SideMenuItem("My Favorites", "fa-star", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseActivity.setContent(new SearchResultFragment().favsMode());
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("Settings", "fa-cogs", new View.OnClickListener() {
            @Override public void onClick(View v) {
                BaseActivity.setContent(new PrefsFragment());
            }
        }));

        if(!leftMenuAdapter.contains(loginMenuItem)){
            leftMenuAdapter.insert(loginMenuItem, 3);
        }

        leftMenuList.setAdapter(leftMenuAdapter);

        // RIGHT MENU STUFF
        rightMenuList = (ListView) rightMenu.getMenu().findViewById(R.id.right_menu_list);

        facebook_container = (LinearLayout) findViewById(R.id.facebook_container);
        facebook_profile_pic = (ProfilePictureView) findViewById(R.id.facebook_profile_pic);
        facebook_username = (TextView) findViewById(R.id.facebook_username);

        // SET SAVE SEARCH BY DEFAULT TO TRUE
        SharedPreferences pref = getSharedPreferences(getString(R.string.app_preference_key), Context.MODE_PRIVATE);
        if (!pref.contains("pref_key_save_search")) {
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("pref_key_save_search", true);
            editor.apply();
        }

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        Log.d(TAG, "!!!!! SCREEN WIDTH: " + dpWidth);

        uiLifecycleHelper = new UiLifecycleHelper(this, statusCallback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    public void disableRightMenu() {
        if (rightMenu.isMenuShowing()) rightMenu.toggle(false);
        rightMenu.setSlidingEnabled(false);
    }

    public void enableRightMenu() {
        if (rightMenu.isMenuShowing()) rightMenu.toggle(false);
        rightMenu.setSlidingEnabled(true);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session.isClosed()) {
            facebook_container.setVisibility(View.GONE);
            facebook_profile_pic.setProfileId(null);
            facebook_username.setText("");
            ApiHeaders.clearToken();

            /* Hide my Ads, show login */
                leftMenuAdapter.remove(myAdsMenuItem);
            if(!leftMenuAdapter.contains(loginMenuItem)){
                leftMenuAdapter.insert(loginMenuItem, 3);
            }

        } else {
            // perform AuthRequest to the back end

            AuthRequest request = new AuthRequest(session.getAccessToken());
            spiceManager.execute(request, new RequestListener<AuthRequest.AccessToken>() {

                @Override
                public void onRequestSuccess(AuthRequest.AccessToken accessToken) {
                    ApiHeaders.setAccessToken(accessToken.token);
                    /* Execute get current user request */
                    spiceManager.execute(new CurrentUserRequest(), new RequestListener<User>() {
                        @Override public void onRequestFailure(SpiceException spiceException) {

                        }

                        @Override public void onRequestSuccess(User user) {
                            currentUser = user;
                        }
                    });

                    if (!accessToken.token.isEmpty() && Session.getActiveSession().isOpened() && facebook_container.getVisibility() == View.GONE) {
                        makeMeRequest(Session.getActiveSession());
                    }
                    /* Show my Ads, hide login */
                    if(!leftMenuAdapter.contains(myAdsMenuItem)){
                        leftMenuAdapter.insert(myAdsMenuItem, 4);
                    }
                        leftMenuAdapter.remove(loginMenuItem);
                    hideLogInDialog();
                }

                @Override
                public void onRequestFailure(SpiceException spiceException) {
                    spiceException.printStackTrace();
                }

            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                leftMenu.toggle();
                return true;
            case R.id.action_search:
                searchView.setIconified(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This should fix stupid behaviour of back button & menus.
     * Once you press Back with leftMenu open – app will quit.
     * With this – leftMenu will close.
     *
     * @param keyCode what's pressed
     * @param event   the event object
     * @return whatever.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (leftMenu.isMenuShowing()) {
                leftMenu.toggle();
                return false;
            } else if (rightMenu.isMenuShowing()) {
                rightMenu.toggle();
                return false;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    private void makeMeRequest(final Session session) {
        // Make an API call to get user data and define a
        // new callback to handle the response.
        Request request = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        // If the response is successful
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                // Set the id for the ProfilePictureView
                                facebook_container.setVisibility(View.VISIBLE);
                                facebook_profile_pic.setProfileId(user.getId());
                                facebook_username.setText(user.getName());
                            }
                        }
                        if (response.getError() != null) {
                            // Handle errors, will do so later.
                        }
                    }
                });
        request.executeAsync();
    }

    @Override
    public void onBackPressed() {
        if (backPressedListener == null || !backPressedListener.onBackPressed())
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Tracking.stopUsage(this);
        super.onDestroy();
    }

    // SEARCH STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_activity_actions, menu);
        this.menu = menu;
        MenuItem searchItem = menu.findItem(R.id.action_search);
        if (searchView == null) searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                SuggestionAdapter adapter = (SuggestionAdapter) searchView.getSuggestionsAdapter();
                BaseActivity.setContent(new SearchResultFragment().setSearchQuery(adapter.getItemAt(i)));
                return true;
            }
        });

        final SharedPreferences pref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
        Set<String> suggestionItemSet = pref.getStringSet("pref_key_saved_searches", new HashSet<String>());
        final List<String> suggestionItems = new ArrayList<>(suggestionItemSet.size());
        suggestionItems.addAll(suggestionItemSet);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String result) {
                if (pref.getBoolean("pref_key_save_search", false)) {
                    Set<String> savedSearches = pref.getStringSet("pref_key_saved_searches", new HashSet<String>());
                    if (!savedSearches.contains(result)) {
                        savedSearches.add(result);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putStringSet("pref_key_saved_searches", savedSearches);
                        editor.apply();
                        Log.d(TAG, "SAVED " + result + " AS SEARCH SUGGESTION");
                    }
                }

                BaseActivity.setContent(new SearchResultFragment().setSearchQuery(result));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                SharedPreferences pref = getApplicationContext().getSharedPreferences(getApplicationContext().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
                if (!pref.getBoolean("pref_key_save_search", false)) return false;

                String[] columns = new String[]{"_id", "text"};
                Object[] temp = new Object[]{0, "default"};

                List<String> strings = new ArrayList<>();
                final MatrixCursor cursor = new MatrixCursor(columns);

                for (int i = 0; i < suggestionItems.size(); i++) {
                    if (suggestionItems.get(i).toLowerCase().startsWith(s.trim().toLowerCase()) && s.trim().length() > 0) {
                        strings.add(suggestionItems.get(i));
                        temp[0] = i;
                        temp[1] = suggestionItems.get(i);
                        cursor.addRow(temp);
                    }
                }

                searchView.setSuggestionsAdapter(new SuggestionAdapter(self, cursor, strings));

                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}
