# FitWorkUp — documento de continuidade para o Codex

Atualizado em: **18 de agosto de 2026**  
Objetivo: permitir que o desenvolvimento continue em outro computador ou em uma nova conversa do Codex sem depender do histórico desta conversa.

> Este arquivo descreve o estado encontrado nos projetos locais, separando o que está implementado, o que está parcial e o que ainda é apenas proposta. Antes de começar a trabalhar no notebook, leia também a seção **Estado do Git no momento do handoff**.

## 1. Visão geral

O FitWorkUp é composto por:

- aplicativo Android nativo em Kotlin e Jetpack Compose;
- API REST em Java e Spring Boot;
- banco oficial PostgreSQL;
- persistência local Android com Room;
- autenticação por JWT;
- sensores do aparelho para passos, movimento e GPS;
- gamificação com XP, níveis, FitCoins, conquistas, ranking e loja.

Fluxo arquitetural pretendido:

```text
Sensores/GPS do Android
        ↓
WorkoutSensorService
        ↓
Room (registro local e pendências)
        ↓
Retrofit + JWT
        ↓
Spring Boot (validação, regras e recompensas)
        ↓
PostgreSQL
```

A API deve ser a autoridade para autenticação, amizades, validação de atividade, recompensas, FitCoins, loja, ranking, conquistas e grupos. O Room deve garantir leitura local e fila de sincronização, sem substituir as regras do servidor.

## 2. Pastas dos projetos

No computador original:

```text
C:\Users\ronal\Downloads\Projetos\FitWorkUp-frontend
C:\Users\ronal\Downloads\Projetos\FitWorkUp-api
```

No notebook, os caminhos podem ser diferentes. Abra ambos no mesmo projeto local do Codex:

- pasta principal: `FitWorkUp-frontend`;
- pasta adicional: `FitWorkUp-api`.

O módulo Android ativo é o `app/` da raiz de `FitWorkUp-frontend`. Não usar um eventual módulo antigo `FitWorkUpMobile/app/`.

## 3. Tecnologias e versões relevantes

### Android

- Kotlin;
- Jetpack Compose + Material 3;
- Android Gradle Plugin configurado pelo catálogo de versões;
- `compileSdk 35`, `targetSdk 35`, `minSdk 26`;
- Java/JVM 17;
- Hilt;
- Retrofit 2.11 + Gson;
- OkHttp 4.12;
- DataStore Preferences;
- Room 2.6.1;
- WorkManager;
- Google Maps Compose;
- Fused Location Provider;
- SensorManager/contador de passos.

### API

- Java 21;
- Spring Boot 3.3.5;
- Spring Web, Security, Validation, Data JPA e Mail;
- JJWT 0.12.6;
- PostgreSQL em desenvolvimento;
- H2 nos testes;
- Maven Wrapper.

## 4. Estado das funcionalidades

Legenda:

- **Implementado:** existe integração entre as camadas principais.
- **Parcial:** existe código útil, mas ainda falta completar ou validar o fluxo.
- **Planejado:** ainda não existe implementação completa.

### 4.1 Autenticação e sessão — implementado

- cadastro e login pela API;
- login aceita e-mail ou username;
- JWT enviado pelo `AuthInterceptor`;
- token armazenado no DataStore por `TokenStore`;
- `SessionManager` invalida a sessão em resposta HTTP 401 autenticada;
- 401 apaga o token e redireciona para o login;
- erro de conexão e erro HTTP 500 não devem apagar a sessão;
- Splash decide entre login e área autenticada;
- logout limpa a sessão local;
- DTOs Retrofit foram separados para autenticação;
- Google Sign-In continua como `TODO` na tela de login.

Arquivos centrais:

