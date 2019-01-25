package com.sp.gravitask;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    //private static final int CHOOSE_IMAGE = 101;
    CircleImageView circleImageView;
    EditText editName, editEmail;
    Button updateProfile;
    Uri uriProfileImage;
    ProgressBar progressBar;
    FirebaseAuth auth;
    FirebaseUser user;
    private StorageReference profileImageRef, storageRef;

    String profileImageUrl;
    FirebaseFirestore db;

    /*private int request_code = 1;
    private Bitmap bitmap_foto;
    private byte[] bytes;*/

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        //Get Firebase current user
        user = auth.getCurrentUser();

        //Get Firestore Database reference
        db = FirebaseFirestore.getInstance();


        circleImageView = v.findViewById(R.id.profile_image);
        editName = v.findViewById(R.id.profileName);
        editEmail = v.findViewById(R.id.email);
        updateProfile = v.findViewById(R.id.button_updateProfile);
        progressBar = v.findViewById(R.id.progressbar);

        displayUserInfo();

        circleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImageChoose();

            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmailOnAuth();
                displayImage();
            }
        });

        return v;
    }


    private void changeEmailOnAuth() {
        String uid = auth.getUid();
        final String email_updated = editEmail.getText().toString().trim();

        db.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String email = task.getResult().getString("Email");
                    String password = task.getResult().getString("Password");
                    // Get auth credentials from the user for re-authentication
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(email, password); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getActivity(), "User re-authenticated", Toast.LENGTH_SHORT).show();
                                    //Now change your email address \\
                                    //----------------Code for Changing Email Address----------\\
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.updateEmail(email_updated)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                       Toast.makeText(getActivity(), "Email Finally updated", Toast.LENGTH_SHORT).show();
                                                        saveUserInformation();
                                                    }
                                                }
                                            });
                                    //----------------------------------------------------------\\
                                }
                            });



                }
            }
        });
    }

    private void saveUserInformation() { //TODO: FIND THE PROBLEM USE THE GETTER AND SETTER SOMEHOW
        String email = editEmail.getText().toString().trim();
        String profileName = editName.getText().toString().trim();
        String Uid = auth.getUid();
        Map<String, Object> User = new HashMap<>();
        User.put("Name", profileName);
        User.put("Email", email);

        db.collection("Users").document(Uid).set(User, SetOptions.merge());
        //db.collection("Users").document(Uid).update("Name"); //TODO: TAKE NOTE UPDATE FOR SINGLE FIELD


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == CHOOSE_IMAGE && requestCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uriProfileImage);
                circleImageView.setImageBitmap(bitmap);
                uploadImageToFireBaseStorage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

       /* if (resultCode == RESULT_OK && requestCode == request_code) {
            uriProfileImage = data.getData();
            circleImageView.setImageURI(uriProfileImage);
           // bytes = imageToByte(circleImageView);
            uploadImageToFireBaseStorage(); //TODO: Merge with show choose image


        }*/

       if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data !=null && data.getData() !=null ) {
           uriProfileImage = data.getData();

           circleImageView.setImageURI(uriProfileImage);
           uploadImageToFireBaseStorage();
       }
    }

    private void uploadImageToFireBaseStorage() {
        storageRef = FirebaseStorage.getInstance().getReference("ProfileImage/");
        final String uid = auth.getUid();


        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef = storageRef.child(uid + "." + getFileExtension(uriProfileImage));

            profileImageRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    //String profileImageUrl = profileImageRef.getDownloadUrl().toString();
                    profileImageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            String downloadUrl = task.getResult().toString();
                            Map<String, Object> User = new HashMap<>();
                            User.put("ProfileImage", downloadUrl);
                            db.collection("Users").document(uid).set(User, SetOptions.merge());
                        }
                    });
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void displayUserInfo(){
        String uid = auth.getUid();

        db.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    editName.setText(task.getResult().getString("Name"));
                    editEmail.setText(task.getResult().getString("Email"));



                }
            }
        });
    }

    private void showImageChoose() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);

    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void displayImage() {
        String uid = auth.getUid();

        db.collection("Users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    String imageurl = task.getResult().getString("ProfileImage");
                    Picasso.get().load(imageurl).into(circleImageView);

                }
            }
        });
    }

}

