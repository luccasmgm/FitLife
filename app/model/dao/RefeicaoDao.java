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
     * Lista as refeições planejadas do usuário (Café, Almoço, Jantar, Snack),
     * ordenadas pelo horário sugerido.
     */
    @Query("SELECT * FROM refeicao WHERE usuario_id = :usuarioId ORDER BY horario_sugerido ASC")
    LiveData<List<Refeicao>> observarRefeicoesDoUsuario(long usuarioId);

    /**
     * Lista os alimentos consumidos em um registro diário específico,
     * usado para somar o total de calorias ingeridas no dia na Dashboard.
     */
    @Query("SELECT * FROM alimento_consumido WHERE registro_diario_id = :registroDiarioId")
    LiveData<List<AlimentoConsumido>> observarAlimentosDoDia(long registroDiarioId);

    @Query("SELECT SUM(calorias) FROM alimento_consumido WHERE registro_diario_id = :registroDiarioId")
    LiveData<Float> observarTotalCaloriasConsumidasNoDia(long registroDiarioId);
}
