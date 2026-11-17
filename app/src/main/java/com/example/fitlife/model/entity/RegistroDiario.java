package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Representa o resumo de UM dia para UM usuário.
 * Alimenta diretamente a tela de Dashboard (resumo):
 * - idas à academia / treinos feitos
 * - calorias queimadas
 * - peso perdido/ganho (inserido manualmente)
 * - hidratação consumida no dia
 */
@Entity(
        tableName = "registro_diario",
        foreignKeys = @ForeignKey(
                entity = Usuario.class,
                parentColumns = "id",
                childColumns = "usuario_id",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("usuario_id")}
)
public class RegistroDiario {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "usuario_id")
    private long usuarioId;

    /**
     * Data no formato yyyy-MM-dd (string) para facilitar query e comparação.
     */
    @ColumnInfo(name = "data")
    private String data;

    @ColumnInfo(name = "foi_treinar")
    private boolean foiTreinar;

    @ColumnInfo(name = "calorias_queimadas")
    private float caloriasQueimadas;

    /**
     * Peso registrado manualmente pelo usuário NESTE dia.
     * Permite calcular ganho/perda comparando com registros anteriores.
     */
    @ColumnInfo(name = "peso_registrado_kg")
    private Float pesoRegistradoKg; // Float (objeto) pois pode ser nulo (nem todo dia o usuário pesa)

    @ColumnInfo(name = "ml_agua_consumida")
    private int mlAguaConsumida;

    /**
     * Meta de hidratação calculada para o dia (vem do DietaController),
     * guardada aqui para a dashboard mostrar "350ml / 2500ml" sem recalcular.
     */
    @ColumnInfo(name = "ml_agua_meta")
    private int mlAguaMeta;

    public RegistroDiario(long usuarioId, String data) {
        this.usuarioId = usuarioId;
        this.data = data;
        this.foiTreinar = false;
        this.caloriasQueimadas = 0f;
        this.mlAguaConsumida = 0;
        this.mlAguaMeta = 0;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isFoiTreinar() {
        return foiTreinar;
    }

    public void setFoiTreinar(boolean foiTreinar) {
        this.foiTreinar = foiTreinar;
    }

    public float getCaloriasQueimadas() {
        return caloriasQueimadas;
    }

    public void setCaloriasQueimadas(float caloriasQueimadas) {
        this.caloriasQueimadas = caloriasQueimadas;
    }

    public Float getPesoRegistradoKg() {
        return pesoRegistradoKg;
    }

    public void setPesoRegistradoKg(Float pesoRegistradoKg) {
        this.pesoRegistradoKg = pesoRegistradoKg;
    }

    public int getMlAguaConsumida() {
        return mlAguaConsumida;
    }

    public void setMlAguaConsumida(int mlAguaConsumida) {
        this.mlAguaConsumida = mlAguaConsumida;
    }

    public int getMlAguaMeta() {
        return mlAguaMeta;
    }

    public void setMlAguaMeta(int mlAguaMeta) {
        this.mlAguaMeta = mlAguaMeta;
    }
}
