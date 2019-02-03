package com.sp.gravitask;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class TaskAdapter extends FirestoreRecyclerAdapter<Task, TaskAdapter.TaskHolder> {

    public TaskAdapter(@NonNull FirestoreRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskHolder holder, int position, @NonNull Task model) {
        holder.textViewName.setText(model.getName());
        holder.textViewDescription.setText(model.getDescription());
    }

    @NonNull
    @Override
    public TaskHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragments_tasks_item, viewGroup, false);
        return new TaskHolder(v);
    }

    class TaskHolder extends RecyclerView.ViewHolder {

        TextView textViewName;
        TextView textViewDescription;

        public TaskHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.task_view_name);
            textViewDescription = itemView.findViewById(R.id.task_view_description);
        }
    }
}
