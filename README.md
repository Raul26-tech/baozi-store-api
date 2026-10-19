# Baozi Store API

API REST acadêmica da disciplina Desenvolvimento Web Back-End (Uninter).
A especificação oficial é o enunciado **TRABALHO_DE_DESENVOLVIMENTO_WEB_BACK_2026**;
**ATIVIDADE PRATICA - MODELO** define a estrutura do relatório de entrega.

## Escopo implementado

API acadêmica em Java 21 / Spring Boot 4.1.1, Spring MVC, Spring Data JPA e MySQL.
Implementados os 12 endpoints obrigatórios de Cliente, Produto e Pedido.
Cada pedido contém um cliente, um produto e uma quantidade positiva.
Sem atualização (PUT), autenticação ou outras funcionalidades opcionais.

## Requisitos

- JDK 21 (`java -version` deve indicar 21; configure `JAVA_HOME`).
- MySQL 8.0.46 em execução local, sem Docker.
- Acesso à internet na primeira execução do Maven Wrapper.

Não é necessário instalar Maven. O Wrapper está incluído para Linux/macOS
(`mvnw`) e Windows (`mvnw.cmd`).

## Banco e variáveis de ambiente

Se o banco ainda não existir, crie-o usando uma conta administrativa do MySQL:

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

## Criar as tabelas

O Hibernate mantém `spring.jpa.hibernate.ddl-auto=none`; scripts SQL são
executados manualmente, uma única vez e em ordem, no banco definido em `DB_URL`:

1. `database/001-create-clientes.sql`
2. `database/002-create-produtos.sql`
3. `database/003-create-pedidos.sql`

Se a tabela de clientes já foi criada na etapa anterior, **não execute 001 novamente**.
Execute somente 002 e 003. Os scripts não apagam dados, não alteram a tabela
`clientes` e não fazem migração de tabelas preexistentes. Não desative as chaves
estrangeiras; se uma tabela já existir, confira seu esquema antes de continuar.

Exemplo no Bash (ajuste host, porta, usuário e banco conforme `DB_URL`):

```bash
mysql -h localhost -P 3306 -u seu_usuario -p baozi_store < database/002-create-produtos.sql
mysql -h localhost -P 3306 -u seu_usuario -p baozi_store < database/003-create-pedidos.sql
```

No cliente MySQL interativo, inclusive no Windows:

```sql
USE baozi_store;
SOURCE database/002-create-produtos.sql;
SOURCE database/003-create-pedidos.sql;
SHOW CREATE TABLE produtos;
SHOW CREATE TABLE pedidos;
```

Todas as tabelas usam InnoDB e ID `BIGINT AUTO_INCREMENT`. O preço usa
`DECIMAL(12,2)`. `pedidos.cliente_id` e `pedidos.produto_id` são chaves estrangeiras
com `ON DELETE RESTRICT`, sem exclusão em cascata. Exclua os pedidos antes de
excluir o cliente/produto associado. As referências são mapeadas como IDs na
entidade Pedido para manter o JSON exigido; a relação 1:N é garantida no banco.

`estoque` é Boolean de disponibilidade, conforme o enunciado, e não um contador.
Não há baixa automática nem bloqueio de pedidos baseado nesse campo.

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
credenciais configuradas e os scripts de criação das tabelas já executados.

## Endpoints

| Recurso | Criar | Listar | Consultar | Excluir |
| --- | --- | --- | --- | --- |
| Cliente | `POST /clientes` | `GET /clientes` | `GET /clientes/{id}` | `DELETE /clientes/{id}` |
| Produto | `POST /produtos` | `GET /produtos` | `GET /produtos/{id}` | `DELETE /produtos/{id}` |
| Pedido | `POST /pedidos` | `GET /pedidos` | `GET /pedidos/{id}` | `DELETE /pedidos/{id}` |

- POST válido: **201 Created**, JSON com os campos da entidade e ID gerado.
- GET válido: **200 OK**, objeto na consulta por ID ou array na listagem (`[]` se vazio).
- DELETE válido: **204 No Content**, sem corpo.
- Consulta/exclusão de ID inexistente ou referência ausente no POST de Pedido: **404 Not Found**.
- Campos inválidos, ID não numérico na URL ou JSON malformado: **400 Bad Request**.
- Exclusão de Cliente/Produto associado a pedidos: **409 Conflict**, sem exclusão em cascata.
- Conflito de integridade entre a checagem da referência e a gravação de Pedido: **409 Conflict**.

O conflito de integridade usa `ProblemDetail` com status e mensagem genérica,
sem expor detalhes SQL. Os demais erros usam o comportamento padrão do Spring;
não há contrato personalizado de mensagens por campo.

### Exemplos para curl e Postman

