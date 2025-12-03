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
    private static final int DATABASE_VERSION = 1;

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

        db.execSQL(createCompetitorsTable);
        db.execSQL(createCategoriesTable);
        db.execSQL(createCompetitionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPETITORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPETITIONS);
        onCreate(db);
    }

    // CRUD Competidores
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

    // Nueva función: Buscar competidores por coincidencia en el folio
    public List<Competitor> searchCompetitorsByFolio(String searchTerm) {
        List<Competitor> competitors = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_COMPETITORS +
                " WHERE " + COL_COMP_FOLIO + " LIKE ? ORDER BY " + COL_COMP_FOLIO;

        Cursor cursor = db.rawQuery(query, new String[]{"%" + searchTerm + "%"});

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

    // CRUD Categorías
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

    // Nueva función: Buscar categorías por coincidencia en el folio
    public List<Category> searchCategoriesByFolio(String searchTerm) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_CATEGORIES +
                " WHERE " + COL_CAT_FOLIO + " LIKE ? ORDER BY " + COL_CAT_FOLIO;

        Cursor cursor = db.rawQuery(query, new String[]{"%" + searchTerm + "%"});

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

    // Métodos para Competencias
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