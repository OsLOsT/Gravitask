package com.sp.gravitask;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.koalap.geofirestore.GeoFire;
import com.koalap.geofirestore.GeoQuery;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.content.Context.MODE_PRIVATE;

public class TaskViewFragment extends Fragment {

    private TextView name, description;
    private ImageView imageView;
    private Button viewMap, finishTask;
    private SharedPreferences prefs_docId, prefs_checkForTask, prefs_removeGeoQuery;
    private String docId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String uid;
    private boolean taskFinished, checkForTask, removeGeoQuery;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_view, container, false);

        name = (TextView) v.findViewById(R.id.view_name);
        description = (TextView) v.findViewById(R.id.view_description);
        imageView = (ImageView) v.findViewById(R.id.view_imageshown);
        viewMap = (Button) v.findViewById(R.id.view_map);
        finishTask = (Button) v.findViewById(R.id.accept_task);

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        uid = auth.getUid();

        //Get Firebase current user
        user = auth.getCurrentUser();

        prefs_removeGeoQuery = getActivity().getSharedPreferences("removeGeoQuery", MODE_PRIVATE);
        removeGeoQuery = prefs_removeGeoQuery.getBoolean("removeGeoQuery", false); //NOT NEEDED JUST TO STORE HERE TEMP


        prefs_docId = getActivity().getSharedPreferences("docId", MODE_PRIVATE);
        docId = prefs_docId.getString("docId", "");

        prefs_checkForTask = getActivity().getSharedPreferences("checkForTask", MODE_PRIVATE);
        checkForTask = prefs_checkForTask.getBoolean("checkForTask", false);

        if((docId.equals("") && !checkForTask) || docId.equals("") || !checkForTask ){
            Toast.makeText(getContext(), "Go and accept some task", Toast.LENGTH_SHORT).show();
            finishTask.setEnabled(false);
            viewMap.setEnabled(false);

         } else {
            db.collection("TasksView").document(docId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    taskFinished = task.getResult().getBoolean("taskFinished"); //task accepted will be issued true when the user is stadning near the END geofire

                    if (!taskFinished) { //remove the ! after prototype
                        finishTask.setEnabled(true); //to FULLY ENABLE THEFINISHTASK BUTTON STILL NEED TO CHECK IF THE USER HAS REACHED THE LOCATION
                    } else {
                        finishTask.setEnabled(false);
                    }

                }
            });

            db.collection("TasksView").document(docId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    String taskName = task.getResult().getString("name");
                    String taskDescription = task.getResult().getString("description");
                    String errandImage = task.getResult().getString("errandimage");

                    if (taskName != null) {
                        name.setText(task.getResult().getString("name"));
                    }

                    if (taskDescription != null) {
                        description.setText(task.getResult().getString("description"));
                    }

                    if (errandImage != null) {
                        String imageurl = task.getResult().getString("errandimage");
                        Picasso.get().load(imageurl).into(imageView);
                    }


                }
            });
        }





        viewMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ErrandMap.class);
                startActivity(intent);
            }
        });

        finishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Remove query
                Toast.makeText(getContext(), docId, Toast.LENGTH_SHORT).show();
                prefs_docId.edit().putString("docId", "").apply(); //REMOVE AFTER PROTOTYPE
                prefs_checkForTask.edit().putBoolean("checkForTask", false); //REMOVE AFTER PROTOTYPE
                getActivity().finish(); //REMOVE AFTER PROTOTYPE
                startActivity(getActivity().getIntent()); //REMOVE AFTER PROTOTYPE
                //doneTask();
            }
        });


        return v;
    }


    private void doneTask() {
        db.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //remove everything
                long points = task.getResult().getLong("points");
                points = points + 1;
                HashMap<String, Object> User = new HashMap<>();
                User.put("points", points);
                db.collection("Users").document(uid).set(User, SetOptions.merge());

                db.collection("TasksView").document(docId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getActivity(), "Deleted task and dusted", Toast.LENGTH_SHORT).show();
                        prefs_docId.edit().putString("docId", "").apply();
                        prefs_checkForTask.edit().putBoolean("checkForTask", false);
                        prefs_removeGeoQuery.edit().putBoolean("removeGeoQuery", true);
                        getActivity().finish();
                        startActivity(getActivity().getIntent());
                    }
                });

            }
        });
    }
}