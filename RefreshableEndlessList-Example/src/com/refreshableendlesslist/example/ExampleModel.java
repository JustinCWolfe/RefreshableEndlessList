package com.refreshableendlesslist.example;

import java.util.List;

import com.refreshableendlesslist.RefreshableEndlessListModel;

public class ExampleModel extends RefreshableEndlessListModel
{
    public static final String TWEETS_PROPERTY_NAME = "TWEETS";

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
        MODEL.publishPropertyChanged(TWEETS_PROPERTY_NAME);
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
        MODEL.publishPropertyChanged(TWEETS_PROPERTY_NAME);
    }
}

