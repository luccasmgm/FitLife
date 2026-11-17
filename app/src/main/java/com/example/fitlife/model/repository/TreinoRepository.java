package com.example.fitlife.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.fitlife.model.dao.CargaExercicioDao;
import com.example.fitlife.model.dao.ExercicioDao;
import com.example.fitlife.model.database.AppDatabase;
import com.example.fitlife.model.entity.CargaExercicio;
import com.example.fitlife.model.entity.Exercicio;

import java.util.List;

public class TreinoRepository {

    private final ExercicioDao exercicioDao;
    private final CargaExercicioDao cargaExercicioDao;

    public TreinoRepository(Application application) {
        AppDatabase database = AppDatabase.getInstancia(application);
        this.exercicioDao = database.exercicioDao();
        this.cargaExercicioDao = database.cargaExercicioDao();
    }

    public LiveData<List<Exercicio>> observarTodosExercicios() {
        return exercicioDao.observarTodos();
    }

    public LiveData<List<Exercicio>> observarExerciciosPorTipo(Exercicio.TipoExercicio tipo) {
        return exercicioDao.observarPorTipo(tipo);
    }

    public interface RegistrarCargaCallback {
        void aoRegistrar(long novoRegistroId);
    }

    /**
     * Só registra carga se o exercício permitir (musculação).
     * Regra de negócio protegida no Repository — a View não precisa saber disso.
     */
    public void registrarCarga(long usuarioId, long exercicioId, String data,
                                float cargaKg, int series, int repeticoes,
                                RegistrarCargaCallback callback) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            Exercicio exercicio = exercicioDao.buscarPorId(exercicioId);
            if (exercicio == null || !exercicio.isPermiteCarga()) return;

            CargaExercicio novaCarga = new CargaExercicio(
                    usuarioId, exercicioId, data, cargaKg, series, repeticoes
            );
            long novoId = cargaExercicioDao.inserir(novaCarga);
            if (callback != null) callback.aoRegistrar(novoId);
        });
    }

    public LiveData<List<CargaExercicio>> observarHistoricoDeCarga(long usuarioId, long exercicioId) {
        return cargaExercicioDao.observarHistoricoPorExercicio(usuarioId, exercicioId);
    }
}
