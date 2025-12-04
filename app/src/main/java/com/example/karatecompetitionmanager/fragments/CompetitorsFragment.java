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
import com.example.karatecompetitionmanager.adapters.CompetitorAdapter;
import com.example.karatecompetitionmanager.database.DatabaseHelper;
import com.example.karatecompetitionmanager.models.Competitor;

import java.util.List;

public class CompetitorsFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private CompetitorAdapter adapter;
    private Button btnAdd, btnUpdate, btnDelete, btnView;
    private RadioGroup rgSort;
    private boolean ascending = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_competitors, container, false);

        dbHelper = new DatabaseHelper(getContext());

        recyclerView = view.findViewById(R.id.recycler_competitors);
        btnAdd = view.findViewById(R.id.btn_add_competitor);
        btnUpdate = view.findViewById(R.id.btn_update_competitor);
        btnDelete = view.findViewById(R.id.btn_delete_competitor);
        btnView = view.findViewById(R.id.btn_view_competitors);
        rgSort = view.findViewById(R.id.rg_sort_order);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnAdd.setOnClickListener(v -> showAddCompetitorDialog());
        btnUpdate.setOnClickListener(v -> showUpdateCompetitorDialog());
        btnDelete.setOnClickListener(v -> showDeleteCompetitorDialog());
        btnView.setOnClickListener(v -> loadCompetitors());

        rgSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_ascending) {
                ascending = true;
            } else {
                ascending = false;
            }
            loadCompetitors();
        });

        loadCompetitors();

        return view;
    }

    private void showAddCompetitorDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_competitor, null);

        EditText etName = dialogView.findViewById(R.id.et_competitor_name);
        EditText etDojo = dialogView.findViewById(R.id.et_competitor_dojo);
        EditText etAge = dialogView.findViewById(R.id.et_competitor_age);
        Spinner spBelt = dialogView.findViewById(R.id.sp_competitor_belt);
        RadioGroup rgParticipation = dialogView.findViewById(R.id.rg_participation);

        // Configurar spinner de cinturones
        String[] belts = {"Blanco", "Amarillo", "Naranja", "Verde", "Azul", "Marron", "Negro"};
        ArrayAdapter<String> beltAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, belts);
        beltAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBelt.setAdapter(beltAdapter);

        new AlertDialog.Builder(getContext())
                .setTitle("Agregar Competidor")
                .setView(dialogView)
                .setPositiveButton("Agregar", (dialog, which) -> {
                    String name = etName.getText().toString().trim();
                    String dojo = etDojo.getText().toString().trim();
                    String ageStr = etAge.getText().toString().trim();
                    String belt = spBelt.getSelectedItem().toString();

                    if (name.isEmpty() || dojo.isEmpty() || ageStr.isEmpty()) {
                        Toast.makeText(getContext(), "Complete todos los campos",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Verificar que se haya seleccionado una opción
                    int selectedId = rgParticipation.getCheckedRadioButtonId();
                    if (selectedId == -1) {
                        Toast.makeText(getContext(), "Debe seleccionar Kata o Kumite",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Determinar qué opción se seleccionó
                    RadioButton selectedRadio = dialogView.findViewById(selectedId);
                    boolean kata = selectedRadio.getId() == R.id.rb_kata;
                    boolean kumite = selectedRadio.getId() == R.id.rb_kumite;

                    int age = Integer.parseInt(ageStr);
                    String type = kata ? "KA" : "KU";
                    String folio = Competitor.generateFolio(name, belt, age, type);

                    Competitor competitor = new Competitor(folio, name, dojo, age, belt,
                            kata, kumite);

                    long result = dbHelper.insertCompetitor(competitor);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Competidor agregado. Folio: " + folio,
                                Toast.LENGTH_LONG).show();
                        loadCompetitors();
                    } else {
                        Toast.makeText(getContext(), "Error al agregar competidor",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showUpdateCompetitorDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_search_folio, null);

        EditText etFolio = dialogView.findViewById(R.id.et_search_folio);

        new AlertDialog.Builder(getContext())
                .setTitle("Buscar Competidor")
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String folio = etFolio.getText().toString().trim();
                    Competitor competitor = dbHelper.getCompetitor(folio);

                    if (competitor == null) {
                        Toast.makeText(getContext(), "Competidor no encontrado",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showEditCompetitorDialog(competitor);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showEditCompetitorDialog(Competitor competitor) {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_add_competitor, null);

        EditText etName = dialogView.findViewById(R.id.et_competitor_name);
        EditText etDojo = dialogView.findViewById(R.id.et_competitor_dojo);
        EditText etAge = dialogView.findViewById(R.id.et_competitor_age);
        Spinner spBelt = dialogView.findViewById(R.id.sp_competitor_belt);
        RadioGroup rgParticipation = dialogView.findViewById(R.id.rg_participation);

        // Cargar datos actuales
        etName.setText(competitor.getName());
        etDojo.setText(competitor.getDojo());
        etAge.setText(String.valueOf(competitor.getAge()));

        // Seleccionar el RadioButton correcto
        if (competitor.isParticipateKata()) {
            rgParticipation.check(R.id.rb_kata);
        } else if (competitor.isParticipateKumite()) {
            rgParticipation.check(R.id.rb_kumite);
        }

        String[] belts = {"Blanco", "Amarillo", "Naranja", "Verde", "Azul", "Marron", "Negro"};
        ArrayAdapter<String> beltAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, belts);
        beltAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBelt.setAdapter(beltAdapter);

        for (int i = 0; i < belts.length; i++) {
            if (belts[i].equals(competitor.getBelt())) {
                spBelt.setSelection(i);
                break;
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Editar Competidor - " + competitor.getFolio())
                .setView(dialogView)
                .setPositiveButton("Guardar", null)
                .setNegativeButton("Cancelar", null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Verificar que se haya seleccionado una opción
            int selectedId = rgParticipation.getCheckedRadioButtonId();
            if (selectedId == -1) {
                Toast.makeText(getContext(), "Debe seleccionar Kata o Kumite",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Primera confirmación
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirmar cambios")
                    .setMessage("¿Está seguro de modificar este competidor?")
                    .setPositiveButton("Sí", (d2, w2) -> {
                        // Segunda confirmación
                        new AlertDialog.Builder(getContext())
                                .setTitle("Segunda confirmación")
                                .setMessage("Confirme nuevamente los cambios")
                                .setPositiveButton("Confirmar", (d3, w3) -> {
                                    RadioButton selectedRadio = dialogView.findViewById(selectedId);
                                    boolean kata = selectedRadio.getId() == R.id.rb_kata;
                                    boolean kumite = selectedRadio.getId() == R.id.rb_kumite;

                                    competitor.setName(etName.getText().toString().trim());
                                    competitor.setDojo(etDojo.getText().toString().trim());
                                    competitor.setAge(Integer.parseInt(
                                            etAge.getText().toString().trim()));
                                    competitor.setBelt(spBelt.getSelectedItem().toString());
                                    competitor.setParticipateKata(kata);
                                    competitor.setParticipateKumite(kumite);

                                    int result = dbHelper.updateCompetitor(competitor);
                                    if (result > 0) {
                                        Toast.makeText(getContext(),
                                                "Competidor actualizado",
                                                Toast.LENGTH_SHORT).show();
                                        loadCompetitors();
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void showDeleteCompetitorDialog() {
        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_search_folio, null);

        EditText etFolio = dialogView.findViewById(R.id.et_search_folio);

        new AlertDialog.Builder(getContext())
                .setTitle("Eliminar Competidor")
                .setView(dialogView)
                .setPositiveButton("Buscar", (dialog, which) -> {
                    String folio = etFolio.getText().toString().trim();
                    Competitor competitor = dbHelper.getCompetitor(folio);

                    if (competitor == null) {
                        Toast.makeText(getContext(), "Competidor no encontrado",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Primera confirmación
                    new AlertDialog.Builder(getContext())
                            .setTitle("Confirmar eliminación")
                            .setMessage("¿Eliminar a " + competitor.getName() + "?")
                            .setPositiveButton("Sí", (d2, w2) -> {
                                // Segunda confirmación
                                new AlertDialog.Builder(getContext())
                                        .setTitle("Segunda confirmación")
                                        .setMessage("Esta acción no se puede deshacer")
                                        .setPositiveButton("Confirmar", (d3, w3) -> {
                                            int result = dbHelper.deleteCompetitor(folio);
                                            if (result > 0) {
                                                Toast.makeText(getContext(),
                                                        "Competidor eliminado",
                                                        Toast.LENGTH_SHORT).show();
                                                loadCompetitors();
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

    private void loadCompetitors() {
        List<Competitor> competitors = dbHelper.getAllCompetitors("name", true);
        if (competitors != null) {
            competitors.sort((c1, c2) -> {
                int result = Integer.compare(c1.getAge(), c2.getAge());
                if (result == 0) {
                    result = c1.getName().compareToIgnoreCase(c2.getName());
                }
                return ascending ? result : -result;
            });
        }
        adapter = new CompetitorAdapter(competitors);
        recyclerView.setAdapter(adapter);
    }
}