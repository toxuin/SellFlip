package ru.toxuin.sellflip.library;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

    private int layoutResource;

    public CategoryListAdapter(Activity context) {
        super();
        content = new LinkedList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.setLayoutResource(R.layout.category_list_element);
    }
    
    public void addAll(Collection<Category> that) {
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
        final ImageView expand = (ImageView) view.findViewById(R.id.cat_list_expand);

        if (root!= null && root.equals(cat)) {
            view.setBackgroundColor(Color.GREEN);
        }

        if (cat.hasSubcategories()) {
            expand.setVisibility(View.VISIBLE);
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
}
