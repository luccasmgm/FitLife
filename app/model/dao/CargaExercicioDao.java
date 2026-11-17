package com.example.fitlife.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.fitlife.model.entity.CargaExercicio;

import java.util.List;

@Dao
public interface CargaExercicioDao {

    @Insert
    long inserir(CargaExercicio carga);

    /**
     * Histórico de cargas de um usuário em um exercício específico,
     * ordenado da mais recente para a mais antiga — útil para mostrar
     * evolução de carga (ex: gráfico de progressão) na tela de detalhe
     * do exercício.
     */
    @Query("SELECT * FROM carga_exercicio WHERE usuario_id = :usuarioId AND exercicio_id = :exercicioId ORDER BY data DESC")
    LiveData<List<CargaExercicio>> observarHistoricoPorExercicio(long usuarioId, long exercicioId);

    @Query("SELECT * FROM carga_exercicio WHERE usuario_id = :usuarioId AND exercicio_id = :exercicioId ORDER BY data DESC LIMIT 1")
    CargaExercicio buscarUltimaCarga(long usuarioId, long exercicioId);
}
