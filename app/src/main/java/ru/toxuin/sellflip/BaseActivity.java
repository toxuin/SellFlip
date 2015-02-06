package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import ru.toxuin.sellflip.entities.SideMenuItem;
import ru.toxuin.sellflip.library.LeftMenuAdapter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseActivity extends ActionBarActivity {

    private static final float MENU_FADE_DEGREE = 0.35f;
    private static BaseActivity self;
    private SlidingMenu leftMenu;
    private SlidingMenu rightMenu;
    private FragmentManager fragmentManager;
    private Fragment activeFragment;

    /**
     * Call this to set contents.
     * @param fragment Fragment to set as content.
     */
    public static void setContent(Fragment fragment) {
        if (self == null) return;
        if (self.leftMenu.isMenuShowing()) self.leftMenu.toggle();
        if (self.rightMenu.isMenuShowing()) self.rightMenu.toggle();
        if (getActiveFragment().getClass().equals(fragment.getClass())) return; // QUESTIONABLE
        self.fragmentManager.beginTransaction().replace(R.id.content, fragment)
                .addToBackStack(null)
                .commit();
        self.activeFragment = fragment;
    }

    /**
     * Exposes the currently active fragment.
     * @return Fragment that is currently occupying content view
     */
    public static Fragment getActiveFragment() {
        return self.activeFragment;
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

        // MAKE HOME BUTTON IN ACTIONBAR SEXY
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);

        // CONTENT MANAGEMENT STUFF
        fragmentManager = getSupportFragmentManager();
        activeFragment = new SearchResultFragment();
        fragmentManager.beginTransaction().replace(R.id.content, activeFragment).commit();

        // ADD ITEMS TO LEFT MENU
        ListView leftMenuList= (ListView) leftMenu.getMenu().findViewById(R.id.left_menu_list);

        LeftMenuAdapter leftMenuAdapter = new LeftMenuAdapter(this);
        leftMenuAdapter.add(new SideMenuItem("Top ads", "fa-line-chart", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.setContent(new SearchResultFragment());
            }
        }));
        leftMenuAdapter.add(new SideMenuItem("Post", "fa-camera-retro", null));
        leftMenuAdapter.add(new SideMenuItem("My Favourites", "fa-heart", null));
        leftMenuAdapter.add(new SideMenuItem("Settings", "fa-cogs", null));

        leftMenuList.setAdapter(leftMenuAdapter);
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
     * @param keyCode what's pressed
     * @param event the event object
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
}
