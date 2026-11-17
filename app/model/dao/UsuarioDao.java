package com.example.fitlife.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.fitlife.model.entity.Usuario;

@Dao
public interface UsuarioDao {

    @Insert
    long inserir(Usuario usuario);

    @Update
    void atualizar(Usuario usuario);

    @Delete
    void deletar(Usuario usuario);

    /**
     * Usado na tela de login para validar credenciais.
     * A comparação de senha (hash) é feita no Controller, nunca aqui.
     */
    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    Usuario buscarPorEmail(String email);

    @Query("SELECT * FROM usuario WHERE id = :usuarioId LIMIT 1")
    LiveData<Usuario> observarPorId(long usuarioId);

    @Query("SELECT * FROM usuario WHERE id = :usuarioId LIMIT 1")
    Usuario buscarPorId(long usuarioId);

    @Query("SELECT COUNT(*) FROM usuario WHERE email = :email")
    int contarPorEmail(String email);
}
