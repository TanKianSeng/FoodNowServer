package com.example.androideatitserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Model.User;
import com.example.androideatitserver.databinding.ActivityMainBinding;
import com.example.androideatitserver.databinding.ActivitySignInBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class SignIn extends AppCompatActivity {
    ActivitySignInBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //init firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference table_user = database.getReference("User");

        binding.btnSignIn.setOnClickListener(v -> {

            ProgressDialog mDialog = new ProgressDialog(SignIn.this);
            mDialog.setMessage("Please waiting...");
            mDialog.show();

            table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    mDialog.dismiss();

                    if(binding.editTextPhoneNo.getText().toString().isEmpty() || binding.editTextPassword.getText().toString().isEmpty()){
                        Toast.makeText(SignIn.this, "Please type in correctly.", Toast.LENGTH_SHORT).show();
                    }else{
                        if (snapshot.child(binding.editTextPhoneNo.getText().toString()).exists()){
                            //user account exist
                            mDialog.dismiss();
                            User user = snapshot.child(binding.editTextPhoneNo.getText().toString()).getValue(User.class);
                            user.setPhone(binding.editTextPhoneNo.getText().toString());

                            if (Objects.requireNonNull(user).getPassword().equals(binding.editTextPassword.getText().toString())){
                                //if user password correct
                                if(Objects.requireNonNull(user).getIsStaff().toString().equals("true")){
                                    Toast.makeText(SignIn.this, "Sign in successfully. Welcome Admin.", Toast.LENGTH_SHORT).show();
                                    binding.editTextPhoneNo.setText(null);
                                    binding.editTextPassword.setText(null);

                                    Intent intent = new Intent(SignIn.this,Home.class);
                                    Common.User_current = user;
                                    startActivity(intent);
                                    finish();
                                }else{
                                    Toast.makeText(SignIn.this, "Only Admin is allow to access this platform.", Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                //if user password incorrect
                                Toast.makeText(SignIn.this, "Password incorrect.", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            //user account not existed
                            Toast.makeText(SignIn.this, "User does not exist in database.", Toast.LENGTH_SHORT).show();
                        }
                    }



                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });
    }
}