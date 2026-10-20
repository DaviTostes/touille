# Publicar o Touille na Google Play

Tudo que é necessário para a primeira publicação já está gerado. Este guia explica o que usar,
como reenviar versões e o que **não** pode ser perdido.

---

## 1. O que foi gerado

| Arquivo | Para que serve |
|---|---|
| `publishing/touille-1.0-release.aab` | **Bundle para subir no Play Console** (formato exigido para apps novos) |
| `publishing/touille-1.0-release.apk` | APK assinado para instalar direto em aparelhos (teste/distribuição fora da Play) |
| `publishing/CHECKSUMS.txt` | SHA-256 dos dois artefatos |
| `publishing/keystore-info.txt` | Alias, validade e fingerprints da chave |
| `publishing/privacy-policy.html` | Política de privacidade pronta (PT + EN) — precisa ser hospedada |
| `publishing/listing.md` | Nome, descrições PT/EN, categoria, respostas de segurança de dados e checklist |
| `publishing/assets/icon-512.png` | Ícone da ficha da loja (512×512) |
| `publishing/assets/feature-graphic-1024x500.png` | Feature graphic (1024×500) |
| `publishing/screenshots/` | 4 capturas de tela (1080×2400) |

**AAB:** SHA-256 `bb5af0d8a085459e80fe8f399e0855ee415b95a1576196e9a4f6c09ebc5c7c74` (3,8 MB)
**APK:** SHA-256 `655c5265cd07ec8d7f104ed95248d1569fcd0acf8c8080ca976d89103e540fcb` (1,6 MB)

Assinatura (upload key):
- Alias `touille`, RSA 4096, SHA-384, válida até **2056**
- SHA-256: `3E:24:7B:E0:D9:B8:FB:FA:51:6A:4E:E8:1E:65:A8:AD:39:91:1B:E2:3B:7C:3A:95:13:9A:86:13:5F:5E:4D:46`

---

## 2. ⚠️ A keystore: guarde como ouro

- **Arquivo:** `~/keystores/touille-release.jks`
- **Senhas:** `keystore.properties` (na raiz do projeto; já está no `.gitignore`)
- **Fora do projeto:** a keystore foi criada fora da pasta do app para não ir para o Git.

Faça **dois backups** (ex.: pendrive + cofre de senhas/armazenamento em nuvem privado) com:
1. `touille-release.jks`
2. `keystore.properties`

Sem essa chave **não é possível enviar atualizações** para o app já publicado (só dá para pedir
reset da chave de upload ao Google, o que demora). Nunca suba a keystore para o GitHub.

> `keystore.properties` tem a senha em texto puro — é o padrão do Android para builds locais.
> Mantenha o arquivo somente na sua máquina/backup privado.

---

## 3. Passo a passo no Play Console

1. **Conta de desenvolvedor** — https://play.google.com/console
   - Taxa única de US$ 25.
   - Verificação de identidade (pessoa física ou jurídica). Para CNPJ, pode ser pedido D-U-N-S.
2. **Criar app**
   - Nome: `Touille`
   - Idioma padrão: Português (Brasil)
   - Tipo: Aplicativo · Gratuito
   - Declarações: não é jogo, não é para crianças.
3. **Configurar a assinatura**
   - Em *Configuração → Integridade do app*, aceite o **Play App Signing**.
   - Use a chave gerada aqui como **chave de upload** (o AAB já está assinado com ela).
   - O Google gera e guarda a chave de assinatura final dos usuários; você só reenvia AABs.
4. **Ficha da loja** (*Presença na loja → Ficha principal*)
   - Use os textos de `publishing/listing.md`.
   - Ícone 512×512, feature graphic 1024×500 e as capturas de `publishing/`.
   - Categoria: **Comida e bebida**.
   - E-mail de contato: troque o placeholder pelo seu.
5. **Política de privacidade**
   - Publique `publishing/privacy-policy.html` em uma URL pública (GitHub Pages, Google Sites,
     Notion público ou seu domínio) e cole o link no Play Console.
   - O app **não** coleta dados, então a política é curta — mas o link é obrigatório.
