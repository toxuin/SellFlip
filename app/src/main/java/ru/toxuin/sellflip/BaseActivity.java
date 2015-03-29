package ru.toxuin.sellflip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import ru.toxuin.sellflip.entities.SideMenuItem;
import ru.toxuin.sellflip.library.LeftMenuAdapter;
import ru.toxuin.sellflip.library.OnBackPressedListener;
import ru.toxuin.sellflip.restapi.ApiConnector;
import ru.toxuin.sellflip.restapi.AuthRequestTask;
import ru.toxuin.sellflip.restapi.AuthResponseListener;

public class BaseActivity extends ActionBarActivity implements AuthResponseListener {

    public static final String TAG = "BaseActivity";

    private static final float MENU_FADE_DEGREE = 0.35f;
    private static BaseActivity self;
    private static FragmentManager fragmentManager;
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
    public static void setContent(Fragment fragment) {
        String fragName = fragment.getClass().getName();
        if (self == null) return;
        if (self.leftMenu.isMenuShowing()) self.leftMenu.toggle();
        if (self.rightMenu.isMenuShowing()) self.rightMenu.toggle();
        self.disableRightMenu();
        if (fragment instanceof SearchResultFragment) {
            self.enableRightMenu();
        }
        self.fragmentManager.beginTransaction()
                .replace(R.id.content, fragment, fragment.getClass().getName())
                .addToBackStack(fragName).commit(); // add frags with a tag will allow to pop them by tag
        self.activeFragment = fragment;
        self.backPressedListener = null;
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
        AuthDialog authDialog = new AuthDialog();
        authDialog.show(fragmentManager, "Authenticate");
    }

    @Override protected void onResume() {
        super.onResume();
        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }
        uiLifecycleHelper.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        setContent(new SearchResultFragment());

        // ADD ITEMS TO LEFT MENU
        ListView leftMenuList = (ListView) leftMenu.getMenu().findViewById(R.id.left_menu_list);

        leftMenuAdapter = new LeftMenuAdapter(this);

        leftMenuAdapter.add(new SideMenuItem("Top ads", "fa-line-chart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.setContent(new SearchResultFragment());
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("Login", "fa-sign-in", new View.OnClickListener() {
            @Override public void onClick(View v) {
                sendBroadcast(new Intent(getString(R.string.broadcast_intent_auth)));
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("Add ad", "fa-plus", new View.OnClickListener() {
            @Override public void onClick(View v) {
                BaseActivity.setContent(new CaptureVideoFragment());
            }
        }));

        leftMenuAdapter.add(new SideMenuItem("My Favourites", "fa-heart", null));
        leftMenuAdapter.add(new SideMenuItem("Settings", "fa-cogs", null));

        leftMenuList.setAdapter(leftMenuAdapter);

        // RIGHT MENU STUFF
        rightMenuList = (ListView) rightMenu.getMenu().findViewById(R.id.right_menu_list);

        facebook_container = (LinearLayout) findViewById(R.id.facebook_container);
        facebook_profile_pic = (ProfilePictureView) findViewById(R.id.facebook_profile_pic);
        facebook_username = (TextView) findViewById(R.id.facebook_username);

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
            ApiConnector.getAuthHeaders().clearToken();
        } else {
            // perform AuthRequest to the back end
            new AuthRequestTask().registerResponseListener(this).execute(session.getAccessToken());
        }
    }

    @Override public void onAuthSuccess() {
        if (Session.getActiveSession().isOpened() &&
                facebook_container.getVisibility() == View.GONE) {
            makeMeRequest(Session.getActiveSession());
        }
    }

    @Override public void onAuthFailure() {
        // authentication with back end failed
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                leftMenu.toggle();
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
}
