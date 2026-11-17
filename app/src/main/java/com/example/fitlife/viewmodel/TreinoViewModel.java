package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.fitlife.model.entity.Exercicio;
import com.example.fitlife.model.entity.RegistroDiario;
import com.example.fitlife.model.repository.NutricaoRepository;
import com.example.fitlife.model.repository.TreinoRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TreinoViewModel extends AndroidViewModel {

    private final TreinoRepository treinoRepository;
    private final NutricaoRepository nutricaoRepository;

    private final MutableLiveData<Exercicio.TipoExercicio> tipoSelecionado = new MutableLiveData<>(null);

    /**
     * Declarado sem inicialização aqui — será atribuído no construtor,
     * DEPOIS que treinoRepository já existir. Isso resolve o erro
     * "variable might not have been initialized": campos inicializados
     * inline rodam antes do construtor, então o lambda do switchMap
     * tentava usar treinoRepository quando ele ainda era null.
     */
    private final LiveData<List<Exercicio>> exerciciosFiltrados;

    private final MutableLiveData<String> mensagemFeedback = new MutableLiveData<>();
    private final MutableLiveData<Boolean> treinoConcluido = new MutableLiveData<>(false);

    private long usuarioIdAtual = -1;
    private RegistroDiario registroAtual = null;

    public TreinoViewModel(@NonNull Application application) {
        super(application);
        // 1. Repositórios inicializados primeiro
        this.treinoRepository = new TreinoRepository(application);
        this.nutricaoRepository = new NutricaoRepository(application);

        // 2. switchMap criado DEPOIS — treinoRepository já existe aqui
        this.exerciciosFiltrados = Transformations.switchMap(tipoSelecionado, tipo -> {
            if (tipo == null) {
                return treinoRepository.observarTodosExercicios();
            } else {
                return treinoRepository.observarExerciciosPorTipo(tipo);
            }
        });
    }

    public void inicializar(long usuarioId) {
        if (usuarioIdAtual != -1) return;
        this.usuarioIdAtual = usuarioId;

        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        nutricaoRepository.obterOuCriarRegistroDeHoje(usuarioId, hoje, registro -> {
            registroAtual = registro;
            treinoConcluido.postValue(registro.isFoiTreinar());
        });
    }

    public LiveData<List<Exercicio>> getExerciciosFiltrados() { return exerciciosFiltrados; }
    public LiveData<String> getMensagemFeedback()             { return mensagemFeedback; }
    public LiveData<Boolean> getTreinoConcluido()             { return treinoConcluido; }

    public void filtrarPorTipo(Exercicio.TipoExercicio tipo) {
        tipoSelecionado.setValue(tipo);
    }

    public void concluirTreino() {
        if (registroAtual == null) {
            mensagemFeedback.postValue("Aguarde um momento e tente novamente.");
            return;
        }
        if (Boolean.TRUE.equals(treinoConcluido.getValue())) {
            mensagemFeedback.postValue("Treino já marcado como concluído hoje!");
            return;
        }
        nutricaoRepository.marcarTreinoFeito(registroAtual, 300f);
        registroAtual.setFoiTreinar(true);
        treinoConcluido.postValue(true);
        mensagemFeedback.postValue("Treino do dia concluído! 💪");
    }
}
