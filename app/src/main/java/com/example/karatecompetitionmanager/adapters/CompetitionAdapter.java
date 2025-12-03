package com.example.karatecompetitionmanager.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Category;
import com.example.karatecompetitionmanager.models.Competition;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.List;

public class CompetitionAdapter extends RecyclerView.Adapter<CompetitionAdapter.ViewHolder> {

    private List<Competition> competitions;
    private DatabaseHelper dbHelper;
    private OnCompetitionClickListener listener;

    // Interface para manejar clics
    public interface OnCompetitionClickListener {
        void onCompetitionClick(Competition competition);
    }

    // Constructor sin listener (compatibilidad)
    public CompetitionAdapter(List<Competition> competitions, DatabaseHelper dbHelper) {
        this.competitions = competitions;
        this.dbHelper = dbHelper;
        this.listener = null;
    }

    // Constructor con listener
    public CompetitionAdapter(List<Competition> competitions, DatabaseHelper dbHelper,
                              OnCompetitionClickListener listener) {
        this.competitions = competitions;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_competition, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Competition competition = competitions.get(position);
        holder.bind(competition, dbHelper, listener);
    }

    @Override
    public int getItemCount() {
        return competitions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryInfo;
        private TextView tvDate;
        private TextView tvFirstPlace;
        private TextView tvSecondPlace;
        private TextView tvThirdPlace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryInfo = itemView.findViewById(R.id.tv_competition_category);
            tvDate = itemView.findViewById(R.id.tv_competition_date);
            tvFirstPlace = itemView.findViewById(R.id.tv_first_place);
            tvSecondPlace = itemView.findViewById(R.id.tv_second_place);
            tvThirdPlace = itemView.findViewById(R.id.tv_third_place);
        }

        public void bind(Competition competition, DatabaseHelper dbHelper,
                         OnCompetitionClickListener listener) {

            // Obtener información de la categoría
            Category category = dbHelper.getCategory(competition.getCategoryFolio());
            if (category != null) {
                String type = category.getType().equals("kata") ? "Kata" :
                        (category.getType().equals("kumite") ? "Kumite" : "Kata y Kumite");

                String categoryInfo = "Categoría: " + category.getBelt() +
                        " (" + category.getMinAge() + "-" + category.getMaxAge() + " años) - " + type;
                tvCategoryInfo.setText(categoryInfo);
            } else {
                tvCategoryInfo.setText("Categoría: " + competition.getCategoryFolio());
            }

            // Formatear fecha
            String dateFormatted = formatDate(competition.getDate());
            tvDate.setText("Fecha: " + dateFormatted);

            // Obtener nombres de los competidores - Primer lugar
            if (competition.getFirstPlace() != null && !competition.getFirstPlace().isEmpty()) {
                Competitor first = dbHelper.getCompetitor(competition.getFirstPlace());
                String firstName = first != null ? first.getName() : competition.getFirstPlace();
                tvFirstPlace.setText("🥇 1er Lugar: " + firstName);
                tvFirstPlace.setVisibility(View.VISIBLE);
            } else {
                tvFirstPlace.setText("🥇 1er Lugar: N/A");
                tvFirstPlace.setVisibility(View.VISIBLE);
            }

            // Segundo lugar
            if (competition.getSecondPlace() != null && !competition.getSecondPlace().isEmpty()) {
                Competitor second = dbHelper.getCompetitor(competition.getSecondPlace());
                String secondName = second != null ? second.getName() : competition.getSecondPlace();
                tvSecondPlace.setText("🥈 2do Lugar: " + secondName);
                tvSecondPlace.setVisibility(View.VISIBLE);
            } else {
                tvSecondPlace.setText("🥈 2do Lugar: N/A");
                tvSecondPlace.setVisibility(View.VISIBLE);
            }

            // Tercer lugar
            if (competition.getThirdPlace() != null && !competition.getThirdPlace().isEmpty()) {
                Competitor third = dbHelper.getCompetitor(competition.getThirdPlace());
                String thirdName = third != null ? third.getName() : competition.getThirdPlace();
                tvThirdPlace.setText("🥉 3er Lugar: " + thirdName);
                tvThirdPlace.setVisibility(View.VISIBLE);
            } else {
                tvThirdPlace.setText("🥉 3er Lugar: N/A");
                tvThirdPlace.setVisibility(View.VISIBLE);
            }

            // Configurar click listener
            if (listener != null) {
                itemView.setOnClickListener(v -> listener.onCompetitionClick(competition));
            }
        }

        private String formatDate(String date) {
            // Formato: "2024-01-15 14:30:00"
            // Convertir a: "15 Ene 2024, 14:30"
            try {
                String[] parts = date.split(" ");
                if (parts.length >= 2) {
                    String[] dateParts = parts[0].split("-");
                    String[] timeParts = parts[1].split(":");

                    if (dateParts.length >= 3 && timeParts.length >= 2) {
                        String year = dateParts[0];
                        String month = getMonthName(dateParts[1]);
                        String day = dateParts[2];
                        String time = timeParts[0] + ":" + timeParts[1];

                        return day + " " + month + " " + year + ", " + time;
                    }
                }
            } catch (Exception e) {
                // Si hay error, retornar fecha original
            }
            return date;
        }

        private String getMonthName(String month) {
            switch (month) {
                case "01": return "Ene";
                case "02": return "Feb";
                case "03": return "Mar";
                case "04": return "Abr";
                case "05": return "May";
                case "06": return "Jun";
                case "07": return "Jul";
                case "08": return "Ago";
                case "09": return "Sep";
                case "10": return "Oct";
                case "11": return "Nov";
                case "12": return "Dic";
                default: return month;
            }
        }
    }
}