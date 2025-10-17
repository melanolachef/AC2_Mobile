package com.example.myapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText edtTitulo, edtDiretor, edtAno;
    RatingBar ratingBar;
    Spinner spinnerGenero, spinnerFiltro;
    CheckBox checkViuCinema;
    Button btnSalvar, btnFiltrar, btnOrdenarNota, btnOrdenarAno, btnMostrarTodos;
    ListView listViewFilmes;

    BancoHelper databaseHelper;
    ArrayAdapter<String> adapter;
    ArrayList<String> listaFilmes;
    ArrayList<Integer> listaIds;

    private int filmeIdEmEdicao = -1;
    private boolean modoEdicao = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Inicializar componentes
            edtTitulo = findViewById(R.id.edtTitulo);
            edtDiretor = findViewById(R.id.edtDiretor);
            edtAno = findViewById(R.id.edtAno);
            ratingBar = findViewById(R.id.ratingBar);
            spinnerGenero = findViewById(R.id.spinnerGenero);
            spinnerFiltro = findViewById(R.id.spinnerFiltro);
            checkViuCinema = findViewById(R.id.checkViuCinema);
            btnSalvar = findViewById(R.id.btnSalvar);
            btnFiltrar = findViewById(R.id.btnFiltrar);
            btnOrdenarNota = findViewById(R.id.btnOrdenarNota);
            btnOrdenarAno = findViewById(R.id.btnOrdenarAno);
            btnMostrarTodos = findViewById(R.id.btnMostrarTodos);
            listViewFilmes = findViewById(R.id.listViewFilmes);

            databaseHelper = new BancoHelper(this);

            // Configurar Spinner de Gêneros
            String[] generos = {"Ação", "Drama", "Comédia", "Ficção Científica",
                    "Terror", "Romance", "Suspense", "Aventura", "Animação"};
            ArrayAdapter<String> adapterGenero = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, generos);
            adapterGenero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerGenero.setAdapter(adapterGenero);

            // Configurar Spinner de Filtros
            configurarSpinnerFiltros();

            // Carregar filmes
            carregarFilmes(null, "todos");

            // Botão Salvar/Atualizar
            btnSalvar.setOnClickListener(v -> {
                String titulo = edtTitulo.getText().toString().trim();
                String diretor = edtDiretor.getText().toString().trim();
                String anoStr = edtAno.getText().toString().trim();
                float nota = ratingBar.getRating();
                String genero = spinnerGenero.getSelectedItem().toString();
                boolean viuCinema = checkViuCinema.isChecked();

                if (titulo.isEmpty() || diretor.isEmpty() || anoStr.isEmpty()) {
                    Toast.makeText(this, "Preencha todos os campos!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                int ano;
                try {
                    ano = Integer.parseInt(anoStr);
                    if (ano < 1800 || ano > 2100) {
                        Toast.makeText(this, "Ano inválido!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Ano inválido!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (modoEdicao) {
                    // Atualizar filme
                    int resultado = databaseHelper.atualizarFilme(filmeIdEmEdicao,
                            titulo, diretor, ano, nota, genero, viuCinema);
                    if (resultado > 0) {
                        Toast.makeText(this, "Filme atualizado com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarFilmes(null, "todos");
                        configurarSpinnerFiltros();
                    } else {
                        Toast.makeText(this, "Erro ao atualizar filme!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    long resultado = databaseHelper.inserirFilme(titulo, diretor,
                            ano, nota, genero, viuCinema);
                    if (resultado != -1) {
                        Toast.makeText(this, "Filme salvo com sucesso!",
                                Toast.LENGTH_SHORT).show();
                        limparCampos();
                        carregarFilmes(null, "todos");
                        configurarSpinnerFiltros();
                    } else {
                        Toast.makeText(this, "Erro ao salvar filme!",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

            listViewFilmes.setOnItemClickListener((parent, view, position, id) -> {
                int filmeId = listaIds.get(position);

                Cursor cursor = databaseHelper.listarFilmes();
                if (cursor.moveToFirst()) {
                    do {
                        if (cursor.getInt(0) == filmeId) {
                            edtTitulo.setText(cursor.getString(1));
                            edtDiretor.setText(cursor.getString(2));
                            edtAno.setText(String.valueOf(cursor.getInt(3)));
                            ratingBar.setRating(cursor.getFloat(4));

                            String generoFilme = cursor.getString(5);
                            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerGenero.getAdapter();
                            int posicaoGenero = adapter.getPosition(generoFilme);
                            spinnerGenero.setSelection(posicaoGenero);

                            checkViuCinema.setChecked(cursor.getInt(6) == 1);

                            modoEdicao = true;
                            filmeIdEmEdicao = filmeId;
                            btnSalvar.setText("Atualizar");
                            btnSalvar.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_dark));

                            Toast.makeText(this, "Modo edição ativado", Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            });

            listViewFilmes.setOnItemLongClickListener((parent, view, position, id) -> {
                int filmeId = listaIds.get(position);
                String nomeFilme = listaFilmes.get(position).split("\n")[0].replace("📽️ ", "");

                new AlertDialog.Builder(this)
                        .setTitle("Excluir Filme")
                        .setMessage("Deseja realmente excluir o filme '" + nomeFilme + "'?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            int deletado = databaseHelper.excluirFilme(filmeId);
                            if (deletado > 0) {
                                Toast.makeText(this, "Filme excluído com sucesso!",
                                        Toast.LENGTH_SHORT).show();
                                carregarFilmes(null, "todos");
                                configurarSpinnerFiltros();
                                limparCampos();
                            }
                        })
                        .setNegativeButton("Não", null)
                        .show();

                return true;
            });

            btnFiltrar.setOnClickListener(v -> {
                String filtroSelecionado = spinnerFiltro.getSelectedItem().toString();

                if (filtroSelecionado.equals("Todos")) {
                    carregarFilmes(null, "todos");
                } else if (filtroSelecionado.equals("Vistos no Cinema")) {
                    carregarFilmes(null, "cinema");
                } else {
                    carregarFilmes(null, "genero:" + filtroSelecionado);
                }
            });

            btnOrdenarNota.setOnClickListener(v -> {
                carregarFilmes(null, "nota");
                Toast.makeText(this, "Ordenado por nota (maior para menor)",
                        Toast.LENGTH_SHORT).show();
            });

            btnOrdenarAno.setOnClickListener(v -> {
                carregarFilmes(null, "ano");
                Toast.makeText(this, "Ordenado por ano (mais recente)",
                        Toast.LENGTH_SHORT).show();
            });

            btnMostrarTodos.setOnClickListener(v -> {
                carregarFilmes(null, "todos");
                spinnerFiltro.setSelection(0);
                Toast.makeText(this, "Mostrando todos os filmes", Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void configurarSpinnerFiltros() {
        ArrayList<String> filtros = new ArrayList<>();
        filtros.add("Todos");
        filtros.add("Vistos no Cinema");

        Cursor cursor = databaseHelper.listarGeneros();
        if (cursor.moveToFirst()) {
            do {
                filtros.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filtros);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltro.setAdapter(adapterFiltro);
    }

    private void carregarFilmes(Cursor cursor, String tipo) {
        if (cursor == null) {
            if (tipo.equals("todos")) {
                cursor = databaseHelper.listarFilmes();
            } else if (tipo.equals("cinema")) {
                cursor = databaseHelper.listarFilmesVisualizadosNoCinema();
            } else if (tipo.equals("nota")) {
                cursor = databaseHelper.listarFilmesOrdenadosPorNota();
            } else if (tipo.equals("ano")) {
                cursor = databaseHelper.listarFilmesOrdenadosPorAno();
            } else if (tipo.startsWith("genero:")) {
                String genero = tipo.substring(7);
                cursor = databaseHelper.listarFilmesPorGenero(genero);
            }
        }

        listaFilmes = new ArrayList<>();
        listaIds = new ArrayList<>();

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String titulo = cursor.getString(1);
                String diretor = cursor.getString(2);
                int ano = cursor.getInt(3);
                float nota = cursor.getFloat(4);
                String genero = cursor.getString(5);
                boolean viuCinema = cursor.getInt(6) == 1;

                String estrelas = gerarEstrelas(nota);
                String cinemaIcon = viuCinema ? "🎬 " : "";

                String infoFilme = "📽️ " + titulo + "\n" +
                        "🎭 Diretor: " + diretor + "\n" +
                        "📅 Ano: " + ano + " | 🎪 Gênero: " + genero + "\n" +
                        "⭐ Nota: " + estrelas + " (" + nota + "/5)" +
                        (viuCinema ? "\n" + cinemaIcon + "Visto no cinema" : "");

                listaFilmes.add(infoFilme);
                listaIds.add(id);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaFilmes);
        listViewFilmes.setAdapter(adapter);
    }

    private String gerarEstrelas(float nota) {
        StringBuilder estrelas = new StringBuilder();
        int notaInteira = (int) nota;
        boolean temMeia = (nota - notaInteira) >= 0.5;

        for (int i = 0; i < notaInteira; i++) {
            estrelas.append("⭐");
        }
        if (temMeia) {
            estrelas.append("✨");
        }
        return estrelas.toString();
    }

    private void limparCampos() {
        edtTitulo.setText("");
        edtDiretor.setText("");
        edtAno.setText("");
        ratingBar.setRating(3);
        spinnerGenero.setSelection(0);
        checkViuCinema.setChecked(false);
        modoEdicao = false;
        filmeIdEmEdicao = -1;
        btnSalvar.setText("Salvar");
        btnSalvar.setBackgroundColor(getResources().getColor(android.R.color.holo_green_dark));
    }
}