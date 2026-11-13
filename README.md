# Pizzaria API

Este projeto é uma API REST para ajudar no atendimento de uma pizzaria: dá para organizar categorias e produtos, receber pedidos e acompanhar cada pedido até a finalização. A ideia é manter o fluxo simples para quem atende no balcão e oferecer uma base segura para evoluir o sistema.

## O que a API oferece

- Cadastro e consulta de categorias.
- Cadastro, edição, consulta e desativação de produtos.
- Armazenamento de imagens de produtos no Cloudinary.
- Cadastro de usuários e autenticação com JWT.
- Criação de pedidos, gerenciamento dos itens e acompanhamento do preparo.
- Documentação interativa das rotas com Swagger.

## Tecnologias

- Java 25 e Spring Boot
- Spring Web MVC, Spring Data JPA e Spring Security
- PostgreSQL e Flyway
- JWT para autenticação
- MapStruct para conversão entre entidades e DTOs
- Cloudinary para imagens
- JUnit, Mockito e Spring MockMvc para testes
- Testcontainers para testes de integração com PostgreSQL
- Springdoc OpenAPI / Swagger UI

## Antes de começar

Você vai precisar de:

- JDK 25
- Docker Desktop e Docker Compose para subir o banco local
- Uma conta Cloudinary para enviar imagens de produtos

O Maven já vem pelo **Maven Wrapper**, então não é necessário instalar o Maven separadamente. No IntelliJ, selecione o JDK 25 tanto no Project SDK quanto no Maven Runner.

## Configuração local

Antes de iniciar o projeto, configure as variáveis de ambiente abaixo. No PowerShell, substitua os valores de exemplo pelos seus dados:

```powershell
$env:DB_USER = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
$env:JWT_SECRET = "sua_chave_base64"
$env:CLOUDINARY_URL = "cloudinary://api_key:api_secret@seu_cloud_name"
```

O `JWT_SECRET` deve ser uma chave em Base64 que represente pelo menos 32 bytes. Para gerar uma chave aleatória no PowerShell:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Não publique essas credenciais nem as coloque no Git.

Suba o PostgreSQL definido em `docker-compose.yaml`:

```powershell
docker compose up -d db
```

O banco fica disponível em `localhost:5555`. O Compose cria o banco `pizzaria` usando `DB_USER` e `DB_PASSWORD`. Ao iniciar a aplicação, o Flyway aplica as migrations e o Hibernate valida se o schema corresponde às entidades. Alterações no banco devem ser feitas com novas migrations em `src/main/resources/db/migration`, não pelo Hibernate.

Inicie a API:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação fica disponível em `http://localhost:8080`. Para parar o banco:

```powershell
docker compose down
```

O volume `pizzaria-data` mantém os dados entre reinicializações. Só remova o volume se realmente quiser apagar o banco local.

## Swagger e documentação das rotas

Com a aplicação rodando, abra:

