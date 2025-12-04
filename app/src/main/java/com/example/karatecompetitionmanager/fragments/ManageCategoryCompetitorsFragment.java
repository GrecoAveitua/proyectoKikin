package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
        String searchTerm = etCategoryFolio.getText().toString().trim();

        if (searchTerm.isEmpty()) {
            Toast.makeText(getContext(), "Ingrese un término de búsqueda",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar categorías que coincidan con el término
        List<Category> results = dbHelper.searchCategoriesByFolio(searchTerm);

        if (results.isEmpty()) {
            Toast.makeText(getContext(), "No se encontraron categorías",
                    Toast.LENGTH_SHORT).show();
            resetView();
            return;
        }

        if (results.size() == 1) {
            // Si hay solo una categoría, cargarla directamente
            loadCategory(results.get(0));
        } else {
            // Si hay múltiples resultados, mostrar diálogo de selección
            showCategorySelectionDialog(results);
        }
    }

    private void showCategorySelectionDialog(List<Category> categories) {
        String[] options = new String[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            String type = c.getType().equals("kata") ? "Kata" :
                    (c.getType().equals("kumite") ? "Kumite" : "Kata y Kumite");
            options[i] = c.getFolio() + " - " + c.getBelt() + " (" +
                    c.getMinAge() + "-" + c.getMaxAge() + " años, " + type + ")";
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccione una categoría")
                .setItems(options, (dialog, which) -> {
                    loadCategory(categories.get(which));
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadCategory(Category category) {
        currentCategory = category;
        displayCategoryInfo();
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
        // Obtener todos los competidores elegibles para esta categoría
        categoryCompetitors = getEligibleCompetitorsForCategory(currentCategory);

        if (categoryCompetitors.isEmpty()) {
            tvCompetitorCount.setText("No hay competidores elegibles para esta categoría");
            tvCompetitorCount.setVisibility(View.VISIBLE);
        } else {
            tvCompetitorCount.setText("Total de competidores elegibles: " + categoryCompetitors.size());
            tvCompetitorCount.setVisibility(View.VISIBLE);
        }

        adapter = new CompetitorAdapter(categoryCompetitors);
        recyclerView.setAdapter(adapter);
    }

    private List<Competitor> getEligibleCompetitorsForCategory(Category category) {
        List<Competitor> eligible = new ArrayList<>();
        List<Competitor> allCompetitors = dbHelper.getAllCompetitors("age", true);

        for (Competitor competitor : allCompetitors) {
            // Verificar si el competidor cumple los requisitos de la categoría
            if (isCompetitorEligible(competitor, category)) {
                eligible.add(competitor);
            }
        }

        return eligible;
    }

    private boolean isCompetitorEligible(Competitor competitor, Category category) {
        // Verificar cinturón
        if (!competitor.getBelt().equals(category.getBelt())) {
            return false;
        }

        // Verificar edad
        if (competitor.getAge() < category.getMinAge() ||
                competitor.getAge() > category.getMaxAge()) {
            return false;
        }

        // Verificar tipo de participación
        String categoryType = category.getType();
        if (categoryType.equals("kata")) {
            return competitor.isParticipateKata();
        } else if (categoryType.equals("kumite")) {
            return competitor.isParticipateKumite();
        } else { // "both"
            return competitor.isParticipateKata() || competitor.isParticipateKumite();
        }
    }

    private void showAddCompetitorDialog() {
        if (currentCategory == null) {
            Toast.makeText(getContext(), "Primero seleccione una categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryCompetitors.isEmpty()) {
            Toast.makeText(getContext(),
                    "No hay competidores elegibles para esta categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear diálogo con checkboxes
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_assign_competitors, null);

        LinearLayout layoutCompetitors = dialogView.findViewById(R.id.layout_competitors_checkboxes);

        // Lista para almacenar los checkboxes
        final List<CheckBox> checkBoxes = new ArrayList<>();

        // Crear un checkbox por cada competidor elegible
        for (Competitor competitor : categoryCompetitors) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(competitor.getName() + " (" + competitor.getFolio() + ") - " +
                    competitor.getAge() + " años, " + competitor.getBelt());
            checkBox.setTag(competitor);
            checkBox.setPadding(16, 12, 16, 12);
            layoutCompetitors.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Seleccionar Competidores")
                .setMessage("Seleccione los competidores que participarán en esta categoría:")
                .setView(dialogView)
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    List<String> selectedCompetitors = new ArrayList<>();
                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isChecked()) {
                            Competitor comp = (Competitor) checkBox.getTag();
                            selectedCompetitors.add(comp.getName());
                        }
                    }

                    if (selectedCompetitors.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No se seleccionó ningún competidor",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Se asignaron " + selectedCompetitors.size() + " competidor(es)",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showRemoveCompetitorDialog() {
        if (currentCategory == null) {
            Toast.makeText(getContext(), "Primero seleccione una categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (categoryCompetitors.isEmpty()) {
            Toast.makeText(getContext(), "No hay competidores en esta categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear diálogo con checkboxes para eliminar
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_assign_competitors, null);

        LinearLayout layoutCompetitors = dialogView.findViewById(R.id.layout_competitors_checkboxes);

        final List<CheckBox> checkBoxes = new ArrayList<>();

        for (Competitor competitor : categoryCompetitors) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(competitor.getName() + " (" + competitor.getFolio() + ")");
            checkBox.setTag(competitor);
            checkBox.setPadding(16, 12, 16, 12);
            layoutCompetitors.addView(checkBox);
            checkBoxes.add(checkBox);
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Competidores")
                .setMessage("Seleccione los competidores que desea eliminar de esta categoría:")
                .setView(dialogView)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    List<String> removedCompetitors = new ArrayList<>();
                    for (CheckBox checkBox : checkBoxes) {
                        if (checkBox.isChecked()) {
                            Competitor comp = (Competitor) checkBox.getTag();
                            removedCompetitors.add(comp.getName());
                        }
                    }

                    if (removedCompetitors.isEmpty()) {
                        Toast.makeText(getContext(),
                                "No se seleccionó ningún competidor",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Se eliminaron " + removedCompetitors.size() + " competidor(es)",
                                Toast.LENGTH_LONG).show();
                        loadCategoryCompetitors(); // Recargar lista
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void autoAssignCompetitors() {
        if (currentCategory == null) {
            Toast.makeText(getContext(), "Primero seleccione una categoría",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Recargar competidores elegibles
        loadCategoryCompetitors();

        if (categoryCompetitors.isEmpty()) {
            Toast.makeText(getContext(),
                    "No hay competidores elegibles para asignar a esta categoría",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Mostrar diálogo de confirmación automática
        StringBuilder message = new StringBuilder();
        message.append("Se asignarán automáticamente ").append(categoryCompetitors.size())
                .append(" competidor(es) elegible(s):\n\n");

        for (Competitor comp : categoryCompetitors) {
            message.append("• ").append(comp.getName())
                    .append(" (").append(comp.getFolio()).append(")\n");
        }

        message.append("\nCriterios de elegibilidad:\n")
                .append("✓ Cinturón: ").append(currentCategory.getBelt()).append("\n")
                .append("✓ Edad: ").append(currentCategory.getMinAge())
                .append("-").append(currentCategory.getMaxAge()).append(" años\n")
                .append("✓ Tipo: ");

        String type = currentCategory.getType().equals("kata") ? "Kata" : "Kumite";
        message.append(type);

        new AlertDialog.Builder(getContext())
                .setTitle("Asignación Automática")
                .setMessage(message.toString())
                .setPositiveButton("Confirmar", (dialog, which) -> {
                    Toast.makeText(getContext(),
                            "Se asignaron " + categoryCompetitors.size() + " competidor(es) automáticamente",
                            Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void resetView() {
        currentCategory = null;
        categoryCompetitors.clear();

        tvCategoryInfo.setVisibility(View.GONE);
        tvCompetitorCount.setVisibility(View.GONE);

        if (adapter != null) {
            adapter = new CompetitorAdapter(categoryCompetitors);
            recyclerView.setAdapter(adapter);
        }

        btnAddCompetitor.setEnabled(false);
        btnRemoveCompetitor.setEnabled(false);
        btnAutoAssign.setEnabled(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}