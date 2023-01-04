package com.example.androideatitserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Model.Category;
import com.example.androideatitserver.ViewHolder.MenuViewHolder;
import com.example.androideatitserver.databinding.AddNewMenuLayoutBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatitserver.databinding.ActivityHomeBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityHomeBinding binding;

    //init firebase
    FirebaseDatabase database;
    DatabaseReference category;
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    TextView txtFullName;
    Category newCategory;

    //View
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    Uri saveUri;
    Intent intent;
    //private final int PICK_IMAGE_REQUEST = 71;

    Button btnSelect,btnUpload;
    EditText editTextName;
    DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference("/images/");





        setSupportActionBar(binding.appBarHome.toolbar);
        binding.appBarHome.fab.setOnClickListener(view ->

                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show());
        showDialog());
        drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.bringToFront();

        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_home);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            // Setup NavigationUI here
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.bringToFront();
        }

        //Set Name for user
        View headView = navigationView.getHeaderView(0);
        txtFullName = headView.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.User_current.getName());

        //Init View
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        
        loadMenu();
    }

    private void showDialog() {
        AlertDialog.Builder categoryDialog = new AlertDialog.Builder(Home.this);
        categoryDialog.setTitle("Add new Category");
        categoryDialog.setMessage("Please fill up full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        editTextName = add_menu_layout.findViewById(R.id.editTextName);



        btnSelect.setOnClickListener(view -> chooseImage());
        btnUpload.setOnClickListener(view -> uploadImage());
        
        categoryDialog.setView(add_menu_layout);
        categoryDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            if (newCategory != null){
                category.push().setValue(newCategory);
                Snackbar.make(drawer,"New Category "+newCategory.getName()+" was added",Snackbar.LENGTH_SHORT).show();
            }
        });
        categoryDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        categoryDialog.show();

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
                        Toast.makeText(Home.this,"Uploaded completed.",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                            newCategory = new Category(editTextName.getText().toString(), uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
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

        //startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST);


        chooseImageActivityResultLauncher.launch(intent);

    }

    private void loadMenu() {
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>().setQuery(category,Category.class).build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options){

            @Override
            public void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category category) {
                viewHolder.menu_name.setText(category.getName());
                Picasso.with(getBaseContext()).load(category.getImage()).fit().into(viewHolder.menu_image);

                viewHolder.setItemClickListener((view, position1, isLongClick) -> {

                    //get Category ID and sent it to new Activity
                    //Toast.makeText(Home.this, adapter.getRef(position).getKey(), Toast.LENGTH_SHORT).show();//----test code for onClick functionality
                    Intent intent = new Intent(Home.this,FoodList.class);
                    //Because Category ID is key, so we need to get key from this item
                    intent.putExtra("categoryId",adapter.getRef(position).getKey());
                    startActivity(intent);


                });
            }
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_home);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if (item.getTitle().equals(Common.DELETE)){
            deleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteCategory(String key) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Delete Category");
        alertDialog.setMessage("Do you confirm to delete the selected category?");

        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            //Delete Category
            category.child(key).removeValue();
            Toast.makeText(Home.this,"Delete completed.",Toast.LENGTH_SHORT).show();
        });
        alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();


    }

    private void showUpdateDialog(String key, Category item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Update Category");
        alertDialog.setMessage("Please fill up full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_menu_layout,null);

        btnSelect = add_menu_layout.findViewById(R.id.btnSelect);
        btnUpload = add_menu_layout.findViewById(R.id.btnUpload);
        editTextName = add_menu_layout.findViewById(R.id.editTextName);

        editTextName.setText(item.getName());



        btnSelect.setOnClickListener(view -> chooseImage());
        btnUpload.setOnClickListener(view -> changeImage(item));

        alertDialog.setView(add_menu_layout);
        alertDialog.setIcon(R.drawable.ic_baseline_shopping_cart_24);




        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            //Update information
            item.setName(editTextName.getText().toString());
            category.child(key).setValue(item);
        });
        alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
        alertDialog.show();
    }

    private void changeImage(Category item) {
        if (saveUri != null){
            ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            StorageReference imageFolder = storageReference.child("images/"+imageName);

            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        mDialog.dismiss();
                        Toast.makeText(Home.this,"Uploaded completed.",Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(uri ->
                            item.setImage(uri.toString())
                        );
                    })
                    .addOnFailureListener(e -> {
                        mDialog.dismiss();
                        Toast.makeText(Home.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        mDialog.setMessage("Uploaded " +progress+ "%");
                    });
        }
    }
}