```text
app/src/main/java/fitworkup/app/data/remote/api/AuthApiService.kt
app/src/main/java/fitworkup/app/data/remote/AuthInterceptor.kt
app/src/main/java/fitworkup/app/data/session/TokenStore.kt
app/src/main/java/fitworkup/app/data/session/SessionManager.kt
app/src/main/java/fitworkup/app/data/repository/AuthRepositoryImpl.kt
app/src/main/java/fitworkup/app/ui/screens/login/LoginViewModel.kt
app/src/main/java/fitworkup/app/ui/screens/splash/SplashViewModel.kt
src/main/java/com/fitworkup/security/
src/main/java/com/fitworkup/service/AuthService.java
```

Regra importante: o `Principal` do Spring pode conter e-mail. Consultas de usuário autenticado devem usar `findByEmailOrUsername`, e não somente `findByUsername`.

### 4.2 Recuperação de senha — implementado, pendente de validação final no celular

- solicitação de recuperação por e-mail;
- geração de código temporário no backend;
- envio por SMTP/Gmail;
- redefinição com e-mail, código e nova senha;
- tela Compose com etapas de e-mail, código e sucesso;
- testes unitários do serviço de recuperação existem na API.

Endpoints:

```text
POST /api/v1/auth/password/forgot
POST /api/v1/auth/password/reset
```

Variáveis necessárias:

```text
MAIL_USERNAME=email_do_remetente@gmail.com
MAIL_PASSWORD=senha_de_aplicativo_do_Google
```

Não armazenar a senha de aplicativo no Git. A conta configurada é a conta remetente do sistema, usada para enviar os códigos aos usuários.

### 4.3 Perfil e amizades — implementado, pendente de validação visual final

- perfil do usuário obtido por `GET /api/v1/users/me`;
- busca de usuários;
- envio de solicitação;
- listagem de solicitações pendentes;
- aceitar e rejeitar solicitação;
- listar amizades aceitas;
- remover amizade;
- abrir o perfil público de uma conexão;
- perfil público expõe informações resumidas e conquistas;
- componentes de erro remoto e conectividade foram adicionados localmente;
- a tela do perfil do amigo recebeu correções de layout/tema, mas deve ser retestada em telas pequenas e no tema escuro.

Endpoints:

```text
GET    /api/v1/users/search
GET    /api/v1/users/{userId}/public-profile
GET    /api/v1/friendships
GET    /api/v1/friendships/pending
POST   /api/v1/friendships/request
PUT    /api/v1/friendships/{id}/accept
PUT    /api/v1/friendships/{id}/reject
DELETE /api/v1/friendships/{id}
```

### 4.4 Configurações — parcial

- logout está funcional;
- várias opções visuais da `ConfigScreen` ainda possuem `/* TODO */`;
- ainda falta persistir preferências de tema, notificações e demais configurações;
- o DataStore atual é usado para a sessão, não representa uma implementação completa das preferências do aplicativo.

Próximo passo recomendado: criar um `SettingsDataStore` separado do `TokenStore` e ligar cada controle da tela a um `ConfigViewModel`.

### 4.5 Registro de treino, sensores e mapa — parcial avançado

- `WorkoutSensorService` é um foreground service;
- coleta contador de passos e localização;
- mantém `pathPoints` filtrados durante o treino;
- atualiza passos, distância e mapa em tempo real;
- `WorkoutViewModel` envia os pontos finais no `ActivityRequest` Android;
- treino é salvo primeiro no Room;
- rota é armazenada localmente como JSON em `ActivityEntity.routeJson`;
- calendário e último percurso leem as atividades locais;
- o minimapa enquadra a rota salva;
- manifesto possui permissões de localização, atividade, internet, notificações e foreground service.

Limitação crítica atual:

- o DTO Android possui `routePoints`, mas o `ActivityRequest` Java, a entidade `Activity` e o banco oficial ainda não persistem esses pontos;
- portanto, a rota sobrevive localmente no mesmo aparelho, mas ainda não é restaurada do servidor em outro aparelho;
- implementar o contrato de rota no backend antes de considerar a sincronização completa.

Arquivos centrais:

