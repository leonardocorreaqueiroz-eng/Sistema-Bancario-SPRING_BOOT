# 🏦 Sistema Bancário — REST API

API REST de um sistema bancário desenvolvida com **Java 21 e Spring Boot 4**, simulando operações financeiras e aplicando conceitos de arquitetura em camadas, segurança, persistência de dados, testes automatizados, integração contínua e containerização.

O projeto foi desenvolvido com foco no aprendizado prático de desenvolvimento backend, buscando aplicar boas práticas utilizadas na construção de aplicações modernas.

## 🛠️ Tecnologias Utilizadas

| Tecnologia | Aplicação |
|---|---|
| Java 21 | Linguagem de programação |
| Spring Boot 4 | Desenvolvimento da API REST |
| Spring Data JPA / Hibernate | Persistência e mapeamento objeto-relacional |
| MySQL | Banco de dados relacional |
| Spring Security + JWT | Autenticação e autorização |
| Swagger / OpenAPI | Documentação da API |
| JUnit 5 / Mockito | Testes automatizados |
| Maven | Gerenciamento de dependências e build |
| Docker / Docker Compose | Containerização e orquestração |
| Nginx | Load Balancer |
| Git / GitHub | Versionamento e colaboração |
| GitHub Actions | Automação de CI/CD |
| GitHub Container Registry (GHCR) | Armazenamento de imagens Docker |

## 🏗️ Arquitetura e Infraestrutura

A aplicação utiliza uma arquitetura em camadas, separando responsabilidades entre Controllers, Services e Repositories.

Na infraestrutura, são executadas duas instâncias da API Spring Boot, utilizando o Nginx como Load Balancer e o MySQL como banco de dados compartilhado.

```text
                    ┌─────────────────┐
                    │     Cliente     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │      Nginx      │
                    │  Load Balancer  │
                    └────────┬────────┘
                             │
                    ┌────────┴────────┐
                    ▼                 ▼
             ┌─────────────┐   ┌─────────────┐
             │  API Java 1 │   │  API Java 2 │
             │ Spring Boot │   │ Spring Boot │
             └──────┬──────┘   └──────┬──────┘
                    │                 │
                    └────────┬────────┘
                             ▼
                    ┌─────────────────┐
                    │      MySQL      │
                    └─────────────────┘
```

### Load Balancing

São executadas duas instâncias da aplicação:

- `api-java1`
- `api-java2`

O Nginx recebe as requisições e distribui o tráfego entre as instâncias disponíveis.

A API possui um endpoint de diagnóstico:

```http
GET /api/instance
```

Esse endpoint identifica qual instância processou a requisição, permitindo verificar a distribuição do tráfego.

Exemplo de resposta:

```text
api-java2
api-java1
api-java2
api-java1
```

## 🎯 Principais Funcionalidades e Regras de Negócio

- **Abertura de contas:** ao cadastrar um cliente, o sistema gera automaticamente uma Conta Corrente e uma Conta Investimento vinculadas.
- **Autenticação:** autenticação stateless utilizando JWT e Spring Security.
- **Transações financeiras:** suporte a transferências via PIX, TEF, TED e DOC.
- **Limite de DOC:** operações via DOC possuem limite de R$ 4.999,00.
- **Movimentações:** depósitos e saques com aplicação das respectivas regras de negócio.
- **Investimentos:** permite aportes e resgates entre Conta Corrente e Conta Investimento.
- **Rendimentos:** valores investidos possuem controle individual e cálculo baseado em datas.
- **Extratos:** geração de histórico consolidado das movimentações financeiras.

## 🔐 Segurança

A API utiliza Spring Security com autenticação baseada em JWT.

Principais recursos:

- Autenticação stateless;
- Senhas protegidas com hashing;
- Filtro para validação dos tokens JWT;
- Controle de acesso aos endpoints;
- Validação de dados de entrada;
- Proteção contra acesso não autenticado.

## 🧪 Testes Automatizados

O projeto possui **111 testes automatizados**, abrangendo diferentes componentes e fluxos da aplicação.

A suíte contempla:

- Testes unitários;
- Testes de integração;
- Testes de Controllers;
- Testes de Services;
- Testes de Repositories;
- Testes de segurança;
- Testes End-to-End (E2E).

Os testes buscam validar tanto componentes isolados quanto fluxos completos da aplicação, garantindo maior confiabilidade nas operações implementadas.

## ⚠️ Tratamento de Exceções

A aplicação possui tratamento centralizado de exceções utilizando:

- `@ExceptionHandler`;
- `ResponseEntity`;
- Exceções de negócio customizadas;
- Respostas padronizadas de erro;
- Códigos HTTP adequados para cada situação.

Exemplo de resposta:

```json
{
  "status": 400,
  "message": "Saldo insuficiente"
}
```

## 📊 Rastreabilidade e Logs

A aplicação utiliza logs em pontos relevantes do fluxo de execução, incluindo:

- Autenticação;
- Operações financeiras;
- Falhas de negócio;
- Processamento de transações;
- Diagnóstico da aplicação.

Esses registros auxiliam na depuração e no acompanhamento do comportamento da aplicação.

## 🔄 Integração e Entrega Contínua (CI/CD)

O projeto utiliza **GitHub Actions** para automatizar a integração contínua, a construção da imagem Docker e sua publicação no GitHub Container Registry (GHCR).

### Fluxo do pipeline

```text
       Push / Pull Request
                │
                ▼
        ┌───────────────┐
        │ GitHub Actions│
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │ Maven Verify  │
        │ Testes e Build│
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │ Upload do JAR │
        └───────┬───────┘
                │
                ▼
        ┌───────────────┐
        │ Docker Build  │
        └───────┬───────┘
                │
                ▼
        ┌─────────────────────┐
        │ Push na master?     │
        └───────┬─────────────┘
                │
                ▼
        ┌───────────────┐
        │ Publicação no │
        │      GHCR     │
        └───────────────┘
```

