package com.example.fitlife.model.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.fitlife.model.dao.RefeicaoDao;
import com.example.fitlife.model.dao.RegistroDiarioDao;
import com.example.fitlife.model.dao.UsuarioDao;
import com.example.fitlife.model.database.AppDatabase;
import com.example.fitlife.model.entity.AlimentoConsumido;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.model.entity.RegistroDiario;
import com.example.fitlife.model.entity.Usuario;
import com.example.fitlife.util.CalculoUtils;

import java.util.List;

public class NutricaoRepository {

    private final RefeicaoDao refeicaoDao;
    private final RegistroDiarioDao registroDiarioDao;
    private final UsuarioDao usuarioDao;

    public NutricaoRepository(Application application) {
        AppDatabase database = AppDatabase.getInstancia(application);
        this.refeicaoDao = database.refeicaoDao();
        this.registroDiarioDao = database.registroDiarioDao();
        this.usuarioDao = database.usuarioDao();
    }

    public interface RegistroDiarioCallback {
        void aoObter(RegistroDiario registro);
    }

    /** Busca ou cria o registro do dia atual para o usuário. */
    public void obterOuCriarRegistroDeHoje(long usuarioId, String dataHoje, RegistroDiarioCallback callback) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            RegistroDiario registro = registroDiarioDao.buscarPorUsuarioEData(usuarioId, dataHoje);
            if (registro == null) {
                registro = new RegistroDiario(usuarioId, dataHoje);
                long novoId = registroDiarioDao.inserir(registro);
                registro.setId(novoId);
            }
            callback.aoObter(registro);
        });
    }

    public LiveData<RegistroDiario> observarRegistroDoDia(long usuarioId, String data) {
        return registroDiarioDao.observarPorUsuarioEData(usuarioId, data);
    }

    public LiveData<List<RegistroDiario>> observarHistorico(long usuarioId) {
        return registroDiarioDao.observarHistorico(usuarioId);
    }

    public LiveData<Integer> observarTotalIdasTreino(long usuarioId) {
        return registroDiarioDao.observarTotalIdasTreino(usuarioId);
    }

    public LiveData<Float> observarTotalCaloriasQueimadas(long usuarioId) {
        return registroDiarioDao.observarTotalCaloriasQueimadas(usuarioId);
    }

    public void adicionarAguaConsumida(RegistroDiario registro, int mlAdicional) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            registro.setMlAguaConsumida(registro.getMlAguaConsumida() + mlAdicional);
            registroDiarioDao.atualizar(registro);
        });
    }

    public void atualizarPesoDoDia(RegistroDiario registro, float pesoKg) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            registro.setPesoRegistradoKg(pesoKg);
            registroDiarioDao.atualizar(registro);
        });
    }

    public void marcarTreinoFeito(RegistroDiario registro, float caloriasQueimadas) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            registro.setFoiTreinar(true);
            registro.setCaloriasQueimadas(registro.getCaloriasQueimadas() + caloriasQueimadas);
            registroDiarioDao.atualizar(registro);
        });
    }

    // ---------- Refeições e alimentos ----------

    public LiveData<List<Refeicao>> observarRefeicoesDoUsuario(long usuarioId) {
        return refeicaoDao.observarRefeicoesDoUsuario(usuarioId);
    }

    public void criarRefeicao(Refeicao refeicao) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> refeicaoDao.inserir(refeicao));
    }

    /**
     * NOVO. Cria as 4 refeições padrão do usuário na primeira vez que ele
     * abre a tela de Nutrição. Se ele já tiver refeições, não faz nada
     * (idempotente), então é seguro chamar sempre que a tela abrir.
     *
     * A meta de cada refeição é uma fração da meta calórica diária:
     * café 25%, almoço 35%, lanche 15%, jantar 25%.
     */
    public void garantirRefeicoesPadrao(long usuarioId) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> {
            if (refeicaoDao.contarRefeicoesDoUsuario(usuarioId) > 0) return;

            Usuario usuario = usuarioDao.buscarPorId(usuarioId);
            if (usuario == null) return;

            int metaDiaria = CalculoUtils.calcularMetaCaloricaDoUsuario(usuario);

            refeicaoDao.inserir(new Refeicao(usuarioId, Refeicao.TipoRefeicao.CAFE_DA_MANHA, "07:30", metaDiaria * 0.25f));
            refeicaoDao.inserir(new Refeicao(usuarioId, Refeicao.TipoRefeicao.ALMOCO,        "12:00", metaDiaria * 0.35f));
            refeicaoDao.inserir(new Refeicao(usuarioId, Refeicao.TipoRefeicao.SNACK,         "16:00", metaDiaria * 0.15f));
            refeicaoDao.inserir(new Refeicao(usuarioId, Refeicao.TipoRefeicao.JANTAR,        "19:30", metaDiaria * 0.25f));
        });
    }

    public LiveData<List<AlimentoConsumido>> observarAlimentosDoDia(long registroDiarioId) {
        return refeicaoDao.observarAlimentosDoDia(registroDiarioId);
    }

    /** NOVO. Soma das calorias de todos os alimentos registrados no dia. */
    public LiveData<Float> observarTotalCaloriasConsumidasNoDia(long registroDiarioId) {
        return refeicaoDao.observarTotalCaloriasConsumidasNoDia(registroDiarioId);
    }

    public void registrarAlimentoConsumido(AlimentoConsumido alimento) {
        AppDatabase.DATABASE_WRITE_EXECUTOR.execute(() -> refeicaoDao.inserirAlimentoConsumido(alimento));
    }
}
