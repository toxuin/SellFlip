package ru.toxuin.sellflip;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.jeremyfeinstein.slidingmenu.lib.*;

public class BaseActivty extends ActionBarActivity {

    private SlidingMenu leftMenu;
    private SlidingMenu rightMenu;
    private static final float MENU_FADE_DEGREE = 0.35f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        rightMenu.setMenu(R.layout.left_main_menu);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_base_activty, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            if (leftMenu.isMenuShowing()){
                leftMenu.toggle(true);
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
