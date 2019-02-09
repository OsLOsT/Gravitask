package com.sp.gravitask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static com.firebase.ui.auth.AuthUI.TAG;

public class TasksFragment extends Fragment {

    private FirebaseFirestore db;
    private CollectionReference tasksref;
    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private double Lat_start = 0.0d;
    private double Lng_start = 0.0d;
    private double Lat_end = 0.0d;
    private double Lng_end = 0.0d;
    private boolean taskAccepted = false;
    private boolean hasAccepted = false;
    private SharedPreferences prefs_docId, prefs_checkForTask;
    private String docId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tasks, container, false);

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();

        //Get the reference for Errands
        tasksref = db.collection("Errands");

        recyclerView = v.findViewById(R.id.task_recycler_view);

        prefs_docId = getActivity().getSharedPreferences("docId", MODE_PRIVATE);

        prefs_checkForTask = getActivity().getSharedPreferences("checkForTask", MODE_PRIVATE);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Get Firebase current user
        user = auth.getCurrentUser();

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

        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                TaskAcceptDialog taskAcceptDialog = documentSnapshot.toObject(TaskAcceptDialog.class);

                docId = documentSnapshot.getId();
                prefs_docId.edit().putString("docId", docId).apply();
                Toast.makeText(getContext(), docId, Toast.LENGTH_SHORT).show();

                showAcceptTaskDialog(taskAcceptDialog.getName(), taskAcceptDialog.getDescription(), taskAcceptDialog.getUid(), taskAcceptDialog.getGpstart(), taskAcceptDialog.getGpend(), taskAcceptDialog.getErrandimage());
            }
        });
    }

    private void showAcceptTaskDialog(String name, String description, String uid, GeoPoint start, GeoPoint end, String imageurl) {

        String currentUid = auth.getUid();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.task_accept_dialog, null);

        dialogBuilder.setView(dialogView);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();


        final TextView acceptName = (TextView) dialogView.findViewById(R.id.accept_name);
        final TextView acceptDescription = (TextView) dialogView.findViewById(R.id.accept_description);
        final ImageView acceptImage = (ImageView) dialogView.findViewById(R.id.accept_imageshown);
        final Button acceptMap = (Button) dialogView.findViewById(R.id.accept_map);
        final Button acceptButton = (Button) dialogView.findViewById(R.id.accept_button);

        acceptName.setText(name);
        acceptDescription.setText(description);

        if (imageurl != null) {
            Picasso.get().load(imageurl).into(acceptImage);
        } else {
            Picasso.get().load(R.drawable.test_3).into(acceptImage);
        }

        acceptMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getActivity(), ErrandMap.class);

                Lat_start = start.getLatitude();
                Lng_start = start.getLongitude();
                Lat_end = end.getLatitude();
                Lng_end = end.getLongitude();


                intent.putExtra("Lat_start", Lat_start);
                intent.putExtra("Lng_start", Lng_start);
                intent.putExtra("Lat_end", Lat_end);
                intent.putExtra("Lng_end", Lng_end);

                startActivity(intent);
            }
        });

        //Check if the user has already accepted the task.
        db.collection("Users").document(currentUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    hasAccepted = task.getResult().getBoolean("taskAccepted");

                    if (uid.equals(currentUid) && hasAccepted) {
                        acceptButton.setEnabled(false);

                    } else {
                        acceptButton.setEnabled(true);
                        acceptButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                taskAccepted = true;
                                //String uid = auth.getUid();
                                Map<String, Object> User = new HashMap<>();
                                User.put("taskAccepted", taskAccepted);

                                db.collection("Users").document(uid).set(User, SetOptions.merge());

                                tasksref.document(docId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                        String descripton = task.getResult().getString("description");
                                        String errandImage = task.getResult().getString("errandimage");
                                        GeoPoint gpstart = task.getResult().getGeoPoint("gpstart");
                                        GeoPoint gpend = task.getResult().getGeoPoint("gpend");
                                        String name = task.getResult().getString("name");
                                        String uid = task.getResult().getString("uid");
                                        boolean taskFinished = task.getResult().getBoolean("taskFinished");

                                        TaskAcceptDialog transferTask = new TaskAcceptDialog(name, descripton, uid, taskFinished, gpstart, gpend, errandImage);
                                        db.collection("TasksView").document(docId).set(transferTask).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                tasksref.document(docId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(), "Successfully transfer", Toast.LENGTH_SHORT).show();
                                                        prefs_checkForTask.edit().putBoolean("checkForTask", true).apply();
                                                        alertDialog.dismiss();
                                                    }
                                                });
                                            }
                                        });


                                    }
                                });

                   /* // Create new fragment and transaction //TODO: FInd a way to change layout and also change the highlight of the nav drawer to the respective layout in place
                    Fragment newFragment = new TasksFragment();
                    // consider using Java coding conventions (upper first char class names!!!)
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    transaction.replace(R.id.fragment_container, newFragment);
                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();*/

                            }
                        });
                    }


                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}