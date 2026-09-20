# 🏦 Sistema Bancário Completo — REST API

API REST de um sistema bancário desenvolvida com **Java 21 e Spring Boot 4**, simulando operações financeiras e aplicando conceitos de segurança, arquitetura em camadas, persistência, testes automatizados, containerização e balanceamento de carga.

## 🛠️ Tecnologias Utilizadas

* **Java 21**
* **Spring Boot 4**
* **Spring Data JPA / Hibernate**
* **MySQL**
* **Spring Security + JWT**
* **Swagger / OpenAPI**
* **JUnit 5 / Mockito**
* **Docker / Docker Compose**
* **Nginx**
* **Git**

## 🏗️ Arquitetura e Infraestrutura

A aplicação pode ser executada com múltiplas instâncias da API atrás de um **Nginx atuando como Load Balancer**.

```text
                    ┌─────────────────┐
                    │     Cliente     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │      Nginx      │
                    │ Load Balancer   │
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

* `api-java1`
* `api-java2`

O Nginx recebe as requisições e distribui o tráfego entre as instâncias.

A API possui um endpoint de diagnóstico:

```http
GET /api/instance
```

que identifica qual instância processou a requisição, permitindo verificar o funcionamento do balanceamento.

Exemplo:

```text
api-java2
api-java1
api-java2
api-java1
```

## 🎯 Principais Funcionalidades e Regras de Negócio

* **Abertura de Contas:** ao cadastrar um cliente, o sistema gera automaticamente uma Conta Corrente e uma Conta Investimento vinculadas.
* **Autenticação:** autenticação stateless utilizando JWT e Spring Security.
* **Transações Financeiras:** suporte a transferências via PIX, TEF, TED e DOC.
* **Limite de DOC:** operações via DOC possuem limite de R$ 4.999,00.
* **Movimentações:** depósitos e saques com aplicação das regras de negócio correspondentes.
* **Investimentos:** permite aportes e resgates entre Conta Corrente e Conta Investimento.
* **Rendimentos:** valores investidos possuem controle individual e cálculo baseado em datas.
* **Extratos:** geração de histórico consolidado das movimentações financeiras.

## 🔐 Segurança

A API utiliza **Spring Security** com autenticação baseada em JWT.

Principais recursos:

* Autenticação stateless;
* Senhas protegidas com hashing;
* Filtro para validação dos tokens JWT;
* Controle de acesso aos endpoints;
* Validação de dados de entrada;
* Proteção contra acesso não autenticado.

## 🧪 Testes Automatizados

O projeto possui **111 testes automatizados**, distribuídos entre diferentes níveis:

* Testes unitários;
* Testes de integração;
* Testes de controllers;
* Testes de services;
* Testes de repositories;
* Testes de segurança;
* Testes End-to-End (E2E).

A divisão dos testes busca validar tanto componentes isolados quanto fluxos completos da aplicação.

## ⚠️ Tratamento de Exceções

A aplicação possui tratamento centralizado de exceções utilizando:

* `@ExceptionHandler`;
* `ResponseEntity`;
* Exceções de negócio customizadas;
* Respostas padronizadas de erro;
* Códigos HTTP adequados para cada situação.

Exemplo de resposta:

```json
{
  "status": 400,
  "message": "Saldo insuficiente"
}
```

## 📊 Rastreabilidade e Logs

A aplicação utiliza logs em pontos relevantes do fluxo de execução, incluindo:

* Autenticação;
* Operações financeiras;
* Falhas de negócio;
* Processamento de transações;
* Diagnóstico da aplicação.

Esses registros auxiliam na depuração e no acompanhamento do comportamento da aplicação.

## 📖 Documentação da API

A API possui documentação interativa utilizando **Swagger / OpenAPI**.

Com a aplicação em execução, a documentação pode ser acessada através do Swagger UI.

## 🐳 Execução com Docker

O projeto possui configuração para execução utilizando **Docker Compose**.

A infraestrutura inclui:

* MySQL;
* Duas instâncias da API Spring Boot;
* Nginx como Load Balancer;
* Rede Docker compartilhada entre os serviços;
* Volume persistente para o MySQL;
* Healthcheck do banco de dados;
* Variáveis de ambiente para configuração sensível.

Para iniciar os serviços:

```bash
docker compose up --build
```

A API pode ser acessada através do Nginx:

```text
http://localhost:8080
```

> As credenciais e configurações sensíveis são mantidas em variáveis de ambiente e não devem ser versionadas no repositório.

## 📌 Objetivos do Projeto

Este projeto foi desenvolvido com foco em praticar conceitos utilizados no desenvolvimento de APIs backend modernas, incluindo:

* Arquitetura em camadas;
* Orientação a objetos;
* REST APIs;
* Spring Security;
* JWT;
* JPA/Hibernate;
* Modelagem de dados;
* Testes automatizados;
* Tratamento de exceções;
* Docker;
* Docker Compose;
* Nginx;
* Load Balancing;
* Documentação com OpenAPI;
* Boas práticas de desenvolvimento.

---

### 🚀 Evolução do Projeto

O projeto está em evolução contínua, com implementação gradual de recursos relacionados a **backend, segurança, testes, containerização e infraestrutura**.
