package com.example.quizme.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.quizme.QuizActivity;
import com.example.quizme.R;
import com.example.quizme.model.CategoryModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>{
    Context context;
    ArrayList<CategoryModel> categoryModels;

    static int DEDUCTION_COINS_10 = -10;

    public CategoryAdapter(Context context, ArrayList<CategoryModel> categoryModels){
        this.context = context;
        this.categoryModels = categoryModels;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, null);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryModel model = categoryModels.get(position);

        holder.textView.setText(model.getCategoryName());

        Glide.with(context)
                .load(model.getCategoryImage())
                .into(holder.imageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Trừ tiền khi vào trận
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection("users")
                        .document(FirebaseAuth.getInstance().getUid())
                        .update("coins", FieldValue.increment(CategoryAdapter.DEDUCTION_COINS_10));

                Toast.makeText(context, "Bạn bị trừ "+CategoryAdapter.DEDUCTION_COINS_10+ "coins", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(context, QuizActivity.class);
                intent.putExtra("catId", model.getCategoryId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryModels.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            textView = itemView.findViewById(R.id.category);
        }
    }
}
