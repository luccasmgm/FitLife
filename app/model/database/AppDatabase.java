package com.example.fitlife.model.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.fitlife.model.dao.CargaExercicioDao;
import com.example.fitlife.model.dao.ExercicioDao;
import com.example.fitlife.model.dao.RefeicaoDao;
import com.example.fitlife.model.dao.RegistroDiarioDao;
import com.example.fitlife.model.dao.UsuarioDao;
import com.example.fitlife.model.entity.AlimentoConsumido;
import com.example.fitlife.model.entity.CargaExercicio;
import com.example.fitlife.model.entity.Exercicio;
import com.example.fitlife.model.entity.Refeicao;
import com.example.fitlife.model.entity.RegistroDiario;
import com.example.fitlife.model.entity.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Classe central do Room. Mantém UMA única instância do banco (singleton)
 * durante toda a vida do app — abrir várias instâncias é um erro comum
 * que causa travamentos e inconsistência.
 */
@Database(
        entities = {
                Usuario.class,
                RegistroDiario.class,
                Refeicao.class,
                AlimentoConsumido.class,
                Exercicio.class,
                CargaExercicio.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({Converters.class})
public abstract class AppDatabase {

    private static final String NOME_BANCO = "fittrack_database";

    private static volatile AppDatabase instancia;

    /**
     * Executor dedicado para operações de banco. Room NUNCA deve ser
     * acessado na thread principal (UI) — toda leitura/escrita feita via
     * DAO com retorno direto (não-LiveData) precisa rodar aqui.
     * Consultas que retornam LiveData já são assíncronas automaticamente.
     */
    public static final ExecutorService DATABASE_WRITE_EXECUTOR = Executors.newFixedThreadPool(4);

    public abstract UsuarioDao usuarioDao();

    public abstract RegistroDiarioDao registroDiarioDao();

    public abstract RefeicaoDao refeicaoDao();

    public abstract ExercicioDao exercicioDao();

    public abstract CargaExercicioDao cargaExercicioDao();

    public static AppDatabase getInstancia(final Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    NOME_BANCO
                            )
                            .addCallback(seedCallback)
                            .build();
                }
            }
        }
        return instancia;
    }

    /**
     * Callback executado UMA vez, quando o banco é criado pela primeira vez.
     * Usado para popular (seed) a tabela de exercícios com um catálogo
     * inicial, já que ela não depende de nenhum usuário.
     */
    private static final RoomDatabase.Callback seedCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            DATABASE_WRITE_EXECUTOR.execute(() -> {
                ExercicioDao dao = instancia.exercicioDao();
                dao.inserirTodos(criarCatalogoInicialDeExercicios());
            });
        }
    };

    private static java.util.List<Exercicio> criarCatalogoInicialDeExercicios() {
        java.util.List<Exercicio> lista = new java.util.ArrayList<>();

        // Musculação (permite carga)
        lista.add(new Exercicio("Supino Reto", Exercicio.TipoExercicio.MUSCULACAO, "Peito", "URL_VIDEO_SUPINO", true));
        lista.add(new Exercicio("Agachamento Livre", Exercicio.TipoExercicio.MUSCULACAO, "Pernas", "URL_VIDEO_AGACHAMENTO", true));
        lista.add(new Exercicio("Puxada Frontal", Exercicio.TipoExercicio.MUSCULACAO, "Costas", "URL_VIDEO_PUXADA", true));

        // Cardio (não permite carga)
        lista.add(new Exercicio("Esteira", Exercicio.TipoExercicio.CARDIO, null, "URL_VIDEO_ESTEIRA", false));
        lista.add(new Exercicio("Bicicleta Ergométrica", Exercicio.TipoExercicio.CARDIO, null, "URL_VIDEO_BIKE", false));

        // Mobilidade (não permite carga)
        lista.add(new Exercicio("Alongamento de Isquiotibiais", Exercicio.TipoExercicio.MOBILIDADE, null, "URL_VIDEO_ALONGAMENTO", false));

        return lista;
    }
}