No Postman, configure o método e a URL e use **Body → raw → JSON** nos POSTs.
O header é `Content-Type: application/json`. Substitua o nome e RU de exemplo
pelos seus dados reais para os prints do trabalho.

```bash
curl -i -X POST http://localhost:8080/clientes \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Raul1234567","clienteDesde":"2026-01-01"}'

curl -i -X POST http://localhost:8080/produtos \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Baozi de porco","preco":12.50,"estoque":true}'

# Use os IDs retornados acima, que podem ser diferentes de 1.
curl -i -X POST http://localhost:8080/pedidos \
  -H 'Content-Type: application/json' \
  -d '{"clienteId":1,"produtoId":1,"quantidade":3}'
```

Respostas de exemplo dos POSTs (IDs ilustrativos):

```json
{"id":1,"nome":"Raul1234567","clienteDesde":"2026-01-01"}
```

```json
{"id":1,"nome":"Baozi de porco","preco":12.50,"estoque":true}
```

```json
{"id":1,"clienteId":1,"produtoId":1,"quantidade":3}
```

Consultas:

```bash
curl -i http://localhost:8080/clientes
curl -i http://localhost:8080/clientes/1
curl -i http://localhost:8080/produtos
curl -i http://localhost:8080/produtos/1
curl -i http://localhost:8080/pedidos
curl -i http://localhost:8080/pedidos/1
```

Exclusões, respeitando as dependências (use IDs de registros de teste):

```bash
curl -i -X DELETE http://localhost:8080/pedidos/1
curl -i -X DELETE http://localhost:8080/clientes/1
curl -i -X DELETE http://localhost:8080/produtos/1
```

### Validações

| Entidade | Regras |
| --- | --- |
| Cliente | `nome` obrigatório, não vazio/em branco, até 255 caracteres; `clienteDesde` obrigatória, data válida em `yyyy-MM-dd`, atual ou passada. |
| Produto | `nome` obrigatório, não vazio/em branco, até 255 caracteres; `preco` obrigatório, de 0.01 a 9999999999.99, até 2 casas decimais; `estoque` Boolean obrigatório (`true` ou `false`). |
| Pedido | `clienteId` e `produtoId` obrigatórios, positivos e existentes; `quantidade` Integer obrigatória, positiva, até 2147483647. |

IDs dos novos registros são gerados pelo banco; IDs enviados nos POSTs são ignorados.
Nomes duplicados são permitidos. Não há total de pedido ou múltiplos produtos.
A desserialização rejeita números fracionários para IDs/quantidades, evitando
truncamento silencioso (`spring.jackson.deserialization.accept-float-as-int=false`).
As configurações de MySQL e carregamento do `.env` foram preservadas.

### Coleção Postman

Importe `postman/baozi-store.postman_collection.json`. A variável `baseUrl` vale
`http://localhost:8080`. Os POSTs guardam automaticamente `clienteId`, `produtoId`
e `pedidoId` retornados nas variáveis da coleção. Os valores de nome/RU são exemplos.

Execute na ordem: criar Cliente → criar Produto → criar Pedido → listar e consultar.
A coleção inclui tentativas de exclusão dos pais com pedido associado (esperado 409),
dados inválidos (400), consulta ausente (404), e exclusão final na ordem correta (204).
As requisições de exclusão removem os registros criados para o teste.
Revise as variáveis antes de executá-las contra um banco com dados importantes.
Guarde prints de POST, listagem, consulta e DELETE para o relatório da faculdade.

## Testes automatizados

```bash
./mvnw clean verify
```

Testes MVC com MockMvc, JSON e Bean Validation reais, com repositórios simulados.
Incluem os testes existentes do POST de Cliente, novos GET/DELETE das três entidades,
POST de Produto/Pedido, campos inválidos e limites, referências ausentes e conflitos
simulados de integridade. Mockito usa o mock maker por subclasses.

Não exigem MySQL ou `.env` e não substituem a validação local de persistência.
Ainda precisam ser validados no **MySQL 8.0.46** local:

- Execução de 002 e 003, esquema e chaves estrangeiras.
- ID automático e leitura dos registros persistidos pelos três recursos.
- Cliente/Produto vinculados não podem ser apagados e o Pedido permanece intacto.
- Após excluir Pedido, exclusão de Cliente/Produto retorna 204; nova consulta retorna 404.
- As evidências no Postman devem usar os mesmos dados da situação fictícia do relatório.

## Organização

- `model/`: entidades Cliente, Produto e Pedido.
- `repository/`: interfaces Spring Data JPA.
- `controller/`: controladores MVC e tratamento de conflito de integridade.
- `database/`: scripts SQL manuais e ordenados.
- `src/test/`: testes MVC.
- `postman/`: coleção para validação manual.

Não foram adicionadas dependências para esta etapa.
