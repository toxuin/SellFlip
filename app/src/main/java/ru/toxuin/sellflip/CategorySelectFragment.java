package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.OnBackPressedListener;
import ru.toxuin.sellflip.library.SpiceFragment;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;
import ru.toxuin.sellflip.restapi.spicerequests.CategoryRequest;

import java.util.List;

public class CategorySelectFragment extends SpiceFragment implements OnBackPressedListener {
    private static final String TAG = "CATEGORY_UI";
    private View rootView;

    String category;
    Bundle args;
    CategoryListAdapter adapter;

    ListView list;
    List<Category> categories;
    private boolean hintShown = false;
    protected SpiceManager spiceManager = new SpiceManager(SellFlipSpiceService.class);

    public CategorySelectFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_categoryselect, container, false);
        list = (ListView) rootView.findViewById(R.id.category_list);
        args = getArguments();

        BaseActivity.setContentTitle("Select category");
        BaseActivity.registerBackPressedListener(this);

        spiceManager.execute(new CategoryRequest(), CategoryRequest.getCacheKey(), DurationInMillis.ONE_WEEK, new RequestListener<Category.List>() {
            @Override
            public void onRequestSuccess(Category.List cats) {
                categories = cats;
                drawList(null);
            }
            @Override
            public void onRequestFailure(SpiceException spiceException) {
                spiceException.printStackTrace();
            }
        });

        return rootView;
    }

    private void drawList(Category root) {
        adapter = new CategoryListAdapter(getActivity());
        if (root != null) adapter.setRoot(root);
        else adapter.addAll(categories);
        list.setOnItemClickListener(selectCatClickListener);
        list.setAdapter(adapter);

        if (!hintShown && root != null) {
            SuperToast superToast = new SuperToast(rootView.getContext().getApplicationContext(), Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP));
            superToast.setDuration(SuperToast.Duration.SHORT);
            superToast.setText(getString(R.string.use_back_btn_to_nav));
            superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
            superToast.show();
            hintShown = true;
        }
    }

    private AdapterView.OnItemClickListener selectCatClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            Category cat = adapter.getItem(position);

            if (cat.hasSubcategories() && !cat.equals(adapter.getRoot())) {
                drawList(cat);
            } else {
                category = cat.getId();

                CreateAdFragment content = new CreateAdFragment();
                args.putString("category", category);
                content.setArguments(args);
                BaseActivity.setContent(content);
            }
        }
    };

    @Override
    public boolean onBackPressed() {
        Category root = adapter.getRoot();
        if (root == null) return false;
        Category parent = adapter.findParent(categories, root);
        drawList(parent);
        return true;
    }

    @Override
    public SpiceManager getSpiceManager() {
        return spiceManager;
    }
}
