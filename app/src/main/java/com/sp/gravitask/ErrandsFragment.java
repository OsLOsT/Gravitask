package com.sp.gravitask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class ErrandsFragment extends Fragment {

    private EditText errandName, errandDescription;
    private ImageView imageShown;
    private Button publishTask, mapView, takePicture;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private Integer REQUEST_CAMERA = 1, SELECT_FILE = 0;
    private Uri selectImageUri, cameraImageUri;
    private String docId;
    private StorageReference errandImageRef, storageRef;
    private SharedPreferences prefs_start, prefs_end, prefs_docId;
    private GPSTracker gpsTracker;
    private double lat_start = 0.0d;
    private double lng_start = 0.0d;
    private double lat_end = 0.0d;
    private double lng_end = 0.0d;
    private double myLatitude = 0.0d;
    private double myLongitude = 0.0d;
    private static File tempFile = null;
    private boolean taskFinished;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_errands, container, false);

        //TODO: Need to add 2 markers to the map to indicate one where the task will be held two where to pass the stuff to the tasker to unless the the tasker task is only one way then dont add second marker and no location will be added to the map :-)

        errandName = v.findViewById(R.id.errand_name);
        errandDescription = v.findViewById(R.id.errand_description);
        imageShown = v.findViewById(R.id.errand_imageshown);
        publishTask = v.findViewById(R.id.errand_publish);
        takePicture = v.findViewById(R.id.errand_picturebutton);
        mapView = v.findViewById(R.id.errand_map);

        gpsTracker = new GPSTracker(getContext());

        //Get Firebase firestore reference
        db = FirebaseFirestore.getInstance();

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Get Firebase current user
        user = auth.getCurrentUser();


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

        myLatitude = gpsTracker.getLatitude();
        myLongitude = gpsTracker.getLongitude();

        intent.putExtra("MyLat", myLatitude);
        intent.putExtra("MyLng", myLongitude);



        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {

            if (requestCode == REQUEST_CAMERA) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                imageShown.setImageBitmap(imageBitmap);
                //cameraImageUri = getImageUri(getActivity(), imageBitmap);
                cameraImageUri = getImageUri(getContext(), imageBitmap);

            } else if (requestCode == SELECT_FILE) {
                selectImageUri = data.getData();
                imageShown.setImageURI(selectImageUri);

            }

        }

    }

    public static Uri getImageUri(Context context, Bitmap bm) {
        tempFile = new File(context.getExternalCacheDir(), "image.png");

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        byte[] bitmapData = bytes.toByteArray();

        //write the bytes in file
        try {
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (Exception e) {

        }

        Uri uriImage = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                tempFile);

        return uriImage;
    }

   /* public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver()  , inImage, "Title", null);
        return Uri.parse(path);
    }*/

    private void takeOrSelect() {

        final CharSequence[] items = {"Take a picture", "Choose from gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (items[i].equals("Take a picture")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);

                } else if (items[i].equals("Choose from gallery")) {
                    Intent intent = new Intent();
                    //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    //startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);

                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, SELECT_FILE);

                } else if (items[i].equals("Cancel")) {
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


        if (selectImageUri != null) {
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
            errandImageRef = storageRef.child(docId + "." + getFileExtension(cameraImageUri));

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
        final String errandAddName = errandName.getText().toString().trim();
        final String errandAddDescription = errandDescription.getText().toString().trim();

        final String uid = auth.getUid();

            prefs_start = this.getActivity().getSharedPreferences("LatLng_start", MODE_PRIVATE);
            prefs_end = this.getActivity().getSharedPreferences("LatLng_end", MODE_PRIVATE);

            lat_start = Double.parseDouble(prefs_start.getString("Lat_start", "0"));
            lng_start = Double.parseDouble(prefs_start.getString("Lng_start", "0"));

            lat_end = Double.parseDouble(prefs_end.getString("Lat_end", "0"));
            lng_end = Double.parseDouble(prefs_end.getString("Lng_end", "0"));


            GeoPoint gpstart = new GeoPoint(lat_start, lng_start);
            GeoPoint gpend = new GeoPoint(lat_end, lng_end);


            Errands errands = new Errands(errandAddName, errandAddDescription, uid, taskFinished, gpstart, gpend);

            db.collection("Errands").add(errands).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Toast.makeText(getActivity(), "Errand Successfully added", Toast.LENGTH_SHORT).show();
                    docId = documentReference.getId();
                    Toast.makeText(getActivity(), docId, Toast.LENGTH_SHORT).show();
                    uploadImageToFireBaseStorage();
                    clearField();

                }
            });


       /* // Create new fragment and transaction
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

    private void clearField() {
        errandName.getText().clear();
        errandDescription.getText().clear();
        Picasso.get().load(R.drawable.test_3).into(imageShown);
        //TODO: CLEAR THE MARKERS PLACE
    }

}
