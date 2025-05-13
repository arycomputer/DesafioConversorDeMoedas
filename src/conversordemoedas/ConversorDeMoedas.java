package conversordemoedas;

import static conversordemoedas.ExchangeRateFetcher.fetchRatesSimple;
import conversordemoedas.ExchangeRateResponseSimple.Moeda;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Ary
 */
public class ConversorDeMoedas {

    static List<Moeda> moedas = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);
    static Integer opcao;
    static String fromCurrency;
    static String toCurrency;
    static double amount;
    static boolean entradaValida = false;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        carregaMoedas();
        do {
            menu();
            switch (opcao) {
                case 1 ->
                    imprimeMoedas();
                case 2 ->
                    converterMoeda();
                case 3 -> {
                    System.out.println("\nPrograma encerrado.");
                    scanner.close();
                }
            }
        } while (opcao != 3);
    }

    public static void menu() {
        System.out.println("\n=== Conversor de Moedas ===");
        System.out.println("1-Ver os codigos aceitos");
        System.out.println("2-Converter moeda");
        System.out.println("3-Sair");
        opcao = scanner.nextInt();
    }

    public static void carregaMoedas() {
        try {
            moedas = ExchangeRateFetcher.getCurrencyCodes();
        } catch (Exception e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    public static void imprimeMoedas() {
        for (Moeda moeda : moedas) {
            System.out.println(moeda.id() + " - " + moeda.name());
        }
    }

    public static Boolean hasMoeda(String moedaId) {
        Boolean contem = false;
        for (Moeda moeda : moedas) {
            if (moeda.id().equals(moedaId)) {
                contem = true;
            }
        }
        return contem;
    }

    public static void converterMoeda() {
        Boolean moedaValida = false;

        while (!moedaValida) {
            System.out.print("\nDigite a moeda de origem(ex: USD, EUR, BRL) : ");
            fromCurrency = scanner.nextLine().trim().toUpperCase();
            if (hasMoeda(fromCurrency)) {
                moedaValida = true;
            } else {
                System.out.println("Moeda inválida. Tente novamente.");
            }
        }

        moedaValida = false;
        while (!moedaValida) {
            System.out.print("Moeda de destino (ex: EUR): ");
            toCurrency = scanner.nextLine().trim().toUpperCase();
            if (hasMoeda(toCurrency)) {
                moedaValida = true;
            } else {
                System.out.println("Moeda inválida. Tente novamente.");
            }
        }

        while (!entradaValida) {
            System.out.print("Digite um número maior que zero: ");
            try {
                amount = Double.parseDouble(scanner.nextLine());

                if (amount > 0) {
                    entradaValida = true;
                } else {
                    System.out.println("O número deve ser maior que zero. Tente novamente.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Digite um número decimal válido.");
            }
        }

        try {
            ExchangeRateResponseSimple response = fetchRatesSimple(fromCurrency, toCurrency, amount);
            if ("success".equalsIgnoreCase(response.getResult())) {
                double rate = response.getConversion_rate();
                double converted = response.getConversion_result();
                String last_update = response.getTime_last_update_utc();
                System.out.println("\nUltima atualização " + new Date(last_update).toLocaleString());
                System.out.printf("Taxa de conversão de %s para %s: %.4f%n", fromCurrency, toCurrency, rate);
                System.out.printf("Valor convertido: %.2f %s -> %.2f %s%n", amount, fromCurrency, converted, toCurrency);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.toString());
            System.out.println("Moeda de destino inválida. Verifique o código da moeda.");
        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("Erro de conexão ou API. Tente novamente mais tarde.");
        } catch (Exception e) {
            System.out.println(e.toString());
            System.out.println("Ocorreu um erro inesperado.");
        }
    }
}