**[Swagger UI](http://localhost:8080/swagger-ui/index.html)**

O documento OpenAPI em JSON também fica disponível em `http://localhost:8080/v3/api-docs`. A interface permite consultar os parâmetros e exemplos das rotas e enviar requisições para a API.

Para testar uma rota protegida:

1. Faça login em `POST /session`.
2. Copie o token JWT retornado.
3. Clique em **Authorize** no Swagger e informe o token.
4. Execute a rota desejada.

O Swagger está liberado sem autenticação. O contato da documentação é **Marques Souza**; ao clicar no e-mail, você pode iniciar uma mensagem para `marques.wsouza357@gmail.com`.

## Autenticação e permissões

O cadastro público (`POST /users`) cria usuários com o papel `STAFF`. O login (`POST /session`) valida e-mail e senha e retorna um JWT. Nas rotas protegidas, envie o token no cabeçalho:

```http
Authorization: Bearer <seu-token>
```

As consultas e operações de pedido exigem autenticação. Criar, editar, desativar ou reativar categorias e produtos exige o papel `ADMIN`. O projeto não oferece uma rota pública para promover usuários a `ADMIN`; essa configuração deve ser feita por um processo administrativo seguro.

## Rotas disponíveis

`{id}`, `{orderId}` e `{itemId}` representam UUIDs. As rotas marcadas como autenticadas exigem um JWT válido.

### Usuários e autenticação

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| `POST` | `/users` | Público | Cadastra um usuário como `STAFF` |
| `POST` | `/session` | Público | Faz login e retorna um JWT |
| `GET` | `/me` | Autenticado | Consulta os dados do usuário do token |

Exemplo de cadastro e login:

```json
{
  "name": "Maria",
  "email": "maria@example.com",
  "password": "senha123"
}
```

O login recebe `email` e `password`.

### Categorias

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| `POST` | `/categories` | `ADMIN` | Cria uma categoria |
| `GET` | `/categories` | Autenticado | Lista categorias |
| `GET` | `/categories/{id}` | Autenticado | Busca uma categoria |
| `PATCH` | `/categories/{id}` | `ADMIN` | Atualiza uma categoria |
| `DELETE` | `/categories/{id}` | `ADMIN` | Desativa uma categoria |

Exemplo de categoria:

```json
{
  "name": "Pizzas",
  "description": "Pizzas salgadas e doces"
}
```

### Produtos

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| `POST` | `/products` | `ADMIN` | Cadastra produto e imagem |
| `GET` | `/products/{id}` | Autenticado | Busca um produto |
| `GET` | `/products` | Autenticado | Lista produtos ativos com paginação |
| `PATCH` | `/products/{id}` | `ADMIN` | Atualiza dados e/ou imagem |
| `DELETE` | `/products/{id}` | `ADMIN` | Desativa um produto |
| `PUT` | `/products/{id}/reactivate` | `ADMIN` | Reativa um produto |

O cadastro e a atualização usam `multipart/form-data`. Para cadastrar, envie os campos `name`, `description`, `price` e `categoryId`, além do arquivo de imagem no campo `file`. Na atualização, os campos e a imagem são opcionais, mas é preciso enviar pelo menos uma alteração. O limite de upload é de 5 MB.

É possível filtrar a lista pela categoria e controlar a paginação:

```text
GET /products?page=0&size=10&categoryId=<uuid-da-categoria>
```

O número da página começa em zero. Por padrão, os produtos são ordenados pelo nome em ordem decrescente.

### Pedidos

| Método | Rota | Acesso | O que faz |
|---|---|---|---|
| `POST` | `/orders` | Autenticado | Cria um pedido em rascunho |
| `GET` | `/orders` | Autenticado | Lista pedidos com paginação e itens |
| `GET` | `/orders/{orderId}` | Autenticado | Consulta um pedido e seus itens |
| `POST` | `/orders/{orderId}/items` | Autenticado | Adiciona um produto ao rascunho |
| `DELETE` | `/orders/{orderId}/items/{itemId}` | Autenticado | Remove um item do rascunho |
| `PATCH` | `/orders/{orderId}/send` | Autenticado | Envia o pedido para preparo |
| `PATCH` | `/orders/{orderId}/finish` | Autenticado | Finaliza um pedido enviado |
| `DELETE` | `/orders/{orderId}` | Autenticado | Exclui um pedido que ainda está em rascunho |

Para criar um pedido:

```json
{
  "table": 5,
  "name": "Maria"
}
```

Para adicionar um produto:

```json
{
  "productId": "<uuid-do-produto>",
  "amount": 2
}
```

Um pedido começa como rascunho. Enquanto estiver nessa etapa, é possível adicionar ou remover itens e excluir o pedido. Para enviar ao preparo, ele precisa ter ao menos um item. Depois de enviado, pode ser finalizado; pedidos enviados ou finalizados não podem ser excluídos pela API.

A listagem de pedidos também aceita `page`, `size` e `sort`. Por padrão, retorna 10 pedidos por página, do mais recente para o mais antigo.

## Respostas de erro

As respostas de erro seguem o formato `ProblemDetail`. Entre os status mais comuns:

| Status | Significado |
|---|---|
| `400 Bad Request` | Dados enviados inválidos |
| `401 Unauthorized` | Token ausente ou inválido |
| `403 Forbidden` | Usuário autenticado sem permissão para a operação |
| `404 Not Found` | Recurso não encontrado |
| `409 Conflict` | Conflito, como recurso duplicado ou pedido vazio ao enviar |

## Estrutura do projeto

O código da aplicação está em `src/main/java/br/com/pizzaria`:

- `controller`: recebe as requisições HTTP e define as rotas.
- `service`: concentra as regras de negócio.
- `repository`: consultas e persistência com Spring Data JPA.
- `entity`: entidades mapeadas para as tabelas do banco.
- `dtos`: formatos de entrada e saída da API.
- `mapper`: conversões entre DTOs e entidades com MapStruct.
- `exception`: tratamento padronizado de erros.
- `config`: configurações de segurança, JWT, OpenAPI e Cloudinary.

As migrations do banco ficam em `src/main/resources/db/migration`. Os testes estão em `src/test/java`.

## Executar os testes

Para rodar a suíte completa:

```powershell
.\mvnw.cmd test
```

Os testes unitários, de controllers e de segurança não precisam de um banco real. Os testes de integração com PostgreSQL usam Testcontainers e precisam do Docker Desktop funcionando. Se o Docker não estiver acessível, esses testes são ignorados; confira o resumo do Maven para confirmar se foram executados.

Para executar somente os testes de integração:

```powershell
.\mvnw.cmd -Dtest=PostgreSqlIntegrationTest test
```

Os relatórios detalhados ficam em `target/surefire-reports`.
