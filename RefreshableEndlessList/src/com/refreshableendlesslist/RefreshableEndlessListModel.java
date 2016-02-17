package com.refreshableendlesslist;

import java.util.Observable;

public abstract class RefreshableEndlessListModel extends Observable
{
    protected void publishPropertyChanged(String propertyName)
    {
        setChanged();
        notifyObservers(propertyName);
    }
}

