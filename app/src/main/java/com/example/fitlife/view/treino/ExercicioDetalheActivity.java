package com.example.fitlife.view.treino;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fitlife.R;
import com.example.fitlife.view.adapter.CargaHistoricoAdapter;
import com.example.fitlife.viewmodel.ExercicioDetalheViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Tela de detalhe de um exercício.
 * Recebe os dados do exercício via Intent (enviados pela TreinoActivity)
 * para evitar uma consulta extra ao banco.
 */
public class ExercicioDetalheActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID            = "USUARIO_ID";
    public static final String EXTRA_EXERCICIO_ID          = "EXERCICIO_ID";
    public static final String EXTRA_EXERCICIO_NOME        = "EXERCICIO_NOME";
    public static final String EXTRA_EXERCICIO_GRUPO       = "EXERCICIO_GRUPO";
    public static final String EXTRA_EXERCICIO_URL_VIDEO   = "EXERCICIO_URL_VIDEO";
    public static final String EXTRA_EXERCICIO_PERMITE_CARGA = "EXERCICIO_PERMITE_CARGA";

    private ExercicioDetalheViewModel viewModel;
    private CargaHistoricoAdapter historicoAdapter;

    private WebView webViewVideo;
    private TextView textNome, textGrupo, textFeedbackCarga;
    private TextInputEditText editCarga, editSeries, editRepeticoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercicio_detalhe);

        // Habilita o botão de voltar na ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        vincularViews();
        carregarDadosDoIntent();
        inicializarViewModel();
        observarDados();
    }

    private void vincularViews() {
        webViewVideo      = findViewById(R.id.webViewVideo);
        textNome          = findViewById(R.id.textDetalheNome);
        textGrupo         = findViewById(R.id.textDetalheGrupo);
        textFeedbackCarga = findViewById(R.id.textFeedbackCarga);
        editCarga         = findViewById(R.id.editCarga);
        editSeries        = findViewById(R.id.editSeries);
        editRepeticoes    = findViewById(R.id.editRepeticoes);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void carregarDadosDoIntent() {
        String nome        = getIntent().getStringExtra(EXTRA_EXERCICIO_NOME);
        String grupo       = getIntent().getStringExtra(EXTRA_EXERCICIO_GRUPO);
        String urlVideo    = getIntent().getStringExtra(EXTRA_EXERCICIO_URL_VIDEO);
        boolean permiteCarga = getIntent().getBooleanExtra(EXTRA_EXERCICIO_PERMITE_CARGA, false);

        textNome.setText(nome);

        if (grupo != null) {
            textGrupo.setText(grupo);
            textGrupo.setVisibility(View.VISIBLE);
        } else {
            textGrupo.setVisibility(View.GONE);
        }

        // Configura o WebView para reproduzir o vídeo do YouTube
        // JavaScript é necessário para o embed do YouTube funcionar.
        WebSettings settings = webViewVideo.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webViewVideo.setWebChromeClient(new WebChromeClient());

        if (urlVideo != null && !urlVideo.isEmpty() && urlVideo.startsWith("http")) {
            /**
             * Carregamos o HTML do embed diretamente, em vez de carregar a URL
             * do YouTube diretamente. Isso dá controle sobre o tamanho e evita
             * o redirecionamento para o app do YouTube.
             * Substitua as URLs de "URL_VIDEO_*" no seed do AppDatabase por
             * URLs no formato: https://www.youtube.com/embed/VIDEO_ID
             */
            String htmlVideo = "<html><body style='margin:0;padding:0;background:#000'>"
                    + "<iframe width='100%' height='100%' src='" + urlVideo
                    + "?rel=0' frameborder='0' allowfullscreen></iframe>"
                    + "</body></html>";
            webViewVideo.loadUrl(urlVideo);
        }

        // Mostra o card de carga apenas para musculação
        if (permiteCarga) {
            findViewById(R.id.cardRegistrarCarga).setVisibility(View.VISIBLE);

            ((MaterialButton) findViewById(R.id.btnRegistrarCarga)).setOnClickListener(v -> {
                String carga = editCarga.getText() != null ? editCarga.getText().toString().trim() : "";
                String series = editSeries.getText() != null ? editSeries.getText().toString().trim() : "";
                String reps = editRepeticoes.getText() != null ? editRepeticoes.getText().toString().trim() : "";
                viewModel.registrarCarga(carga, series, reps);
            });
        }
    }

    private void inicializarViewModel() {
        long usuarioId   = getIntent().getLongExtra(EXTRA_USUARIO_ID, -1);
        long exercicioId = getIntent().getLongExtra(EXTRA_EXERCICIO_ID, -1);

        if (usuarioId == -1 || exercicioId == -1) { finish(); return; }

        viewModel = new ViewModelProvider(this).get(ExercicioDetalheViewModel.class);

        // Reconstruímos um Exercicio mínimo a partir dos dados do Intent
        // para não precisar de uma query extra ao banco
        com.example.fitlife.model.entity.Exercicio exercicio =
                new com.example.fitlife.model.entity.Exercicio(
                        getIntent().getStringExtra(EXTRA_EXERCICIO_NOME),
                        null, // tipo não é necessário aqui
                        getIntent().getStringExtra(EXTRA_EXERCICIO_GRUPO),
                        getIntent().getStringExtra(EXTRA_EXERCICIO_URL_VIDEO),
                        getIntent().getBooleanExtra(EXTRA_EXERCICIO_PERMITE_CARGA, false)
                );
        exercicio.setId(exercicioId);

        viewModel.inicializar(usuarioId, exercicio);

        // Configura RecyclerView do histórico somente se há histórico para mostrar
        if (getIntent().getBooleanExtra(EXTRA_EXERCICIO_PERMITE_CARGA, false)) {
            configurarHistorico();
        }
    }

    private void configurarHistorico() {
        RecyclerView recycler = findViewById(R.id.recyclerHistorico);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        historicoAdapter = new CargaHistoricoAdapter();
        recycler.setAdapter(historicoAdapter);
        findViewById(R.id.cardHistorico).setVisibility(View.VISIBLE);
    }

    private void observarDados() {
        // Histórico de cargas (só existe se o card de histórico foi configurado)
        if (viewModel.getHistoricoCarga() != null) {
            viewModel.getHistoricoCarga().observe(this, cargas -> {
                if (historicoAdapter != null) {
                    historicoAdapter.submitList(cargas);
                }
            });
        }

        viewModel.getMensagemFeedback().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) return;
            textFeedbackCarga.setText(msg);
            textFeedbackCarga.setVisibility(View.VISIBLE);

            // Limpa os campos após salvar com sucesso
            if (msg.contains("sucesso")) {
                editCarga.setText("");
                editSeries.setText("");
                editRepeticoes.setText("");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
