package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.model.repository.UsuarioRepository;

public class LoginViewModel extends AndroidViewModel {

    private final UsuarioRepository usuarioRepository;

    public enum EstadoLogin {
        CARREGANDO,
        SUCESSO,
        ERRO_CREDENCIAIS,  // e-mail não encontrado ou senha errada
        ERRO_CAMPOS        // campos vazios
    }

    private final MutableLiveData<EstadoLogin> estadoLogin = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemErro = new MutableLiveData<>();

    /**
     * Guarda o usuário logado para a Activity poder passar o ID
     * para a Dashboard (que precisará saber qual usuário está ativo).
     */
    private final MutableLiveData<Usuario> usuarioLogado = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.usuarioRepository = new UsuarioRepository(application);
    }

    public LiveData<EstadoLogin> getEstadoLogin() {
        return estadoLogin;
    }

    public LiveData<String> getMensagemErro() {
        return mensagemErro;
    }

    public LiveData<Usuario> getUsuarioLogado() {
        return usuarioLogado;
    }

    public void login(String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            estadoLogin.setValue(EstadoLogin.ERRO_CAMPOS);
            mensagemErro.setValue("Preencha o e-mail e a senha.");
            return;
        }

        estadoLogin.setValue(EstadoLogin.CARREGANDO);

        usuarioRepository.login(email, senha, new UsuarioRepository.LoginCallback() {
            @Override
            public void aoLogar(Usuario usuario) {
                usuarioLogado.postValue(usuario);
                estadoLogin.postValue(EstadoLogin.SUCESSO);
            }

            @Override
            public void aoFalhar(String mensagem) {
                mensagemErro.postValue(mensagem);
                estadoLogin.postValue(EstadoLogin.ERRO_CREDENCIAIS);
            }
        });
    }
}
