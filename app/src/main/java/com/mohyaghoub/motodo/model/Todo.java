package com.mohyaghoub.motodo.model;

import android.content.Context;

import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoFile;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoLoadable;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoOnLoadingBackListener;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoSavable;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSearchable.MoSearchableInterface.MoSearchableItem;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSearchable.MoSearchableUtils;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableInterface.MoSelectableItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
}