```text
app/src/main/java/fitworkup/app/service/WorkoutSensorService.kt
app/src/main/java/fitworkup/app/util/GpsLocationFilter.kt
app/src/main/java/fitworkup/app/ui/screens/workout/WorkoutViewModel.kt
app/src/main/java/fitworkup/app/data/local/entity/ActivityEntity.kt
app/src/main/java/fitworkup/app/data/repository/ActivityRepositoryImpl.kt
app/src/main/java/fitworkup/app/ui/screens/dashboard/DashboardViewModel.kt
app/src/main/java/fitworkup/app/ui/components/MiniMapa.kt
```

### 4.6 Calendário e resumo diário — parcial

- calendário mensal agrupa atividades locais por data;
- último percurso usa a atividade local mais recente que contém rota;
- resumo diário tenta a API e usa Room como fallback;
- API ainda retorna calorias no contrato de resumo para compatibilidade;
- as calorias foram removidas da tela inicial e da tela de treino por não haver precisão suficiente para apresentá-las ao usuário;
- durante o treino, o espaço anteriormente usado por calorias passou a mostrar a duração observada.

Pontos a corrigir:

- remover futuramente o campo de calorias do contrato da API, caso se decida abandonar definitivamente a métrica;
- se a métrica voltar, definir uma única regra no backend e indicar o resultado como **estimativa**;
- não reintroduzir peso padrão, pois ele produz um número potencialmente enganoso;
- separar gasto do treino de TMB/TDEE: TDEE não representa a energia de uma atividade isolada.

### 4.7 Persistência offline e sincronização — parcial

Já existe:

- Room para atividades;
- status `isSynced`;
- fallback local do resumo diário;
- monitor de conectividade;
- `OfflineBanner` e `RemoteContentError`;
- salvamento local antes do envio remoto.

Ainda falta:

- `WorkoutSyncWorker.doWork()` está vazio e retorna sucesso sem enviar pendências;
- agendar `WorkManager` com restrição de rede;
- reconstruir o `ActivityRequest` a partir de cada `ActivityEntity` pendente;
- marcar como sincronizado somente após HTTP 2xx;
- definir política para HTTP 400/401 e evitar repetição infinita;
- cache de perfil, amizades, ranking e loja ainda não está completo;
- tela offline deve manter navegação, dados locais e logout disponíveis.

Não usar um `CoroutineScope` solto como única garantia de sincronização: o processo Android pode ser encerrado. A garantia resiliente deve vir do WorkManager.

### 4.8 Antifraude e recompensas — implementado como mitigação inicial

- app calcula passos aceitos, retidos, risco e motivos;
- API reavalia a atividade;
- apenas atividades aprovadas devem gerar recompensa;
- API calcula e persiste atividade, XP e FitCoins;
- multiplicadores ativos são aplicados pela gamificação no servidor.

Na documentação acadêmica, não afirmar que o mecanismo “impede fraudes”. A redação correta é que ele **verifica coerência e mitiga registros inconsistentes**, dentro dos sinais e limites avaliados.

### 4.9 Conquistas — implementado somente para passos diários

Conquistas criadas automaticamente no perfil `dev`:

| Nome | Critério | XP | FitCoins |
|---|---:|---:|---:|
| Primeiros 1.000 | 1.000 passos válidos no dia | 50 | 5 |
| Caminhante 5K | 5.000 passos válidos no dia | 150 | 15 |
| Desafio 10K | 10.000 passos válidos no dia | 300 | 30 |

Ainda não foram implementadas categorias por número de treinos, distância total, sequência, grupo ou desafios especiais. O serviço atual entende o critério `DAILY_STEPS`.

### 4.10 Loja — implementado para compra, inventário e boosts

- catálogo remoto em grade;
- cards compactos;
- descrição completa apresentada no diálogo de confirmação;
- saldo insuficiente não impede a visualização dos detalhes;
- compra transacional no servidor;
- inventário para cosméticos;
- equipar moldura e suporte backend para categoria `TITLE`;
- boosts repetíveis com extensão do tempo restante;
- `XP_MULTIPLIER` e `FITCOINS_MULTIPLIER`;
- endpoint para boosts ativos.

Itens de desenvolvimento atuais:

