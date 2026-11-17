package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fitlife.model.entity.CargaExercicio;
import com.example.fitlife.model.entity.Exercicio;
import com.example.fitlife.model.repository.TreinoRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel da tela de detalhe de um exercício.
 * Responsável por: exibir o exercício, listar histórico de cargas,
 * e registrar uma nova carga (somente para musculação).
 */
public class ExercicioDetalheViewModel extends AndroidViewModel {

    private final TreinoRepository treinoRepository;

    private final MutableLiveData<Exercicio> exercicioAtual = new MutableLiveData<>();
    private final MutableLiveData<String> mensagemFeedback = new MutableLiveData<>();
    private LiveData<List<CargaExercicio>> historicoCarga;

    private long usuarioId = -1;
    private long exercicioId = -1;

    public ExercicioDetalheViewModel(@NonNull Application application) {
        super(application);
        this.treinoRepository = new TreinoRepository(application);
    }

    public void inicializar(long usuarioId, Exercicio exercicio) {
        if (this.exercicioId != -1) return;
        this.usuarioId = usuarioId;
        this.exercicioId = exercicio.getId();

        exercicioAtual.setValue(exercicio);

        // Inicia observação do histórico de cargas para este usuário e exercício
        historicoCarga = treinoRepository.observarHistoricoDeCarga(usuarioId, exercicioId);
    }

    // ---------- Getters ----------

    public LiveData<Exercicio> getExercicioAtual() {
        return exercicioAtual;
    }

    public LiveData<List<CargaExercicio>> getHistoricoCarga() {
        return historicoCarga;
    }

    public LiveData<String> getMensagemFeedback() {
        return mensagemFeedback;
    }

    // ---------- Ações ----------

    public void registrarCarga(String cargaStr, String seriesStr, String repeticoesStr) {
        // Validação de campos
        if (cargaStr.isEmpty() || seriesStr.isEmpty() || repeticoesStr.isEmpty()) {
            mensagemFeedback.setValue("Preencha carga, séries e repetições.");
            return;
        }

        float carga;
        int series, repeticoes;
        try {
            carga      = Float.parseFloat(cargaStr.replace(",", "."));
            series     = Integer.parseInt(seriesStr);
            repeticoes = Integer.parseInt(repeticoesStr);
        } catch (NumberFormatException e) {
            mensagemFeedback.setValue("Valores inválidos. Use números.");
            return;
        }

        if (carga <= 0 || series <= 0 || repeticoes <= 0) {
            mensagemFeedback.setValue("Os valores devem ser maiores que zero.");
            return;
        }

        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        treinoRepository.registrarCarga(usuarioId, exercicioId, hoje, carga, series, repeticoes,
                novoId -> mensagemFeedback.postValue("Carga registrada com sucesso!")
        );
    }
}
