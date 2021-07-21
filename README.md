# MoTodo
A simple todo list demonstrating [Moessentials](https://github.com/mohyghb/MoEssentials) capabilities


### Let us go through a simple To-do list app and see how effortlessly you can develop features for this app

1.	First create our model class, we call it Todo, and it just has a simple field called title. Since our app is small, we just need a title for a todo item.
2.	As you can see, Todo implements
  a.	 MoSavable (to have the ability to save this class, something like serialization of a json)
  b.	MoLoadable (for loading this class from a saved string)
  c.	MoSelectableItem – for making this object be selectable
  d.	MoSearchableItem – for making this object be searchable
  
```*.java
public class Todo implements MoSavable, MoLoadable, MoSelectableItem, MoSearchableItem {
    String title = "";

    boolean isSearched, isSelected;

    public Todo(String title) {
        this.title = title;
    }

    public Todo() {
    }

    public String getTitle() {
        return title;
    }

    public Todo setTitle(String title) {
        this.title = title;
        return this;
    }

    public boolean isSearched() {
        return isSearched;
    }

    public Todo setSearched(boolean searched) {
        isSearched = searched;
        return this;
    }
```

Let us look at some of the methods inside this class

1.	Load – for loading back a saved string into this class
2.	getData – for easily returning all the things that need to be saved from this class, e.g right now we only save title, so simply pass in title.
3.	updateSearchable – returns true if this object satisfies the criteria (the searched text is passed to the first object param)
4.	the other methods are self explanatory, they are just getters and setters for our classes to be able to access data into this class.

```*.java
@Override
    public void load(String s, Context context) {
        // load in the objects from s
        String[] components = MoFile.loadable(s);
        title = components[0];
    }

    @Override
    public String getData() {
        // pass in the objects that you want to save
        return MoFile.getData(title);
    }

    @Override
    public boolean updateSearchable(Object... objects) {
        // given a set of objects return true if this item should be shown as search result (objects[0] contain the enter text of user)
        // first param of MoSearchableUtils.isSearchable if set to true, returns the item when the search is empty, otherwise doesn't return true
        return MoSearchableUtils.isSearchable(true, objects, title);
    }

    @Override
    public boolean isSearchable() {
        return isSearched;
    }

    @Override
    public void setSearchable(boolean b) {
        this.isSearched = b;
    }

    @Override
    public void setSelected(boolean b) {
        this.isSelected = b;
    }

    @Override
    public boolean isSelected() {
        return this.isSelected;
    }
```


Now we need a manager to handle adding, removing, loading, and saving these todo items. Most of this is just adding/removing items to a list and saving the list each time. Basically, you are getting the content for all the different to-do items inside the list, and then saving that inside a file. For loading, we use a similar approach. Read the saved file and create a new to-do item for each of the saved entries.
```*.java
public class TodoManager {

    private static final String FILE_NAME = "todofiles";
    public static final TodoManager instance = new TodoManager();

    private List<Todo> todos = new ArrayList<>();


    public List<Todo> getTodos(Context context) {
        if (todos.isEmpty()) {
            // try loading it if it's empty
            load(context);
        }
        return todos;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public void add(Context context, Todo todo) {
        todos.add(todo);
        // for more optimized saving, you can save each todo to its own file, please take a look at MoFileSavable
        // inside my other projects such as moweb, I have implemented it that way for MoTab,
        // since not a lot of optimization is needed here, we are going to simply save each time a todo
        // item is added or removed
        save(context);
    }

    public void remove(Context context, List<Todo> toRemoveTodos) {
        todos.removeAll(toRemoveTodos);
        save(context);
    }

    public void save(Context context) {
        try {
            MoFileManager.writeInternalFile(context, FILE_NAME, MoFile.getData(todos));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(Context context) {
        if (!todos.isEmpty())
            return;
        try {
            String content = MoFileManager.readInternalFile(context, FILE_NAME);
            MoFile.setData(context, MoFile.loadable(content)[0], todos, (context1, s) -> {
                Todo todo = new Todo();
                todo.load(s, context1);
                return todo;
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## Adding Toolbar and Search bar
To add a toolbar, simply create a new MoToolBar and customize different icons, titles, and listeners. 
```*.java
// init the main toolbar
toolBar = new MoToolBar(this);
toolBar.hideLeft()
        .setMiddleIcon(R.drawable.ic_baseline_search_24)
        .setExtraIcon(R.drawable.ic_baseline_delete_outline_24)
        .setExtraOnClickListener((v)-> {
            // add some sort of animation for deleting items
            TransitionManager.beginDelayedTransition(getGroupRootView());
            // remove the selected items
            TodoManager.instance.remove(this, adapter.getSelectedItems());

            // because there might be a case where user is currently searching, so we don't wanna
            // show all of the todos to the user, only the ones that came up from their search
            adapter.getDataSet().removeAll(adapter.getSelectedItems());
            updateAdapter(adapter.getDataSet());

            // telling the sync that we are done with selecting
            listViewSync.removeAction();
        })
        .hideExtraButton()
        .setRightIcon(R.drawable.ic_baseline_more_vert_24)
        .setRightOnClickListener((v) -> {
            // just a demonstration on how easily you can create pop up menus
            new MoPopUpMenu(this).setEntries(
                    new Pair<>("First option", menuItem -> {
                        Toast.makeText(MainActivity.this, "First option clicked", LENGTH_SHORT).show();
                        return false;
                    }),
                    new Pair<>("Second option", menuItem -> {
                        Toast.makeText(MainActivity.this, "Second option clicked", LENGTH_SHORT).show();
                        return false;
                    })
            ).show(v);
        });

// need to sync the main title with this toolbar title, so when one goes down the other one is shown
syncTitle(toolBar.getTitle());

searchBar = new MoSearchBar(this);

// setting up a multiple toolbars and setting toolbar as the active one
setupMultipleToolbars(toolBar, toolBar, searchBar);
```

## Adding Input bar
Now we need to obtain the input from user to set it as the title of a to-do item. To do that, we need to make an input bar. You can customize different aspect of the input bar such as the hint text, positive text, title, description, and more.
```*.java
inputBar = new MoInputBar(this);
inputBar.setTitle("Add todo")
        .setDescription("Type here to add a note to your todo list")
        .setHint("Enter a todo")
        .showDescription()
        .showPositiveButton()
        .setPositiveButtonText("Add Todo")
        .showDividerInvisible()
        .setPositiveClickListener((v)-> {
            String inputText = inputBar.getInputText();
            if (inputText.isEmpty()) {
                // show error
                inputBar.setError("Please enter a description for your todo item");
                return;
            }
            TodoManager.instance.add(this, new Todo(inputText));
            updateAdapter();
            inputBar.clearText();
            MoKeyboardUtils.hideSoftKeyboard(v);
            // show a success snack bar
            Snackbar.make(l, "Your todo item has been added successfully", Snackbar.LENGTH_SHORT).show();
        });
inputBar.getCardView().setCardElevation(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? 6f : 0f);
// adding 8dp top margin for the input bar
l.linearNested.addView(inputBar, MoMarginBuilder.getLinearParams(this, 0,8,0,8));
```

## Recycler View
A card recycler view allows to make the corners of the recycler view round. (At the time of creation of this lib, the only way you could round the corners of a recycler view was to wrap it into a card view). You can easily change the layout manager type to make the recycler view be staggered grid layout, and there is more customization. Make sure you add the recycler view to the correct view though, which is l.linearNested.
```*.java
// init recycler view stuff here
cardRecyclerView = new MoCardRecyclerView(this);
adapter = new TodoItemRecyclerAdapter(this, new ArrayList<>());
recyclerView = MoRecyclerUtils.get(cardRecyclerView.getRecyclerView(), adapter)
        .setLayoutManagerType(MoRecyclerView.STAGGERED_GRID_LAYOUT_MANAGER)
        .show();
recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
// adding the card view that has a recycler view inside it to the nested linear layout
l.linearNested.addView(cardRecyclerView);
```

## Selectable
To make a recycler view selectable, simply wrap the adapter into a Moselectable class. From there, you can change various aspects like what happens when we start selecting and more. Normal Views are views that are only visible when user is NOT selecting. Unnormal views are views that are only visible when user is selecting. This way you can limit what is shown to user when user starts selecting or not.
```*.java
// selectable
// this is the class that allows our adapter to be selectable
selectable = new MoSelectable<>(this, getGroupRootView(), adapter)
        .setCounterView(l.title)
        // for allowing the user to check all of the items
        .setSelectAllCheckBox(toolBar.getCheckBox())
        // if you wanna update the title to what it was after selection is done
        .updateTitle(true)
        // what views to hide when selecting has been started
        .addNormalViews(toolBar.getRightButton(), toolBar.getMiddleButton(), l.subtitle)
        // what views to show when selecting has been started
        .addUnNormalViews(toolBar.getCheckBox(), toolBar.getExtraButton())
        .setOnCanceledListener(() -> adapter.getSelectedItems().clear())
        .setOnEmptySelectionListener(() -> {
            // want to cancel selecting when selection is empty
            listViewSync.removeAction();
        });
```

## Searchable
To make a list searchable, simple provide a list of items that you wanna search over. Then you can customize it as you desire. Like Selectable, you can add normal and unnormal views.

```*.java
// searchable
searchable = new MoSearchable(this, getGroupRootView(), TodoManager.instance::getTodos);
searchable.setShowNothingWhenSearchEmpty(false)
        .setSearchOnTextChanged(true)
        .setSearchTextView(searchBar.ETId())
        .setAppBarLayout(l.appBarLayout)
        .setSearchButton(toolBar.MId())
        .addNormalViews(toolBar)
        .addUnNormalViews(searchBar)
        .setCancelButton(searchBar.LBId())
        .setClearSearch(searchBar.RBId())
        .setActivity(this)
        .setOnSearchFinished(list -> {
            //noinspection unchecked
            updateAdapter((ArrayList<Todo>) list);
        });
```

## ListView Sync
This view was created to sync actions between a selectables and searchables items. This can be used for when user presses onBackPressed, or when user activates selecting mode while they are in search mode. This syncs the events between those states and makes sure that you will not have problems. If you only use one of selectable or searchable classes, you do not need to implement this.
```*.java
// now we need to sync between selecting and searching
// this scenario is designed for when user wants to search something and delete multiple items
// we need to deal with different states

this.listViewSync = new MoListViewSync(getGroupRootView(), this.searchable, this.selectable)
        .setPutOnHold(true)
        .setSharedElements(this.inputBar)
        .setOnEmptyOnHoldsListener(this::updateAdapter);
```

