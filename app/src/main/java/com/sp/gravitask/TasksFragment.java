package com.sp.gravitask;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class TasksFragment extends Fragment {

    FirebaseFirestore db;
    CollectionReference tasksref;
    RecyclerView recyclerView;
    TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();

        //Get the reference for Errands
        tasksref = db.collection("Errands");

       recyclerView = v.findViewById(R.id.task_recycler_view);
        
        setUpRecyclerView();

        return v;
    }

    private void setUpRecyclerView() {

        Query query = tasksref.orderBy("name");

        FirestoreRecyclerOptions<Task> options = new FirestoreRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        adapter = new TaskAdapter(options);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public  void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}