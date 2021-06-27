package com.mohyaghoub.motodo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mohyaghoub.motodo.R;
import com.mohyaghoub.motodo.model.Todo;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInflatorView.MoInflaterView;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectable;
import com.moofficial.moessentials.MoEssentials.MoUI.MoInteractable.MoSelectable.MoSelectableUtils;
import com.moofficial.moessentials.MoEssentials.MoUI.MoRecyclerView.MoRecyclerAdapters.MoSelectableAdapter;
import com.moofficial.moessentials.MoEssentials.MoUI.MoView.MoViews.MoNormal.MoCardView;

import java.util.List;

public class TodoItemRecyclerAdapter extends MoSelectableAdapter<TodoViewHolder, Todo> {

    public TodoItemRecyclerAdapter(Context c, List<Todo> dataSet) {
        super(c, dataSet);
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TodoViewHolder(MoInflaterView.inflate(R.layout.cell_todo_item, parent, context));
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        holder.rootCardView.setOnClickListener((v) -> {
            if (isSelecting()) {
                // if we are currently in selecting mode, the select this item
                onSelect(position);
            } else {
                // TODO launch edit todo item
            }
        });
        holder.rootCardView.setOnLongClickListener((v) -> {
            if (isNotSelecting()) {
                // if we are not selecting, starting selecting on this position
                startSelecting(position);

                // consume the long click listener
                return true;
            }
            return false;
        });
        holder.textView.setText(dataSet.get(position).getTitle());
        MoSelectableUtils.applySelectedColor(context, holder.linearLayout, dataSet.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position, @NonNull List<Object> payloads) {
        // by passing a payload, it is more efficient to just make the changes you need when an item is selected/unselected
        // instead of rebuilding the whole view
        if (payloads != null && !payloads.isEmpty() && payloads.get(0) == MoSelectable.PAYLOAD_SELECTED_ITEM) {
            // automatically applies a background color to a selected item
            // ultimately you can use the condition below to set your preferred background when selected or not selected
            //        if (dataSet.get(position).isSelected()) {
            //
            //        } else {
            //
            //        }
            MoSelectableUtils.applySelectedColor(context, holder.linearLayout, dataSet.get(position));
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }
}

class TodoViewHolder extends RecyclerView.ViewHolder {

    MoCardView rootCardView;
    LinearLayout linearLayout;
    TextView textView;

    public TodoViewHolder(@NonNull View itemView) {
        super(itemView);
        rootCardView = itemView.findViewById(R.id.card_todoItem_root);
        linearLayout = itemView.findViewById(R.id.layout_todoItem_linear);
        textView = itemView.findViewById(R.id.text_todoItem_description);
    }
}
