package model;

public class WeatherData {
    private String cityName;
    private double temperature;
    private String weatherDescription;
    private int humidity;
    private double windSpeed;
    private double feelsLike;
    private String iconCode;
    private double pressure;
    private int visibility;
    
    // Constructor
    public WeatherData(String cityName, double temperature, String weatherDescription, 
                      int humidity, double windSpeed, double feelsLike, String iconCode,
                      double pressure, int visibility) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.weatherDescription = weatherDescription;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.feelsLike = feelsLike;
        this.iconCode = iconCode;
        this.pressure = pressure;
        this.visibility = visibility;
    }
    
    // Getters
    public String getCityName() {
        return cityName;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public String getWeatherDescription() {
        return weatherDescription;
    }
    
    public int getHumidity() {
        return humidity;
    }
    
    public double getWindSpeed() {
        return windSpeed;
    }
    
    public double getFeelsLike() {
        return feelsLike;
    }
    
    public String getIconCode() {
        return iconCode;
    }
    
    public double getPressure() {
        return pressure;
    }
    
    public int getVisibility() {
        return visibility;
    }
    
    @Override
    public String toString() {
        return "City: " + cityName + 
               "\nTemperature: " + temperature + "°C" +
               "\nFeels like: " + feelsLike + "°C" +
               "\nDescription: " + weatherDescription +
               "\nHumidity: " + humidity + "%" +
               "\nPressure: " + pressure + " hPa" +
               "\nVisibility: " + visibility + " m" +
               "\nWind Speed: " + windSpeed + " m/s";
    }
}