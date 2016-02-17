package com.refreshableendlesslist;

import java.util.Observable;

public abstract class ObservableModel extends Observable
{
    public static class ObservableModelProperty
    {
        public static ObservableModelProperty create()
        {
            return new ObservableModelProperty();
        }

        private ObservableModelProperty()
        {

        }
    }
    
    protected void publishPropertyChanged(ObservableModelProperty property)
    {
        setChanged();
        notifyObservers(property);
    }
}

