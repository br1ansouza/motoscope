# MotoScope

Aplicativo Android em desenvolvimento para exibição e gravação de telemetria de motocicletas por adaptador OBD2 Bluetooth.

O foco inicial é a Royal Enfield Hunter 350. O projeto ainda não se comunica com a ECU: o scanner e o adaptador não chegaram, e até lá o painel roda sobre um simulador. A integração prevista fará somente leitura, sem alteração de parâmetros ou comandos de atuação; a lista de comandos AT permitidos e os modos OBD de leitura estão fixados no código.

## Estado atual

- Painel em tempo real com RPM central, barra de rotação, indicadores de transporte e de ECU, seleção de métricas visíveis e três layouts.
- Gravação de sessão com serviço em primeiro plano, que continua com a tela apagada, e recuperação de sessão interrompida.
- Histórico de sessões com resumo por métrica, diário de eventos e exportação em CSV e JSON pelo seletor de pasta do Android.
- Tela de diagnóstico com a inicialização do adaptador e a descoberta de PIDs suportados, para usar quando o scanner chegar.
- Protocolo ELM327 com framing, parsing, conferência de eco e fila serial com timeout e reenvio.
- Sem transporte Bluetooth, sem PIDs da Hunter e sem conexão real com a moto.
- Sem backend, conta ou permissão de internet. O primeiro build precisa baixar dependências.

## Stack

Kotlin 2.4.20, Jetpack Compose, Material 3, Coroutines/Flow, Room, Hilt, KSP, Gradle Kotlin DSL e Version Catalog. Qualidade com JUnit, Robolectric, Android Lint, detekt e ktlint.

## Executar localmente

Use JDK 21, Android SDK Platform 37 e Build Tools 37.0.0. O aplicativo requer Android 8.0/API 26 ou superior; a versão do aparelho dedicado ainda será confirmada. O `targetSdk` permanece em 35 até que a mudança de comportamento em execução seja validada em aparelho.

Copie `local.properties.example` para `local.properties` e ajuste `sdk.dir`, ou configure `ANDROID_HOME`. Abra a pasta no Android Studio compatível com AGP 9.4.0.

```sh
./gradlew :app:assembleDebug
./gradlew quality
```

O APK de debug fica em `app/build/outputs/apk/debug/app-debug.apk`. Instale com `adb install -r app/build/outputs/apk/debug/app-debug.apk`. O release é minificado e não assinado; nenhuma chave é necessária para compilar.

`quality` compila debug/release e executa testes unitários das duas variantes, lint, detekt e verificação de formatação. Para formatar: `./gradlew ktlintFormat`.

## Organização

- `app`: Application, Activity, raiz de composição Hilt e navegação entre painel, histórico e diagnóstico.
- `core:model`: contratos de telemetria, conexão e sessão, em Kotlin puro.
- `core:ui`: tema, cores, tipografia, formas, espaçamento e rótulos de métrica.
- `core:telemetry`: feed, engine de estado ao vivo e janela de frescor.
- `core:recording`: motor de gravação em lotes, diário de eventos e escrita transacional.
- `core:history`: leitura de sessões, resumo, eventos, amostras paginadas e exportação.
- `core:settings`: catálogo do painel, visibilidade de métrica, layout e moto.
- `core:vehicle`: perfis das motos J-350.
- `core:database`: Room; sessões, amostras, eventos e preferências, com migrações explícitas e schemas exportados.
- `protocol:elm327`: comandos, framing, parsing, fila serial e sondagem de PIDs suportados.
- `simulator`: feed de telemetria e transporte ELM327 simulados.
- `feature:dashboard`, `feature:recording`, `feature:history`, `feature:diagnostics`: telas e ciclos de uso.
- `buildSrc`: convenções de módulos Android e qualidade.

Os módulos ainda não criados, como `core:bluetooth` e `vehicle:hunter350`, dependem da fase de descoberta com o hardware em mãos.

O contexto local fica em `AGENTS.md` e `docs/README.md`, com decisões em `docs/architecture/fundacao.md`. Esses arquivos são privados ao ambiente de trabalho e ignorados pelo Git; não acompanham o clone público.

## Contribuir

Use `feature/*`, `fix/*` ou `chore/*`, integre em `dev` e promova para `main` somente após `./gradlew quality` passar. `main` e `dev` exigem pull request com CI verde e não aceitam push direto. Commits seguem Conventional Commits em português. Dependabot abre propostas semanais para `dev`; atualizações exigem revisão de compatibilidade e CI, sem merge automático. Mudanças em Kotlin/KSP, AGP/Gradle, Compose e processadores devem ser avaliadas em conjunto, num ciclo próprio.

O lint mantém recomendações de atualização de versões e target SDK como informações visíveis no relatório: este ciclo valida um conjunto estável, sem publicação na Play Store. Os demais warnings continuam bloqueando a verificação.
