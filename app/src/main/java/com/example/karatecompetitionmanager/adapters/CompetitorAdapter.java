package com.example.karatecompetitionmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.List;

public class CompetitorAdapter extends RecyclerView.Adapter<CompetitorAdapter.ViewHolder> {

    private List<Competitor> competitors;

    public CompetitorAdapter(List<Competitor> competitors) {
        this.competitors = competitors;
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
        holder.bind(competitor);
    }

    @Override
    public int getItemCount() {
        return competitors.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvFolio, tvName, tvDojo, tvAge, tvBelt, tvParticipation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFolio = itemView.findViewById(R.id.tv_competitor_folio);
            tvName = itemView.findViewById(R.id.tv_competitor_name);
            tvDojo = itemView.findViewById(R.id.tv_competitor_dojo);
            tvAge = itemView.findViewById(R.id.tv_competitor_age);
            tvBelt = itemView.findViewById(R.id.tv_competitor_belt);
            tvParticipation = itemView.findViewById(R.id.tv_competitor_participation);
        }

        public void bind(Competitor competitor) {
            tvFolio.setText("Folio: " + competitor.getFolio());
            tvName.setText("Nombre: " + competitor.getName());
            tvDojo.setText("Dojo: " + competitor.getDojo());
            tvAge.setText("Edad: " + competitor.getAge());
            tvBelt.setText("Cinturón: " + competitor.getBelt());

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