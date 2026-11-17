package com.example.fitlife.view.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitlife.R;
import com.example.fitlife.view.cadastro.CadastroActivity;
import com.example.fitlife.view.dashboard.DashboardActivity;
import com.example.fitlife.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText editEmail, editSenha;
    private MaterialButton btnEntrar;
    private ProgressBar progressBar;
    private TextView textErro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inicializarViewModel();
        vincularViews();
        configurarBotoes();
        observarEstado();
    }

    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
    }

    private void vincularViews() {
        editEmail   = findViewById(R.id.editEmail);
        editSenha   = findViewById(R.id.editSenha);
        btnEntrar   = findViewById(R.id.btnEntrar);
        progressBar = findViewById(R.id.progressBar);
        textErro    = findViewById(R.id.textErro);
    }

    private void configurarBotoes() {
        btnEntrar.setOnClickListener(v -> tentarLogin());
        findViewById(R.id.textIrParaCadastro).setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class))
        );
    }

    private void tentarLogin() {
        String email = editEmail.getText() != null ? editEmail.getText().toString().trim() : "";
        String senha = editSenha.getText() != null ? editSenha.getText().toString() : "";
        viewModel.login(email, senha);
    }

    private void observarEstado() {
        viewModel.getEstadoLogin().observe(this, estado -> {
            switch (estado) {
                case CARREGANDO:
                    btnEntrar.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    textErro.setVisibility(View.GONE);
                    break;
                case SUCESSO:
                    progressBar.setVisibility(View.GONE);
                    break;
                case ERRO_CREDENCIAIS:
                case ERRO_CAMPOS:
                    btnEntrar.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    textErro.setVisibility(View.VISIBLE);
                    break;
            }
        });

        viewModel.getMensagemErro().observe(this, mensagem -> {
            if (mensagem != null && !mensagem.isEmpty()) {
                textErro.setText(mensagem);
            }
        });

        // Navegação para Dashboard com o ID do usuário logado
        viewModel.getUsuarioLogado().observe(this, usuario -> {
            if (usuario != null) {
                Intent intent = new Intent(this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.EXTRA_USUARIO_ID, usuario.getId());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }
}
