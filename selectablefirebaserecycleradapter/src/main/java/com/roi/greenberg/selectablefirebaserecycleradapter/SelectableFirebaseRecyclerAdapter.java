package com.roi.greenberg.selectablefirebaserecycleradapter;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

public class SelectableFirebaseRecyclerAdapter<T, H extends SelectableFirebaseRecyclerAdapter.SelectableHolder> extends FirebaseRecyclerAdapter<T, H> {
    @SuppressWarnings("unused")
    private static final String TAG = SelectableFirebaseRecyclerAdapter.class.getSimpleName();

    private SparseBooleanArray selectedItems;
    private boolean mode;
    private AppCompatActivity mActivity;
    private SelectableActionModeCallback actionModeCallback;
    private ActionMode actionMode;




    public SelectableFirebaseRecyclerAdapter(FirebaseRecyclerOptions<T> options, AppCompatActivity activity) {
        super(options);
        Log.d(TAG, "SelectableFirebaseRecyclerAdapter");
        selectedItems = new SparseBooleanArray();
        mode = false;
        mActivity = activity;
        actionModeCallback = new SelectableActionModeCallback(mActivity, R.menu.selected_menu);
    }

    public void setActionModeCallback(SelectableActionModeCallback actionModeCallback) {
        this.actionModeCallback = actionModeCallback;
    }

    /**
     * Indicates if the item at position position is selected
     * @param position Position of the item to check
     * @return true if the item is selected, false otherwise
     */
    protected boolean isSelected(int position) {
        return getSelectedItems().contains(position);
    }

    /**
     * Toggle the selection status of the item at a given position
     * @param position Position of the item to toggle the selection status for
     */
    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clear the selection status for all items
     */
    public void clearSelection() {
        List<Integer> selection = getSelectedItems();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }

    /**
     * Count the selected items
     * @return Selected items count
     */
    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    /**
     * Indicates the list of selected items
     * @return SList of selected items ids
     */
    private List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    @Override
    protected void onBindViewHolder(@NonNull H holder, int position, @NonNull T model) {

        holder.setSelection(isSelected(position));

    }



    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    public boolean isSelectedMode(){ return mode;}
    public void setSelectedMode(boolean mode) {
        this.mode = mode;
        notifyDataSetChanged();
    }

    public void OnClick(int position, boolean isLong){
        Log.d(TAG, "On"+ (isLong? "Long" :"") +"Click pos " + position + " selectionMode is " + isSelectedMode());


        if (!isSelectedMode()) {
            if (!isLong) {
                return;
            } else if (actionMode == null) {
                Log.d(TAG, "start actionMode");
                setSelectedMode(true);
                actionMode = mActivity.startSupportActionMode(actionModeCallback);
            }
        }

        toggleSelection(position);

        int count = getSelectedItemCount();
        if (count == 0) {
//            setSelectedMode(false);
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }



    public class SelectableHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {


        public SelectableHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);


        }

        public void setSelection(boolean isSelected) {
            Log.d("ROIGR", "set selection " + isSelected);
            if (isSelected) {
                itemView.setBackgroundResource(R.color.selectedItem);
            } else {
                itemView.setBackgroundResource(R.color.transparent);
            }
        }

        public void bindItem() {

        }

        @Override
        public void onClick(View v) {
            OnClick(getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            OnClick(getAdapterPosition(), true);
            return true;
        }
    }

    public class SelectableActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = SelectableActionModeCallback.class.getSimpleName();
        private Context context;
        private @MenuRes int menu_layout;

        public SelectableActionModeCallback(Context context, @MenuRes int menu_layout) {
            this.context = context;
            this.menu_layout = menu_layout;
        }

        @Override
        public boolean onCreateActionMode(final ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate (menu_layout, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
            Log.d(TAG, "onActionItemClicked");
//            setSelectedMode(false);
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d(TAG, "onDestroyActionMode");
            clearSelection();
            setSelectedMode(false);
            actionMode = null;
        }
    }
}