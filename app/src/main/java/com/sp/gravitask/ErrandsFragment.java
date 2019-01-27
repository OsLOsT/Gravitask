package com.sp.gravitask;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ErrandsFragment extends Fragment {

    EditText errandName, errandDescription;
    ImageView imageShown;
    Button publishTask, mapView, takePicture;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        errandName = v.findViewById(R.id.errand_name);
        errandDescription = v.findViewById(R.id.errand_description);
        imageShown = v.findViewById(R.id.errand_imageshown);
        publishTask = v.findViewById(R.id.errand_publish);
        takePicture = v.findViewById(R.id.errand_picturebutton);
        mapView = v.findViewById(R.id.errand_map);

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();


        takePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeOrSelect();
            }
        });

        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMap();
            }
        });

        publishTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });


        return v;
    }

    private void viewMap() {
        Intent intent = new Intent(getActivity(), ErrandMap.class);
        startActivity(intent);
    }

    private void takeOrSelect() {
    }

    private void addTask() {
        String errandAddName = errandName.getText().toString().trim();
        String errandAddDescription = errandDescription.getText().toString().trim();


        Map<String, Object> Errand = new HashMap<>();
        Errand.put("Name", errandAddName);
        Errand.put("Description", errandAddDescription);

        db.collection("Tasks").add(Errand).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getActivity(), "Errand Successfully added", Toast.LENGTH_SHORT).show();
            }
        });
    }

}