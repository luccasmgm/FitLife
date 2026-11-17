package com.example.fitlife.view.cadastro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.fitlife.R;
import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.view.login.LoginActivity;
import com.example.fitlife.viewmodel.CadastroViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * View da tela de Cadastro. Sua única responsabilidade é:
 * 1. Exibir os campos e reagir a interações do usuário
 * 2. Passar os dados para o ViewModel quando o botão for pressionado
 * 3. Observar o LiveData do ViewModel e atualizar a UI conforme o estado
 *
 * A Activity NÃO valida campos, NÃO acessa banco, NÃO conhece Repository.
 * Toda lógica está no CadastroViewModel.
 */
public class CadastroActivity extends AppCompatActivity {

    // ViewModel — criado via ViewModelProvider, não com "new CadastroViewModel()".
    // ViewModelProvider garante que a mesma instância seja reutilizada se a
    // Activity for recriada (ex: rotação de tela), preservando o estado.
    private CadastroViewModel viewModel;

    // Referências aos elementos do layout
    private TextInputEditText editNome, editEmail, editSenha, editConfirmacaoSenha;
    private TextInputEditText editPeso, editAltura, editIdade;
    private RadioGroup radioGroupSexo;
    private Spinner spinnerRotina, spinnerObjetivo;
    private MaterialButton btnCadastrar;
    private ProgressBar progressBar;
    private TextView textErro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        inicializarViewModel();
        vincularViews();
        configurarSpinners();
        configurarBotoes();
        observarEstado();
    }

    private void inicializarViewModel() {
        // ViewModelProvider usa a Activity como "dono" do ViewModel.
        // Enquanto esta Activity existir (mesmo após rotação), o mesmo
        // ViewModel será retornado — os dados digitados não se perdem.
        viewModel = new ViewModelProvider(this).get(CadastroViewModel.class);
    }

    private void vincularViews() {
        editNome              = findViewById(R.id.editNome);
        editEmail             = findViewById(R.id.editEmail);
        editSenha             = findViewById(R.id.editSenha);
        editConfirmacaoSenha  = findViewById(R.id.editConfirmacaoSenha);
        editPeso              = findViewById(R.id.editPeso);
        editAltura            = findViewById(R.id.editAltura);
        editIdade             = findViewById(R.id.editIdade);
        radioGroupSexo        = findViewById(R.id.radioGroupSexo);
        spinnerRotina         = findViewById(R.id.spinnerRotina);
        spinnerObjetivo       = findViewById(R.id.spinnerObjetivo);
        btnCadastrar          = findViewById(R.id.btnCadastrar);
        progressBar           = findViewById(R.id.progressBar);
        textErro              = findViewById(R.id.textErro);
    }

    private void configurarSpinners() {
        // Rotina — rótulos amigáveis para cada valor do enum
        String[] rotulosRotina = {
                "Sedentário (pouco ou sem exercício)",
                "Levemente ativo (exercício leve 1–3x/semana)",
                "Moderadamente ativo (exercício moderado 3–5x/semana)",
                "Muito ativo (exercício intenso 6–7x/semana)"
        };
        ArrayAdapter<String> adapterRotina = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, rotulosRotina
        );
        adapterRotina.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRotina.setAdapter(adapterRotina);

        // Objetivo
        String[] rotulosObjetivo = {
                "Perda de peso",
                "Manutenção",
                "Ganho de massa muscular"
        };
        ArrayAdapter<String> adapterObjetivo = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, rotulosObjetivo
        );
        adapterObjetivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerObjetivo.setAdapter(adapterObjetivo);
    }

    private void configurarBotoes() {
        btnCadastrar.setOnClickListener(v -> tentarCadastrar());

        // Link "Já tem conta? Entrar" — volta para Login sem empilhar
        // uma nova Activity, por isso usamos finish() ao invés de
        // startActivity(). Como CadastroActivity foi aberta por LoginActivity
        // via startActivity(), finish() a remove da pilha e revela o Login.
        findViewById(R.id.textIrParaLogin).setOnClickListener(v -> finish());
    }

    private void tentarCadastrar() {
        // Lê os valores dos campos — getText() pode retornar null em
        // TextInputEditText, por isso o trim() com fallback para string vazia.
        String nome    = editNome.getText()             != null ? editNome.getText().toString().trim() : "";
        String email   = editEmail.getText()            != null ? editEmail.getText().toString().trim() : "";
        String senha   = editSenha.getText()            != null ? editSenha.getText().toString() : "";
        String conf    = editConfirmacaoSenha.getText() != null ? editConfirmacaoSenha.getText().toString() : "";
        String peso    = editPeso.getText()             != null ? editPeso.getText().toString().trim() : "";
        String altura  = editAltura.getText()           != null ? editAltura.getText().toString().trim() : "";
        String idade   = editIdade.getText()            != null ? editIdade.getText().toString().trim() : "";

        // Converte o RadioGroup em enum
        Usuario.Sexo sexo = null;
        int radioSelecionado = radioGroupSexo.getCheckedRadioButtonId();
        if (radioSelecionado == R.id.radioMasculino) sexo = Usuario.Sexo.MASCULINO;
        else if (radioSelecionado == R.id.radioFeminino) sexo = Usuario.Sexo.FEMININO;

        // Converte posição do Spinner em enum — a ordem dos itens no array
        // deve ser a mesma que a ordem dos valores no enum.
        Usuario.Rotina rotina = Usuario.Rotina.values()[spinnerRotina.getSelectedItemPosition()];
        Usuario.Objetivo objetivo = Usuario.Objetivo.values()[spinnerObjetivo.getSelectedItemPosition()];

        // Delega ao ViewModel — a Activity não valida nada além de coletar os valores
        viewModel.cadastrar(nome, email, senha, conf, peso, altura, idade, sexo, rotina, objetivo);
    }

    /**
     * Aqui está a essência do MVVM na prática: a Activity "assina" os
     * LiveData do ViewModel e atualiza a UI conforme os valores mudam.
     * Cada observe() recebe um lambda que só roda quando o valor muda —
     * nunca fica em polling ou loop verificando estado manualmente.
     */
    private void observarEstado() {
        viewModel.getEstadoCadastro().observe(this, estado -> {
            switch (estado) {
                case CARREGANDO:
                    btnCadastrar.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    textErro.setVisibility(View.GONE);
                    break;

                case SUCESSO:
                    // Cadastro feito — vai para Login e limpa a pilha de volta,
                    // para o usuário não poder pressionar "voltar" e cair no
                    // formulário já preenchido.
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                    break;

                case ERRO_EMAIL:
                case ERRO_CAMPOS:
                    btnCadastrar.setEnabled(true);
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
    }
}
