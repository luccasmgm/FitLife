package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.model.repository.UsuarioRepository;

/**
 * ViewModel da tela de Cadastro.
 *
 * Estende AndroidViewModel (e não apenas ViewModel) porque precisamos
 * passar Application para o UsuarioRepository. AndroidViewModel já
 * recebe Application no construtor e o guarda com segurança — diferente
 * de guardar uma Activity ou Context comum, que causaria memory leak.
 *
 * O ViewModel sobrevive a mudanças de configuração (ex: rotação de tela).
 * Se o usuário estiver preenchendo o formulário e girar o celular, os
 * dados não são perdidos porque o ViewModel não é destruído junto com
 * a Activity — só a View é recriada.
 */
public class CadastroViewModel extends AndroidViewModel {

    private final UsuarioRepository usuarioRepository;

    /**
     * Estados possíveis da operação de cadastro.
     * Usamos um enum encapsulado aqui para a Activity poder reagir
     * a cada estado sem precisar interpretar Strings ou códigos numéricos.
     */
    public enum EstadoCadastro {
        CARREGANDO,     // operação em andamento (exibir loading)
        SUCESSO,        // cadastro concluído — ir para Login
        ERRO_EMAIL,     // e-mail já cadastrado ou inválido
        ERRO_CAMPOS     // campos obrigatórios faltando ou inválidos
    }

    /**
     * MutableLiveData é privado — só o ViewModel pode alterar o valor.
     * A Activity recebe apenas o LiveData (imutável) via getEstadoCadastro().
     * Esse padrão evita que a View altere o estado diretamente,
     * mantendo o fluxo de dados sempre unidirecional: ViewModel → View.
     */
    private final MutableLiveData<EstadoCadastro> estadoCadastro = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemErro = new MutableLiveData<>();

    public CadastroViewModel(@NonNull Application application) {
        super(application);
        this.usuarioRepository = new UsuarioRepository(application);
    }

    public LiveData<EstadoCadastro> getEstadoCadastro() {
        return estadoCadastro;
    }

    public LiveData<String> getMensagemErro() {
        return mensagemErro;
    }

    /**
     * Ponto de entrada da tela: a Activity chama este método quando o
     * usuário pressiona o botão "Cadastrar". O ViewModel valida os campos,
     * chama o Repository e publica o resultado via LiveData.
     *
     * A Activity não precisa saber NADA do que acontece aqui dentro —
     * ela só observa estadoCadastro e reage ao novo valor.
     */
    public void cadastrar(String nomeCompleto, String email, String senha, String confirmacaoSenha,
                           String pesoStr, String alturaStr, String idadeStr,
                           Usuario.Sexo sexo, Usuario.Rotina rotina, Usuario.Objetivo objetivo) {

        // Validação de campos obrigatórios — feita no ViewModel, não na Activity,
        // porque é regra de apresentação/negócio, não de interface visual.
        if (nomeCompleto.isEmpty() || email.isEmpty() || senha.isEmpty()
                || pesoStr.isEmpty() || alturaStr.isEmpty() || idadeStr.isEmpty()
                || sexo == null || rotina == null || objetivo == null) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("Preencha todos os campos antes de continuar.");
            return;
        }

        if (!senha.equals(confirmacaoSenha)) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("As senhas não coincidem.");
            return;
        }

        if (senha.length() < 6) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("A senha deve ter pelo menos 6 caracteres.");
            return;
        }

        float peso, altura;
        int idade;
        try {
            peso = Float.parseFloat(pesoStr.replace(",", "."));
            altura = Float.parseFloat(alturaStr.replace(",", "."));
            idade = Integer.parseInt(idadeStr);
        } catch (NumberFormatException e) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("Peso, altura e idade devem ser números válidos.");
            return;
        }

        // Validações de intervalo razoável
        if (peso < 20 || peso > 300) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("Insira um peso válido (entre 20 e 300 kg).");
            return;
        }

        if (altura < 100 || altura > 250) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("Insira uma altura válida (entre 100 e 250 cm).");
            return;
        }

        if (idade < 10 || idade > 120) {
            estadoCadastro.setValue(EstadoCadastro.ERRO_CAMPOS);
            mensagemErro.setValue("Insira uma idade válida.");
            return;
        }

        // Campos válidos — inicia a operação de cadastro
        estadoCadastro.setValue(EstadoCadastro.CARREGANDO);

        usuarioRepository.cadastrar(
                nomeCompleto, email, senha, peso, altura, idade, sexo, rotina, objetivo,
                new UsuarioRepository.CadastroCallback() {
                    @Override
                    public void aoCadastrar(long novoUsuarioId) {
                        // Callback vem da thread de background (DATABASE_WRITE_EXECUTOR).
                        // postValue (e não setValue) é obrigatório aqui: postValue
                        // enfileira a atualização para a thread principal de forma
                        // segura. setValue lançaria exceção se chamado fora da UI thread.
                        estadoCadastro.postValue(EstadoCadastro.SUCESSO);
                    }

                    @Override
                    public void aoFalhar(String mensagem) {
                        estadoCadastro.postValue(EstadoCadastro.ERRO_EMAIL);
                        mensagemErro.postValue(mensagem);
                    }
                }
        );
    }
}
