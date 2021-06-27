package com.mohyaghoub.motodo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Pair;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mohyaghoub.motodo.adapter.TodoItemRecyclerAdapter;
import com.mohyaghoub.motodo.manager.TodoManager;
import com.mohyaghoub.motodo.model.Todo;
import com.moofficial.moessentials.MoEssentials.MoLog.MoLog;
import com.moofficial.moessentials.MoEssentials.MoUI.MoActivity.MoSmartActivity;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoListViewSync;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSearchable.MoSearchable;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSearchable.MoSearchableInterface.MoSearchableItem;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSearchable.MoSearchableInterface.MoSearchableList;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectable;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableInterface.MoOnCanceledListener;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableInterface.MoOnEmptySelectionListener;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableInterface.MoOnSelectFinishedListener;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableInterface.MoOnSelectListener;
import com.moofficial.moessentials.MoEssentials.MoUI.MoPopUpMenu.MoPopUpMenu;
import com.moofficial.moessentials.MoEssentials.MoUI.MoRecyclerView.MoRecyclerUtils;
import com.moofficial.moessentials.MoEssentials.MoUI.MoRecyclerView.MoRecyclerView;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViewBuilder.MoMarginBuilder;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoBars.MoInputBar;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoBars.MoSearchBar;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoBars.MoToolBar;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoNormal.MoCardRecyclerView;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoViewUtils;
import com.moofficial.moessentials.MoEssentials.MoUtils.MoKeyboardUtils.MoKeyboardUtils;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.LENGTH_SHORT;

public class MainActivity extends MoSmartActivity {

    MoToolBar toolBar;
    MoSearchBar searchBar;
    MoCardRecyclerView cardRecyclerView;
    MoRecyclerView recyclerView;
    TodoItemRecyclerAdapter adapter;
    MoInputBar inputBar;
    MoSelectable<Todo> selectable;
    MoSearchable searchable;
    MoListViewSync listViewSync;

    @Override
    protected void init() {
        TodoManager.instance.load(this);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        // where you initialize views and load different things
        setTitle(R.string.app_name);
        setSubTitle(R.string.mainActivity_description);

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




        // init recycler view stuff here
        cardRecyclerView = new MoCardRecyclerView(this);
        adapter = new TodoItemRecyclerAdapter(this, new ArrayList<>());
        recyclerView = MoRecyclerUtils.get(cardRecyclerView.getRecyclerView(), adapter)
                .setLayoutManagerType(MoRecyclerView.STAGGERED_GRID_LAYOUT_MANAGER)
                .show();
        recyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
        // adding the card view that has a recycler view inside it to the nested linear layout
        l.linearNested.addView(cardRecyclerView);


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


        // now we need to sync between selecting and searching
        // this scenario is designed for when user wants to search something and delete multiple items
        // we need to deal with different states

        this.listViewSync = new MoListViewSync(getGroupRootView(), this.searchable, this.selectable)
                .setPutOnHold(true)
                .setSharedElements(this.inputBar)
                .setOnEmptyOnHoldsListener(this::updateAdapter);
    }

    private void updateAdapter() {
        updateAdapter(TodoManager.instance.getTodos(this));
    }

    private void updateAdapter(List<Todo> newList) {
        adapter.update(this, newList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateAdapter();
    }

    @Override
    public void onBackPressed() {
        if (listViewSync.hasAction()) {
            // if we are selecting or searching, cancel them
            listViewSync.removeAction();
        } else {
            super.onBackPressed();
        }
    }
}