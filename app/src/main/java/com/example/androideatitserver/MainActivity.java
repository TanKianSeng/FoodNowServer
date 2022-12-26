package com.example.androideatitserver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import com.example.androideatitserver.Model.Category;
import com.example.androideatitserver.ViewHolder.MenuViewHolder;

import com.example.androideatitserver.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this,SignIn.class);
            startActivity(intent);
        });
    }
}