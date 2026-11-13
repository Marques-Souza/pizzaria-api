# Pizzaria API

API REST para gerenciamento de categorias, produtos, usuarios e pedidos de uma pizzaria. O projeto usa Java 25, Spring Boot, PostgreSQL, Spring Data JPA, Flyway, Spring Security com JWT, MapStruct e Cloudinary para imagens de produtos.

## Requisitos

- JDK 25
- Docker e Docker Compose
- Maven (opcional; o projeto inclui Maven Wrapper)
- Uma conta Cloudinary para upload de imagens de produtos

Configure o IntelliJ para usar JDK 25 tanto no Project SDK quanto no Maven Runner.

## Configuracao local

Defina as variaveis de ambiente antes de iniciar a aplicacao:

| Variavel | Uso |
|---|---|
| `DB_USER` | Usuario do PostgreSQL |
| `DB_PASSWORD` | Senha do PostgreSQL |
| `JWT_SECRET` | Chave Base64 para assinatura JWT; deve representar pelo menos 32 bytes |
| `CLOUDINARY_URL` | URL de conexao da conta Cloudinary |

Exemplo no PowerShell (substitua os valores pelos seus; nao versione credenciais):

```powershell
$env:DB_USER = "seu_usuario"
$env:DB_PASSWORD = "sua_senha"
$env:JWT_SECRET = "sua_chave_base64"
$env:CLOUDINARY_URL = "cloudinary://sua_api_key:sua_api_secret@seu_cloud_name"
```

Gere uma chave JWT aleatoria com pelo menos 32 bytes usando PowerShell:

```powershell
[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
```

Inicie o banco configurado em `docker-compose.yaml`:

```powershell
docker compose up -d db
```

O PostgreSQL fica disponivel em `localhost:5555`; o banco e o usuario sao configurados por `POSTGRES_DB`, `DB_USER` e `DB_PASSWORD`. Ao iniciar a aplicacao, Flyway aplica as migrations e Hibernate valida o schema.

Inicie a API:

```powershell
.\mvnw.cmd spring-boot:run
```

A API fica disponivel em `http://localhost:8080`. Para parar o banco:

```powershell
docker compose down
```

O volume `pizzaria-data` preserva os dados entre reinicializacoes. Para apagar os dados, remova o volume explicitamente.

## Estrutura

O codigo principal fica em `src/main/java/br/com/pizzaria`:

- `controller`: endpoints HTTP
- `service`: regras de negocio
- `repository`: acesso a dados com Spring Data JPA
- `entity`: entidades persistidas
- `dtos`: contratos de entrada e resposta
- `mapper`: conversoes MapStruct entre DTOs e entidades
- `exception`: excecoes de negocio e respostas de erro
- `config`: seguranca, JWT, senha e Cloudinary

As migrations SQL ficam em `src/main/resources/db/migration`. O Hibernate usa `ddl-auto: validate`; alteracoes de schema devem ser feitas por novas migrations Flyway.

## Autenticacao e autorizacao

As rotas, exceto `POST /users` e `POST /session`, exigem um JWT valido. Envie o token no header:

```http
Authorization: Bearer <token>
```

O login retorna o token. O cadastro publico cria usuarios com papel `STAFF`. Escritas de categoria e produto exigem papel `ADMIN`; leituras exigem autenticacao. No estado atual, endpoints de pedidos exigem JWT valido, sem restricao adicional de papel.

## Endpoints

`{id}`, `{orderId}` e `{itemId}` sao UUIDs. Salvo indicacao em contrario, endpoints protegidos exigem Bearer token.

### Usuarios e sessao

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| `POST` | `/users` | Publico | Cadastra usuario com papel `STAFF` |
| `POST` | `/session` | Publico | Autentica e retorna JWT |
| `GET` | `/me` | Autenticado | Retorna o usuario do token |

Exemplo de cadastro:

```json
{
  "name": "Maria",
  "email": "maria@example.com",
  "password": "senha123"
}
```

Corpo do login em `POST /session`:

```json
{
  "email": "maria@example.com",
  "password": "senha123"
}
```

