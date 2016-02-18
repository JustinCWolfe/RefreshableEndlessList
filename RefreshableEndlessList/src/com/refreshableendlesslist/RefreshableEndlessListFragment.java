package com.refreshableendlesslist;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.refreshableendlesslist.CompleteListenerTask.CompleteListener;
import com.refreshableendlesslist.CompleteListenerTask.TaskResult;
import com.refreshableendlesslist.CompleteListenerTask.TaskStatus;
import com.refreshableendlesslist.ObservableModel.ObservableModelProperty;
import com.refreshableendlesslist.RefreshableEndlessListFragment.PagingFrame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class RefreshableEndlessListFragment<T, U extends ArrayAdapter<T>, V extends PagingFrame> extends
        ListFragment implements SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener, Observer
{
    public enum LoadType {
        NOT_LOADING, CACHE_LOAD, INITIAL_LOAD, REFRESH, ENDLESS_LIST_LOAD;
    }

    public static class PagingFrame
    {
        public final int dataSize;

        public final LoadType loadType;

        public PagingFrame(int dataSize, LoadType loadType)
        {
            this.dataSize = dataSize;
            this.loadType = loadType;
        }
    }

    public static class PagingFrameHolder<V>
    {
        private V pagingFrame;

        public void put(V pagingFrame)
        {
            this.pagingFrame = pagingFrame;
        }

        public V remove()
        {
            V tempPagingFrame = pagingFrame;
            this.pagingFrame = null;
            return tempPagingFrame;
        }
    }

    public static class ScrollContext<T>
    {
        public T firstObject;

        public T lastObject;

        public int firstVisibleObjectIndex;

        public int firstVisibleObjectScrollY;

        public int lastObjectIndex;

        public int lastVisibleObjectIndex;

        public ScrollContext(T firstObject, T lastObject, int firstVisibleObjectIndex, int firstVisibleObjectScrollY,
                int lastObjectIndex, int lastVisibleObjectIndex)
        {
            this.firstObject = firstObject;
            this.lastObject = lastObject;
            this.firstVisibleObjectIndex = firstVisibleObjectIndex;
            this.firstVisibleObjectScrollY = firstVisibleObjectScrollY;
            this.lastObjectIndex = lastObjectIndex;
            this.lastVisibleObjectIndex = lastVisibleObjectIndex;
        }
    }

    private static final int PULLDOWN_REFRESH_ENABLED_LIST_POSITION = 0;

    private static final int NUMBER_OF_ITEMS_LEFT_LOAD_THRESHOLD = 5;

    private static final int DEFAULT_DATA_SIZE = 50;

    private static final String REQUIRED_FIELD_NOT_SET_MSG = "Fields 'propertyToObserve' and 'model' must be set.";

    protected ObservableModelProperty propertyToObserve;

    protected ObservableModel model;

    protected Integer dataSize;

    protected Drawable listViewDivider;

    protected Integer listViewDividerHeight;

    private U adapter;
    
    private PagingFrameHolder<V> pagingFrameHolder;

    private ScrollContext<T> scrollContext = new ScrollContext<>(null, null, 0, 0, 0, 0);

    private SwipeRefreshLayout swipeLayout;

    private LoadType loadType = LoadType.NOT_LOADING;

    private boolean refreshingAdapter;

    private boolean endOfDataReached;

    private View progressView;

    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
    {
        if (propertyToObserve == null || model == null) {
            throw new UnsupportedOperationException(REQUIRED_FIELD_NOT_SET_MSG);
        }

        View view = inflater.inflate(getRefreshableEndlessListLayoutId(), container, false);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light,
                android.R.color.holo_orange_light, android.R.color.holo_red_light);

        progressView = view.findViewById(R.id.progress_view);

        listView = (ListView) view.findViewById(android.R.id.list);
        if (listViewDivider != null) {
            listView.setDivider(listViewDivider);
        }
        if (listViewDividerHeight != null) {
            listView.setDividerHeight(listViewDividerHeight);
        }

        pagingFrameHolder = new PagingFrameHolder<>();

        model.addObserver(this);
        return view;
    }

    protected int getRefreshableEndlessListLayoutId()
    {
        return R.layout.fragment_refreshable_endless_list;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        // If savedInstanceState bundle value is not null the view state is being
        // restored in cases where app is started after its process is killed.
        // In these cases, close this activity and force the user to begin
        // at the main activity instead.
        if (savedInstanceState != null) {
            return;
        }
        getListView().setOnScrollListener(this);
        initializeFromCacheOrLoad();
    }

    @Override
    public void onDestroyView()
    {
        model.deleteObserver(this);
        super.onDestroyView();
    }

    @Override
    public void onRefresh()
    {
        initializeAndLoad(LoadType.REFRESH);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        switch (view.getId()) {
            case android.R.id.list:
                if (totalItemCount > 0) {
                    int lastIndex = adapter.getCount() - 1;
                    scrollContext.firstObject = adapter.getItem(0);
                    scrollContext.lastObject = adapter.getItem(lastIndex);
                    scrollContext.firstVisibleObjectIndex = firstVisibleItem;
                    int firstVisibleItemOffset = getVisibleScrollItemOffsetRelativeToParent(getListView(),
                            firstVisibleItem);
                    scrollContext.firstVisibleObjectScrollY = firstVisibleItemOffset - view.getPaddingTop();
                    scrollContext.lastObjectIndex = lastIndex;
                    scrollContext.lastVisibleObjectIndex = view.getLastVisiblePosition();
                    // In order for pull down refresh to be enabled, you must be at the top
                    // row in the list and at the top of that row itself.
                    boolean enableSwipeLayout = (PULLDOWN_REFRESH_ENABLED_LIST_POSITION == firstVisibleItem) &&
                            (firstVisibleItemOffset == 0);
                    swipeLayout.setEnabled(enableSwipeLayout);
                }
                break;
        }
    }

    protected abstract V createPagingFrame(int dataSize, List<T> data, LoadType loadType);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState)
    {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            int totalItemCount = adapter.getCount();
            boolean thereIsDataInList = totalItemCount > 0;
            boolean testIfLoadIsRequired = thereIsDataInList && LoadType.NOT_LOADING.equals(loadType) &&
                    !refreshingAdapter && !endOfDataReached;
            if (testIfLoadIsRequired) {
                double currentNumberOfItemsLeft = totalItemCount - scrollContext.lastVisibleObjectIndex;
                if (currentNumberOfItemsLeft <= NUMBER_OF_ITEMS_LEFT_LOAD_THRESHOLD) {
                    load(LoadType.ENDLESS_LIST_LOAD);
                }
            }
        }
    }

    protected abstract U createPagingAdapter(Context context, List<T> data);

    @Override
    public void update(Observable observable, Object data)
    {
        if (propertyToObserve == data) {
            List<T> listData = getDataToBindToList();
            if (listData != null) {
                refreshingAdapter = true;
                adapter = createPagingAdapter(getActivity(), listData);
                setListAdapter(adapter);
                getListView().setSelectionFromTop(scrollContext.firstVisibleObjectIndex,
                        scrollContext.firstVisibleObjectScrollY);
                refreshingAdapter = false;
            }
        }
    }

    protected void setScrollToTop()
    {
        scrollContext.firstVisibleObjectIndex = 0;
        scrollContext.firstObject = null;
        scrollContext.lastObjectIndex = 0;
        scrollContext.lastObject = null;
        getListView().setSelection(scrollContext.firstVisibleObjectIndex);
    }

    protected void setScrollToBottom()
    {
        scrollContext.firstVisibleObjectIndex = 0;
        scrollContext.firstObject = null;
        int lastObjectIndex = adapter.getCount() - 1;
        scrollContext.lastObjectIndex = (lastObjectIndex < 0) ? 0 : lastObjectIndex;
        scrollContext.lastObject = (lastObjectIndex < 0) ? null : adapter.getItem(scrollContext.lastObjectIndex);
        getListView().setSelection(scrollContext.lastObjectIndex);
    }

    protected abstract List<T> getDataToBindToList();

    protected boolean shouldLoadFromServer()
    {
        List<T> cacheData = getDataToBindToList();
        return (cacheData == null || cacheData.size() == 0);
    }

    protected void initializeFromCacheOrLoad()
    {
        initializeAndLoad(shouldLoadFromServer() ? LoadType.INITIAL_LOAD : LoadType.CACHE_LOAD);
    }

    private void initializeAndLoad(LoadType loadType)
    {
        endOfDataReached = false;
        load(loadType);
    }

    protected abstract void setLoadedData(List<T> data);

    protected abstract void addLoadedData(List<T> data, LoadType loadType);

    protected abstract CompleteListenerTask<T[]> createLoadTask(V pagingFrame, CompleteListener<T[]> completeListener);

    protected boolean getIsAddLoadedData(LoadType loadType)
    {
        return (LoadType.ENDLESS_LIST_LOAD.equals(loadType));
    }
    
    private void load(final LoadType requestedLoadType)
    {
        if (LoadType.CACHE_LOAD.equals(requestedLoadType)) {
            List<T> cacheData = getDataToBindToList();
            if (cacheData != null) {
                setLoadedData(cacheData);
            }
        } else {
            if (LoadType.NOT_LOADING.equals(loadType)) {
                loadType = requestedLoadType;
                final int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                if (shouldShowProgressSpinner(requestedLoadType)) {
                    setViewVisibility(shortAnimTime, progressView, true);
                }
                int actualDataSize = firstNonNull(dataSize, DEFAULT_DATA_SIZE);
                V newLoadTypePagingFrame = createPagingFrame(actualDataSize, getDataToBindToList(), requestedLoadType);
                // If there is a paging frame stored in the holder, use that paging frame for load.
                final V actualPagingFrame = firstNonNull(pagingFrameHolder.remove(),
                        newLoadTypePagingFrame);
                final LoadType actualLoadType = actualPagingFrame.loadType;
                createLoadTask(actualPagingFrame, new CompleteListenerTask.CompleteListener<T[]>()
                {
                    @Override
                    public void onComplete(TaskResult<T[]> taskResult)
                    {
                        List<T> loadedData = new ArrayList<>();
                        boolean isSetLoadedData = !getIsAddLoadedData(actualLoadType);
                        // If we are setting the loaded data, clear out the current
                        // data regardless of whether or not the service call passed.
                        if (isSetLoadedData) {
                            setLoadedData(loadedData);
                        }
                        if (TaskStatus.PASSED.equals(taskResult.status)) {
                            for (T loadedObject : taskResult.result) {
                                loadedData.add(loadedObject);
                            }
                            if (isSetLoadedData) {
                                setLoadedData(loadedData);
                            } else {
                                addLoadedData(loadedData, actualLoadType);
                            }
                        }
                        // Some refresh loads will get more recent data than what has already been loaded.
                        // Those loads may not retrieve datasize records so don't indicate that we are at
                        // the end of our data for refresh loads.
                        boolean lessDataRetrievedThanExpected = TaskStatus.PASSED.equals(taskResult.status)
                                && actualLoadType != LoadType.REFRESH
                                && taskResult.result.length < actualPagingFrame.dataSize;
                        if (TaskStatus.FAILED.equals(taskResult.status) || lessDataRetrievedThanExpected) {
                            endOfDataReached = true;
                        }
                        swipeLayout.setRefreshing(false);
                        if (shouldShowProgressSpinner(requestedLoadType)) {
                            setViewVisibility(shortAnimTime, progressView, false);
                        }
                        loadType = LoadType.NOT_LOADING;
                    }
                }).execute();
            }
        }
    }

    private static boolean shouldShowProgressSpinner(LoadType loadType)
    {
        return LoadType.INITIAL_LOAD.equals(loadType) || LoadType.ENDLESS_LIST_LOAD.equals(loadType);
    }

    private static <T> T firstNonNull(T first, T second) throws NullPointerException
    {
        T nonNull = first;
        if (nonNull == null) {
            nonNull = second;
        }
        if (nonNull == null) {
            throw new NullPointerException();
        }
        return nonNull;
    }

    private static void setViewVisibility(int shortAnimTime, final View view, boolean visible)
    {
        final int newVisibility = visible ? View.VISIBLE : View.GONE;
        view.animate()
                .setDuration(shortAnimTime)
                .alpha(visible ? 1 : 0)
                .setListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        view.setVisibility(newVisibility);
                    }
                });
    }

    private static int getVisibleScrollItemOffsetRelativeToParent(ListView listView, int itemIndex)
    {
        // getChildAt requires the visible index (the index within the current visible
        // view, rather than the absolute index of the item in the data source.
        int actualItemIndex = itemIndex - listView.getFirstVisiblePosition();
        View childView = listView.getChildAt(actualItemIndex);
        return (childView != null) ? childView.getTop() : 0;
    }
}
