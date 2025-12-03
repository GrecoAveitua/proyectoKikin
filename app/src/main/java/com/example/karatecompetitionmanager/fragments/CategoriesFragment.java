package com.example.karatecompetitionmanager.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatecompetitionmanager.R;
import com.example.karatecompetitionmanager.adapters.CategoryAdapter;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Category;

import java.util.List;

public class CategoriesFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private Button btnAdd, btnUpdate, btnDelete, btnView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recycler_categories);
        btnAdd = view.findViewById(R.id.btn_add_category);
        btnUpdate = view.findViewById(R.id.btn_update_category);
        btnDelete = view.findViewById(R.id.btn_delete_category);
        btnView = view.findViewById(R.id.btn_view_categories);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd.setOnClickListener(v -> showAddCategoryDialog());
        btnUpdate.setOnClickListener(v -> showUpdateCategoryDialog());
        btnDelete.setOnClickListener(v -> showDeleteCategoryDialog());
        btnView.setOnClickListener(v -> loadCategories());

        loadCategories();

        return view;
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_category, null);

        Spinner spBelt = dialogView.findViewById(R.id.sp_category_belt);
        EditText etMinAge = dialogView.findViewById(R.id.et_min_age);
        EditText etMaxAge = dialogView.findViewById(R.id.et_max_age);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_category_type);

        String[] belts = {"Blanco", "Amarillo", "Naranja", "Verde", "Azul", "Marron", "Negro"};
        ArrayAdapter<String> beltAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, belts);
        beltAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBelt.setAdapter(beltAdapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Crear Categoría")
                .setView(dialogView)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String belt = spBelt.getSelectedItem().toString();
                    String minAgeStr = etMinAge.getText().toString().trim();
                    String maxAgeStr = etMaxAge.getText().toString().trim();

                    if (minAgeStr.isEmpty() || maxAgeStr.isEmpty()) {
                        Toast.makeText(getContext(), "Complete todos los campos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int minAge = Integer.parseInt(minAgeStr);
                    int maxAge = Integer.parseInt(maxAgeStr);

                    if (minAge >= maxAge) {
                        Toast.makeText(getContext(), "Edad mínima debe ser menor que máxima",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int selectedId = rgType.getCheckedRadioButtonId();
                    RadioButton rbSelected = dialogView.findViewById(selectedId);
                    String type = rbSelected.getText().toString().toLowerCase();

                    if (type.equals("ambos")) type = "both";

                    String folio = Category.generateFolio(belt, minAge, maxAge, type);
                    Category category = new Category(folio, belt, minAge, maxAge, type);

                    long result = dbHelper.insertCategory(category);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Categoría creada. Folio: " + folio,
                                Toast.LENGTH_LONG).show();
                        loadCategories();
                    } else {
                        Toast.makeText(getContext(), "Error al crear categoría",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showUpdateCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_search_folio, null);

        EditText etFolio = dialogView.findViewById(R.id.et_search_folio);

        new AlertDialog.Builder(getContext())
                .setTitle("Buscar Categoría")
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String folio = etFolio.getText().toString().trim();
                    Category category = dbHelper.getCategory(folio);

                    if (category == null) {
                        Toast.makeText(getContext(), "Categoría no encontrada",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showEditCategoryDialog(category);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_category, null);

        Spinner spBelt = dialogView.findViewById(R.id.sp_category_belt);
        EditText etMinAge = dialogView.findViewById(R.id.et_min_age);
        EditText etMaxAge = dialogView.findViewById(R.id.et_max_age);
        RadioGroup rgType = dialogView.findViewById(R.id.rg_category_type);

        // Cargar datos actuales
        String[] belts = {"Blanco", "Amarillo", "Naranja", "Verde", "Azul", "Marron", "Negro"};
        ArrayAdapter<String> beltAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, belts);
        beltAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBelt.setAdapter(beltAdapter);

        for (int i = 0; i < belts.length; i++) {
            if (belts[i].equals(category.getBelt())) {
                spBelt.setSelection(i);
                break;
            }
        }

        etMinAge.setText(String.valueOf(category.getMinAge()));
        etMaxAge.setText(String.valueOf(category.getMaxAge()));

        if (category.getType().equals("kata")) {
            rgType.check(R.id.rb_kata);
        } else if (category.getType().equals("kumite")) {
            rgType.check(R.id.rb_kumite);
        } else {
            rgType.check(R.id.rb_both);
        }

        AlertDialog editDialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Categoría - " + category.getFolio())
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .show();

        editDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String belt = spBelt.getSelectedItem().toString();
            int minAge = Integer.parseInt(etMinAge.getText().toString().trim());
            int maxAge = Integer.parseInt(etMaxAge.getText().toString().trim());

            int selectedId = rgType.getCheckedRadioButtonId();
            RadioButton rbSelected = dialogView.findViewById(selectedId);
            String type = rbSelected.getText().toString().toLowerCase();
            if (type.equals("ambos")) type = "both";

            category.setBelt(belt);
            category.setMinAge(minAge);
            category.setMaxAge(maxAge);
            category.setType(type);

            int result = dbHelper.updateCategory(category);
            if (result > 0) {
                Toast.makeText(getContext(), "Categoría actualizada",
                        Toast.LENGTH_SHORT).show();
                loadCategories();
                editDialog.dismiss();
            }
        });
    }

    private void showDeleteCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_search_folio, null);

        EditText etFolio = dialogView.findViewById(R.id.et_search_folio);

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Categoría")
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String folio = etFolio.getText().toString().trim();
                    Category category = dbHelper.getCategory(folio);

                    if (category == null) {
                        Toast.makeText(getContext(), "Categoría no encontrada",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Primera confirmación
                    new AlertDialog.Builder(getContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Eliminar categoría " + folio + "?")
                            .setPositiveButton("Sí", (d2, w2) -> {
                                // Segunda confirmación
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Segunda confirmación")
                                        .setMessage("Esta acción no se puede deshacer")
                                        .setPositiveButton("Confirmar", (d3, w3) -> {
                                            int result = dbHelper.deleteCategory(folio);
                                            if (result > 0) {
                                                Toast.makeText(getContext(),
                                                        "Categoría eliminada",
                                                        Toast.LENGTH_SHORT).show();
                                                loadCategories();
                                            }
                                        })
                                        .setNegativeButton("Cancelar", null)
                                        .show();
                            })
                            .setNegativeButton("No", null)
                            .show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void loadCategories() {
        List<Category> categories = dbHelper.getAllCategories();
        adapter = new CategoryAdapter(categories);
        recyclerView.setAdapter(adapter);
    }
}