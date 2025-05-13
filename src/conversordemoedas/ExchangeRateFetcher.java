package conversordemoedas;

/**
 *
 * @author Ary
 */
import com.google.gson.JsonArray;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import conversordemoedas.ExchangeRateResponseSimple.Moeda;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Classe para representar a resposta da API
class ExchangeRateResponseSimple {

    private final String result;
    private final String time_last_update_utc;
    private final String base_code;
    private final String target_code;
    private final Double conversion_rate;
    private final Double conversion_result;

    public ExchangeRateResponseSimple(String result, String time_last_update_utc, String base_code, String target_code, Double conversion_rate, Double conversion_result) {
        this.result = result;
        this.time_last_update_utc = time_last_update_utc;
        this.base_code = base_code;
        this.target_code = target_code;
        this.conversion_rate = conversion_rate;
        this.conversion_result = conversion_result;
    }

    public record Moeda(String id, String name) {

    }

    ;
    
    public String getResult() {
        return result;
    }

    public String getTime_last_update_utc() {
        return time_last_update_utc;
    }

    public String getBase_code() {
        return base_code;
    }

    public String getTarget_code() {
        return target_code;
    }

    public double getConversion_rate() {
        return conversion_rate;
    }

    public double getConversion_result() {
        return conversion_result;
    }
}

public class ExchangeRateFetcher {

    private static final int HTTP_COD_SUCESSO = 200;
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";
    private static final String apiKey = "b3f23e85d4f4d76186f943d3";

    public static ExchangeRateResponseSimple fetchRatesSimple(String base_code, String target_code, Double amount) throws Exception {
        String urlStrAmount = BASE_URL + apiKey + "/pair/" + base_code + "/" + target_code + "/" + amount;
        URL url = new URL(urlStrAmount);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();

        int status = request.getResponseCode();

        if (status != HTTP_COD_SUCESSO) {
            String errorMessage = request.getErrorStream().toString();
            throw new IOException("Erro HTTP " + status + ": " + errorMessage);
        }

        try (InputStreamReader reader = new InputStreamReader(request.getInputStream())) {
            JsonElement root = JsonParser.parseReader(reader);
            JsonObject jsonObj = root.getAsJsonObject();

            if (!jsonObj.has("result") || !"success".equalsIgnoreCase(jsonObj.get("result").getAsString())) {
                throw new IOException("Resposta inválida da API: " + jsonObj.toString());
            }

            String result = jsonObj.get("result").getAsString();
            Double conversion_rate = jsonObj.get("conversion_rate").getAsDouble();
            Double conversion_result = jsonObj.get("conversion_result").getAsDouble();
            String time_last_update_utc = jsonObj.get("time_last_update_utc").getAsString();

            return new ExchangeRateResponseSimple(result, time_last_update_utc, base_code, target_code, conversion_rate, conversion_result);
        } catch (Exception e) {
            System.out.println("ERRO " + e.toString());
            return null;
        }
    }

    public static List<Moeda> getCurrencyCodes() throws Exception {
        String urlString = BASE_URL + apiKey + "/codes";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int status = conn.getResponseCode();
        if (status != 200) {
            throw new RuntimeException("Erro na requisição: HTTP " + status);
        }

        StringBuilder jsonBuilder;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            jsonBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                jsonBuilder.append(line);
            }
        }
        conn.disconnect();

        String json = jsonBuilder.toString();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        if (!"success".equals(root.get("result").getAsString())) {
            throw new RuntimeException("Erro da API: " + root.get("error-type").getAsString());
        }

        JsonArray codes = root.getAsJsonArray("supported_codes");
        List<Moeda> result = new ArrayList<>();

        for (int i = 0; i < codes.size(); i++) {
            JsonArray codePair = codes.get(i).getAsJsonArray();
            String code = codePair.get(0).getAsString();
            String name = codePair.get(1).getAsString();
            Moeda moeda = new Moeda(code, name);
            result.add(moeda);
        }
        return result;
    }
}
