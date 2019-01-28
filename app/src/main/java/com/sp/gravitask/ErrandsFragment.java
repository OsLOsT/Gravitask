package com.sp.gravitask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import io.grpc.Context;

public class ErrandsFragment extends Fragment {

    EditText errandName, errandDescription;
    ImageView imageShown;
    Button publishTask, mapView, takePicture;
    FirebaseFirestore db;
    Integer REQUEST_CAMERA = 1, SELECT_FILE=0;
    Uri selectImageUri,cameraImageUri;
    String docId;
    private StorageReference errandImageRef, storageRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_errands, container, false);

        //TODO: Need to add 2 markers to the map to indicate one where the task will be held two where to pass the stuff to the tasker to unless the the tasker task is only one way then dont add second marker and no location will be added to the map :-)

        //TODO: The user close to the area of the tasks will be alerted 50m radius geofencing
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode,data);

        if(resultCode== Activity.RESULT_OK && data !=null){

            if(requestCode==REQUEST_CAMERA){
                cameraImageUri = data.getData();
                imageShown.setImageURI(cameraImageUri);
                uploadImageToFireBaseStorage();

            }else if(requestCode==SELECT_FILE){
                selectImageUri = data.getData();
                imageShown.setImageURI(selectImageUri);
                uploadImageToFireBaseStorage();

            }

        }

    }

    private void takeOrSelect() {

        final CharSequence[] items = {"Take a picture", "Choose from gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(items[i].equals("Take a picture")){

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                }else if(items[i].equals("Choose from gallery")){

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                }else if(items[i].equals("Cancel")){
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadImageToFireBaseStorage() {
        storageRef = FirebaseStorage.getInstance().getReference("ErrandsImage/");


        if (selectImageUri!=null) {
            errandImageRef = storageRef.child(docId + "." + getFileExtension(selectImageUri));

            errandImageRef.putFile(selectImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //String profileImageUrl = profileImageRef.getDownloadUrl().toString();
                    errandImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String downloadUrl = task.getResult().toString();
                            Map<String, Object> Errand = new HashMap<>();
                            Errand.put("errandimage", downloadUrl);
                            db.collection("Errands").document(docId).set(Errand, SetOptions.merge());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else if (cameraImageUri != null) {
            errandImageRef = storageRef.child(docId + "." + "png");

            errandImageRef.putFile(cameraImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    errandImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String downloadUrl = task.getResult().toString();
                            Map<String, Object> Errand = new HashMap<>();
                            Errand.put("errandimage", downloadUrl);
                            db.collection("Errands").document(docId).set(Errand, SetOptions.merge());

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
        }
    }


    private void addTask() {
        String errandAddName = errandName.getText().toString().trim();
        String errandAddDescription = errandDescription.getText().toString().trim();


        Map<String, Object> Errand = new HashMap<>();
        Errand.put("name", errandAddName);
        Errand.put("description", errandAddDescription);

        db.collection("Errands").add(Errand).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getActivity(), "Errand Successfully added", Toast.LENGTH_SHORT).show();
                docId = documentReference.getId();

            }
        });
    }

}