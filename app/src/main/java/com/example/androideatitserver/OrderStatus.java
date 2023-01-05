package com.example.androideatitserver;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Model.Request;
import com.example.androideatitserver.databinding.ActivityOrderStatusBinding;
import com.example.androideatitserver.databinding.OrderLayoutBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {
    ActivityOrderStatusBinding binding;
    OrderLayoutBinding orderLayoutBinding;

    FirebaseDatabase db;
    DatabaseReference request;
    MaterialSpinner spinner;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;
    RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //setup database
        db = FirebaseDatabase.getInstance();
        request = db.getReference("Request");

        binding.listOrders.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        binding.listOrders.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(request, Request.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {


                orderLayoutBinding.orderId.setText(adapter.getRef(position).getKey());
                orderLayoutBinding.orderStatus.setText(convertCodeToStatus(model.getStatus()));
                orderLayoutBinding.orderAddress.setText(model.getAddress());
                orderLayoutBinding.orderPhone.setText(model.getPhone());


            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                orderLayoutBinding = OrderLayoutBinding.inflate(inflater,parent,false);

                return new OrderViewHolder(orderLayoutBinding);
            }
        };
        binding.listOrders.setAdapter(adapter);

    }
    public static class OrderViewHolder extends RecyclerView.ViewHolder{
        public OrderViewHolder(@NonNull OrderLayoutBinding itemView) {
            super(itemView.getRoot());
        }
    }
    private String convertCodeToStatus(String status){
        if (status.equals("0")){
            return "Placed";
        }else if (status.equals("1")){
            return "On my way";
        }else{
            return "Shipped";
        }
    }
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)){
            showUpdateOrder(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }else if (item.getTitle().equals(Common.DELETE)){
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteOrder(String key) {
        request.child(key).removeValue();
    }

    private void showUpdateOrder(String key, Request item) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.update_order_layout,null);

        spinner = view.findViewById(R.id.statusSpinner);
        spinner.setItems("Place","On my way","Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("Yes", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            item.setStatus(String.valueOf(spinner.getSelectedIndex()));

            request.child(localKey).setValue(item);
        });
        alertDialog.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());

        alertDialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter!=null){
            adapter.stopListening();

        }
    }
}