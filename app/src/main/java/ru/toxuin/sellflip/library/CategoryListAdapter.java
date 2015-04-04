package ru.toxuin.sellflip.library;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.entities.Category;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CategoryListAdapter extends BaseAdapter {
    private static final String TAG = "CATEGORY_LIST_ADAPTER";
    protected final List<Category> content;
    private Category root;

    private LayoutInflater inflater;

    private VisibilityMode arrowVisibilityMode = VisibilityMode.SELECTION;

    private final int ROOT_COLOR;
    private int layoutResource;

    public CategoryListAdapter(Activity context) {
        super();
        content = new LinkedList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ROOT_COLOR = context.getResources().getColor(R.color.single_ad_secondary);
        this.setLayoutResource(R.layout.category_list_element);
    }
    
    public void addAll(Collection<Category> that) {
        if (that == null) return;
        content.addAll(that);
    }

    @Override
    public int getCount() {
        return content.size();
    }

    @Override
    public Category getItem(int position) {
        return content.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = populateView(parent, position);
        } else view = convertView;
        return view;
    }

    private View populateView(ViewGroup parent, int position) {
        Category cat = content.get(position);

        View view = inflater.inflate(layoutResource, parent, false);
        TextView caption = (TextView) view.findViewById(R.id.cat_list_caption);
        final ImageView forward = (ImageView) view.findViewById(R.id.cat_list_forwards);
        final ImageView backward = (ImageView) view.findViewById(R.id.cat_list_backwards);

        if (root != null && root.equals(cat)) {
            view.setBackgroundColor(ROOT_COLOR);
        }

        if (root != null && cat.hasSubcategories()) {
            if (arrowVisibilityMode == VisibilityMode.NAVIGATION) {
                if (getRoot().equals(cat)) backward.setVisibility(View.VISIBLE);
                else {
                    forward.setImageResource(R.drawable.ic_action_next_item);
                    forward.setVisibility(View.VISIBLE);
                }
            } else {
                forward.setVisibility(View.VISIBLE);
            }
        }

        caption.setText(cat.getName());
        view.setTag(cat);

        if (!(parent instanceof AdapterView)) {
            parent.addView(view);
            parent.invalidate();
        }
        return view;
    }

    public void setLayoutResource(int layoutResource) {
        this.layoutResource = layoutResource;
    }

    public void setRoot(Category root) {
        this.root = root;
        content.add(0, root);
        content.addAll(root.getSubcategories());
    }

    public Category getRoot() {
        return root;
    }

    public Category findParent(Collection<Category> haystack, Category needle) {
        for (Category cat : haystack) {
            if (!cat.hasSubcategories()) continue;
            if (cat.contains(needle)) {
                return cat;
            }
            return findParent(cat.getSubcategories(), needle);
        }
        return null;
    }

    public void setArrowButtonVisibitityMode(VisibilityMode mode) {
        arrowVisibilityMode = mode;
    }

    public enum VisibilityMode {
        NAVIGATION, SELECTION
    };

}
