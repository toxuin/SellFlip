package ru.toxuin.sellflip.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.SingleAdFragment;
import ru.toxuin.sellflip.entities.SingleAd;

import java.text.NumberFormat;
import java.util.List;

public class SearchResultAdapter extends ArrayAdapter<SingleAd> {
    private final Context context;
    private final List<SingleAd> itemsList;

    public SearchResultAdapter(Context context, List<SingleAd> objects) {
        super(context, R.layout.search_result_item, objects);
        this.context = context;
        this.itemsList = objects;
        // TODO: ENDLESS SCROLLING
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SearchResultViewHolder viewHolder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.search_result_item, parent, false);
            viewHolder = new SearchResultViewHolder();

            viewHolder.title = (TextView) rowView.findViewById(R.id.item_title);
            viewHolder.description = (TextView) rowView.findViewById(R.id.item_description);
            viewHolder.price = (TextView) rowView.findViewById(R.id.item_price);
            viewHolder.thumbnail = (ImageView) rowView.findViewById(R.id.item_thumbnail);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (SearchResultViewHolder) rowView.getTag();
        }

        viewHolder.title.setText(itemsList.get(position).getTitle());
        viewHolder.description.setText(itemsList.get(position).getDescription());
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        viewHolder.price.setText(formatter.format(itemsList.get(position).getPrice()));

        // THIS DOES NOT DOWNLOAD, JUST DRAWS
        new AsyncTask<SearchResultViewHolder, Void, Bitmap>() {
            private SearchResultViewHolder viewHolder;

            @Override
            protected void onPreExecute() {
                viewHolder.thumbnail.setImageResource(R.drawable.loading);
                LayerDrawable progressAnimation = (LayerDrawable) viewHolder.thumbnail.getDrawable();
                ((Animatable) progressAnimation.getDrawable(0)).start();
                ((Animatable) progressAnimation.getDrawable(1)).start();
            }

            @Override
            protected Bitmap doInBackground(SearchResultViewHolder... params) {
                viewHolder = params[0];
                //TODO:
                //return BitmapDownloader.getThumbnailForAd(_____id_____);
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                if (viewHolder.position == position) {
                    if (result == null) {
                        viewHolder.thumbnail.setImageDrawable(viewHolder.thumbnail.getContext().getResources()
                                .getDrawable(R.drawable.no_image));
                    } else {
                        viewHolder.thumbnail.setImageBitmap(result);
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, viewHolder);  // ANDROID 3.0+ ONLY


        return rowView;
    }

    public OnItemClickListener searchReslutsItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SingleAdFragment adFragment = new SingleAdFragment();
            adFragment.setId(itemsList.get(position).getId());
            BaseActivity.setContent(adFragment);
        }
    };

    /*public OnScrollListener searchResultsEndlessScrollListener = new OnScrollListener() {
        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                new DataDownloader().execute(currentPage + 1);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public void setThreshold(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }
    };       */

    static class SearchResultViewHolder {
        int position;
        TextView title;
        TextView price;
        TextView description;
        ImageView thumbnail;
    }
}
