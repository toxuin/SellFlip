package ru.toxuin.sellflip;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

/**
 * Created by max on 2015-03-18.
 */
public class LogInFragment extends Fragment {
    private static final String TAG = "LOGIN_FRAG";
    private View rootView;
    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback statusCallback = new Session.StatusCallback() {
        @Override public void call(Session session, SessionState sessionState, Exception e) {

        }
    };

    public LogInFragment() {
    }

    @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_login, container, false);
        String title = getString(R.string.login);
        getActivity().setTitle(title);

        LoginButton authButton = (LoginButton) rootView.findViewById(R.id.authButton);

        authButton.setFragment(this);
        authButton.setReadPermissions(Arrays.asList("user_status"));

        return rootView;
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLifecycleHelper = new UiLifecycleHelper(getActivity(), statusCallback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    private void onClickLogin() {
        Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this)
                    .setPermissions(Arrays.asList("public_profile"))
                    .setCallback(statusCallback));
        } else {
            Session.openActiveSession(getActivity(), this, true, statusCallback);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiLifecycleHelper.onResume();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
    }

}
