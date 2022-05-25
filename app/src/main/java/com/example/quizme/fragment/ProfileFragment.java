package com.example.quizme.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.quizme.LoginActivity;
import com.example.quizme.MainActivity;
import com.example.quizme.R;
import com.example.quizme.databinding.FragmentProfileBinding;
import com.example.quizme.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseFirestore database;
    FirebaseStorage storage;
    User user;
    Uri seclectedImage;
    ProgressDialog dialog;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);

        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        String uId = FirebaseAuth.getInstance().getUid();

        dialog = new ProgressDialog(getContext());
        dialog.setMessage("Updating profile...");
        dialog.setCancelable(false);


        database.collection("users")
                .document(uId)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);

                binding.emailBox.setText(user.getEmail());
                binding.nameBox.setText(user.getName());
                binding.passBox.setText(user.getPass());

                Glide.with(getContext())
                        .load(user.getProfile())
                        .into(binding.profileImage);
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                if(seclectedImage != null) {
                    StorageReference reference = storage.getReference().child("profiles").child(uId);

                    reference.putFile(seclectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()) {
                                reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String imageUrl = uri.toString();
                                        String name = binding.nameBox.getText().toString();
                                        String pass = binding.passBox.getText().toString();
                                        Map<String,Object> map = new HashMap<>();
                                        map.put("name", name);
                                        map.put("pass", pass);
                                        map.put("profile", imageUrl);
                                        database.collection("users")
                                                .document(uId)
                                                .update(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), "Update successfully.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                            }
                            else {
                                dialog.dismiss();
                                Toast.makeText(getContext(), "Thất bại", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        binding.profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 45);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data != null) {
            if(data.getData() != null) {
                binding.profileImage.setImageURI(data.getData());
                seclectedImage = data.getData();
            }
        }
    }
}