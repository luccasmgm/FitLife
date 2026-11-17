package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Representa um alimento que o usuário efetivamente registrou ter consumido,
 * vinculado a uma Refeicao (qual tipo de refeição) e a um RegistroDiario
 * (em qual dia). É isso que alimenta o botão "registrar consumo" da tela
 * de nutrição e os totais da dashboard.
 */
@Entity(
        tableName = "alimento_consumido",
        foreignKeys = {
                @ForeignKey(
                        entity = Refeicao.class,
                        parentColumns = "id",
                        childColumns = "refeicao_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = RegistroDiario.class,
                        parentColumns = "id",
                        childColumns = "registro_diario_id",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {@Index("refeicao_id"), @Index("registro_diario_id")}
)
public class AlimentoConsumido {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "refeicao_id")
    private long refeicaoId;

    @ColumnInfo(name = "registro_diario_id")
    private long registroDiarioId;

    @ColumnInfo(name = "nome_alimento")
    private String nomeAlimento;

    /**
     * Quantidade em gramas (ou unidade definida na UI, ex: "1 unidade").
     */
    @ColumnInfo(name = "quantidade")
    private String quantidade;

    @ColumnInfo(name = "calorias")
    private float calorias;

    public AlimentoConsumido(long refeicaoId, long registroDiarioId, String nomeAlimento,
                              String quantidade, float calorias) {
        this.refeicaoId = refeicaoId;
        this.registroDiarioId = registroDiarioId;
        this.nomeAlimento = nomeAlimento;
        this.quantidade = quantidade;
        this.calorias = calorias;
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRefeicaoId() {
        return refeicaoId;
    }

    public void setRefeicaoId(long refeicaoId) {
        this.refeicaoId = refeicaoId;
    }

    public long getRegistroDiarioId() {
        return registroDiarioId;
    }

    public void setRegistroDiarioId(long registroDiarioId) {
        this.registroDiarioId = registroDiarioId;
    }

    public String getNomeAlimento() {
        return nomeAlimento;
    }

    public void setNomeAlimento(String nomeAlimento) {
        this.nomeAlimento = nomeAlimento;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    public float getCalorias() {
        return calorias;
    }

    public void setCalorias(float calorias) {
        this.calorias = calorias;
    }
}
