package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Catálogo geral de exercícios disponíveis no app (musculação, cardio, mobilidade).
 * NÃO pertence a um usuário específico — é uma tabela "mestre" populada
 * uma vez (seed) na criação do banco. Cada exercício tem vídeo de execução.
 */
@Entity(tableName = "exercicio")
public class Exercicio {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "nome")
    private String nome;

    @ColumnInfo(name = "tipo")
    private TipoExercicio tipo;

    @ColumnInfo(name = "grupo_muscular")
    private String grupoMuscular; // ex: "Peito", "Pernas", "Costas" — null para cardio/mobilidade

    /**
     * URL do vídeo demonstrativo (YouTube embed, link direto, ou nome de
     * arquivo em res/raw se preferir empacotar localmente).
     */
    @ColumnInfo(name = "url_video")
    private String urlVideo;

    /**
     * Indica se este exercício possui campo de carga (somente musculação,
     * conforme regra de negócio do roteiro).
     */
    @ColumnInfo(name = "permite_carga")
    private boolean permiteCarga;

    public Exercicio(String nome, TipoExercicio tipo, String grupoMuscular,
                      String urlVideo, boolean permiteCarga) {
        this.nome = nome;
        this.tipo = tipo;
        this.grupoMuscular = grupoMuscular;
        this.urlVideo = urlVideo;
        this.permiteCarga = permiteCarga;
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoExercicio getTipo() {
        return tipo;
    }

    public void setTipo(TipoExercicio tipo) {
        this.tipo = tipo;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
    }

    public String getUrlVideo() {
        return urlVideo;
    }

    public void setUrlVideo(String urlVideo) {
        this.urlVideo = urlVideo;
    }

    public boolean isPermiteCarga() {
        return permiteCarga;
    }

    public void setPermiteCarga(boolean permiteCarga) {
        this.permiteCarga = permiteCarga;
    }

    public enum TipoExercicio {
        MUSCULACAO, CARDIO, MOBILIDADE
    }
}
