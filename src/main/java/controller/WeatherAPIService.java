package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import model.WeatherData;

public class WeatherAPIService {
    private static final String API_KEY = "Add your API Key"; // Replace with your OpenWeatherMap API key
    private static final String API_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String AIR_QUALITY_URL = "https://api.openweathermap.org/data/2.5/air_pollution";
    
    public WeatherData getCurrentWeather(String cityName) throws IOException {
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String urlString = API_URL + "?q=" + encodedCityName + "&appid=" + API_KEY + "&units=metric";
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            
            // Parse the JSON response
            JSONObject main = jsonResponse.getJSONObject("main");
            JSONArray weatherArray = jsonResponse.getJSONArray("weather");
            JSONObject weather = weatherArray.getJSONObject(0);
            JSONObject wind = jsonResponse.getJSONObject("wind");
            
            double temperature = main.getDouble("temp");
            double feelsLike = main.getDouble("feels_like");
            int humidity = main.getInt("humidity");
            double pressure = main.getDouble("pressure");
            String description = weather.getString("description");
            String iconCode = weather.getString("icon");
            double windSpeed = wind.getDouble("speed");
            String city = jsonResponse.getString("name");
            
            // Get visibility (in meters)
            int visibility = jsonResponse.has("visibility") ? jsonResponse.getInt("visibility") : 10000; // Default to 10km if not available
            
            return new WeatherData(city, temperature, description, humidity, windSpeed, feelsLike, iconCode, pressure, visibility);
        } else {
            throw new IOException("Error fetching weather data. Response code: " + responseCode);
        }
    }
    
    public JSONObject getForecast(String cityName) throws IOException {
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String urlString = FORECAST_URL + "?q=" + encodedCityName + "&appid=" + API_KEY + "&units=metric&cnt=40"; // Get 5 days forecast (40 x 3-hour intervals)
        
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            return new JSONObject(response.toString());
        } else {
            throw new IOException("Error fetching forecast data. Response code: " + responseCode);
        }
    }
    
    public String getAirQuality(String cityName) throws IOException {
        // First get coordinates for the city
        String encodedCityName = URLEncoder.encode(cityName, StandardCharsets.UTF_8.toString());
        String geoUrlString = "https://api.openweathermap.org/geo/1.0/direct?q=" + encodedCityName + "&limit=1&appid=" + API_KEY;
        
        URL geoUrl = new URL(geoUrlString);
        HttpURLConnection geoConnection = (HttpURLConnection) geoUrl.openConnection();
        geoConnection.setRequestMethod("GET");
        
        int geoResponseCode = geoConnection.getResponseCode();
        if (geoResponseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(geoConnection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            
            JSONArray locations = new JSONArray(response.toString());
            if (locations.length() > 0) {
                JSONObject location = locations.getJSONObject(0);
                double lat = location.getDouble("lat");
                double lon = location.getDouble("lon");
                
                // Now get air quality data
                String aqiUrlString = AIR_QUALITY_URL + "?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY;
                
                URL aqiUrl = new URL(aqiUrlString);
                HttpURLConnection aqiConnection = (HttpURLConnection) aqiUrl.openConnection();
                aqiConnection.setRequestMethod("GET");
                
                int aqiResponseCode = aqiConnection.getResponseCode();
                if (aqiResponseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader aqiIn = new BufferedReader(new InputStreamReader(aqiConnection.getInputStream()));
                    StringBuilder aqiResponse = new StringBuilder();
                    
                    while ((inputLine = aqiIn.readLine()) != null) {
                        aqiResponse.append(inputLine);
                    }
                    aqiIn.close();
                    
                    JSONObject aqiData = new JSONObject(aqiResponse.toString());
                    JSONArray list = aqiData.getJSONArray("list");
                    if (list.length() > 0) {
                        JSONObject current = list.getJSONObject(0);
                        JSONObject components = current.getJSONObject("components");
                        JSONObject main = current.getJSONObject("main");
                        int aqi = main.getInt("aqi");
                        
                        // AQI values: 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
                        switch (aqi) {
                            case 1: return "Good";
                            case 2: return "Fair";
                            case 3: return "Moderate";
                            case 4: return "Poor";
                            case 5: return "Very Poor";
                            default: return "Unknown";
                        }
                    }
                }
            }
        }
        
        return "Unknown"; // Default if we can't get air quality data
    }
}
