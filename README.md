# RefreshableEndlessList

Android library implementing a refreshable endlist list.

## Description

An Android abstract fragment and supporting code which allows you to easily create list views that support endless scrolling as well as pull down refresh.  Both endless list and pull down refresh will initiate loads from a service you define and will update the view accordingly through an observable model.

Pull down refresh uses a SwipeRefreshLayout widget.

## Usage

1. Include the RefreshableEndlessList library in your project.

2. Create an entity object which contains the data that each of your list items will be bound to.
  * See [Tweet](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/Tweet.java) for example. 
  
3. Create a layout resource that will be used for each item in your list.
  * See [tweet_grid_item](../master/RefreshableEndlessList-Example/res/layout/tweet_grid_item.xml) for example. 
  
4. Create an adapter derived from ArrayAdapter that will perform the binding between the entity object you created in step 2 and the layout you created in step 3.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.

5. Create a task derived from CompleteListenerTask that will load data from a service.  
  * This task should handle both refresh and endless list loads.
  * See [GetTweetsTask](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/GetTweetsTask.java) for example.

6. Create a paging frame derived from RefreshableEndlessListFragment.PagingFrame which defines how you want paging for data loads to work.
  * See [GetTweetsTask](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/GetTweetsTask.java) for example.
  
7. Create a fragment derived from RefreshableEndlessListFragment.
  * RefreshableEndlessListFragment is an abstract generic class that extends ListFragment.
  * The first bounded type T is for the type of the entity, from step 2, your list items will be bound to.
  * The second bounded type U, which extends ArrayAdapter, is for the type of your list view adapter, from step 4.
  * The third bounded type V, which extends PagingFrame, is for the type of paging frame, from step 5, used by your view during data loads.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
8. Create a model derived from ObservableModel.
  * Create an ObservableModelProperty object that will be sent when the data source your list view is observing is changed.
  * Create get, set and add methods for the data source your list view is observing.
  * See [ExampleModel](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/ExampleModel.java) for example.

9. At the start of your fragment onCreateView method, set the following properties.
  * dataSize (optional; defaults to 50) - how many items will be requested for each load.
  * listViewDivider (optional) - a custom list view item divider.
  * listViewDividerHeight (optional) - a custom height for your list view divider.
  * model (required) - a model object, which extends ObservableModel, that your view will observe for data changes.
  * propertyToObserve (required) - an ObservableModelProperty object that is sent from the model when the datasource your view is concerned with is changed.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
10. Override the createPagingFrame method.
  * Construct and return the paging frame object you defined in step 6.
  * The paging frame object will normally be different depending on the type of load (refresh, endless list, etc).
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
11. Override the createPagingAdapter method.
  * Construct and return the paging adapter object you defined in step 4.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
12. Override the getDataToBindToList method.
  * Call the get method for the model property you are observing.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
13. Override the setLoadedData method.
  * Call the set method for the model property you are observing.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
14. Override the addLoadedData method.
  * Call the add method for the model property you are observing.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
15. Override the createLoadTask method.
  * Construct and return the task object you defined in step 5.
  * See [TweetsFragment](../master/RefreshableEndlessList-Example/src/com/refreshableendlesslist/example/TweetsFragment.java) for example.
  
## License

[MIT License](../master/LICENSE)
