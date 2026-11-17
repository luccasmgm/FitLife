package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Registra a carga utilizada por um usuário em um exercício específico,
 * em uma data específica. Permite acompanhar evolução de carga ao longo
 * do tempo (progressão de treino). Válido apenas para exercícios de
 * musculação (Exercicio.permiteCarga == true).
 */
@Entity(
        tableName = "carga_exercicio",
        foreignKeys = {
                @ForeignKey(
                        entity = Usuario.class,
                        parentColumns = "id",
                        childColumns = "usuario_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Exercicio.class,
                        parentColumns = "id",
                        childColumns = "exercicio_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("usuario_id"), @Index("exercicio_id")}
)
public class CargaExercicio {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "usuario_id")
    private long usuarioId;

    @ColumnInfo(name = "exercicio_id")
    private long exercicioId;

    /**
     * Data no formato yyyy-MM-dd.
     */
    @ColumnInfo(name = "data")
    private String data;

    @ColumnInfo(name = "carga_kg")
    private float cargaKg;

    @ColumnInfo(name = "series")
    private int series;

    @ColumnInfo(name = "repeticoes")
    private int repeticoes;

    public CargaExercicio(long usuarioId, long exercicioId, String data,
                           float cargaKg, int series, int repeticoes) {
        this.usuarioId = usuarioId;
        this.exercicioId = exercicioId;
        this.data = data;
        this.cargaKg = cargaKg;
        this.series = series;
        this.repeticoes = repeticoes;
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public long getExercicioId() {
        return exercicioId;
    }

    public void setExercicioId(long exercicioId) {
        this.exercicioId = exercicioId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public float getCargaKg() {
        return cargaKg;
    }

    public void setCargaKg(float cargaKg) {
        this.cargaKg = cargaKg;
    }

    public int getSeries() {
        return series;
    }

    public void setSeries(int series) {
        this.series = series;
    }

    public int getRepeticoes() {
        return repeticoes;
    }

    public void setRepeticoes(int repeticoes) {
        this.repeticoes = repeticoes;
    }
}
