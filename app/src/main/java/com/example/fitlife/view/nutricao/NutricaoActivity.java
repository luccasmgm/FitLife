package com.example.fitlife.view.nutricao;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.view.adapter.RefeicaoAdapter;
import com.example.fitlife.viewmodel.NutricaoViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Tela de Nutrição: resumo calórico do dia no topo e uma lista de
 * refeições (café, almoço, lanche, jantar) onde o usuário registra
 * o que comeu.
 */
public class NutricaoActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "USUARIO_ID";

    private NutricaoViewModel viewModel;
    private RefeicaoAdapter adapter;
    private long usuarioId;

    private TextView textConsumidas, textMeta, textRestantes, textFeedback;
    private ProgressBar progressCalorias;

    // Guardamos os dois valores para recalcular o resumo quando qualquer um mudar
    private int metaAtual = 0;
    private int consumidoAtual = 0;

    private final NumberFormat formatoNumero = NumberFormat.getIntegerInstance(new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutricao);

        usuarioId = getIntent().getLongExtra(EXTRA_USUARIO_ID, -1);
        if (usuarioId == -1) { finish(); return; }

        vincularViews();
        configurarRecyclerView();
        inicializarViewModel();
        observarDados();
    }

    private void vincularViews() {
        textConsumidas   = findViewById(R.id.textCaloriasConsumidas);
        textMeta         = findViewById(R.id.textMetaCalorica);
        textRestantes    = findViewById(R.id.textCaloriasRestantes);
        textFeedback     = findViewById(R.id.textFeedbackNutricao);
        progressCalorias = findViewById(R.id.progressCalorias);
    }

    private void configurarRecyclerView() {
        RecyclerView recycler = findViewById(R.id.recyclerRefeicoes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RefeicaoAdapter(this::mostrarDialogoAdicionarAlimento);
        recycler.setAdapter(adapter);
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(NutricaoViewModel.class);
        viewModel.inicializar(usuarioId);
    }

    /**
     * Abre o diálogo de registro de alimento. O botão "Adicionar" é
     * configurado no OnShowListener (e não no setPositiveButton) porque
     * o AlertDialog fecha sozinho ao clicar em qualquer botão padrão.
     * Queremos fechar APENAS se o ViewModel aceitar os dados — se houver
     * erro de validação, o diálogo continua aberto para o usuário corrigir.
     */
    private void mostrarDialogoAdicionarAlimento(Refeicao refeicao) {
        View conteudo = getLayoutInflater().inflate(R.layout.dialog_adicionar_alimento, null);
        TextInputEditText editNome       = conteudo.findViewById(R.id.editNomeAlimento);
        TextInputEditText editQuantidade = conteudo.findViewById(R.id.editQuantidadeAlimento);
        TextInputEditText editCalorias   = conteudo.findViewById(R.id.editCaloriasAlimento);

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(RefeicaoAdapter.rotuloDeTipo(refeicao.getTipo()))
                .setView(conteudo)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Adicionar", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    boolean aceito = viewModel.registrarAlimento(
                            refeicao.getId(),
                            textoDe(editNome),
                            textoDe(editQuantidade),
                            textoDe(editCalorias)
                    );
                    if (aceito) dialog.dismiss();
                })
        );
        dialog.show();
    }

    private String textoDe(TextInputEditText campo) {
        return campo.getText() != null ? campo.getText().toString().trim() : "";
    }

    private void observarDados() {
        viewModel.getItensRefeicao().observe(this, itens -> adapter.submitList(itens));

        viewModel.getMetaCaloricaDiaria().observe(this, meta -> {
            metaAtual = meta != null ? meta : 0;
            atualizarResumo();
        });

        // SUM() retorna null quando ainda não há nenhum alimento registrado
        viewModel.getTotalCaloriasConsumidas().observe(this, total -> {
            consumidoAtual = total != null ? Math.round(total) : 0;
            atualizarResumo();
        });

        viewModel.getMensagemFeedback().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) return;
            textFeedback.setText(msg);
            textFeedback.setVisibility(View.VISIBLE);
            textFeedback.postDelayed(() -> textFeedback.setVisibility(View.GONE), 2500);
        });
    }

    private void atualizarResumo() {
        textConsumidas.setText(formatoNumero.format(consumidoAtual));
        textMeta.setText("de " + formatoNumero.format(metaAtual) + " kcal");

        int percentual = metaAtual > 0 ? Math.min(100, consumidoAtual * 100 / metaAtual) : 0;
        progressCalorias.setProgress(percentual);

        int restante = metaAtual - consumidoAtual;
        if (restante >= 0) {
            textRestantes.setText("Restam " + formatoNumero.format(restante) + " kcal hoje");
        } else {
            textRestantes.setText("Meta ultrapassada em " + formatoNumero.format(-restante) + " kcal");
        }
    }
}
