# MotoScope

Aplicativo Android em desenvolvimento para exibição e gravação de telemetria de motocicletas por adaptador OBD2 Bluetooth.

O foco inicial é a Royal Enfield Hunter 350. O projeto prepara a base técnica e ainda não se comunica com a ECU. A integração prevista fará somente leitura, sem alteração de parâmetros ou comandos de atuação.

## Estado atual

- Tela inicial informativa com tema próprio sobre Material 3.
- Módulos `app`, `core:ui` e `core:database`.
- Hilt, estrutura inicial Room e testes de persistência.
- Sem conexão Bluetooth, dashboard, gravação ou histórico implementados.
- Sem backend, conta ou permissão de internet. O primeiro build precisa baixar dependências.

## Stack

Kotlin 2.2.10, Jetpack Compose, Material 3, Coroutines/Flow, Room, Hilt, KSP, Gradle Kotlin DSL e Version Catalog. Qualidade com JUnit, Robolectric, Android Lint, detekt e ktlint.

## Executar localmente

Use JDK 21, Android SDK Platform 35 e Build Tools 35.0.0. O aplicativo requer Android 8.0/API 26 ou superior; a versão do aparelho dedicado ainda será confirmada.

Copie `local.properties.example` para `local.properties` e ajuste `sdk.dir`, ou configure `ANDROID_HOME`. Abra a pasta no Android Studio compatível com AGP 8.9.2.

```sh
./gradlew :app:assembleDebug
./gradlew quality
```

O APK de debug fica em `app/build/outputs/apk/debug/app-debug.apk`. Instale com `adb install -r app/build/outputs/apk/debug/app-debug.apk`. O release é minificado e não assinado; nenhuma chave é necessária para compilar.

`quality` compila debug/release e executa testes unitários das duas variantes, lint, detekt e verificação de formatação. Para formatar: `./gradlew ktlintFormat`.

## Organização

- `app`: Application, Activity e composição do aplicativo.
- `core:ui`: tema, cores, tipografia, formas e espaçamento.
- `core:database`: persistência interna; não depende da interface.
- `buildSrc`: convenções de módulos Android e qualidade.

Os contratos de domínio, Canvas do painel e módulos de protocolo, telemetria e features serão criados conforme entrarem em uso. Não há simulador nesta etapa.

O contexto local fica em `AGENTS.md` e `docs/README.md`, com decisões em `docs/architecture/fundacao.md`. Esses arquivos são privados ao ambiente de trabalho e ignorados pelo Git; não acompanham o clone público.

## Contribuir

Use `feature/*`, `fix/*` ou `chore/*`, integre em `dev` e promova para `main` somente após `./gradlew quality` passar. Commits seguem Conventional Commits em português. Dependabot abre propostas semanais para `dev`; atualizações exigem revisão de compatibilidade e CI, sem merge automático. Mudanças em Kotlin/KSP, AGP/Gradle, Compose e processadores devem ser avaliadas em conjunto.
