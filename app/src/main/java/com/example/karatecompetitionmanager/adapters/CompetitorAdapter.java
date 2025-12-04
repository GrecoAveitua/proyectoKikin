package com.example.karatecompetitionmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompetitorAdapter extends RecyclerView.Adapter<CompetitorAdapter.ViewHolder> {

    private List<Competitor> competitors;
    // Map para almacenar imágenes por cinturón
    private Map<String, Integer> beltImages;

    public CompetitorAdapter(List<Competitor> competitors) {
        this.competitors = competitors;
        initializeBeltImages();
    }

    private void initializeBeltImages() {
        beltImages = new HashMap<>();
        beltImages.put("Blanco", R.drawable.ic_belt_white);
        beltImages.put("Amarillo", R.drawable.ic_belt_yellow);
        beltImages.put("Naranja", R.drawable.ic_belt_orange);
        beltImages.put("Verde", R.drawable.ic_belt_green);
        beltImages.put("Azul", R.drawable.ic_belt_blue);
        beltImages.put("Marron", R.drawable.ic_belt_brown);
        beltImages.put("Negro", R.drawable.ic_belt_black);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_competitor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Competitor competitor = competitors.get(position);
        holder.bind(competitor, beltImages);
    }

    @Override
    public int getItemCount() {
        return competitors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivBeltIcon;
        private TextView tvFolio, tvName, tvDojo, tvAge, tvBelt, tvParticipation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBeltIcon = itemView.findViewById(R.id.iv_belt_icon);
            tvFolio = itemView.findViewById(R.id.tv_competitor_folio);
            tvName = itemView.findViewById(R.id.tv_competitor_name);
            tvDojo = itemView.findViewById(R.id.tv_competitor_dojo);
            tvAge = itemView.findViewById(R.id.tv_competitor_age);
            tvBelt = itemView.findViewById(R.id.tv_competitor_belt);
            tvParticipation = itemView.findViewById(R.id.tv_competitor_participation);
        }

        public void bind(Competitor competitor, Map<String, Integer> beltImages) {
            tvFolio.setText("Folio: " + competitor.getFolio());
            tvName.setText("Nombre: " + competitor.getName());
            tvDojo.setText("Dojo: " + competitor.getDojo());
            tvAge.setText("Edad: " + competitor.getAge());
            tvBelt.setText("Cinturón: " + competitor.getBelt());

            // Establecer imagen del cinturón
            Integer imageRes = beltImages.get(competitor.getBelt());
            if (imageRes != null) {
                ivBeltIcon.setImageResource(imageRes);
            } else {
                ivBeltIcon.setImageResource(R.drawable.ic_belt_white);
            }

            String participation = "";
            if (competitor.isParticipateKata() && competitor.isParticipateKumite()) {
                participation = "Kata y Kumite";
            } else if (competitor.isParticipateKata()) {
                participation = "Kata";
            } else if (competitor.isParticipateKumite()) {
                participation = "Kumite";
            }
            tvParticipation.setText("Participa en: " + participation);
        }
    }
}