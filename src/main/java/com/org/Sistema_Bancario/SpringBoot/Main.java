//package com.org.Sistema_Bancario.SpringBoot;
//
//import jakarta.persistence.PersistenceException;
//import main.exceptions.AplicacaoException;
//import main.exceptions.AplicacaoNaoEncontradaException;
//import main.exceptions.ContaException;
//import main.exceptions.CpfInvalidoException;
//import main.exceptions.MovimentacaoException;
//import main.exceptions.NomeInvalidoException;
//import main.exceptions.OperacaoNaoPermitidaException;
//import main.exceptions.ValorInvalidoException;
//import main.movimentacoes.Deposito;
//import main.movimentacoes.Saque;
//import modelos.*;
//import servicos.AplicacaoDeRendimentos;
//import servicos.BancoService;
//import servicos.TipoMovimentacao;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.Scanner;
//
//import static main.repository.ClienteRepository.buscarPorCpf;
//import static main.repository.ContaRepository.aplicarDeposito;
//import static main.repository.ContaRepository.aplicarSaque;
//import static main.repository.ContaRepository.buscarConta;
//import static main.repository.ContaRepository.cadastrarConta;
//import static main.repository.ExtratoRepository.verExtratos;
//import static util.Validar.validarCpf;
//import static util.Validar.validarNome;
//
//
//public class Main {
//
//    public static void main(String[] args) {
//
//        try (Scanner scanner = new Scanner(System.in)) {
//            BancoService banco = new BancoService();
//            boolean rodando = true;
//            while (rodando) {
//                int op = lerInteiro(scanner, """
//                        ===== MENU =====
//                        1 - Criar Conta
//                        2 - Entrar
//                        3 - Sair
//                        """);
//                switch (op) {
//
//                    case 1 -> realizarCadastroDaConta(scanner, banco);
//                    case 2 -> menuPrincipal(scanner, banco);
//                    case 3 -> {
//                        rodando = false;
//                        System.out.println("Encerrando sistema...");
//                    }
//                    default -> System.out.println("Opção inválida!");
//                }
//            }
//        }
//    }
//    private static void menuPrincipal(Scanner scanner, BancoService banco) {
//        String CPF;
//        int numeroContaInvestimento;
//        int numeroContaCorrente;
//        try {
//            CPF = lerString(scanner,"Digite seu CPF: ");
//            validarCpf(CPF);
//            Conta contaCorrente =
//                    buscarPorCpf(CPF,TipoConta.CORRENTE)
//                            .orElseThrow(ContaException::new);
//
//            Conta contaInvestimento =
//                    buscarPorCpf(CPF,TipoConta.INVESTIMENTO)
//                            .orElseThrow(ContaException::new);
//
//            numeroContaCorrente = contaCorrente.getNumero();
//            numeroContaInvestimento = contaInvestimento.getNumero();
//        } catch (ContaException | CpfInvalidoException e) {
//            System.out.println(e.getMessage());
//            return;
//        }
//
//        boolean acesso = true;
//        while (acesso) {
//
//            int input = lerInteiro(scanner,"""
//            0 - Investimento
//            1 - Depositar
//            2 - Sacar
//            3 - Consultar saldo
//            4 - Transferir
//            5 - Listar contas
//            6 - Ver Extrato
//            7 - Sair da Conta
//            """);
//            switch (input) {
//
//                case 0 -> menuDeInvestimento(scanner, banco, numeroContaCorrente, numeroContaInvestimento);
//                case 1, 2 -> realizarMovimentacao(scanner, numeroContaCorrente, banco, input);
//                case 3 -> consultarSaldo(numeroContaCorrente);
//                case 4 -> realizarTransferencia(scanner, banco, numeroContaCorrente);
//                case 5 -> listarContas(banco, CPF);
//                case 6 -> verExtrato(CPF);
//                case 7 -> acesso = false;
//                default -> System.out.println("Opção inválida.");
//            }
//        }
//    }
//
//    private static void menuDeInvestimento(Scanner scanner, BancoService banco, int numeroContaCorrente, int numeroContaInvestimento) {
//        boolean acesInvest = true;
//        while (acesInvest) {
//
//            int inputInvest = lerInteiro(scanner,"""
//            1 - Depositar
//            2 - Sacar
//            3 - Consultar saldo
//            4 - Voltar
//            """);
//            switch (inputInvest) {
//                case 1 -> depositarNaContaInvestimento(scanner, banco, numeroContaCorrente, numeroContaInvestimento);
//                case 2 -> resgatarInvestimento(scanner, banco, numeroContaInvestimento, numeroContaCorrente);
//                case 3 -> consultarInvestimento(numeroContaInvestimento);
//                case 4 -> acesInvest = false;
//                default -> System.out.println("Opção inválida.");
//            }
//        }
//    }
//
//    private static void realizarCadastroDaConta(Scanner scanner, BancoService banco) {
//        try {
//            String nome = lerString(scanner,"Nome: ");
//            validarNome(nome);
//            String cpf = lerString(scanner,"CPF: ");
//            validarCpf(cpf);
//            banco.cpfExiste(cpf);
//            TipoConta tipo = TipoConta.CORRENTE;
//            TipoConta tipo2 = TipoConta.INVESTIMENTO;
//            Cliente cliente = new Cliente(nome, cpf, LocalDate.now());
//            Conta contaCorrente = banco.criarConta(cliente, tipo);
//            Conta contaInvestimento = banco.criarConta(cliente, tipo2);
//            cadastrarConta(cliente, contaCorrente, contaInvestimento);
//
//            System.out.println("Conta criada com sucesso!");
//            System.out.println("Número da conta corrente: " + contaCorrente.getNumero());
//            System.out.println("Número da conta de investimento: " + contaInvestimento.getNumero() + "\n");
//        } catch (PersistenceException | CpfInvalidoException | NomeInvalidoException | ContaException ex) {
//            System.out.println("Erro ao cadastrar conta - " + ex.getMessage());
//        }
//    }
//
//    private static void depositarNaContaInvestimento(Scanner scanner, BancoService banco, int numeroContaCorrente, int numeroContaInvestimento) {
//        BigDecimal valor =
//                lerBigDecimal(scanner, "Digite o valor que deseja depositar:");
//        banco.transferir(
//                numeroContaCorrente,
//                numeroContaInvestimento,
//                valor,
//                TipoMovimentacao.APLICACAO);
//        System.out.println("Depósito realizado!");
//    }
//
//    private static void resgatarInvestimento(Scanner scanner, BancoService banco, int numeroContaInvestimento, int numeroContaCorrente) {
//        BigDecimal valor =
//                lerBigDecimal(scanner,"Digite o valor que deseja resgatar:");
//        banco.transferir(
//                numeroContaInvestimento,
//                numeroContaCorrente,
//                valor,
//                TipoMovimentacao.RESGATE);
//        System.out.println("Resgate realizado!");
//    }
//
//    private static void consultarInvestimento(int numeroContaInvestimento) {
//        try {
//            Conta conta = buscarConta(numeroContaInvestimento);
//            AplicacaoDeRendimentos.aplicarRendimentos(conta);
//            System.out.println("R$: " + conta.getSaldo());
//        } catch (AplicacaoException | PersistenceException e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private static void verExtrato(String CPF) {
//        try {
//            verExtratos(CPF).forEach(System.out::println);
//        } catch (AplicacaoNaoEncontradaException e){
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private static void listarContas(BancoService banco, String CPF) {
//        var contas = banco.listarContas(CPF);
//
//        if (contas.isEmpty()) {
//            System.out.println("Nenhuma conta cadastrada.");
//            return;
//        }
//
//        for (Conta c : contas) {
//            System.out.printf(
//                    "Conta: %d | Saldo: R$ %.2f | Tipo: %s\n",
//                    c.getNumero(),
//                    c.getSaldo(),
//                    c.getTipoConta()
//            );
//        }
//    }
//
//    private static void realizarMovimentacao(Scanner scanner, int numeroContaCorrente, BancoService banco, int opcao) {
//        try {
//            BigDecimal valor =
//                    lerBigDecimal(scanner,"Digite o valor que deseja sacar:");
//            Conta conta = buscarConta(numeroContaCorrente);
//            if (opcao == 1) {
//                Deposito deposito =  new Deposito(conta,valor,
//                        banco.getTime().getData(), banco.getTime().getHora());
//                aplicarDeposito(conta,valor,deposito);
//                System.out.println("Depósito realizado!");
//                return;
//            }
//            if (opcao == 2) {
//                Saque saque =  new Saque(conta,valor,
//                        banco.getTime().getData(), banco.getTime().getHora());
//                aplicarSaque(conta,valor,saque);
//                System.out.println("Saque realizado!");
//            }
//        } catch (PersistenceException | ValorInvalidoException | ContaException ex) {
//            System.out.println("Erro ao realizar a transação: " + ex.getMessage());
//        }
//    }
//
//    private static void realizarTransferencia(Scanner scanner, BancoService banco, int numeroContaCorrente) {
//        int destino = lerInteiro(scanner,"Conta destino: ");
//        BigDecimal valor = lerBigDecimal(scanner,"Valor: ");
//        TipoMovimentacao tipo = lerMovimentacao(scanner,"""
//        ====Selecione a forma de transferencia====
//        1 - PIX
//        2 - TED
//        3 - TEF
//        4 - DOC
//        """);
//        try {
//            if (buscarConta(destino).getTipoConta() == TipoConta.INVESTIMENTO){
//                throw new OperacaoNaoPermitidaException("Não é possível realizar transferencia para uma conta investimento");
//            }
//            banco.transferir(numeroContaCorrente, destino, valor, tipo);
//            System.out.println("Transferência realizada!");
//        } catch (ContaException | MovimentacaoException
//                 | ValorInvalidoException e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    private static void consultarSaldo(int numeroContaCorrente) {
//        try {
//            Conta conta = buscarConta(numeroContaCorrente);
//            System.out.printf("Saldo: R$ %.2f\n", conta.getSaldo());
//        } catch (ContaException e) {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    public static int lerInteiro(Scanner scanner, String message) {
//        while (true) {
//            try {
//                System.out.println(message);
//                return Integer.parseInt(scanner.nextLine());
//            } catch (NumberFormatException e) {
//                System.out.println("Digite um número inteiro válido.");
//            }
//        }
//    }
//    public static String lerString(Scanner scanner, String message) {
//        while (true) {
//            System.out.println(message);
//            String valor = scanner.nextLine().trim();
//            if (!valor.isEmpty()) {
//                return valor;
//            }
//            System.out.println("Campo obrigatório");
//        }
//    }
//    public static BigDecimal lerBigDecimal(Scanner scanner, String message) {
//        while (true) {
//            try {
//                System.out.println(message);
//                BigDecimal valor = new BigDecimal(scanner.nextLine());
//                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
//                    throw new ValorInvalidoException(valor);
//                }
//                return valor;
//            } catch (ValorInvalidoException e) {
//                System.out.println(e.getMessage());
//            } catch (NumberFormatException e) {
//                System.out.println("Digite um valor numérico válido.");
//            }
//        }
//    }
//    public static TipoMovimentacao lerMovimentacao(Scanner scanner, String message) {
//        while (true) {
//            try {
//                System.out.println(message);
//                return TipoMovimentacao.values()[Integer.parseInt(scanner.nextLine())-1];
//            } catch (ArrayIndexOutOfBoundsException e) {
//                System.out.println("Opção inválida!");
//            } catch (NumberFormatException e){
//                System.out.println("Digite um número.");
//            }
//        }
//    }
//
//}