package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife.model.entity.RegistroDiario;
import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.model.repository.NutricaoRepository;
import com.example.fitlife.model.repository.UsuarioRepository;
import com.example.fitlife.util.CalculoUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ViewModel da Dashboard. Centraliza todos os dados que a tela principal
 * precisa exibir: resumo do dia, progresso de hidratação, histórico de
 * treinos e peso atual.
 *
 * Recebe o usuarioId via método inicializar() chamado pela Activity logo
 * após o onCreate — padrão comum quando o ViewModel precisa de um parâmetro
 * externo (o ID vem do Intent do login) mas não pode recebê-lo no construtor
 * (o construtor do AndroidViewModel só aceita Application).
 */
public class DashboardViewModel extends AndroidViewModel {

    private final UsuarioRepository usuarioRepository;
    private final NutricaoRepository nutricaoRepository;

    // LiveData expostos para a Activity observar
    private LiveData<Usuario> usuario;
    private LiveData<RegistroDiario> registroDeHoje;
    private LiveData<Integer> totalIdasTreino;
    private LiveData<Float> totalCaloriasQueimadas;

    /**
     * MutableLiveData para estados que o ViewModel controla diretamente
     * (não vêm do banco via LiveData automático).
     */
    private final MutableLiveData<Integer> metaHidratacaoMl = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemFeedback = new MutableLiveData<>();

    // Guardamos o ID e o registro atual para usar nos métodos de ação
    private long usuarioIdAtual = -1;
    private RegistroDiario registroAtual = null;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.usuarioRepository = new UsuarioRepository(application);
        this.nutricaoRepository = new NutricaoRepository(application);
    }

    /**
     * Chamado pela Activity no onCreate, logo após receber o usuarioId
     * do Intent. Usa um flag (usuarioIdAtual != -1) para garantir que
     * a inicialização só acontece uma vez — sem isso, girar a tela chamaria
     * inicializar() de novo e recriaria os LiveData desnecessariamente.
     */
    public void inicializar(long usuarioId) {
        if (usuarioIdAtual != -1) return;
        this.usuarioIdAtual = usuarioId;

        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Conecta os LiveData do banco — a partir daqui, qualquer mudança
        // nas tabelas notifica a Activity automaticamente sem nenhum código extra
        usuario             = usuarioRepository.observarUsuario(usuarioId);
        registroDeHoje      = nutricaoRepository.observarRegistroDoDia(usuarioId, hoje);
        totalIdasTreino     = nutricaoRepository.observarTotalIdasTreino(usuarioId);
        totalCaloriasQueimadas = nutricaoRepository.observarTotalCaloriasQueimadas(usuarioId);

        // Garante que o registro de hoje existe no banco (padrão "get or create")
        nutricaoRepository.obterOuCriarRegistroDeHoje(usuarioId, hoje, registro -> {
            registroAtual = registro;
        });

        // Observa o usuário para calcular a meta de hidratação assim que
        // os dados chegarem do banco.
        usuario.observeForever(u -> {
            if (u != null) {
                int meta = CalculoUtils.calcularMetaHidratacao(u.getPesoKg(), u.getRotina());
                metaHidratacaoMl.postValue(meta);
            }
        });
    }

    // ---------- Getters dos LiveData ----------

    public LiveData<Usuario> getUsuario()                       { return usuario; }
    public LiveData<RegistroDiario> getRegistroDeHoje()         { return registroDeHoje; }
    public LiveData<Integer> getTotalIdasTreino()               { return totalIdasTreino; }
    public LiveData<Float> getTotalCaloriasQueimadas()          { return totalCaloriasQueimadas; }
    public LiveData<Integer> getMetaHidratacaoMl()              { return metaHidratacaoMl; }
    public LiveData<String> getMensagemFeedback()               { return mensagemFeedback; }

    // ---------- Ações do usuário ----------

    /**
     * Registra uma quantidade de água consumida agora.
     * Opções pré-definidas na UI: 150ml, 250ml, 350ml, 500ml.
     */
    public void registrarAgua(int mlAdicional) {
        if (registroAtual == null) {
            mensagemFeedback.postValue("Aguarde um momento e tente novamente.");
            return;
        }
        nutricaoRepository.adicionarAguaConsumida(registroAtual, mlAdicional);
        registroAtual.setMlAguaConsumida(registroAtual.getMlAguaConsumida() + mlAdicional);
        mensagemFeedback.postValue("+" + mlAdicional + "ml registrado!");
    }

    /**
     * Atualiza o peso do dia no registro diário.
     * Chamado quando o usuário informa o peso na Dashboard.
     */
    public void registrarPesoDoDia(float pesoKg) {
        if (registroAtual == null) return;
        nutricaoRepository.atualizarPesoDoDia(registroAtual, pesoKg);
        registroAtual.setPesoRegistradoKg(pesoKg);
        mensagemFeedback.postValue("Peso atualizado: " + pesoKg + " kg");
    }
}
