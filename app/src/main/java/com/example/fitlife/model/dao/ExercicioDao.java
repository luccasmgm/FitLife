package com.example.fitlife.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.fitlife.model.entity.Exercicio;

import java.util.List;

@Dao
public interface ExercicioDao {

    /**
     * IGNORE evita duplicar exercícios do catálogo caso o seed seja
     * executado mais de uma vez (ex: app reinstalado em modo de teste).
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void inserirTodos(List<Exercicio> exercicios);

    @Query("SELECT * FROM exercicio WHERE tipo = :tipo")
    LiveData<List<Exercicio>> observarPorTipo(Exercicio.TipoExercicio tipo);

    @Query("SELECT * FROM exercicio")
    LiveData<List<Exercicio>> observarTodos();

    @Query("SELECT * FROM exercicio WHERE id = :exercicioId LIMIT 1")
    Exercicio buscarPorId(long exercicioId);

    @Query("SELECT COUNT(*) FROM exercicio")
    int contarExercicios();
}
