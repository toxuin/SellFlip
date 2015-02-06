package ru.toxuin.sellflip.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.LayerDrawable;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;
import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.SingleAdFragment;
import ru.toxuin.sellflip.entities.SingleAd;
import ru.toxuin.sellflip.restapi.ApiConnector;

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SearchResultAdapter extends ArrayAdapter<SingleAd> {
    private final Context context;
    private final List<SingleAd> itemsList;
    private int totalServerItems = 0;

    public SearchResultAdapter(Context context, List<SingleAd> listReference) {
        super(context, R.layout.search_result_item, listReference);
        this.context = context;
        this.itemsList = listReference;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        SearchResultViewHolder viewHolder;
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.search_result_item, parent, false);
            viewHolder = new SearchResultViewHolder();
            viewHolder.position = position;
            viewHolder.title = (TextView) rowView.findViewById(R.id.item_title);
            viewHolder.date = (TextView) rowView.findViewById(R.id.item_date);
            viewHolder.price = (TextView) rowView.findViewById(R.id.item_price);
            viewHolder.thumbnail = (ImageView) rowView.findViewById(R.id.item_thumbnail);
            viewHolder.cardContainer = (LinearLayout) rowView.findViewById(R.id.item_card);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (SearchResultViewHolder) rowView.getTag();
        }
        if (position % 2 == 0) viewHolder.cardContainer.setBackgroundColor(context.getResources().getColor(R.color.even_card));

        viewHolder.title.setText(itemsList.get(position).getTitle());
        // DETERMINE THE DATE
        Date past = itemsList.get(position).getDate();
        Date now = new Date();
        long secondsAgo = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
        long minutesAgo = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - past.getTime());
        long hoursAgo = TimeUnit.MILLISECONDS.toHours(now.getTime() - past.getTime());

        String dateAgo;
        if (secondsAgo < 60) dateAgo = secondsAgo + " seconds ago";
        else if (minutesAgo < 60) dateAgo = minutesAgo + " minutes ago";
        else if (hoursAgo < 24) dateAgo = hoursAgo + " hours ago";
        else {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            dateAgo = format.format(past);
        }
        viewHolder.date.setText(dateAgo);
        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        viewHolder.price.setText(formatter.format(itemsList.get(position).getPrice()));

        viewHolder.thumbnail.setImageResource(R.drawable.loading);
        LayerDrawable progressAnimation = (LayerDrawable) viewHolder.thumbnail.getDrawable();
        ((Animatable) progressAnimation.getDrawable(0)).start();
        ((Animatable) progressAnimation.getDrawable(1)).start();

        new AsyncTask<SearchResultViewHolder, Void, Bitmap>() {
            private SearchResultViewHolder viewHolder;

            @Override
            protected Bitmap doInBackground(SearchResultViewHolder... params) {
                viewHolder = params[0];
                BitmapCache cache = BitmapCache.getInstance();
                String url = "http://lorempixel.com/600/200/technics/" + itemsList.get(position).getId() + "/";
                if (cache.getBitmapFromMemCache(url) == null) {
                    final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
                    final HttpGet getRequest = new HttpGet(url);
                    try {
                        HttpResponse response = client.execute(getRequest);
                        final int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode != HttpStatus.SC_OK) {
                            Log.e("BITMAP_LOADER", "Error " + statusCode
                                    + " while retrieving bitmap from " + url);
                            return null;
                        }
                        final HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            InputStream inputStream = null;
                            try {
                                inputStream = entity.getContent();
                                Bitmap bmp = BitmapFactory.decodeStream(inputStream);
                                cache.addBitmapToMemoryCache(url, bmp);
                                return bmp;
                            } finally {
                                if (inputStream != null) {
                                    inputStream.close();
                                }
                                entity.consumeContent();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        client.close();
                    }
                } else {
                    return cache.getBitmapFromMemCache(url);
                }

                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                //if (viewHolder.position == position) {
                    if (result == null) {
                        viewHolder.thumbnail.setImageDrawable(viewHolder.thumbnail.getContext().getResources()
                                .getDrawable(R.drawable.no_image));
                    } else {
                        viewHolder.thumbnail.setImageBitmap(result);
                    }
                //} else Log.e("IMAGE_DRAW", "WRONG POSITION: " + viewHolder.position + " :: " + position);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, viewHolder);  // ANDROID 3.0+ ONLY


        return rowView;
    }

    public OnItemClickListener searchResultsItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SingleAdFragment adFragment = new SingleAdFragment();
            adFragment.setId(itemsList.get(position).getId());
            BaseActivity.setContent(adFragment);
        }
    };

    public OnScrollListener searchResultsEndlessScrollListener = new OnScrollListener() {
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
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + ApiConnector.getItemsOnPage())) {
                if (currentPage*ApiConnector.getItemsOnPage() < totalServerItems) {
                    requestData(currentPage + 1);
                    loading = true;
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    };

    public void requestData(int page) {
        ApiConnector api = ApiConnector.getInstance();
        api.requestTopAdsPaged(page, new Callback<List<SingleAd>>() {
            @Override
            public void success(List<SingleAd> allAds, Response response) {
                for (Header header : response.getHeaders()) {
                    try {
                        if (header.getName().equals("X-Total-Items")) {
                            totalServerItems = Integer.parseInt(header.getValue());
                        }
                    } catch (NullPointerException e) {
                        // NO ACTION
                    }
                }

                /*//TODO: THIS SHOULD NOT BE LIKE THIS
                for (SingleAd ad : allAds) {
                    if (!itemsList.contains(ad)) itemsList.add(ad);
                } */
                itemsList.addAll(allAds);
                notifyDataSetChanged();
                Log.d("LIST_ADAPTER", "GOT " + allAds.size() + " ITEMS!");
            }

            @Override
            public void failure(RetrofitError error) {
                Toast.makeText(context, "ERROR: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    static class SearchResultViewHolder {
        int position;
        LinearLayout cardContainer;
        TextView title;
        TextView price;
        TextView date;
        ImageView thumbnail;
    }
}
