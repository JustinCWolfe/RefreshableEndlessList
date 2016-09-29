package com.refreshableendlesslist.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.refreshableendlesslist.CompleteListenerTask;
import com.refreshableendlesslist.RefreshableEndlessListFragment.LoadType;
import com.refreshableendlesslist.RefreshableEndlessListFragment.PagingFrame;

public class GetTweetsTask extends CompleteListenerTask<Tweet[]>
{
    private static final String TASK_NAME = GetTweetsTask.class.getSimpleName();

    public static class TweetsPagingFrame extends PagingFrame
    {
        public final Tweet firstTweet;

        public final Tweet lastTweet;

        public TweetsPagingFrame(int dataSize, LoadType loadType, Tweet firstTweet, Tweet lastTweet)
        {
            super(dataSize, loadType);
            this.firstTweet = firstTweet;
            this.lastTweet = lastTweet;
        }
    }

    private final TweetsPagingFrame pagingFrame;

    public GetTweetsTask(TweetsPagingFrame pagingFrame, CompleteListener<Tweet[]> onCompleteListener)
    {
        super (onCompleteListener);
        this.pagingFrame = pagingFrame;
    }

    @Override
    protected String getTaskName()
    {
        return TASK_NAME;
    }

    @Override
    protected Tweet[] doInBackgroundInternal(Void... params) throws Exception
    {
        // Add some latency to simulate a service call. 
        Thread.sleep(2000);
        List<Tweet> loadedTweets = new ArrayList<>();
        if (pagingFrame.loadType == LoadType.REFRESH) {
            simulateLoadingRefreshTweets(loadedTweets);
        } else if (pagingFrame.loadType == LoadType.ENDLESS_LIST_LOAD) {
            simulateLoadingEndlessListTweets(loadedTweets);
        } else {
            simulateInitialLoad(loadedTweets);
        }
        return loadedTweets.toArray(new Tweet[0]);
    }

    @Override
    protected boolean getTaskWasSuccessful(Tweet[] result)
    {
        return result != null;
    }

    /**
     * For refresh, tweets should be newer than the first tweet. Simulate
     * loading dataSize tweets that are newer than the first loaded tweet.
     * 
     * @param loadedTweets
     */
    private void simulateLoadingRefreshTweets(List<Tweet> loadedTweets)
    {
        for (int i = pagingFrame.dataSize; i > 0; i--) {
            int tweetId = pagingFrame.firstTweet.id + i;
            String text = String.format(Locale.getDefault(), "Tweet %d text.", tweetId);
            long tweetMs = pagingFrame.firstTweet.dateMs + i;
            loadedTweets.add(new Tweet(tweetId, text, tweetMs));
        }
    }

    /**
     * For endless list load, tweets should be older. Simulate loading dataSize
     * tweets that are older than the last loaded tweet.
     * 
     * @param loadedTweets
     */
    private void simulateLoadingEndlessListTweets(List<Tweet> loadedTweets)
    {
        for (int i = 1; i <= pagingFrame.dataSize; i++) {
            int tweetId = pagingFrame.lastTweet.id - i;
            String text = String.format(Locale.getDefault(), "Tweet %d text.", tweetId);
            long tweetMs = pagingFrame.lastTweet.dateMs - i;
            loadedTweets.add(new Tweet(tweetId, text, tweetMs));
        }
    }

    /**
     * Initial load. Simulate loading dataSize tweets.
     * 
     * @param loadedTweets
     */
    private void simulateInitialLoad(List<Tweet> loadedTweets)
    {
        int initialTweetId = 10000;
        long initialTweetMs = System.currentTimeMillis();
        for (int i = 0; i < pagingFrame.dataSize; i++) {
            int tweetId = initialTweetId - i;
            String text = String.format(Locale.getDefault(), "Tweet %d text.", tweetId);
            long tweetMs = initialTweetMs + i;
            loadedTweets.add(new Tweet(tweetId, text, tweetMs));
        }
    }
}

