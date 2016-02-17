package com.refreshableendlesslist.example;

import java.util.List;
import java.util.Observable;

import com.refreshableendlesslist.CompleteListenerTask;
import com.refreshableendlesslist.CompleteListenerTask.CompleteListener;
import com.refreshableendlesslist.RefreshableEndlessListFragment;
import com.refreshableendlesslist.example.GetTweetsTask.TweetsPagingFrame;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TweetsFragment extends
        RefreshableEndlessListFragment<Tweet, TweetsFragment.TweetsPagingAdapter, TweetsPagingFrame>
{
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
    {
        // Properties need to be set before calling base class onCreateView.
        dataSize = 50;
        listViewDivider = getResources().getDrawable(R.color.blue);
        listViewDividerHeight = 2;
        model = ExampleModel.getInstance();
        propertyNameToObserve = ExampleModel.TWEETS_PROPERTY_NAME;
        View postsView = super.onCreateView(inflater, container, savedInstance);
        return postsView;
    }

    @Override
    protected TweetsPagingFrame createPagingFrame(int dataSize, List<Tweet> loadedData, LoadType loadType)
    {
        Tweet firstTweet = (loadedData != null && loadedData.size() > 0) ? loadedData.get(0) : null;
        Tweet lastTweet = (loadedData != null && loadedData.size() > 0) ? loadedData.get(loadedData.size() - 1) : null;
        return new TweetsPagingFrame(dataSize, loadType, firstTweet, lastTweet);
    }

    @Override
    protected TweetsPagingAdapter createPagingAdapter(Context context, List<Tweet> data)
    {
        return new TweetsPagingAdapter(context, data);
    }

    @Override
    protected boolean shouldLoadFromServer()
    {
        return super.shouldLoadFromServer();
    }

    @Override
    protected List<Tweet> getDataToBindToList()
    {
        return ExampleModel.getTweets();
    }

    @Override
    protected void setLoadedData(List<Tweet> data)
    {
        ExampleModel.setTweets(data);
    }

    @Override
    protected void addLoadedData(List<Tweet> data, LoadType loadType)
    {
        boolean addToBack = LoadType.ENDLESS_LIST_LOAD.equals(loadType);
        ExampleModel.addTweets(data, addToBack);
    }

    @Override
    protected CompleteListenerTask<Tweet[]> createLoadTask(TweetsPagingFrame pagingFrame,
            CompleteListener<Tweet[]> completeListener)
    {
        return new GetTweetsTask(pagingFrame, completeListener);
    }

    @Override
    protected boolean getIsAddLoadedData(LoadType loadType)
    {
        // For tweets, refresh doesn't pull <dataSize> records.  Instead it pulls
        // only records that are newer than the newest tweet that we have currently.
        return super.getIsAddLoadedData(loadType) || LoadType.REFRESH.equals(loadType);
    }

    @Override
    public void update(Observable observable, Object data)
    {
        super.update(observable, data);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
    }

    public static class TweetsPagingAdapter extends ArrayAdapter<Tweet>
    {
        private static class ViewHolder
        {
            private TextView tweetIdTextView;

            private TextView tweetTextTextView;

            private TextView tweetDateTextView;
        }

        private final LayoutInflater layoutInflater;

        public TweetsPagingAdapter(Context context, List<Tweet> pagingData)
        {
            super(context, R.layout.tweet_grid_item, pagingData);
            this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.tweet_grid_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.tweetIdTextView = (TextView) convertView.findViewById(R.id.tweet_id_text_view);
                viewHolder.tweetTextTextView = (TextView) convertView.findViewById(R.id.tweet_text_text_view);
                viewHolder.tweetDateTextView = (TextView) convertView.findViewById(R.id.tweet_date_text_view);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final Tweet tweet = getItem(position);
            viewHolder.tweetIdTextView.setText(Integer.toString(tweet.id));
            viewHolder.tweetTextTextView.setText(tweet.text);
            viewHolder.tweetDateTextView.setText(Long.toString(tweet.dateMs));
            return convertView;
        }
    }
}
