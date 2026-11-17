package com.example.fitlife.model.database;

import androidx.room.TypeConverter;

import com.example.fitlife.model.entity.Exercicio;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.model.entity.Usuario;

/**
 * O Room não sabe persistir enums nativamente — ele precisa de um conversor
 * explícito para transformar o enum em algo que o SQLite entenda (String)
 * e vice-versa. Esta classe é registrada em @TypeConverters no AppDatabase.
 */
public class Converters {

    // Usuario.Sexo
    @TypeConverter
    public static String fromSexo(Usuario.Sexo sexo) {
        return sexo == null ? null : sexo.name();
    }

    @TypeConverter
    public static Usuario.Sexo toSexo(String value) {
        return value == null ? null : Usuario.Sexo.valueOf(value);
    }

    // Usuario.Rotina
    @TypeConverter
    public static String fromRotina(Usuario.Rotina rotina) {
        return rotina == null ? null : rotina.name();
    }

    @TypeConverter
    public static Usuario.Rotina toRotina(String value) {
        return value == null ? null : Usuario.Rotina.valueOf(value);
    }

    // Usuario.Objetivo
    @TypeConverter
    public static String fromObjetivo(Usuario.Objetivo objetivo) {
        return objetivo == null ? null : objetivo.name();
    }

    @TypeConverter
    public static Usuario.Objetivo toObjetivo(String value) {
        return value == null ? null : Usuario.Objetivo.valueOf(value);
    }

    // Refeicao.TipoRefeicao
    @TypeConverter
    public static String fromTipoRefeicao(Refeicao.TipoRefeicao tipo) {
        return tipo == null ? null : tipo.name();
    }

    @TypeConverter
    public static Refeicao.TipoRefeicao toTipoRefeicao(String value) {
        return value == null ? null : Refeicao.TipoRefeicao.valueOf(value);
    }

    // Exercicio.TipoExercicio
    @TypeConverter
    public static String fromTipoExercicio(Exercicio.TipoExercicio tipo) {
        return tipo == null ? null : tipo.name();
    }

    @TypeConverter
    public static Exercicio.TipoExercicio toTipoExercicio(String value) {
        return value == null ? null : Exercicio.TipoExercicio.valueOf(value);
    }
}