### 1. Integração Contínua (CI)

Em Pull Requests direcionados à branch `master` e em pushes para essa branch, o GitHub Actions executa:

- Checkout do código;
- Configuração do Java 21;
- Configuração do cache do Maven;
- Execução de `mvn clean verify`;
- Execução dos testes automatizados;
- Armazenamento do artefato JAR;
- Construção da imagem Docker.

**Gerenciamento de segredos**

A variável `JWT_SECRET`, necessária durante a execução dos testes, é armazenada nos **GitHub Secrets** e disponibilizada ao workflow por meio de variáveis de ambiente.

Dessa forma, informações sensíveis não precisam ser armazenadas diretamente no código-fonte ou no arquivo de configuração do workflow.

### 2. Publicação da imagem Docker

Após um push na branch `master`, o pipeline:

- Realiza a autenticação no GitHub Container Registry utilizando o `GITHUB_TOKEN`;
- Publica a imagem Docker construída utilizando o `api.dockerfile`;
- Utiliza a tag `latest`.

Em Pull Requests, a imagem é construída para validação, mas não é publicada no registry.

**Imagem publicada:**

```text
ghcr.io/leonardocorreaqueiroz-eng/sistema-bancario:latest
```

Essa abordagem automatiza a geração e a publicação da imagem da aplicação, permitindo que o ambiente Docker utilize a versão disponibilizada no registry.

## 📖 Documentação da API

A API possui documentação interativa utilizando Swagger / OpenAPI.

Com a aplicação em execução, a documentação pode ser acessada através de:

```text
http://localhost:8080/swagger-ui/index.html
```

## 🚀 Objetivos do Projeto

Este projeto foi desenvolvido com foco em praticar conceitos utilizados no desenvolvimento de APIs backend modernas, incluindo:

- Arquitetura em camadas;
- Orientação a objetos;
- Desenvolvimento de REST APIs;
- Spring Security e JWT;
- JPA/Hibernate;
- Modelagem de dados;
- Testes automatizados;
- Tratamento de exceções;
- Docker e Docker Compose;
- Nginx e Load Balancing;
- Integração e entrega contínua;
- Documentação com OpenAPI;
- Boas práticas de desenvolvimento.

## 🐳 Execução com Docker

O projeto possui uma infraestrutura baseada em Docker Compose, composta por:

- MySQL;
- Duas instâncias da API Spring Boot;
- Nginx como Load Balancer;
- Rede Docker compartilhada entre os serviços;
- Volume persistente para o MySQL;
- Healthcheck do banco de dados;
- Variáveis de ambiente para configurações sensíveis.

### Pré-requisitos

Para executar o projeto, é necessário ter instalado:

- Git;
- Docker;
- Docker Compose.

### 1. Clone o repositório

```bash
git clone https://github.com/leonardocorreaqueiroz-eng/Sistema-Bancario-SPRING_BOOT.git

cd Sistema-Bancario-SPRING_BOOT
```

### 2. Configure as variáveis de ambiente

Crie um arquivo `.env` na raiz do projeto com base no arquivo `.env.example`:

```bash
cp .env.example .env
```

Preencha as variáveis do arquivo `.env` com os valores desejados.

Exemplo da estrutura:

```env
# Spring Boot
DB_HOST=
DB_NAME=
DB_USER=
DB_PASSWORD=
JWT_SECRET=

# MySQL
MYSQL_ROOT_PASSWORD=
MYSQL_DATABASE=
MYSQL_USER=
MYSQL_PASSWORD=
```

O arquivo `.env` contém configurações sensíveis e não deve ser versionado no repositório.

Quando executada pelo Docker Compose, a aplicação utiliza `mysql` como `DB_HOST`, pois esse é o nome do serviço MySQL na rede Docker.

### 3. Baixe a imagem e inicie os serviços

```bash
docker compose pull

docker compose up -d
```

O Docker Compose iniciará:

- MySQL;
- `api-java1`;
- `api-java2`;
- Nginx.

### 4. Acesse a API

A aplicação estará disponível através do Nginx:

```text
http://localhost:8080
```

### 5. Acesse o Swagger / OpenAPI

```text
http://localhost:8080/swagger-ui/index.html
```

### 6. Verifique o Load Balancing

O endpoint:

```http
GET /api/instance
```

permite identificar qual instância processou a requisição.

Para realizar múltiplas requisições:

```bash
for i in {1..10}; do
    curl -s http://localhost:8080/api/instance
    echo
done
```

Um possível resultado:

```text
api-java1
api-java2
api-java1
api-java2
api-java2
api-java1
```

Isso permite visualizar a distribuição das requisições entre as instâncias da API.

### 7. Atualizar a aplicação

Para baixar a imagem mais recente publicada no GHCR e atualizar as instâncias:

```bash
docker compose pull

docker compose up -d
```

### 8. Encerrar os containers

Para parar os serviços:

```bash
docker compose down
```

Para remover também o volume persistente do MySQL:

```bash
docker compose down -v
```

**Atenção:** o comando `docker compose down -v` remove os volumes associados ao projeto e, consequentemente, os dados persistidos do banco de dados.

---

## 📌 Evolução do Projeto

O projeto está em evolução contínua, com implementação gradual de recursos relacionados a backend, segurança, qualidade de código, testes automatizados, containerização e automação de processos.

Desenvolvido como projeto de estudo para aprofundar conhecimentos em Java, Spring Boot e desenvolvimento backend.
