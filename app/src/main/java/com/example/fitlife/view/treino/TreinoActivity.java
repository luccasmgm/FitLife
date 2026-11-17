package com.example.fitlife.view.treino;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.model.entity.Exercicio;
import com.example.fitlife.view.adapter.ExercicioAdapter;
import com.example.fitlife.viewmodel.TreinoViewModel;
import com.google.android.material.button.MaterialButton;

/**
 * Tela de Treino: lista de exercícios filtráveis + botão de concluir treino.
 * Ao clicar em um exercício, abre ExercicioDetalheActivity.
 */
public class TreinoActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "USUARIO_ID";

    private TreinoViewModel viewModel;
    private ExercicioAdapter adapter;
    private long usuarioId;

    private Spinner spinnerTipo;
    private MaterialButton btnConcluir;
    private TextView textFeedback;

    /**
     * Opções do Spinner na mesma ordem de mapeamento com o enum.
     * Índice 0 = "Todos" (null), 1 = MUSCULACAO, 2 = CARDIO, 3 = MOBILIDADE.
     */
    private static final String[] OPCOES_TIPO = {
            "Todos os exercícios",
            "Musculação",
            "Cardio",
            "Mobilidade"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treino);

        usuarioId = getIntent().getLongExtra(EXTRA_USUARIO_ID, -1);
        if (usuarioId == -1) { finish(); return; }

        vincularViews();
        configurarRecyclerView();
        configurarSpinner();
        inicializarViewModel();
        configurarBotoes();
        observarDados();
    }

    private void vincularViews() {
        spinnerTipo  = findViewById(R.id.spinnerTipoExercicio);
        btnConcluir  = findViewById(R.id.btnConcluirTreino);
        textFeedback = findViewById(R.id.textFeedbackTreino);
    }

    private void configurarRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerExercicios);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // O adapter notifica o ViewModel via callback quando um item é clicado
        adapter = new ExercicioAdapter(exercicio -> {
            Intent intent = new Intent(this, ExercicioDetalheActivity.class);
            intent.putExtra(ExercicioDetalheActivity.EXTRA_USUARIO_ID, usuarioId);
            // Passamos os dados do exercício pelo Intent para não precisar
            // buscá-lo novamente no banco na próxima Activity.
            intent.putExtra(ExercicioDetalheActivity.EXTRA_EXERCICIO_ID, exercicio.getId());
            intent.putExtra(ExercicioDetalheActivity.EXTRA_EXERCICIO_NOME, exercicio.getNome());
            intent.putExtra(ExercicioDetalheActivity.EXTRA_EXERCICIO_GRUPO, exercicio.getGrupoMuscular());
            intent.putExtra(ExercicioDetalheActivity.EXTRA_EXERCICIO_URL_VIDEO, exercicio.getUrlVideo());
            intent.putExtra(ExercicioDetalheActivity.EXTRA_EXERCICIO_PERMITE_CARGA, exercicio.isPermiteCarga());
            startActivity(intent);
        });

        recycler.setAdapter(adapter);
    }

    private void configurarSpinner() {
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, OPCOES_TIPO
        );
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterSpinner);

        spinnerTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Posição 0 = "Todos" (null), demais mapeiam para o enum
                Exercicio.TipoExercicio tipo = null;
                if (position == 1) tipo = Exercicio.TipoExercicio.MUSCULACAO;
                else if (position == 2) tipo = Exercicio.TipoExercicio.CARDIO;
                else if (position == 3) tipo = Exercicio.TipoExercicio.MOBILIDADE;

                viewModel.filtrarPorTipo(tipo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(TreinoViewModel.class);
        viewModel.inicializar(usuarioId);
    }

    private void configurarBotoes() {
        btnConcluir.setOnClickListener(v -> viewModel.concluirTreino());
    }

    private void observarDados() {
        // Lista de exercícios filtrada — ListAdapter aplica DiffUtil automaticamente
        viewModel.getExerciciosFiltrados().observe(this, exercicios -> {
            adapter.submitList(exercicios);
        });

        // Estado do botão "Concluir" — desabilitado se já concluiu hoje
        viewModel.getTreinoConcluido().observe(this, concluido -> {
            btnConcluir.setEnabled(!concluido);
            btnConcluir.setText(concluido ? "Treino concluído hoje ✓" : "Concluir treino de hoje");
        });

        viewModel.getMensagemFeedback().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) return;
            textFeedback.setText(msg);
            textFeedback.setVisibility(View.VISIBLE);
            textFeedback.postDelayed(() -> textFeedback.setVisibility(View.GONE), 2500);
        });
    }
}