6. **Segurança dos dados**
   - “O app coleta ou compartilha dados obrigatórios?” → **Não**.
   - Anúncios → **Não** · Compras no app → **Não**.
7. **Classificação de conteúdo** — questionário: responda “não” para violência, sexo, drogas,
   apostas, linguagem e compras digitais → **Livre**.
8. **Público-alvo** — não é para crianças; marque **18 anos ou mais** (evita exigências do
   programa Famílias).
9. **Teste interno** (*Testar e lançar → Teste interno*)
   - Suba `touille-1.0-release.aab`, adicione seu e-mail como testador e instale pelo link.
   - Teste login (não existe), navegação, câmera/galeria e o fluxo de lista de compras.
10. **Produção**
    - Se a conta for **pessoal e nova**, o Google exige um teste fechado com pelo menos
      **12 testadores por 14 dias** antes de liberar produção (confira a regra vigente na conta).
    - Contas de organização (CNPJ) normalmente não têm essa exigência.
    - Envie o AAB em *Produção → Criar nova versão* e revise as declarações.

---

## 4. Gerar a próxima versão

Antes de cada envio, aumente o `versionCode` (obrigatório) e, de preferência, o `versionName`:

```kotlin
// app/build.gradle.kts
defaultConfig {
    versionCode = 2          // sempre +1
    versionName = "1.0.1"
}
```

Depois:

```bash
cd ~/touille
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew clean bundleRelease
# saída: app/build/outputs/bundle/release/app-release.aab
```

E copie para `publishing/` com o novo nome (`touille-1.0.1-release.aab`).

### Assinatura
A build lê `keystore.properties` automaticamente. Se o arquivo não existir (ex.: outra máquina),
o release cai na chave de debug — **não publique um AAB gerado assim**. Para conferir:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
# o SHA-256 deve ser 3e247be0d9b8fbfa516a4ee81e65a8ad39911be23b7c3a95139a86135f5e4d46
```

---

## 5. Detalhes técnicos

- **applicationId:** `com.touille`
- **minSdk 26** (Android 8.0) · **targetSdk 36** (exigência do Play desde 31/08/2026)
- **compileSdk 36** · AGP 8.11.2 · Kotlin 2.0.21 · Gradle 8.13
- **R8/minify + shrink de recursos** ativados no release (APK de 1,6 MB)
- **Nenhuma permissão declarada** (sem internet, câmera, localização, etc.)
- **Nenhuma coleta de dados**; tudo fica no armazenamento privado do app
- **Backup do Android:** `android:allowBackup="true"` — o sistema pode incluir os dados no
  backup do usuário (está descrito na política de privacidade)
- Assinatura **v2** do APK + JAR signing no AAB (`jarsigner -verify` → `jar verified`)

---

## 6. Instalar o APK novo neste celular

O app instalado no aparelho hoje foi assinado com a **chave de debug**. O APK de release usa a
chave nova, então o Android recusa a atualização por cima (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).

- Para testar o APK de release **neste** celular: desinstale o Touille antes (isso apaga receitas,
  lista e geladeira) e instale `touille-1.0-release.apk`.
- Isso **não afeta a publicação**: no Play, o Google assina os downloads com a chave de assinatura
  do Play App Signing. O APK de debug pode continuar instalado para você testar.

---

## 7. Checklist final antes de enviar

- [ ] Trocar `contato@touille.app` pelo seu e-mail real (política + ficha da loja)
- [ ] Hospedar a política de privacidade e colar a URL
- [ ] Conferir screenshots (Play prefere 9:16; as atuais são 9:20 — se a loja reclamar, grave
      novamente com `adb shell wm size 1080x1920`, capture e depois `wm size reset`)
- [ ] Subir o AAB no teste interno e instalar em pelo menos 2 aparelhos
- [ ] Backupear a keystore + `keystore.properties`
- [ ] `versionCode` incrementado a cada envio
