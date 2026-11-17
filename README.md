# FitLife

Aplicativo Android nativo, desenvolvido em **Java**, para acompanhar **treino**, **hidratação** e **dieta** em um só lugar. Projeto pessoal de portfólio, construído com **arquitetura MVVM**, banco de dados local com **Room** e interface com **Material Components**.

## Funcionalidades

- **Cadastro e login:** dados pessoais (peso, altura, idade, sexo, nível de atividade e objetivo). A senha é armazenada apenas como hash SHA-256.
- **Dashboard:** resumo do dia com calorias queimadas, total de idas ao treino, progresso de hidratação com meta calculada, botões rápidos para registrar água e registro do peso do dia.
- **Treino:** catálogo de exercícios com filtro por tipo (musculação, cardio e mobilidade), vídeo de execução, registro de carga, séries e repetições (somente musculação), histórico de evolução e botão para concluir o treino do dia.
- **Nutrição:** meta calórica diária calculada pelo perfil do usuário, refeições do dia (café da manhã, almoço, lanche e jantar) e registro dos alimentos consumidos, com total de calorias e quanto ainda resta.

## Tecnologias

| Área | Tecnologia |
|---|---|
| Linguagem | Java 11 |
| Plataforma | Android (minSdk 24, targetSdk 36) |
| Banco de dados | Room 2.6.1 (SQLite local) |
| Arquitetura | MVVM com ViewModel e LiveData 2.7.0 |
| Interface | Material Components, RecyclerView com `ListAdapter` e `DiffUtil`, BottomNavigationView |
| Build | Gradle Kotlin DSL com Version Catalog |

## Arquitetura

O app segue o padrão **MVVM**, com fluxo de dados em uma só direção:

```
View (Activity / Adapter)
   │  observa LiveData            ▲
   ▼                              │ notifica mudanças
ViewModel  ── chama ──►  Repository  ── usa ──►  DAO (Room)  ──►  SQLite
```

- **View:** desenha a tela e repassa as ações do usuário. Não contém regra de negócio nem acessa o banco.
- **ViewModel:** valida entradas, controla estados e expõe `LiveData`. Sobrevive à rotação de tela.
- **Repository:** único ponto de acesso aos DAOs; executa as escritas em uma thread de fundo.
- **Model:** entidades e DAOs do Room.

## Estrutura do projeto

```
app/src/main/java/com/example/fitlife/
├── model/
│   ├── entity/       Usuario, RegistroDiario, Refeicao, AlimentoConsumido, Exercicio, CargaExercicio
│   ├── dao/          um DAO por área de dados
│   ├── database/     AppDatabase (singleton) e Converters
│   └── repository/   UsuarioRepository, TreinoRepository, NutricaoRepository
├── viewmodel/        um ViewModel por tela
├── view/             login, cadastro, dashboard, treino, nutricao e adapter
└── util/             CalculoUtils (fórmulas de saúde)
```

## Regras de negócio e cálculos

| Cálculo | Regra |
|---|---|
| Taxa metabólica basal | Fórmula de Mifflin-St Jeor |
| Gasto energético total | TMB multiplicada pelo fator de atividade (1,2 a 1,725) |
| Meta calórica | Perda de peso: GET − 500 kcal. Manutenção: GET. Ganho de massa: GET + 400 kcal. Piso de 1200 kcal |
| Meta de hidratação | 35 ml por kg de peso, com acréscimo para rotinas moderada e ativa |

## Como executar

1. Clone o repositório:
   ```
   git clone https://github.com/luccasmgm/FitLife.git
   ```
2. Abra a pasta no **Android Studio** e aguarde a sincronização do Gradle.
3. Execute em um emulador ou dispositivo com **Android 7.0 (API 24)** ou superior.

Não há chaves de API nem configurações extras. O banco de dados é criado localmente na primeira execução, já com um catálogo inicial de exercícios.

## Capturas de tela

Adicione as imagens em `docs/screenshots/` e atualize os caminhos abaixo.

| Login | Dashboard | Treino | Nutrição |
|---|---|---|---|
| ![Login](docs/screenshots/login.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Treino](docs/screenshots/treino.png) | ![Nutrição](docs/screenshots/nutricao.png) |

## Roadmap

- [ ] Corrigir a exibição do vídeo de execução dos exercícios
- [ ] Garantir atualizações atômicas do registro diário (evitar sobrescrita de dados)
- [ ] Manter a sessão do usuário e adicionar logout
- [ ] Evolução do peso com gráfico
- [ ] Edição e exclusão de alimentos, com navegação entre dias
- [ ] Testes unitários dos ViewModels e das fórmulas
- [ ] Tema escuro e textos em `strings.xml`

## Autor

**Luccas**, estudante de Análise e Desenvolvimento de Sistemas (Fatec).

- GitHub: [@luccasmgm](https://github.com/luccasmgm)
- LinkedIn: adicione o seu link
