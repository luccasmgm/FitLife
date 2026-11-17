package com.example.fitlife.view.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitlife.R;
import com.example.fitlife.util.CalculoUtils;
import com.example.fitlife.view.nutricao.NutricaoActivity;
import com.example.fitlife.view.treino.TreinoActivity;
import com.example.fitlife.viewmodel.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_USUARIO_ID = "USUARIO_ID";

    private DashboardViewModel viewModel;
    private long usuarioId;

    private BottomNavigationView bottomNav;
    private TextView textSaudacao, textNomeUsuario;
    private TextView textCaloriasQueimadas, textTotalTreinos;
    private TextView textAguaProgresso;
    private ProgressBar progressHidratacao;
    private TextInputEditText editPesoDoDia;
    private TextView textPesoRegistrado;
    private TextView textFeedback;

    private int metaHidratacaoAtual = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        usuarioId = getIntent().getLongExtra(EXTRA_USUARIO_ID, -1);
        if (usuarioId == -1) { finish(); return; }

        vincularViews();
        inicializarViewModel();
        configurarBotoes();
        configurarBottomNavigation();
        observarDados();
    }

    /**
     * Ao voltar de Treino ou Nutrição, a aba "Início" precisa voltar a
     * aparecer marcada. Usamos setChecked no item do menu (e não
     * setSelectedItemId) para marcar a aba sem disparar o listener de
     * navegação de novo.
     */
    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_dashboard).setChecked(true);
    }

    private void vincularViews() {
        bottomNav             = findViewById(R.id.bottomNavigationView);
        textSaudacao          = findViewById(R.id.textSaudacao);
        textNomeUsuario       = findViewById(R.id.textNomeUsuario);
        textCaloriasQueimadas = findViewById(R.id.textCaloriasQueimadas);
        textTotalTreinos      = findViewById(R.id.textTotalTreinos);
        textAguaProgresso     = findViewById(R.id.textAguaProgresso);
        progressHidratacao    = findViewById(R.id.progressHidratacao);
        editPesoDoDia         = findViewById(R.id.editPesoDoDia);
        textPesoRegistrado    = findViewById(R.id.textPesoRegistrado);
        textFeedback          = findViewById(R.id.textFeedback);
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        viewModel.inicializar(usuarioId);
    }

    private void configurarBotoes() {
        ((MaterialButton) findViewById(R.id.btn150ml)).setOnClickListener(v -> viewModel.registrarAgua(150));
        ((MaterialButton) findViewById(R.id.btn250ml)).setOnClickListener(v -> viewModel.registrarAgua(250));
        ((MaterialButton) findViewById(R.id.btn350ml)).setOnClickListener(v -> viewModel.registrarAgua(350));
        ((MaterialButton) findViewById(R.id.btn500ml)).setOnClickListener(v -> viewModel.registrarAgua(500));

        ((MaterialButton) findViewById(R.id.btnSalvarPeso)).setOnClickListener(v -> {
            String pesoStr = editPesoDoDia.getText() != null
                    ? editPesoDoDia.getText().toString().trim() : "";
            if (pesoStr.isEmpty()) return;
            try {
                float peso = Float.parseFloat(pesoStr.replace(",", "."));
                viewModel.registrarPesoDoDia(peso);
                editPesoDoDia.clearFocus();
            } catch (NumberFormatException e) {
                textFeedback.setText("Digite um peso válido.");
                textFeedback.setVisibility(View.VISIBLE);
            }
        });
    }

    private void configurarBottomNavigation() {
        bottomNav.setSelectedItemId(R.id.nav_dashboard);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) return true;

            if (id == R.id.nav_treino) {
                Intent intent = new Intent(this, TreinoActivity.class);
                intent.putExtra(TreinoActivity.EXTRA_USUARIO_ID, usuarioId);
                startActivity(intent);
                return true;
            }

            if (id == R.id.nav_nutricao) {
                Intent intent = new Intent(this, NutricaoActivity.class);
                intent.putExtra(NutricaoActivity.EXTRA_USUARIO_ID, usuarioId);
                startActivity(intent);
                return true;
            }

            return false;
        });
    }

    private void observarDados() {
        viewModel.getUsuario().observe(this, usuario -> {
            if (usuario == null) return;
            textNomeUsuario.setText(usuario.getNomeCompleto());
            textSaudacao.setText(saudacaoPorHorario() + ",");
        });

        viewModel.getRegistroDeHoje().observe(this, registro -> {
            if (registro == null) return;
            textCaloriasQueimadas.setText(String.valueOf((int) registro.getCaloriasQueimadas()));

            int consumido = registro.getMlAguaConsumida();
            int percentual = CalculoUtils.calcularPercentualHidratacao(consumido, metaHidratacaoAtual);
            progressHidratacao.setProgress(percentual);
            textAguaProgresso.setText(
                    CalculoUtils.formatarAgua(consumido) + " / " + CalculoUtils.formatarAgua(metaHidratacaoAtual)
            );

            if (registro.getPesoRegistradoKg() != null) {
                textPesoRegistrado.setText("Registrado: " + registro.getPesoRegistradoKg() + " kg");
            } else {
                textPesoRegistrado.setText("Nenhum peso registrado hoje");
            }
        });

        viewModel.getMetaHidratacaoMl().observe(this, meta -> {
            if (meta == null) return;
            metaHidratacaoAtual = meta;
            textAguaProgresso.setText("— / " + CalculoUtils.formatarAgua(meta));
        });

        viewModel.getTotalIdasTreino().observe(this, total ->
                textTotalTreinos.setText(total != null ? String.valueOf(total) : "0")
        );

        viewModel.getMensagemFeedback().observe(this, msg -> {
            if (msg == null || msg.isEmpty()) return;
            textFeedback.setText(msg);
            textFeedback.setVisibility(View.VISIBLE);
            textFeedback.postDelayed(() -> textFeedback.setVisibility(View.GONE), 2500);
        });
    }

    private String saudacaoPorHorario() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora < 12) return "Bom dia";
        if (hora < 18) return "Boa tarde";
        return "Boa noite";
    }
}
