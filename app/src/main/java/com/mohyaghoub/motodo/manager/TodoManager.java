package com.mohyaghoub.motodo.manager;

import android.content.Context;

import com.mohyaghoub.motodo.MainActivity;
import com.mohyaghoub.motodo.model.Todo;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoFileManager;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoFile;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoFileSavable;
import com.moofficial.moessentials.MoEssentials.MoFileManager.MoIO.MoOnLoadingBackListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
