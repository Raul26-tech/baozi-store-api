# Baozi Store API

API REST acadêmica da disciplina Desenvolvimento Web Back-End (Uninter).
A especificação oficial é o enunciado **TRABALHO_DE_DESENVOLVIMENTO_WEB_BACK_2026**;
**ATIVIDADE PRATICA - MODELO** define a estrutura do relatório de entrega.

## Etapa atual

Estrutura inicial com Java 21, Spring Boot 4.1.1, Maven, Spring Web MVC,
Spring Data JPA e MySQL. Ainda não existem entidades nem endpoints.
Os CRUDs de Cliente, Produto e Pedido serão implementados individualmente,
mediante aprovação. O relatório, diagrama UML e prints do Postman serão
produzidos nas etapas correspondentes.

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
Não versione credenciais. Arquivos `.env` são ignorados pelo Git, mas não são
carregados automaticamente pela aplicação.

O Hibernate está configurado com `ddl-auto=none`: nesta etapa não cria nem
altera tabelas. A estratégia de criação das tabelas será definida ao implementar
as entidades. `open-in-view=false` mantém o acesso JPA fora da camada web.

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

A aplicação utiliza a porta 8080. É necessário ter MySQL acessível e as
credenciais configuradas para iniciar. Como não há endpoints de negócio,
uma chamada a `http://localhost:8080/` retorna 404; isso é esperado.

`clean verify` compila e empacota sem exigir MySQL, pois ainda não há testes
de contexto ou integração. Esta etapa não contém testes automatizados;
o sucesso do build não valida a conexão com o banco.

## Organização

- `src/main/java/br/com/baozi/store/BaoziStoreApiApplication.java`: inicialização.
- `src/main/java/br/com/baozi/store/model/`: futuras entidades.
- `src/main/java/br/com/baozi/store/repository/`: futuros repositórios JPA.
- `src/main/java/br/com/baozi/store/controller/`: futuros controladores REST.
- `src/main/resources/application.properties`: configuração da aplicação.
- `pom.xml`: dependências e compilação Java 21.
- `.mvn/wrapper/`, `mvnw`, `mvnw.cmd`: Maven Wrapper oficial.

Os pacotes usam `package-info.java` para manter sua estrutura no Git sem
introduzir entidades, interfaces de repositório ou controladores nesta etapa.
