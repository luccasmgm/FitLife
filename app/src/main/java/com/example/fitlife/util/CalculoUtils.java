package com.example.fitlife.util;

import com.example.fitlife.model.entity.Usuario;

/**
 * Classe utilitária com as fórmulas de saúde/nutrição do app.
 * Todos os métodos são estáticos — não há necessidade de instanciar
 * esta classe, ela é apenas um agrupador de cálculos puros.
 */
public class CalculoUtils {

    /**
     * Piso de segurança para a meta calórica diária. Dietas com déficit
     * agressivo (GET - 500) podem gerar metas perigosamente baixas para
     * pessoas pequenas/sedentárias; abaixo disso o app não sugere meta.
     */
    public static final int META_CALORICA_MINIMA = 1200;

    private CalculoUtils() {}

    /**
     * TMB pela fórmula de Mifflin-St Jeor (mais precisa que Harris-Benedict).
     * Masculino: (10 × peso kg) + (6.25 × altura cm) - (5 × idade) + 5
     * Feminino:  (10 × peso kg) + (6.25 × altura cm) - (5 × idade) - 161
     */
    public static float calcularTMB(float pesoKg, float alturaCm, int idade, Usuario.Sexo sexo) {
        float base = (10 * pesoKg) + (6.25f * alturaCm) - (5 * idade);
        return sexo == Usuario.Sexo.MASCULINO ? base + 5 : base - 161;
    }

    /** GET (Gasto Energético Total) = TMB × fator de atividade da rotina. */
    public static float calcularGET(float tmb, Usuario.Rotina rotina) {
        return tmb * rotina.getFatorAtividade();
    }

    /**
     * Meta calórica diária ajustada pelo objetivo do usuário.
     * Perda de peso: GET - 500 kcal | Manutenção: GET | Ganho de massa: GET + 400 kcal
     */
    public static float calcularMetaCalorica(float get, Usuario.Objetivo objetivo) {
        switch (objetivo) {
            case PERDA_DE_PESO:    return get - 500;
            case GANHO_DE_MASSA:   return get + 400;
            default:               return get; // MANUTENCAO
        }
    }

    /**
     * NOVO: atalho que encadeia TMB → GET → meta por objetivo, a partir
     * do Usuario inteiro, e aplica o piso de segurança. É o único ponto
     * do app que calcula "quantas kcal o usuário deve comer por dia",
     * evitando fórmulas duplicadas em ViewModel e Repository.
     */
    public static int calcularMetaCaloricaDoUsuario(Usuario u) {
        float tmb = calcularTMB(u.getPesoKg(), u.getAlturaCm(), u.getIdade(), u.getSexo());
        float get = calcularGET(tmb, u.getRotina());
        float meta = calcularMetaCalorica(get, u.getObjetivo());
        return Math.round(Math.max(META_CALORICA_MINIMA, meta));
    }

    /** Meta de hidratação: 35ml por kg de peso, com ajuste por rotina. */
    public static int calcularMetaHidratacao(float pesoKg, Usuario.Rotina rotina) {
        float base = pesoKg * 35;
        switch (rotina) {
            case ATIVO:      return Math.round(base * 1.20f);
            case MODERADO:   return Math.round(base * 1.10f);
            default:         return Math.round(base);
        }
    }

    /** Formata ml para exibição amigável: 1500 → "1,5L" | 800 → "800ml" */
    public static String formatarAgua(int ml) {
        if (ml >= 1000) {
            return String.format("%.1fL", ml / 1000f).replace(".", ",");
        }
        return ml + "ml";
    }

    /** Percentual de progresso (0–100), nunca passa de 100. */
    public static int calcularPercentualHidratacao(int consumido, int meta) {
        if (meta <= 0) return 0;
        return Math.min(100, (int) ((consumido / (float) meta) * 100));
    }
}
