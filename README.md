# 🏦 Sistema Bancário Completo (API Rest)

Esta é uma API robusta de um sistema bancário desenvolvida para simular operações financeiras complexas do mundo real, focando em segurança, rastreabilidade, arquitetura limpa e alta cobertura de testes.

## 🛠️ Tecnologias Utilizadas
* **Linguagem:** Java 21
* **Framework Principal:** Spring Boot 4
* **Persistência & Banco de Dados:** Spring Data JPA / Hibernate / MySQL
* **Segurança:** Spring Security com autenticação Stateless via JWT (JSON Web Tokens)
* **Documentação:** Swagger / OpenAPI
* **Qualidade:** Testes Automatizados (JUnit / Mockito)

## 🎯 Principais Funcionalidades & Regras de Negócio
* **Abertura de Contas:** Ao cadastrar um cliente, o sistema gera automaticamente uma Conta Corrente e uma Conta Investimento vinculadas.
* **Transações Financeiras:** Suporte a transferências via PIX, TEF, TED e DOC (com limite de segurança de R$ 4.999,00 via DOC).
* **Movimentações Básicas:** Depósitos padronizados e saques com aplicação de taxas administrativas.
* **Módulo de Investimento Avançado:** Permite o resgate e o aporte de valores saindo da Conta Corrente. Cada valor depositado rende de forma isolada do saldo geral com base em datas.
* **Herança e Extratos:** Entidade de Rendimentos que herda propriedades de uma classe mãe de Movimentações, permitindo gerar extratos consolidados completos.

## 🛡️ Boas Práticas & Engenharia de Software
* **Rastreabilidade com Loggers:** Implementação de Logs estruturados em pontos-chave da aplicação (como transações, falhas e autenticações) para auditoria, monitoramento em tempo real e facilidade de depuração (*debugging*).
* **Qualidade de Software Elevada:** Cobertura rigorosa contendo **111 testes automatizados**, divididos estrategicamente entre testes unitários, testes de integração e testes de ponta a ponta (E2E).
* **Tratamento de Erros Customizado:** Uso preciso de rotinas com `@ExceptionHandler` e `ResponseEntity` para capturar exceções de negócio e retornar respostas limpas, estruturadas e com os códigos de status HTTP corretos para o cliente.