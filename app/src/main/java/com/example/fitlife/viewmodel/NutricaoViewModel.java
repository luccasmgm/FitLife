package com.example.fitlife.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.fitlife.model.entity.AlimentoConsumido;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.model.repository.NutricaoRepository;
import com.example.fitlife.model.repository.UsuarioRepository;
import com.example.fitlife.util.CalculoUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ViewModel da tela de Nutrição.
 *
 * Todos os LiveData derivados são montados NO CONSTRUTOR, depois que os
 * repositórios existem (mesma lição do erro do TreinoViewModel: campos
 * inicializados inline rodam antes do construtor).
 *
 * O fluxo de dados é uma cadeia reativa:
 *   usuarioIdLive  ──► refeições do usuário
 *                  ──► usuário ──► meta calórica diária
 *   registroIdLive ──► alimentos do dia
 *                  ──► total de calorias consumidas
 * Quando inicializar() define os IDs, tudo se conecta sozinho.
 */
public class NutricaoViewModel extends AndroidViewModel {

    /**
     * Modelo de apresentação: junta uma refeição com os alimentos
     * consumidos nela hoje e o total de calorias. É isso que o adapter
     * desenha — a View não precisa fazer nenhuma conta.
     */
    public static class RefeicaoItem {
        public final Refeicao refeicao;
        public final List<AlimentoConsumido> alimentos;
        public final float caloriasConsumidas;

        public RefeicaoItem(Refeicao refeicao, List<AlimentoConsumido> alimentos, float caloriasConsumidas) {
            this.refeicao = refeicao;
            this.alimentos = alimentos;
            this.caloriasConsumidas = caloriasConsumidas;
        }
    }

    private final NutricaoRepository nutricaoRepository;
    private final UsuarioRepository usuarioRepository;

    private final MutableLiveData<Long> usuarioIdLive = new MutableLiveData<>();
    private final MutableLiveData<Long> registroIdLive = new MutableLiveData<>();

    private final LiveData<Integer> metaCaloricaDiaria;
    private final LiveData<Float> totalCaloriasConsumidas;
    private final LiveData<List<Refeicao>> refeicoes;
    private final LiveData<List<AlimentoConsumido>> alimentosDoDia;

    /** Combina refeições + alimentos do dia na lista final da tela. */
    private final MediatorLiveData<List<RefeicaoItem>> itensRefeicao = new MediatorLiveData<>();

    private final MutableLiveData<String> mensagemFeedback = new MutableLiveData<>();

    private long usuarioIdAtual = -1;

    public NutricaoViewModel(@NonNull Application application) {
        super(application);
        this.nutricaoRepository = new NutricaoRepository(application);
        this.usuarioRepository = new UsuarioRepository(application);

        LiveData<Usuario> usuario = Transformations.switchMap(usuarioIdLive,
                id -> usuarioRepository.observarUsuario(id));

        this.metaCaloricaDiaria = Transformations.map(usuario,
                u -> u == null ? 0 : CalculoUtils.calcularMetaCaloricaDoUsuario(u));

        this.refeicoes = Transformations.switchMap(usuarioIdLive,
                id -> nutricaoRepository.observarRefeicoesDoUsuario(id));

        this.alimentosDoDia = Transformations.switchMap(registroIdLive,
                id -> nutricaoRepository.observarAlimentosDoDia(id));

        this.totalCaloriasConsumidas = Transformations.switchMap(registroIdLive,
                id -> nutricaoRepository.observarTotalCaloriasConsumidasNoDia(id));

        // Sempre que as refeições OU os alimentos mudarem, recalcula a lista da tela
        itensRefeicao.addSource(refeicoes, r -> combinar());
        itensRefeicao.addSource(alimentosDoDia, a -> combinar());
    }

    /**
     * Garante que as refeições padrão existem, descobre (ou cria) o
     * registro diário de hoje e conecta a cadeia de LiveData.
     */
    public void inicializar(long usuarioId) {
        if (usuarioIdAtual != -1) return;
        this.usuarioIdAtual = usuarioId;

        usuarioIdLive.setValue(usuarioId);
        nutricaoRepository.garantirRefeicoesPadrao(usuarioId);

        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        nutricaoRepository.obterOuCriarRegistroDeHoje(usuarioId, hoje,
                registro -> registroIdLive.postValue(registro.getId()));
    }

    private void combinar() {
        List<Refeicao> listaRefeicoes = refeicoes.getValue();
        if (listaRefeicoes == null) return;

        List<AlimentoConsumido> listaAlimentos = alimentosDoDia.getValue();
        if (listaAlimentos == null) listaAlimentos = Collections.emptyList();

        // Agrupa os alimentos por refeição
        Map<Long, List<AlimentoConsumido>> porRefeicao = new HashMap<>();
        for (AlimentoConsumido a : listaAlimentos) {
            List<AlimentoConsumido> grupo = porRefeicao.get(a.getRefeicaoId());
            if (grupo == null) {
                grupo = new ArrayList<>();
                porRefeicao.put(a.getRefeicaoId(), grupo);
            }
            grupo.add(a);
        }

        List<RefeicaoItem> resultado = new ArrayList<>();
        for (Refeicao r : listaRefeicoes) {
            List<AlimentoConsumido> doGrupo = porRefeicao.get(r.getId());
            if (doGrupo == null) doGrupo = Collections.emptyList();

            float total = 0f;
            for (AlimentoConsumido a : doGrupo) total += a.getCalorias();

            resultado.add(new RefeicaoItem(r, doGrupo, total));
        }
        itensRefeicao.setValue(resultado);
    }

    // ---------- Getters ----------

    public LiveData<List<RefeicaoItem>> getItensRefeicao()   { return itensRefeicao; }
    public LiveData<Integer> getMetaCaloricaDiaria()         { return metaCaloricaDiaria; }
    public LiveData<Float> getTotalCaloriasConsumidas()      { return totalCaloriasConsumidas; }
    public LiveData<String> getMensagemFeedback()            { return mensagemFeedback; }

    // ---------- Ações ----------

    /**
     * Valida e registra um alimento consumido. Retorna true se os dados
     * foram aceitos — a Activity usa isso apenas para decidir se fecha o
     * diálogo (se false, o diálogo continua aberto para o usuário corrigir).
     */
    public boolean registrarAlimento(long refeicaoId, String nome, String quantidade, String caloriasStr) {
        if (nome.isEmpty()) {
            mensagemFeedback.setValue("Informe o nome do alimento.");
            return false;
        }

        float calorias;
        try {
            calorias = Float.parseFloat(caloriasStr.replace(",", "."));
        } catch (NumberFormatException e) {
            mensagemFeedback.setValue("Informe as calorias (apenas números).");
            return false;
        }

        if (calorias <= 0 || calorias > 5000) {
            mensagemFeedback.setValue("Calorias devem estar entre 1 e 5000 kcal.");
            return false;
        }

        Long registroId = registroIdLive.getValue();
        if (registroId == null) {
            mensagemFeedback.setValue("Aguarde um momento e tente novamente.");
            return false;
        }

        nutricaoRepository.registrarAlimentoConsumido(
                new AlimentoConsumido(refeicaoId, registroId, nome, quantidade, calorias));
        mensagemFeedback.setValue("Alimento registrado!");
        return true;
    }
}
