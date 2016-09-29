package com.refreshableendlesslist.example;

import java.util.List;

import com.refreshableendlesslist.ObservableModel;

public class ExampleModel extends ObservableModel
{
    public static final ObservableModelProperty TWEETS_PROPERTY = ObservableModelProperty.create();

    private static final ExampleModel MODEL = new ExampleModel();

    private List<Tweet> tweets;

    public static ExampleModel getInstance()
    {
        return MODEL;
    }

    public static List<Tweet> getTweets()
    {
        return MODEL.tweets;
    }

    public static void setTweets(List<Tweet> tweets)
    {
        MODEL.tweets = tweets;
        MODEL.publishPropertyChanged(TWEETS_PROPERTY);
    }

    public static void addTweets(List<Tweet> tweets, boolean addToBack)
    {
        if (addToBack) {
            MODEL.tweets.addAll(tweets);
        } else {
            tweets.addAll(MODEL.tweets);
            MODEL.tweets.clear();
            MODEL.tweets.addAll(tweets);
        }
        MODEL.publishPropertyChanged(TWEETS_PROPERTY);
    }
}

