package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.adapters.CompetitorAdapter;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Category;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoryCompetitorsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private EditText etCategoryFolio;
    private Button btnSearchCategory, btnAddCompetitor, btnRemoveCompetitor, btnAutoAssign;
    private TextView tvCategoryInfo, tvCompetitorCount;
    private RecyclerView recyclerView;
    private CompetitorAdapter adapter;

    private Category currentCategory;
    private List<Competitor> categoryCompetitors;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage_category_competitors, container, false);

        dbHelper = new DatabaseHelper(getContext());

        etCategoryFolio = view.findViewById(R.id.et_category_folio);
        btnSearchCategory = view.findViewById(R.id.btn_search_category);
        btnAddCompetitor = view.findViewById(R.id.btn_add_competitor_to_category);
        btnRemoveCompetitor = view.findViewById(R.id.btn_remove_competitor_from_category);
        btnAutoAssign = view.findViewById(R.id.btn_auto_assign_competitors);
        tvCategoryInfo = view.findViewById(R.id.tv_category_info);
        tvCompetitorCount = view.findViewById(R.id.tv_competitor_count);
        recyclerView = view.findViewById(R.id.recycler_category_competitors);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryCompetitors = new ArrayList<>();

        btnSearchCategory.setOnClickListener(v -> searchCategory());
        btnAddCompetitor.setOnClickListener(v -> showAddCompetitorDialog());
        btnRemoveCompetitor.setOnClickListener(v -> showRemoveCompetitorDialog());
        btnAutoAssign.setOnClickListener(v -> autoAssignCompetitors());

        // Inicialmente ocultar botones hasta que se busque una categoría
        btnAddCompetitor.setEnabled(false);
        btnRemoveCompetitor.setEnabled(false);
        btnAutoAssign.setEnabled(false);

        return view;
    }

    private void searchCategory() {
        String folio = etCategoryFolio.getText().toString().trim();

        if (folio.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese el folio de la categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        currentCategory = dbHelper.getCategory(folio);

        if (currentCategory == null) {
            Toast.makeText(getContext(), "Categoría no encontrada",
                    Toast.LENGTH_SHORT).show();
            tvCategoryInfo.setVisibility(View.GONE);
            tvCompetitorCount.setVisibility(View.GONE);
            btnAddCompetitor.setEnabled(false);
            btnRemoveCompetitor.setEnabled(false);
            btnAutoAssign.setEnabled(false);
            return;
        }

        // Mostrar información de la categoría
        displayCategoryInfo();

        // Cargar competidores de la categoría
        loadCategoryCompetitors();

        // Habilitar botones
        btnAddCompetitor.setEnabled(true);
        btnRemoveCompetitor.setEnabled(true);
        btnAutoAssign.setEnabled(true);
    }

    private void displayCategoryInfo() {
        String type = currentCategory.getType().equals("kata") ? "Kata" :
                (currentCategory.getType().equals("kumite") ? "Kumite" : "Kata y Kumite");

        String info = "📋 Categoría: " + currentCategory.getFolio() + "\n" +
                "🥋 Cinturón: " + currentCategory.getBelt() + "\n" +
                "👥 Edad: " + currentCategory.getMinAge() + "-" + currentCategory.getMaxAge() + " años\n" +
                "🎯 Tipo: " + type;

        tvCategoryInfo.setText(info);
        tvCategoryInfo.setVisibility(View.VISIBLE);
    }

    private void loadCategoryCompetitors() {
        categoryCompetitors = dbHelper.getCompetitorsByCategory(currentCategory.getFolio());

        if (categoryCompetitors.isEmpty()) {
            tvCompetitorCount.setText("No hay competidores en esta categoría");
            tvCompetitorCount.setVisibility(View.VISIBLE);
        } else {
            tvCompetitorCount.setText("Total de competidores: " + categoryCompetitors.size());
            tvCompetitorCount.setVisibility(View.VISIBLE);
        }

        adapter = new CompetitorAdapter(categoryCompetitors);
        recyclerView.setAdapter(adapter);
    }

    private void showAddCompetitorDialog() {
        if (currentCategory == null) return;

        // Obtener competidores elegibles que NO estén ya en la categoría
        List<Competitor> eligibleCompetitors = dbHelper.getEligibleCompetitors(currentCategory);

        // Filtrar los que ya están en la categoría
        List<Competitor> availableCompetitors = new ArrayList<>();
        for (Competitor comp : eligibleCompetitors) {
            if (!dbHelper.isCompetitorInCategory(currentCategory.getFolio(), comp.getFolio())) {
                availableCompetitors.add(comp);
            }
        }

        if (availableCompetitors.isEmpty()) {
            Toast.makeText(getContext(),
                    "No hay competidores disponibles para esta categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear array de nombres para el diálogo
        String[] competitorNames = new String[availableCompetitors.size()];
        for (int i = 0; i < availableCompetitors.size(); i++) {
            Competitor c = availableCompetitors.get(i);
            competitorNames[i] = c.getName() + " (" + c.getFolio() + ")";
        }

        // Mostrar diálogo de selección múltiple
        boolean[] selectedItems = new boolean[availableCompetitors.size()];

        new AlertDialog.Builder(getContext())
                .setTitle("Añadir Competidores")
                .setMultiChoiceItems(competitorNames, selectedItems,
                        (dialog, which, isChecked) -> selectedItems[which] = isChecked)
                .setPositiveButton("Añadir", (dialog, which) -> {
                    int addedCount = 0;
                    for (int i = 0; i < selectedItems.length; i++) {
                        if (selectedItems[i]) {
                            Competitor comp = availableCompetitors.get(i);
                            long result = dbHelper.addCompetitorToCategory(
                                    currentCategory.getFolio(), comp.getFolio());
                            if (result > 0) {
                                addedCount++;
                            }
                        }
                    }

                    if (addedCount > 0) {
                        Toast.makeText(getContext(),
                                addedCount + " competidor(es) añadido(s)",
                                Toast.LENGTH_SHORT).show();
                        loadCategoryCompetitors();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showRemoveCompetitorDialog() {
        if (currentCategory == null || categoryCompetitors.isEmpty()) {
            Toast.makeText(getContext(), "No hay competidores para eliminar",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear array de nombres para el diálogo
        String[] competitorNames = new String[categoryCompetitors.size()];
        for (int i = 0; i < categoryCompetitors.size(); i++) {
            Competitor c = categoryCompetitors.get(i);
            competitorNames[i] = c.getName() + " (" + c.getFolio() + ")";
        }

        // Mostrar diálogo de selección múltiple
        boolean[] selectedItems = new boolean[categoryCompetitors.size()];

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Competidores")
                .setMultiChoiceItems(competitorNames, selectedItems,
                        (dialog, which, isChecked) -> selectedItems[which] = isChecked)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    int removedCount = 0;
                    for (int i = 0; i < selectedItems.length; i++) {
                        if (selectedItems[i]) {
                            Competitor comp = categoryCompetitors.get(i);
                            int result = dbHelper.removeCompetitorFromCategory(
                                    currentCategory.getFolio(), comp.getFolio());
                            if (result > 0) {
                                removedCount++;
                            }
                        }
                    }

                    if (removedCount > 0) {
                        Toast.makeText(getContext(),
                                removedCount + " competidor(es) eliminado(s)",
                                Toast.LENGTH_SHORT).show();
                        loadCategoryCompetitors();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void autoAssignCompetitors() {
        if (currentCategory == null) return;

        // Obtener competidores elegibles
        List<Competitor> eligibleCompetitors = dbHelper.getEligibleCompetitors(currentCategory);

        if (eligibleCompetitors.isEmpty()) {
            Toast.makeText(getContext(),
                    "No hay competidores elegibles para asignar automáticamente",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Confirmar acción
        new AlertDialog.Builder(getContext())
                .setTitle("Asignación Automática")
                .setMessage("Se asignarán " + eligibleCompetitors.size() +
                        " competidores elegibles a esta categoría.\n\n¿Desea continuar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    int addedCount = 0;
                    for (Competitor comp : eligibleCompetitors) {
                        // Solo añadir si no está ya en la categoría
                        if (!dbHelper.isCompetitorInCategory(currentCategory.getFolio(), comp.getFolio())) {
                            long result = dbHelper.addCompetitorToCategory(
                                    currentCategory.getFolio(), comp.getFolio());
                            if (result > 0) {
                                addedCount++;
                            }
                        }
                    }

                    Toast.makeText(getContext(),
                            addedCount + " competidor(es) asignado(s) automáticamente",
                            Toast.LENGTH_LONG).show();
                    loadCategoryCompetitors();
                })
                .setNegativeButton("No", null)
                .show();
    }
}