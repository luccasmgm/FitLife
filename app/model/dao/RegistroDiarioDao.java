package com.example.fitlife.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitlife.model.entity.RegistroDiario;

import java.util.List;

@Dao
public interface RegistroDiarioDao {

    @Insert
    long inserir(RegistroDiario registro);

    @Update
    void atualizar(RegistroDiario registro);

    /**
     * Busca o registro de um dia específico. Como cada usuário tem no
     * máximo 1 registro por dia, o Controller usa este método para
     * verificar se já existe um registro de "hoje" antes de criar um novo.
     */
    @Query("SELECT * FROM registro_diario WHERE usuario_id = :usuarioId AND data = :data LIMIT 1")
    RegistroDiario buscarPorUsuarioEData(long usuarioId, String data);

    @Query("SELECT * FROM registro_diario WHERE usuario_id = :usuarioId AND data = :data LIMIT 1")
    LiveData<RegistroDiario> observarPorUsuarioEData(long usuarioId, String data);

    /**
     * Usado na Dashboard para listar o histórico (gráfico de evolução,
     * contagem de idas à academia, etc).
     */
    @Query("SELECT * FROM registro_diario WHERE usuario_id = :usuarioId ORDER BY data DESC")
    LiveData<List<RegistroDiario>> observarHistorico(long usuarioId);

    @Query("SELECT COUNT(*) FROM registro_diario WHERE usuario_id = :usuarioId AND foi_treinar = 1")
    LiveData<Integer> observarTotalIdasTreino(long usuarioId);

    @Query("SELECT SUM(calorias_queimadas) FROM registro_diario WHERE usuario_id = :usuarioId")
    LiveData<Float> observarTotalCaloriasQueimadas(long usuarioId);
}