- Moldura Rubi;
- Moldura Esmeralda;
- Moldura Lendária;
- 2x XP por 30 minutos;
- 2x FitCoins por 30 minutos.

Os ícones atuais usam emoji/texto (`iconEmoji`). Para imagens reais, definir uma estratégia única: recursos locais por `iconKey` ou URLs remotas controladas. Não salvar binários diretamente na tabela de itens.

Ainda falta:

- cadastrar títulos adquiríveis/desbloqueáveis;
- interface completa para escolher título e avatar;
- diferenciar título conquistado de título comprado, conforme a regra final do produto;
- tela de inventário/personalização mais completa.

### 4.11 Ranking semanal — implementado

Endpoint principal:

```text
GET /api/ranking/weekly
```

Regra atual:

- semana começa na segunda-feira;
- intervalo inclusivo na segunda e exclusivo na segunda seguinte;
- usa passos validados das atividades válidas;
- `100 passos = 1 ponto de movimento`;
- mostra dias ativos;
- desempate: passos, dias ativos e username;
- usuário atual é identificado na resposta.

Foi intencionalmente implementada apenas a visão semanal, sem abas diária/mensal/global.

### 4.12 Corrida em grupo — planejado; a tela atual é somente um rascunho

O Android atualmente possui apenas a opção Solo/Em Grupo e um campo de código. Não existem entidades, endpoints, lobby ou sincronização de grupo no backend.

Regra de produto aprovada para implementar:

#### Criar atividade

- nome da sala, por exemplo “Corrida de Domingo”;
- estilo livre ou meta de distância;
- limite inicial de até 5 participantes;
- sala privada ou aberta para amigos;
- API gera código, por exemplo `FTW-8K2P`;
- somente o criador pode iniciar.

#### Lobby

- exibe nome, meta, participantes e estado Pronto/Aguardando;
- permite compartilhar o código;
- anfitrião inicia quando as condições forem atendidas.

#### Entrar

- usuário informa obrigatoriamente o código;
- API verifica existência, estado e lotação;
- usuário entra no lobby e marca “Estou pronto”.

#### Durante o treino

- cada celular inicia seu próprio `WorkoutSensorService`;
- cada participante coleta seus próprios passos, GPS e distância;
- todos recebem o mesmo `groupSessionId`;
- cada atividade é validada e recompensada individualmente;
- esforço de um participante nunca gera recompensa para outro;
- quem não iniciar ou não concluir não recebe atividade válida.

#### Rotas dos amigos

Decisão ainda necessária:

- durante o treino, compartilhar somente posição recente ou rota simplificada, com consentimento explícito;
- evitar transmissão de GPS em alta frequência;
- após o treino, permitir visualizar rotas somente para participantes autorizados;
- definir expiração, privacidade e possibilidade de ocultar a rota.

Sugestão de ordem para grupos:

1. entidades `GroupSession` e `GroupParticipant`;
2. endpoints criar/entrar/pronto/iniciar/finalizar;
3. lobby Android;
4. associação de cada atividade ao `groupSessionId`;
5. atualização por polling inicialmente;
6. somente depois avaliar WebSocket e compartilhamento de localização ao vivo.

### 4.13 IA Coach — opcional e não concluído

`AiNudgeService` existe na API, mas a integração com Gemini/Spring AI deve ser tratada como complementar e implementada somente se houver tempo. Não deve bloquear as funções essenciais do TCC.

## 5. Contratos HTTP atuais

| Área | Método e rota |
|---|---|
| Login | `POST /api/v1/auth/login` |
| Cadastro | `POST /api/v1/auth/register` |
| Esqueci a senha | `POST /api/v1/auth/password/forgot` |
| Redefinir senha | `POST /api/v1/auth/password/reset` |
| Usuário atual | `GET /api/v1/users/me` |
| Buscar usuários | `GET /api/v1/users/search` |
| Conquistas | `GET /api/v1/users/me/achievements` |
| Perfil público | `GET /api/v1/users/{userId}/public-profile` |
| Resumo diário | `GET /api/v1/activities/today-summary` |
| Registrar atividade | `POST /api/v1/activities` |
| Amizades | `/api/v1/friendships...` |
| Itens da loja | `GET /api/v1/store/items` |
| Comprar | `POST /api/v1/store/purchase/{storeItemId}` |
| Inventário | `GET /api/v1/store/inventory` |
| Equipar | `POST /api/v1/store/equip/{inventoryItemId}` |
| Boosts ativos | `GET /api/v1/store/boosts/active` |
| Ranking semanal | `GET /api/ranking/weekly` |

