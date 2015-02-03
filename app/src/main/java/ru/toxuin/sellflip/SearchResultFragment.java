package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class SearchResultFragment extends Fragment {
    private View rootView;
    public SearchResultFragment() {} // SUBCLASSES OF FRAGMENT NEED EMPTY CONSTRUCTOR

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_results, container, false);
        String title = getString(R.string.search_results);
        getActivity().setTitle(title);

        // DO STUFF
        Button testButton = (Button) rootView.findViewById(R.id.testButton);
        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                BaseActivity.setContent(new SingleAdFragment());
            }
        });

        return rootView;
    }
}
