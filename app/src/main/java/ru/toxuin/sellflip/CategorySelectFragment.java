package ru.toxuin.sellflip;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import retrofit.RetrofitError;
import retrofit.client.Response;
import ru.toxuin.sellflip.entities.Category;
import ru.toxuin.sellflip.library.CategoryListAdapter;
import ru.toxuin.sellflip.library.LoadingCallback;
import ru.toxuin.sellflip.library.OnBackPressedListener;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CategorySelectFragment extends Fragment implements OnBackPressedListener {
    private static final String TAG = "CATEGORY_UI";
    private View rootView;

    String category;
    Bundle args;
    CategoryListAdapter adapter;

    ListView list;
    List<Category> categories;

    public CategorySelectFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_categoryselect, container, false);
        list = (ListView) rootView.findViewById(R.id.category_list);
        args = getArguments();

        BaseActivity.setContentTitle("Select category");
        BaseActivity.registerBackPressedListener(this);

        ApiConnector.getInstance(getActivity()).requestCategories(new LoadingCallback<List<Category>>(getActivity()) {
            @Override
            public void onSuccess(List<Category> cats, Response response) {
                Log.d(TAG, "SUCCESS! GOT CATEGORIES: " + cats.size());
                categories = cats;
                drawList(null);
            }


            @Override
            public void onFailure(RetrofitError error) {
                Log.e(TAG, "FAILURE! ERROR: " + error.getMessage());
                error.printStackTrace();
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

    private Category getParent(Category kid) {
        return lookup(categories, kid);
    }

    private Category lookup(Collection<Category> haystack, Category needle) {
        for (Category cat : haystack) {
            if (!cat.hasSubcategories()) continue;
            if (cat.contains(needle)) {
                return cat;
            }
            return lookup(cat.getSubcategories(), needle);
        }
        Log.d(TAG, "NOT FOUND PARENT FOR " + needle.getName());
        return null;
    }


    @Override
    public boolean onBackPressed() {
        Category root = adapter.getRoot();
        if (root == null) return false;
        Category parent = getParent(root);
        drawList(parent);
        return true;
    }
}
