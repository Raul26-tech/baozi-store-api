# Baozi Store API

API REST acadêmica da disciplina Desenvolvimento Web Back-End (Uninter).
A especificação oficial é o enunciado **TRABALHO_DE_DESENVOLVIMENTO_WEB_BACK_2026**;
**ATIVIDADE PRATICA - MODELO** define a estrutura do relatório de entrega.

## Etapa atual

Java 21, Spring Boot 4.1.1, Maven, Spring Web MVC, Spring Data JPA e MySQL.
Implementado exclusivamente `POST /clientes`. As demais operações e as entidades
Produto e Pedido serão implementadas individualmente, mediante aprovação.
O relatório, diagrama UML e prints do Postman serão produzidos nas etapas correspondentes.

## Requisitos

- JDK 21 (`java -version` deve indicar 21; configure `JAVA_HOME`).
- MySQL 8 ou superior em execução.
- Acesso à internet na primeira execução do Maven Wrapper.

Não é necessário instalar Maven. O Wrapper está incluído para Linux/macOS
(`mvnw`) e Windows (`mvnw.cmd`).

## Banco e variáveis de ambiente

Crie o banco usando uma conta administrativa do MySQL:

```sql
CREATE DATABASE baozi_store CHARACTER SET utf8mb4;
```

Use um usuário MySQL com acesso a esse banco e configure as variáveis
no mesmo terminal em que executará a aplicação.

Linux/macOS (Bash):

```bash
export DB_URL='jdbc:mysql://localhost:3306/baozi_store'
export DB_USERNAME='seu_usuario'
read -rsp 'Senha MySQL: ' DB_PASSWORD
export DB_PASSWORD
printf '\n'
```

Windows (PowerShell):

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/baozi_store'
$env:DB_USERNAME = 'seu_usuario'
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', (Read-Host 'Senha MySQL' -AsSecureString)).Password
```

`DB_URL` é opcional e usa a URL local acima por padrão.
`DB_USERNAME` e `DB_PASSWORD` são obrigatórias e não possuem valores padrão.
Não versione credenciais. O arquivo `.env` na raiz é carregado automaticamente
por `spring.config.import=optional:file:.env[.properties]` e ignorado pelo Git.
Use o formato de propriedades `CHAVE=valor`, sem `export` e sem aspas envolvendo
os valores. Variáveis exportadas no terminal têm prioridade sobre o `.env`.
Execute a aplicação a partir da raiz para que o arquivo seja encontrado.

O Hibernate mantém `ddl-auto=none` e não cria nem altera tabelas.
A estratégia desta etapa é executar manualmente o script versionado
`database/001-create-clientes.sql`, uma única vez no banco indicado por `DB_URL`,
antes de iniciar a aplicação. Por exemplo, usando o cliente MySQL:

```bash
mysql -h localhost -P 3306 -u seu_usuario -p baozi_store < database/001-create-clientes.sql
```

Ajuste host, porta e banco conforme sua configuração. No PowerShell, abra o
cliente `mysql` e execute `SOURCE database/001-create-clientes.sql;`.
O script não é executado automaticamente e não apaga dados existentes.
Se `clientes` já existir, confira seu esquema antes de executar o script;
ele não faz migração de tabelas existentes.

A tabela usa `BIGINT AUTO_INCREMENT` para o ID, `VARCHAR(255) NOT NULL` para
`nome` e `DATE NOT NULL` para `cliente_desde`. `open-in-view=false` foi preservado.

## Compilar e executar

Na raiz do projeto, com JDK 21:

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

No PowerShell, substitua `./mvnw` por `.\mvnw.cmd`.
Se necessário, no Linux/macOS execute `chmod +x mvnw`.

Para executar o JAR após a compilação:

```bash
java -jar target/baozi-store-api-0.0.1-SNAPSHOT.jar
```

A aplicação utiliza a porta 8080. É necessário ter MySQL acessível,
credenciais configuradas e a tabela criada para cadastrar clientes.

## Cadastrar cliente

```bash
curl -i -X POST http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Raul1234567","clienteDesde":"2026-01-01"}'
```

No Postman, selecione `POST`, URL `http://localhost:8080/clientes`,
Body → raw → JSON, e use o mesmo corpo. Substitua `Raul1234567` pelo seu
nome seguido do RU real ao gerar as evidências exigidas pela faculdade.

Resposta esperada: **201 Created** com JSON (o ID pode variar):

```json
{
  "id": 1,
  "nome": "Raul1234567",
  "clienteDesde": "2026-01-01"
}
```

- `nome`: obrigatório, não pode ser vazio ou composto só de espaços; máximo de 255 caracteres.
- `clienteDesde`: obrigatória, formato `yyyy-MM-dd`, deve representar uma data válida, atual ou passada.
- `id`: gerado pelo banco; um ID enviado pelo cliente é ignorado.

Dados inválidos ou JSON malformado retornam **400 Bad Request** sem persistência.
O corpo de erro usa o comportamento padrão do Spring; não há contrato próprio
para mensagens de validação nesta etapa. Nomes duplicados são permitidos.
Não existem outros endpoints de negócio implementados.

## Testes

```bash
./mvnw clean verify
```

Os testes MVC usam o controller real, desserialização JSON e Bean Validation,
com repositório simulado. Cobrem 201 e resposta, ID enviado sendo ignorado,
nome ausente/nulo/vazio/em branco/longo, datas ausentes/nulas/futuras/inválidas,
JSON malformado e limites válidos. Não exigem MySQL nem `.env`. O Mockito usa o mock maker por subclasses
(suficiente para a interface do repositório), sem anexar agentes à JVM.
Não validam persistência real, geração do ID pelo MySQL ou execução do script SQL;
esses pontos precisam ser conferidos localmente com o MySQL e o Postman.

## Organização

- `src/main/java/br/com/baozi/store/BaoziStoreApiApplication.java`: inicialização.
- `src/main/java/br/com/baozi/store/model/`: entidade `Cliente`.
- `src/main/java/br/com/baozi/store/repository/`: `ClienteRepository` com Spring Data JPA.
- `src/main/java/br/com/baozi/store/controller/`: `ClienteController` com `POST /clientes`.
- `src/main/resources/application.properties`: configuração da aplicação.
- `pom.xml`: dependências e compilação Java 21.
- `.mvn/wrapper/`, `mvnw`, `mvnw.cmd`: Maven Wrapper oficial.

- `database/001-create-clientes.sql`: criação manual da tabela.
- `src/test/`: testes MVC do cadastro.
