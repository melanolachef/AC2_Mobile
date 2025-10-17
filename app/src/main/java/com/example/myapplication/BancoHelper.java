package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BancoHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "filmes.db";
    private static final int DATABASE_VERSION = 1;

        private static final String TABLE_NAME = "filmes";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITULO = "titulo";
    private static final String COLUMN_DIRETOR = "diretor";
    private static final String COLUMN_ANO = "ano";
    private static final String COLUMN_NOTA = "nota";
    private static final String COLUMN_GENERO = "genero";
    private static final String COLUMN_VIU_CINEMA = "viu_cinema";

    public BancoHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITULO + " TEXT, "
                + COLUMN_DIRETOR + " TEXT, "
                + COLUMN_ANO + " INTEGER, "
                + COLUMN_NOTA + " REAL, "
                + COLUMN_GENERO + " TEXT, "
                + COLUMN_VIU_CINEMA + " INTEGER)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long inserirFilme(String titulo, String diretor, int ano, float nota,
                             String genero, boolean viuCinema) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITULO, titulo);
        values.put(COLUMN_DIRETOR, diretor);
        values.put(COLUMN_ANO, ano);
        values.put(COLUMN_NOTA, nota);
        values.put(COLUMN_GENERO, genero);
        values.put(COLUMN_VIU_CINEMA, viuCinema ? 1 : 0);
        return db.insert(TABLE_NAME, null, values);
    }

    public Cursor listarFilmes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_TITULO, null);
    }

    public Cursor listarFilmesPorGenero(String genero) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_GENERO + "=? ORDER BY " + COLUMN_TITULO,
                new String[]{genero});
    }

    public Cursor listarFilmesVisualizadosNoCinema() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_VIU_CINEMA + "=1 ORDER BY " + COLUMN_TITULO, null);
    }

    public Cursor listarFilmesOrdenadosPorNota() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_NOTA + " DESC", null);
    }

    public Cursor listarFilmesOrdenadosPorAno() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ANO + " DESC", null);
    }

    public Cursor listarGeneros() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT DISTINCT " + COLUMN_GENERO + " FROM " + TABLE_NAME + " ORDER BY " + COLUMN_GENERO, null);
    }

    public int atualizarFilme(int id, String titulo, String diretor, int ano,
                              float nota, String genero, boolean viuCinema) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITULO, titulo);
        values.put(COLUMN_DIRETOR, diretor);
        values.put(COLUMN_ANO, ano);
        values.put(COLUMN_NOTA, nota);
        values.put(COLUMN_GENERO, genero);
        values.put(COLUMN_VIU_CINEMA, viuCinema ? 1 : 0);
        return db.update(TABLE_NAME, values, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public int excluirFilme(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COLUMN_ID + "=?",
                new String[]{String.valueOf(id)});
    }
}