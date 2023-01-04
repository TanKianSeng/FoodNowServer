package com.example.androideatitserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Model.Category;
import com.example.androideatitserver.Model.Food;
import com.example.androideatitserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {
    RelativeLayout rootLayout;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    StorageReference storageReference;
    FirebaseStorage storage;





    String categoryId = "";
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    Button btnSelect,btnUpload;
    EditText editTextName,editTextDescription,editTextPrice,editTextDiscount;
    Intent intent;
    Uri saveUri;
    Food newFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        //init Firebase
        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("/images/");

        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        rootLayout = findViewById(R.id.rootLayout);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddFoodDialog());
        //get intent here
        if(getIntent()!=null){
            categoryId = getIntent().getStringExtra("categoryId");
        }if(categoryId != null && !categoryId.isEmpty()){
            loadListFood(categoryId);
        }

    }

    private void showAddFoodDialog() {
        AlertDialog.Builder foodDialog = new AlertDialog.Builder(FoodList.this);
        foodDialog.setTitle("Add new Food");
        foodDialog.setMessage("Please fill up full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_food_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        btnSelect = add_food_layout.findViewById(R.id.btnSelect);
        btnUpload = add_food_layout.findViewById(R.id.btnUpload);
        editTextName = add_food_layout.findViewById(R.id.editTextName);
        editTextDescription = add_food_layout.findViewById(R.id.editTextDescription);
        editTextDiscount = add_food_layout.findViewById(R.id.editTextDiscount);
        editTextPrice = add_food_layout.findViewById(R.id.editTextPrice);



        btnSelect.setOnClickListener(view -> chooseImage());
        btnUpload.setOnClickListener(view -> uploadImage());

        foodDialog.setView(add_food_layout);
        foodDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if (newFood != null){
                foodList.push().setValue(newFood);
                Snackbar.make(rootLayout,"New Food "+ newFood.getName()+" was added",Snackbar.LENGTH_SHORT).show();
            }
        });
        foodDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        foodDialog.show();







    }

    private void loadListFood(String categoryId) {
        // represent as "Select * From Food where menuId = String"
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("menuId").equalTo(categoryId), Food.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder viewHolder, int position, @NonNull Food model) {

                viewHolder.food_name.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .fit().into(viewHolder.food_image);

                //final Food clickItem = model;
                viewHolder.setItemClickListener((view, position1, isLongClick) -> {
                    //start new activity
                    //Intent intent = new Intent(FoodList.this, FoodDetail.class);
                    //intent.putExtra("foodId",adapter.getRef(position).getKey());
                    //startActivity(intent);
                });
            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.food_item, parent, false);
                return new FoodViewHolder(itemView);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }
    private void uploadImage() {
        if (saveUri != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        mDialog.dismiss();
                        Toast.makeText(FoodList.this,"Uploaded completed.",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {

                            newFood = new Food();
                            newFood.setName(editTextName.getText().toString());
                            newFood.setDescription(editTextDescription.getText().toString());
                            newFood.setPrice(editTextPrice.getText().toString());
                            newFood.setDiscount(editTextDiscount.getText().toString());
                            newFood.setMenuId(categoryId);
                            newFood.setImage(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(FoodList.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        mDialog.setMessage("Uploaded " +progress+ "%");
                    });
        }
    }

    ActivityResultLauncher<Intent> chooseImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    saveUri = data.getData();
                    btnSelect.setText("Image Selected!");
                }
            });

    private void chooseImage() {

        intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        chooseImageActivityResultLauncher.launch(intent);

    }

    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if (item.getTitle().equals(Common.DELETE)){
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Delete Category");
        alertDialog.setMessage("Do you confirm to delete the selected category?");

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            //Delete Category
            foodList.child(key).removeValue();
            Toast.makeText(FoodList.this,"Delete completed.",Toast.LENGTH_SHORT).show();
        });
        alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();


    }

    private void showUpdateDialog(String key, Food item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(FoodList.this);
        alertDialog.setTitle("Update Food");
        alertDialog.setMessage("Please fill up full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_food_layout = inflater.inflate(R.layout.add_new_food_layout,null);

        btnSelect = add_food_layout.findViewById(R.id.btnSelect);
        btnUpload = add_food_layout.findViewById(R.id.btnUpload);

        editTextName = add_food_layout.findViewById(R.id.editTextName);
        editTextDescription = add_food_layout.findViewById(R.id.editTextDescription);
        editTextDiscount = add_food_layout.findViewById(R.id.editTextDiscount);
        editTextPrice = add_food_layout.findViewById(R.id.editTextPrice);

        editTextName.setText(item.getName());
        editTextDescription.setText(item.getDescription());
        editTextPrice.setText(item.getPrice());
        editTextDiscount.setText(item.getDiscount());


        alertDialog.setView(add_food_layout);

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            //Update information
            item.setName(editTextName.getText().toString());
            item.setDescription(editTextDescription.getText().toString());
            item.setPrice(editTextPrice.getText().toString());
            item.setDiscount(editTextDiscount.getText().toString());
            foodList.child(key).setValue(item);
        });
        alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void changeImage(Food item) {
        if (saveUri != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        mDialog.dismiss();
                        Toast.makeText(FoodList.this,"Uploaded completed.",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(uri ->
                                item.setImage(uri.toString())
                        );
                    })
                    .addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(FoodList.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        mDialog.setMessage("Uploaded " +progress+ "%");
                    });
        }
    }
}