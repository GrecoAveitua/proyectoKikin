package com.example.karatecompetitionmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.models.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFolio, tvBelt, tvAgeRange, tvType;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolio = itemView.findViewById(R.id.tv_category_folio);
            tvBelt = itemView.findViewById(R.id.tv_category_belt);
            tvAgeRange = itemView.findViewById(R.id.tv_category_age_range);
            tvType = itemView.findViewById(R.id.tv_category_type);
        }

        public void bind(Category category) {
            tvFolio.setText("Folio: " + category.getFolio());
            tvBelt.setText("Cinturón: " + category.getBelt());
            tvAgeRange.setText("Edad: " + category.getMinAge() + " - " + category.getMaxAge());

            String type = "";
            if (category.getType().equals("kata")) type = "Kata";
            else if (category.getType().equals("kumite")) type = "Kumite";
            else type = "Kata y Kumite";

            tvType.setText("Tipo: " + type);
        }
    }
}