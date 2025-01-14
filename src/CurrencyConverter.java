package Currency.src;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class CurrencyConverter {
    private static final String API_URL = "https://v6.exchangerate-api.com/v6/401283e3b4b0021c91c90edd/latest/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Bienvenido al convertidor de monedas.");
            System.out.print("Ingrese la moneda base (por ejemplo, USD): ");
            String baseCurrency = scanner.nextLine().toUpperCase();

            System.out.print("Ingrese la moneda a la que desea convertir (por ejemplo, EUR): ");
            String targetCurrency = scanner.nextLine().toUpperCase();

            System.out.print("Ingrese la cantidad que desea convertir: ");
            double amount = scanner.nextDouble();

            double convertedAmount = convertCurrency(baseCurrency, targetCurrency, amount);
            System.out.printf("%.2f %s = %.2f %s%n",
                    amount, baseCurrency, convertedAmount, targetCurrency);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static double convertCurrency(String baseCurrency, String targetCurrency, double amount)
            throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        String url = API_URL + baseCurrency;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error en la API. Código de estado: " + response.statusCode());
        }

        String responseBody = response.body();

        // Buscar la sección de conversion_rates
        String ratesStart = "\"conversion_rates\":{";
        int startIndex = responseBody.indexOf(ratesStart);
        if (startIndex == -1) {
            throw new IOException("Formato de respuesta inválido");
        }

        // Buscar la tasa de cambio específica
        String rateKey = "\"" + targetCurrency + "\":";
        startIndex = responseBody.indexOf(rateKey, startIndex);
        if (startIndex == -1) {
            throw new IOException("Moneda destino no encontrada: " + targetCurrency);
        }

        // Extraer el valor de la tasa
        startIndex += rateKey.length();
        int endIndex = responseBody.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = responseBody.indexOf("}", startIndex);
        }

        if (endIndex == -1) {
            throw new IOException("Error al parsear la respuesta");
        }

        String rateStr = responseBody.substring(startIndex, endIndex).trim();
        try {
            double rate = Double.parseDouble(rateStr);
            return amount * rate;
        } catch (NumberFormatException e) {
            throw new IOException("Tasa de cambio inválida: " + rateStr);
        }
    }
}