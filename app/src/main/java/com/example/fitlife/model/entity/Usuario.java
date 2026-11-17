package com.example.fitlife.model.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidade que representa um usuário do app.
 * Cada usuário é único (regra de negócio 1) e seus dados nunca se misturam
 * com os de outro usuário, pois todas as outras tabelas referenciam usuarioId.
 */
@Entity(tableName = "usuario")
public class Usuario {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "nome_completo")
    private String nomeCompleto;

    @ColumnInfo(name = "email")
    private String email;

    /**
     * NUNCA armazenar senha em texto puro.
     * Aqui guardamos apenas o hash (ver util/SegurancaUtils.java).
     */
    @ColumnInfo(name = "senha_hash")
    private String senhaHash;

    @ColumnInfo(name = "peso_kg")
    private float pesoKg;

    @ColumnInfo(name = "altura_cm")
    private float alturaCm;

    @ColumnInfo(name = "idade")
    private int idade;

    @ColumnInfo(name = "sexo")
    private Sexo sexo;

    @ColumnInfo(name = "rotina")
    private Rotina rotina;

    @ColumnInfo(name = "objetivo")
    private Objetivo objetivo;

    public Usuario(String nomeCompleto, String email, String senhaHash, float pesoKg,
                    float alturaCm, int idade, Sexo sexo, Rotina rotina, Objetivo objetivo) {
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.senhaHash = senhaHash;
        this.pesoKg = pesoKg;
        this.alturaCm = alturaCm;
        this.idade = idade;
        this.sexo = sexo;
        this.rotina = rotina;
        this.objetivo = objetivo;
    }

    // Getters e Setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public float getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(float pesoKg) {
        this.pesoKg = pesoKg;
    }

    public float getAlturaCm() {
        return alturaCm;
    }

    public void setAlturaCm(float alturaCm) {
        this.alturaCm = alturaCm;
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        this.idade = idade;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public Rotina getRotina() {
        return rotina;
    }

    public void setRotina(Rotina rotina) {
        this.rotina = rotina;
    }

    public Objetivo getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(Objetivo objetivo) {
        this.objetivo = objetivo;
    }

    /**
     * Enums usados pelo Usuario. Ficam aqui dentro para facilitar o uso,
     * mas podem ser movidos para arquivos próprios em model/entity se preferir.
     */
    public enum Sexo {
        MASCULINO, FEMININO
    }

    public enum Rotina {
        SEDENTARIO(1.2f),
        LEVE(1.375f),
        MODERADO(1.55f),
        ATIVO(1.725f);

        private final float fatorAtividade;

        Rotina(float fatorAtividade) {
            this.fatorAtividade = fatorAtividade;
        }

        public float getFatorAtividade() {
            return fatorAtividade;
        }
    }

    public enum Objetivo {
        PERDA_DE_PESO,
        MANUTENCAO,
        GANHO_DE_MASSA
    }
}
