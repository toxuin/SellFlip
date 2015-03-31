package ru.toxuin.sellflip.library;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.octo.android.robospice.SpiceManager;

public abstract class SpiceFragment extends Fragment {

    public abstract SpiceManager getSpiceManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        getSpiceManager().start(activity);
    }

    @Override
    public void onDetach() {
        getSpiceManager().shouldStop();
        super.onDetach();
    }
}