Rotas com dados pessoais ou regras de negócio exigem `Authorization: Bearer <JWT>`.

## 6. Configuração no notebook

### 6.1 Pré-requisitos

- Git;
- Android Studio com SDK Android 35;
- JDK 17 para o Android;
- JDK 21 para a API;
- PostgreSQL e pgAdmin;
- dispositivo Android e computador na mesma rede para testes locais;
- chave do Google Maps;
- conta de e-mail remetente com senha de aplicativo, caso teste recuperação de senha.

### 6.2 Banco PostgreSQL

Criar o banco:

```sql
CREATE DATABASE fitworkup_db;
```

Configuração padrão de desenvolvimento encontrada:

```text
jdbc:postgresql://localhost:5432/fitworkup_db
usuário: postgres
porta da API: 8083
```

Prefira variáveis de ambiente para senha e demais segredos:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/fitworkup_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="SUA_SENHA_LOCAL"
$env:JWT_SECRET="UMA_CHAVE_LONGA_ALEATORIA_COM_PELO_MENOS_64_CARACTERES"
$env:MAIL_USERNAME="email_remetente@gmail.com"
$env:MAIL_PASSWORD="SENHA_DE_APLICATIVO"
.\mvnw.cmd spring-boot:run
```

Não levar a chave JWT de desenvolvimento, senha do PostgreSQL, senha de e-mail ou chave do Maps para commits públicos.

### 6.3 Android

Criar ou atualizar `local.properties` sem commitá-lo:

```properties
sdk.dir=C:\\Users\\SEU_USUARIO\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=SUA_CHAVE_GOOGLE_MAPS
API_BASE_URL=http://IP_DO_NOTEBOOK:8083/
```

Para celular físico, não usar `10.0.2.2`: esse endereço é exclusivo do emulador Android. Use o IPv4 do notebook na rede local, libere a porta 8083 no firewall e confirme que celular e notebook estão na mesma rede Wi-Fi.

Compilar:

```powershell
.\gradlew.bat assembleDebug
```

APK esperado:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 6.4 Testes da API

```powershell
.\mvnw.cmd test
```

Em 18/08/2026, uma nova execução automática neste ambiente não conseguiu baixar Gradle/Maven porque o acesso de rede estava bloqueado. Isso não foi um erro de compilação do código. A última verificação anterior registrada havia concluído o APK e os testes relacionados à loja/ranking; retestar no notebook após baixar as dependências.

## 7. Estado do Git no momento do handoff

No momento da última conferência:

- frontend na branch `main`;
- commit mais recente do frontend: `8dd1abe` — recuperação de senha, perfil de amigo, gerenciamento de solicitações e invalidação de sessão;
- API na branch `main`, sem alterações locais detectadas;
- a única alteração ainda não commitada no frontend é este arquivo `CODEX_HANDOFF.md`.

Antes de trocar de computador:

1. revisar este documento;
2. adicioná-lo ao Git e criar um commit;
3. enviar frontend e API aos respectivos repositórios remotos;
4. confirmar no GitHub que o commit `8dd1abe` e o commit deste handoff aparecem;
5. clonar ambos no notebook.

Se qualquer outro arquivo aparecer em `git status` antes da mudança, revise e preserve essa alteração também.

## 8. Problemas e riscos conhecidos

1. A rota GPS ainda não faz parte do contrato/persistência oficial da API.
2. `WorkoutSyncWorker` ainda não sincroniza as atividades pendentes.
3. A sincronização imediata usa coroutine em memória e pode ser interrompida pelo Android.
4. Configurações, exceto logout, ainda são majoritariamente visuais.
5. Google Sign-In não está implementado.
6. Grupos ainda não existem no backend.
7. Personalização de avatar e seleção de títulos estão incompletas.
8. Conquistas existem somente para passos diários.
9. A API ainda mantém calorias no contrato, embora a interface Android não as apresente.
10. Perfil e telas remotas precisam de testes sistemáticos offline e em tema escuro.
11. O README atual do frontend contém apenas o título antigo `FitWorkUpMobile` e precisa ser reescrito.
12. O `pom.xml` ainda contém drivers de SQL Server e PostgreSQL; confirmar se SQL Server pode ser removido após validar que nenhum perfil o utiliza.

## 9. Ordem recomendada para continuar

### Prioridade 0 — preservar o trabalho

1. commit e push das mudanças locais do frontend;
2. clonar frontend e API no notebook;
3. configurar segredos locais;
4. executar testes da API e `assembleDebug`;
5. testar login, logout, expiração 401 e recuperação de senha.

### Prioridade 1 — fechar o fluxo individual

1. implementar sincronização real no `WorkoutSyncWorker`;
2. persistir rota no backend e alinhar DTOs;
3. unificar calorias e resumo diário;
4. testar calendário e último percurso após reinstalação/login em outro aparelho;
5. completar ConfigScreen e preferências do usuário;
6. finalizar personalização, inventário e títulos.

### Prioridade 2 — grupos

1. modelar sessões e participantes na API;
2. criar contratos e testes;
3. construir lobby Compose;
4. associar atividades individuais ao grupo;
5. implementar atualização do lobby;
6. avaliar compartilhamento de rota somente após resolver privacidade e autorização.

### Prioridade 3 — expansão

1. novas conquistas;
2. cache offline de perfil/amizades/loja/ranking;
3. imagens para itens e medalhas;
4. IA Coach opcional;
5. refinamento visual e acessibilidade.

## 10. Regras para futuras conversas do Codex

Ao abrir uma nova conversa, informar:

```text
Leia CODEX_HANDOFF.md por completo antes de alterar o projeto.
Trabalhe no módulo Android app/ da raiz.
Considere FitWorkUp-frontend e FitWorkUp-api como um único sistema.
Confirme contratos Retrofit/Spring antes de mudar DTOs.
Não trate funcionalidades parciais como concluídas.
Preserve alterações locais não relacionadas.
Antes de finalizar, compile/teste na proporção do risco e informe limitações reais.
```

Decisões permanentes:

- servidor é autoridade para saldo e recompensas;
- JWT pode identificar por e-mail ou username;
- somente atividade validada concede recompensa;
- sensores mitigam inconsistências, não garantem antifraude absoluta;
- ranking principal é semanal;
- cada integrante de grupo registra e recebe recompensa individualmente;
- IA é opcional;
- dados duráveis e decisões importantes devem ser registrados no repositório, não apenas no histórico do chat.

## 11. Documentação acadêmica relacionada

O TCC não está dentro destes dois repositórios. Arquivos trabalhados anteriormente estavam em `C:\Users\ronal\Downloads`, incluindo `FitWorkUp.tex` e `biblio.bib`.

Diretrizes de redação já definidas:

- manter DSR na Metodologia;
- relacionar COM-B e técnicas de mudança por matriz de rastreabilidade;
- evitar afirmar eficácia comportamental antes da avaliação;
- descrever antifraude como verificação/mitigação;
- IA Coach como componente complementar e opcional;
- verificar se toda citação usada possui entrada correspondente no `.bib`;
- remover cercas Markdown de dentro dos arquivos LaTeX.

Transfira também a pasta completa do projeto Overleaf/TCC ou mantenha-a sincronizada pelo próprio Overleaf.

---

Este documento deve ser atualizado sempre que uma funcionalidade mudar de **planejada** para **parcial** ou **implementada**, ou quando contratos HTTP, variáveis de ambiente e caminhos importantes forem alterados.
