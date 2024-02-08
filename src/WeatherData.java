import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import org.json.simple.parser.ParseException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/** 
 * Responsible for API calls and data that is actually shown in the app
 * API: https://open-meteo.com/
*/
public class WeatherData {
    final static String GEOCODING_ROOT = "https://geocoding-api.open-meteo.com/v1/search?name=";
    final static String GEO_SUFFIX = "&count=10&language=en&format=json";
    final static String WEATHER_ROOT = "https://api.open-meteo.com/v1/forecast?";
    final static String WEATHER_SUFFIX = "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m&timezone=America%2FNew_York";

    // Suppresses warning for type safety in weatherData.put()
    @SuppressWarnings("unchecked")
    public static JSONObject getWeatherData(String name) {
        // Gets location coordinates from a geolocation API
        JSONArray locationData = getLocationData(name);

        // Extract coordinates from data to get weather at location
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude"), longitude = (double) location.get("longitude");

        String url = WEATHER_ROOT + "latitude=" + latitude + "&longitude=" + longitude + WEATHER_SUFFIX;

        // Attempt to fetch weather data
        try {
            HttpURLConnection connection = fetchResponse(url);
            String result = getResults(connection);
            
            JSONObject hourlyData = parseDataObject(result, "hourly");

            // Get current time to get accurate data
            JSONArray time = (JSONArray) hourlyData.get("time");
            int currentTimeIndex = getTimeIndex(time);

            // Objects to get data from
            JSONArray temperatureJSON = (JSONArray) hourlyData.get("temperature_2m");
            JSONArray humidityJSON = (JSONArray) hourlyData.get("relativehumidity_2m");
            JSONArray weathercodeJSON = (JSONArray) hourlyData.get("weathercode");
            JSONArray windSpeedJSON = (JSONArray) hourlyData.get("windspeed_10m");
            
            // Extract data
            double temperature = (double) temperatureJSON.get(currentTimeIndex);
            long humidity = (long) humidityJSON.get(currentTimeIndex);
            String condition = convertWeathercode((long) weathercodeJSON.get(currentTimeIndex));
            double windSpeed = (double) windSpeedJSON.get(currentTimeIndex);

            // Encode the data into JSON format
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("humidity", humidity);
            weatherData.put("condition", condition);
            weatherData.put("windspeed", windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to get weather data from: \"" + url + "\".");
        }
        
        return null;
    }

    private static int getTimeIndex(JSONArray time) {
        String currentTime = getCurrentTime();

        // Compare current time to find index
        for (int i = 0; i < time.size(); i++) {
            if (currentTime.equals((String) time.get(i))) {
                return i;
            }
        }
        // If it fails to find a time default to index 0
        return 0;
    }

    private static String getCurrentTime() {
        LocalDateTime current = LocalDateTime.now();

        // Adjust format to match: YYYY-MM-ddTHH:mm
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':'mm");
        return current.format(format);
    }

    private static String convertWeathercode(long code) {
        if (code == 0) {
            // Technically clear but thats not important
            return "sunny";
        } else if (1 <= code && code <= 3) {
            return "cloudy";
        } else if ((51 <= code && code <= 67) || (80 <= code && code <= 99)) {
            return "rainy";
        } else if (71 <= code && code <= 77) {
            return "snowy";
        }

        return "unknown";
    }

    // Gets coordinates for given location, used to get weather at the location
    private static JSONArray getLocationData(String location) {
        // Preprocess input to match API's format (replace whitespaces with +)
        location = location.replaceAll("\\s", "+");

        String url = GEOCODING_ROOT + location + GEO_SUFFIX;

        // Attempts to fetch data
        try {
            // API call, if successful it will store the fetched data in `result`
            HttpURLConnection connection = fetchResponse(url);
            String result = getResults(connection);
            
            // Parses data to return
            JSONArray locationData = parseDataArray(result, "results");

            return locationData;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to fetch data from: " + url + ".");
        }

        return null;
    }

    // Process result of API call
    private static String getResults(HttpURLConnection connection) throws Exception {
        int response = connection.getResponseCode();

        // Throw an error if the connection fails
        if (response != 200) {
            connection.disconnect();
            throw new Exception("Connection failed, response: " + response + ".");
        }

        // If successdul, read and return results
        StringBuilder result = new StringBuilder();
        Scanner scan = new Scanner(connection.getInputStream());

        while (scan.hasNext()) {
            result.append(scan.nextLine());
        }

        scan.close();
        connection.disconnect();

        return String.valueOf(result);
    }

    private static HttpURLConnection fetchResponse(String urlString) {
        try {
            // Connection attempt
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("GET");
            connection.connect();
            return connection;
            
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Connection to: \"" + urlString + "\" failed.");
            return null;
        }
    }

    private static JSONArray parseDataArray(String data, String key) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject resultsJSON = (JSONObject) parser.parse(data);
            JSONArray parsedData = (JSONArray) resultsJSON.get(key);

            return parsedData;
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Failed to parse data.");
        }

        return null;
    }

    private static JSONObject parseDataObject(String data, String key) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject resultsJSON = (JSONObject) parser.parse(data);
            JSONObject parsedData = (JSONObject) resultsJSON.get(key);

            return parsedData;
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println("Failed to parse data.");
        }

        return null;
    }
}
