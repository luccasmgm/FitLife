package com.example.fitlife.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitlife.model.entity.AlimentoConsumido;
import com.example.fitlife.model.entity.Refeicao;

import java.util.List;

@Dao
public interface RefeicaoDao {

    @Insert
    long inserir(Refeicao refeicao);

    @Insert
    long inserirAlimentoConsumido(AlimentoConsumido alimento);

    @Update
    void atualizar(Refeicao refeicao);

    /**
     * NOVO: conta quantas refeições o usuário já tem. Usado pelo Repository
     * para decidir se precisa criar as refeições padrão (café, almoço,
     * lanche e jantar) na primeira vez que o usuário abre a tela de Nutrição.
     * Retorna int direto (não LiveData), então só pode rodar em background.
     */
    @Query("SELECT COUNT(*) FROM refeicao WHERE usuario_id = :usuarioId")
    int contarRefeicoesDoUsuario(long usuarioId);

    @Query("SELECT * FROM refeicao WHERE usuario_id = :usuarioId ORDER BY horario_sugerido ASC")
    LiveData<List<Refeicao>> observarRefeicoesDoUsuario(long usuarioId);

    @Query("SELECT * FROM alimento_consumido WHERE registro_diario_id = :registroDiarioId")
    LiveData<List<AlimentoConsumido>> observarAlimentosDoDia(long registroDiarioId);

    @Query("SELECT SUM(calorias) FROM alimento_consumido WHERE registro_diario_id = :registroDiarioId")
    LiveData<Float> observarTotalCaloriasConsumidasNoDia(long registroDiarioId);
}