### Categorias

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| `POST` | `/categories` | ADMIN | Cria categoria |
| `GET` | `/categories` | Autenticado | Lista categorias |
| `GET` | `/categories/{id}` | Autenticado | Busca categoria |
| `PATCH` | `/categories/{id}` | ADMIN | Atualiza categoria |
| `DELETE` | `/categories/{id}` | ADMIN | Desativa categoria |

Corpo de criacao:

```json
{
  "name": "Pizzas",
  "description": "Pizzas salgadas e doces"
}
```

### Produtos

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| `POST` | `/products` | ADMIN | Cria produto com imagem |
| `GET` | `/products/{id}` | Autenticado | Busca produto |
| `GET` | `/products?page=0&size=10` | Autenticado | Lista produtos ativos, paginados |
| `GET` | `/products?categoryId={id}` | Autenticado | Filtra produtos ativos por categoria |
| `PATCH` | `/products/{id}` | ADMIN | Atualiza campos e/ou imagem |
| `DELETE` | `/products/{id}` | ADMIN | Desativa produto |
| `PUT` | `/products/{id}/reactivate` | ADMIN | Reativa produto |

Criacao e atualizacao usam `multipart/form-data`. Os campos do produto sao enviados individualmente e o arquivo de imagem usa o nome `file`. Na criacao, envie `name`, `description`, `price`, `categoryId` e `file`; na atualizacao, todos os campos sao opcionais, mas deve ser enviado ao menos um campo ou uma imagem. O limite configurado para arquivo e requisicao e 5 MB.

### Pedidos

| Metodo | Rota | Acesso | Descricao |
|---|---|---|---|
| `POST` | `/orders` | Autenticado | Cria pedido em rascunho |
| `GET` | `/orders` | Autenticado | Lista pedidos paginados com itens |
| `GET` | `/orders/{orderId}` | Autenticado | Busca pedido e itens |
| `POST` | `/orders/{orderId}/items` | Autenticado | Adiciona produto ao rascunho |
| `DELETE` | `/orders/{orderId}/items/{itemId}` | Autenticado | Remove item de rascunho |
| `PATCH` | `/orders/{orderId}/send` | Autenticado | Envia pedido com itens para preparo |
| `PATCH` | `/orders/{orderId}/finish` | Autenticado | Finaliza pedido enviado |
| `DELETE` | `/orders/{orderId}` | Autenticado | Exclui pedido somente enquanto em rascunho |

Criacao do pedido:

```json
{
  "table": 5,
  "name": "Maria"
}
```

Inclusao de item:

```json
{
  "productId": "UUID_DO_PRODUTO",
  "amount": 2
}
```

Os endpoints paginados aceitam os parametros Spring `page`, `size` e `sort`. A listagem de pedidos usa pagina de 10 itens por padrao, ordenada por `createdAt` decrescente. A paginacao e baseada em zero (`page=0` e a primeira pagina).

### Estados do pedido

- Ao criar, `draft=true` e `status=false`.
- Enquanto `draft=true`, e permitido adicionar/remover itens ou excluir o pedido.
- O envio exige ao menos um item e altera `draft` para `false`.
- A finalizacao exige que o pedido ja tenha sido enviado e altera `status` para `true`.
- Pedidos enviados ou finalizados nao podem ser excluidos pela rota de exclusao atual.

## Validacao e erros

Os DTOs validam os dados de entrada. Erros de negocio sao retornados como `ProblemDetail`, com campos como `status`, `title`, `detail` e `instance`. Exemplos:

- `400 Bad Request`: dados invalidos
- `401 Unauthorized`: token ausente ou invalido
- `403 Forbidden`: papel sem permissao para a operacao
- `404 Not Found`: recurso inexistente
- `409 Conflict`: conflito de estado, categoria/produto duplicado ou pedido vazio ao enviar

## Testes

Execute a suite automatizada com JDK 25:

```powershell
.\mvnw.cmd test
```

Os testes ficam em `src/test/java`. Para compilar sem executar os testes:

```powershell
.\mvnw.cmd -DskipTests compile
```
