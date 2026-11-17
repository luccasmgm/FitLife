package com.example.fitlife.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.fitlife.model.dao.UsuarioDao;
import com.example.fitlife.model.database.AppDatabase;
import com.example.fitlife.model.entity.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Repository responsável por tudo relacionado a Usuario: cadastro, login
 * e consulta de perfil. É a ÚNICA classe do app que conhece o UsuarioDao
 * diretamente — ViewModels falam só com este Repository.
 */
public class UsuarioRepository {

    private final UsuarioDao usuarioDao;

    /**
     * Recebemos Application (não Context genérico) porque é a forma
     * recomendada pelo próprio Android: Application vive durante todo o
     * ciclo de vida do app, então não corremos risco de memory leak
     * (diferente de guardar a Context de uma Activity, que pode ser
     * destruída enquanto o Repository ainda existe).
     */
    public UsuarioRepository(Application application) {
        AppDatabase database = AppDatabase.getInstancia(application);
        this.usuarioDao = database.usuarioDao();
    }

    /**
     * Cadastra um novo usuário. Roda em background (DATABASE_WRITE_EXECUTOR)
     * porque inserir no Room é uma operação bloqueante de I/O — se rodasse
     * na thread principal, travaria a UI e o Android lançaria
     * IllegalStateException.
     *
     * Retorna o resultado via callback porque o Repository não pode
     * simplesmente "retornar long" aqui: o método já encerrou na thread
     * principal antes da inserção no banco terminar na thread de background.
     */
    public interface CadastroCallback {
        void aoCadastrar(long novoUsuarioId);
        void aoFalhar(String mensagemErro);
    }

    public void cadastrar(String nomeCompleto, String email, String senhaPura,
                           float pesoKg, float alturaCm, int idade,
                           Usuario.Sexo sexo, Usuario.Rotina rotina, Usuario.Objetivo objetivo,
                           CadastroCallback callback) {

        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            // Verifica e-mail duplicado ANTES de inserir — regra de negócio
            // simples mas importante: e-mail é a chave de login, não pode repetir.
            int existentes = usuarioDao.contarPorEmail(email);
            if (existentes > 0) {
                callback.aoFalhar("Já existe uma conta cadastrada com este e-mail.");
                return;
            }

            String senhaHash = gerarHashSenha(senhaPura);
            Usuario novoUsuario = new Usuario(
                    nomeCompleto, email, senhaHash, pesoKg, alturaCm, idade, sexo, rotina, objetivo
            );

            long novoId = usuarioDao.inserir(novoUsuario);
            callback.aoCadastrar(novoId);
        });
    }

    public interface LoginCallback {
        void aoLogar(Usuario usuario);
        void aoFalhar(String mensagemErro);
    }

    /**
     * Valida credenciais comparando o hash da senha digitada com o hash
     * salvo no banco. NUNCA comparamos senha em texto puro.
     */
    public void login(String email, String senhaPura, LoginCallback callback) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            Usuario usuario = usuarioDao.buscarPorEmail(email);

            if (usuario == null) {
                callback.aoFalhar("E-mail não encontrado.");
                return;
            }

            String hashDigitado = gerarHashSenha(senhaPura);
            if (!hashDigitado.equals(usuario.getSenhaHash())) {
                callback.aoFalhar("Senha incorreta.");
                return;
            }

            callback.aoLogar(usuario);
        });
    }

    /**
     * Diferente de cadastrar/login, esta consulta retorna LiveData
     * diretamente — não precisa de callback porque o Room já entrega
     * isso de forma assíncrona e observável. É assim que o ViewModel vai
     * "ficar de olho" no usuário logado sem fazer polling manual.
     */
    public LiveData<Usuario> observarUsuario(long usuarioId) {
        return usuarioDao.observarPorId(usuarioId);
    }

    public void atualizar(Usuario usuario) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> usuarioDao.atualizar(usuario));
    }

    /**
     * Gera hash SHA-256 da senha. É uma solução simples e sem dependências
     * externas, adequada para projeto de portfólio. Em produção real,
     * o ideal seria usar BCrypt ou Argon2 (com "salt" único por usuário),
     * que são mais resistentes a ataques de força bruta — mas isso exige
     * adicionar uma biblioteca externa, o que pode ser um passo futuro.
     */
    private String gerarHashSenha(String senhaPura) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(senhaPura.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é garantido existir em qualquer JVM/Android — este
            // catch existe só porque o Java exige tratar a exceção checada.
            throw new RuntimeException("Algoritmo de hash não disponível", e);
        }
    }
}
