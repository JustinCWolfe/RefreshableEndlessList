package com.refreshableendlesslist.example;

public class Tweet
{
    public final int id;

    public final String text;

    public final long dateMs;

    public Tweet(int id, String text, long dateMs)
    {
        this.id = id;
        this.text = text;
        this.dateMs = dateMs;
    }
}

