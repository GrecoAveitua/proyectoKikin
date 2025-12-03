package com.example.karatecompetitionmanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.karatecompetitionmanager.models.Category;
import com.example.karatecompetitionmanager.models.Competitor;
import com.example.karatecompetitionmanager.models.Competition;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "karate_competition.db";
    private static final int DATABASE_VERSION = 2; // ACTUALIZADO DE 1 A 2

    // Tabla Competidores
    private static final String TABLE_COMPETITORS = "competitors";
    private static final String COL_COMP_FOLIO = "folio";
    private static final String COL_COMP_NAME = "name";
    private static final String COL_COMP_DOJO = "dojo";
    private static final String COL_COMP_AGE = "age";
    private static final String COL_COMP_BELT = "belt";
    private static final String COL_COMP_KATA = "participate_kata";
    private static final String COL_COMP_KUMITE = "participate_kumite";

    // Tabla Categorías
    private static final String TABLE_CATEGORIES = "categories";
    private static final String COL_CAT_FOLIO = "folio";
    private static final String COL_CAT_BELT = "belt";
    private static final String COL_CAT_MIN_AGE = "min_age";
    private static final String COL_CAT_MAX_AGE = "max_age";
    private static final String COL_CAT_TYPE = "type";

    // Tabla Competencias
    private static final String TABLE_COMPETITIONS = "competitions";
    private static final String COL_COMP_ID = "id";
    private static final String COL_COMP_CAT_FOLIO = "category_folio";
    private static final String COL_COMP_DATE = "date";
    private static final String COL_COMP_STATUS = "status";
    private static final String COL_COMP_FIRST = "first_place";
    private static final String COL_COMP_SECOND = "second_place";
    private static final String COL_COMP_THIRD = "third_place";

    // NUEVA TABLA: Relación Categorías-Competidores
    private static final String TABLE_CATEGORY_COMPETITORS = "category_competitors";
    private static final String COL_CC_ID = "id";
    private static final String COL_CC_CATEGORY_FOLIO = "category_folio";
    private static final String COL_CC_COMPETITOR_FOLIO = "competitor_folio";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createCompetitorsTable = "CREATE TABLE " + TABLE_COMPETITORS + " (" +
                COL_COMP_FOLIO + " TEXT PRIMARY KEY, " +
                COL_COMP_NAME + " TEXT NOT NULL, " +
                COL_COMP_DOJO + " TEXT NOT NULL, " +
                COL_COMP_AGE + " INTEGER NOT NULL, " +
                COL_COMP_BELT + " TEXT NOT NULL, " +
                COL_COMP_KATA + " INTEGER DEFAULT 0, " +
                COL_COMP_KUMITE + " INTEGER DEFAULT 0)";

        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                COL_CAT_FOLIO + " TEXT PRIMARY KEY, " +
                COL_CAT_BELT + " TEXT NOT NULL, " +
                COL_CAT_MIN_AGE + " INTEGER NOT NULL, " +
                COL_CAT_MAX_AGE + " INTEGER NOT NULL, " +
                COL_CAT_TYPE + " TEXT NOT NULL)";

        String createCompetitionsTable = "CREATE TABLE " + TABLE_COMPETITIONS + " (" +
                COL_COMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COMP_CAT_FOLIO + " TEXT NOT NULL, " +
                COL_COMP_DATE + " TEXT NOT NULL, " +
                COL_COMP_STATUS + " TEXT NOT NULL, " +
                COL_COMP_FIRST + " TEXT, " +
                COL_COMP_SECOND + " TEXT, " +
                COL_COMP_THIRD + " TEXT)";

        // NUEVA TABLA
        String createCategoryCompetitorsTable = "CREATE TABLE " + TABLE_CATEGORY_COMPETITORS + " (" +
                COL_CC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_CC_CATEGORY_FOLIO + " TEXT NOT NULL, " +
                COL_CC_COMPETITOR_FOLIO + " TEXT NOT NULL, " +
                "FOREIGN KEY(" + COL_CC_CATEGORY_FOLIO + ") REFERENCES " +
                TABLE_CATEGORIES + "(" + COL_CAT_FOLIO + ") ON DELETE CASCADE, " +
                "FOREIGN KEY(" + COL_CC_COMPETITOR_FOLIO + ") REFERENCES " +
                TABLE_COMPETITORS + "(" + COL_COMP_FOLIO + ") ON DELETE CASCADE, " +
                "UNIQUE(" + COL_CC_CATEGORY_FOLIO + ", " + COL_CC_COMPETITOR_FOLIO + "))";

        db.execSQL(createCompetitorsTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createCompetitionsTable);
        db.execSQL(createCategoryCompetitorsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Crear la nueva tabla de relaciones
            String createCategoryCompetitorsTable = "CREATE TABLE " + TABLE_CATEGORY_COMPETITORS + " (" +
                    COL_CC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CC_CATEGORY_FOLIO + " TEXT NOT NULL, " +
                    COL_CC_COMPETITOR_FOLIO + " TEXT NOT NULL, " +
                    "FOREIGN KEY(" + COL_CC_CATEGORY_FOLIO + ") REFERENCES " +
                    TABLE_CATEGORIES + "(" + COL_CAT_FOLIO + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + COL_CC_COMPETITOR_FOLIO + ") REFERENCES " +
                    TABLE_COMPETITORS + "(" + COL_COMP_FOLIO + ") ON DELETE CASCADE, " +
                    "UNIQUE(" + COL_CC_CATEGORY_FOLIO + ", " + COL_CC_COMPETITOR_FOLIO + "))";
            db.execSQL(createCategoryCompetitorsTable);
        }
    }

    // ==================== CRUD COMPETIDORES ====================

    public long insertCompetitor(Competitor competitor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMP_FOLIO, competitor.getFolio());
        values.put(COL_COMP_NAME, competitor.getName());
        values.put(COL_COMP_DOJO, competitor.getDojo());
        values.put(COL_COMP_AGE, competitor.getAge());
        values.put(COL_COMP_BELT, competitor.getBelt());
        values.put(COL_COMP_KATA, competitor.isParticipateKata() ? 1 : 0);
        values.put(COL_COMP_KUMITE, competitor.isParticipateKumite() ? 1 : 0);
        return db.insert(TABLE_COMPETITORS, null, values);
    }

    public int updateCompetitor(Competitor competitor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMP_NAME, competitor.getName());
        values.put(COL_COMP_DOJO, competitor.getDojo());
        values.put(COL_COMP_AGE, competitor.getAge());
        values.put(COL_COMP_BELT, competitor.getBelt());
        values.put(COL_COMP_KATA, competitor.isParticipateKata() ? 1 : 0);
        values.put(COL_COMP_KUMITE, competitor.isParticipateKumite() ? 1 : 0);
        return db.update(TABLE_COMPETITORS, values, COL_COMP_FOLIO + "=?",
                new String[]{competitor.getFolio()});
    }

    public int deleteCompetitor(String folio) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_COMPETITORS, COL_COMP_FOLIO + "=?", new String[]{folio});
    }

    public Competitor getCompetitor(String folio) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPETITORS, null, COL_COMP_FOLIO + "=?",
                new String[]{folio}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Competitor competitor = new Competitor(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_FOLIO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_DOJO)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_AGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_BELT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KATA)) == 1,
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KUMITE)) == 1
            );
            cursor.close();
            return competitor;
        }
        return null;
    }

    public List<Competitor> getAllCompetitors(String orderBy, boolean ascending) {
        List<Competitor> competitors = new ArrayList<>();
        String order = ascending ? " ASC" : " DESC";
        String query = "SELECT * FROM " + TABLE_COMPETITORS + " ORDER BY " + orderBy + order;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Competitor competitor = new Competitor(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_DOJO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_BELT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KATA)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KUMITE)) == 1
                );
                competitors.add(competitor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return competitors;
    }

    // ==================== CRUD CATEGORÍAS ====================

    public long insertCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_FOLIO, category.getFolio());
        values.put(COL_CAT_BELT, category.getBelt());
        values.put(COL_CAT_MIN_AGE, category.getMinAge());
        values.put(COL_CAT_MAX_AGE, category.getMaxAge());
        values.put(COL_CAT_TYPE, category.getType());
        return db.insert(TABLE_CATEGORIES, null, values);
    }

    public int updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CAT_BELT, category.getBelt());
        values.put(COL_CAT_MIN_AGE, category.getMinAge());
        values.put(COL_CAT_MAX_AGE, category.getMaxAge());
        values.put(COL_CAT_TYPE, category.getType());
        return db.update(TABLE_CATEGORIES, values, COL_CAT_FOLIO + "=?",
                new String[]{category.getFolio()});
    }

    public int deleteCategory(String folio) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORIES, COL_CAT_FOLIO + "=?", new String[]{folio});
    }

    public Category getCategory(String folio) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, null, COL_CAT_FOLIO + "=?",
                new String[]{folio}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Category category = new Category(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_FOLIO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_BELT)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MIN_AGE)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MAX_AGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_TYPE))
            );
            cursor.close();
            return category;
        }
        return null;
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CATEGORIES +
                " ORDER BY " + COL_CAT_BELT + ", " + COL_CAT_MIN_AGE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_BELT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MIN_AGE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MAX_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_TYPE))
                );
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    // ==================== NUEVOS MÉTODOS: COMPETIDORES EN CATEGORÍAS ====================

    /**
     * Añadir un competidor a una categoría
     */
    public long addCompetitorToCategory(String categoryFolio, String competitorFolio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_CC_CATEGORY_FOLIO, categoryFolio);
        values.put(COL_CC_COMPETITOR_FOLIO, competitorFolio);
        return db.insert(TABLE_CATEGORY_COMPETITORS, null, values);
    }

    /**
     * Eliminar un competidor de una categoría
     */
    public int removeCompetitorFromCategory(String categoryFolio, String competitorFolio) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CATEGORY_COMPETITORS,
                COL_CC_CATEGORY_FOLIO + "=? AND " + COL_CC_COMPETITOR_FOLIO + "=?",
                new String[]{categoryFolio, competitorFolio});
    }

    /**
     * Obtener todos los competidores de una categoría
     */
    public List<Competitor> getCompetitorsByCategory(String categoryFolio) {
        List<Competitor> competitors = new ArrayList<>();
        String query = "SELECT c.* FROM " + TABLE_COMPETITORS + " c " +
                "INNER JOIN " + TABLE_CATEGORY_COMPETITORS + " cc " +
                "ON c." + COL_COMP_FOLIO + " = cc." + COL_CC_COMPETITOR_FOLIO + " " +
                "WHERE cc." + COL_CC_CATEGORY_FOLIO + " = ? " +
                "ORDER BY c." + COL_COMP_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{categoryFolio});

        if (cursor.moveToFirst()) {
            do {
                Competitor competitor = new Competitor(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_DOJO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_BELT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KATA)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KUMITE)) == 1
                );
                competitors.add(competitor);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return competitors;
    }

    /**
     * Obtener todas las categorías de un competidor
     */
    public List<Category> getCategoriesByCompetitor(String competitorFolio) {
        List<Category> categories = new ArrayList<>();
        String query = "SELECT cat.* FROM " + TABLE_CATEGORIES + " cat " +
                "INNER JOIN " + TABLE_CATEGORY_COMPETITORS + " cc " +
                "ON cat." + COL_CAT_FOLIO + " = cc." + COL_CC_CATEGORY_FOLIO + " " +
                "WHERE cc." + COL_CC_COMPETITOR_FOLIO + " = ? " +
                "ORDER BY cat." + COL_CAT_BELT + ", cat." + COL_CAT_MIN_AGE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{competitorFolio});

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_BELT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MIN_AGE)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_CAT_MAX_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CAT_TYPE))
                );
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    /**
     * Verificar si un competidor está en una categoría
     */
    public boolean isCompetitorInCategory(String categoryFolio, String competitorFolio) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORY_COMPETITORS, null,
                COL_CC_CATEGORY_FOLIO + "=? AND " + COL_CC_COMPETITOR_FOLIO + "=?",
                new String[]{categoryFolio, competitorFolio}, null, null, null);

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return exists;
    }

    /**
     * Obtener competidores elegibles para una categoría (que cumplan los requisitos)
     */
    public List<Competitor> getEligibleCompetitors(Category category) {
        List<Competitor> competitors = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_COMPETITORS + " " +
                "WHERE " + COL_COMP_BELT + " = ? " +
                "AND " + COL_COMP_AGE + " >= ? " +
                "AND " + COL_COMP_AGE + " <= ? " +
                "ORDER BY " + COL_COMP_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{
                category.getBelt(),
                String.valueOf(category.getMinAge()),
                String.valueOf(category.getMaxAge())
        });

        if (cursor.moveToFirst()) {
            do {
                Competitor competitor = new Competitor(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_DOJO)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_BELT)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KATA)) == 1,
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_KUMITE)) == 1
                );

                // Verificar tipo de competencia
                boolean isEligible = false;
                if (category.getType().equals("kata") && competitor.isParticipateKata()) {
                    isEligible = true;
                } else if (category.getType().equals("kumite") && competitor.isParticipateKumite()) {
                    isEligible = true;
                } else if (category.getType().equals("both")) {
                    isEligible = true;
                }

                if (isEligible) {
                    competitors.add(competitor);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return competitors;
    }

    // ==================== MÉTODOS PARA COMPETENCIAS ====================

    public long insertCompetition(Competition competition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMP_CAT_FOLIO, competition.getCategoryFolio());
        values.put(COL_COMP_DATE, competition.getDate());
        values.put(COL_COMP_STATUS, competition.getStatus());
        values.put(COL_COMP_FIRST, competition.getFirstPlace());
        values.put(COL_COMP_SECOND, competition.getSecondPlace());
        values.put(COL_COMP_THIRD, competition.getThirdPlace());
        return db.insert(TABLE_COMPETITIONS, null, values);
    }

    public int updateCompetition(Competition competition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COMP_STATUS, competition.getStatus());
        values.put(COL_COMP_FIRST, competition.getFirstPlace());
        values.put(COL_COMP_SECOND, competition.getSecondPlace());
        values.put(COL_COMP_THIRD, competition.getThirdPlace());
        return db.update(TABLE_COMPETITIONS, values, COL_COMP_ID + "=?",
                new String[]{String.valueOf(competition.getId())});
    }

    public List<Competition> getCompletedCompetitions() {
        List<Competition> competitions = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_COMPETITIONS +
                " WHERE " + COL_COMP_STATUS + "='COMPLETED' ORDER BY " + COL_COMP_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Competition competition = new Competition(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_COMP_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_CAT_FOLIO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_STATUS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_FIRST)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_SECOND)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COMP_THIRD))
                );
                competitions.add(competition);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return competitions;
    }
}