package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Representa uma refeição sugerida/planejada para o usuário no seu dia
 * (ex: Café da manhã, Almoço, Jantar, Snack).
 * Não é o consumo em si — apenas o "slot" de refeição do plano alimentar.
 * O que foi efetivamente consumido fica em AlimentoConsumido.
 */
@Entity(
        tableName = "refeicao",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "id",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("usuario_id")}
)
public class Refeicao {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "usuario_id")
    private long usuarioId;

    @ColumnInfo(name = "tipo")
    private TipoRefeicao tipo;

    /**
     * Horário sugerido no formato HH:mm, ex: "08:00"
     */
    @ColumnInfo(name = "horario_sugerido")
    private String horarioSugerido;

    /**
     * Meta de calorias para esta refeição, calculada pelo DietaController
     * a partir do GET total do usuário dividido entre as refeições do dia.
     */
    @ColumnInfo(name = "meta_calorias")
    private float metaCalorias;

    public Refeicao(long usuarioId, TipoRefeicao tipo, String horarioSugerido, float metaCalorias) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.horarioSugerido = horarioSugerido;
        this.metaCalorias = metaCalorias;
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

    public TipoRefeicao getTipo() {
        return tipo;
    }

    public void setTipo(TipoRefeicao tipo) {
        this.tipo = tipo;
    }

    public String getHorarioSugerido() {
        return horarioSugerido;
    }

    public void setHorarioSugerido(String horarioSugerido) {
        this.horarioSugerido = horarioSugerido;
    }

    public float getMetaCalorias() {
        return metaCalorias;
    }

    public void setMetaCalorias(float metaCalorias) {
        this.metaCalorias = metaCalorias;
    }

    public enum TipoRefeicao {
        CAFE_DA_MANHA, ALMOCO, JANTAR, SNACK
    }
}
