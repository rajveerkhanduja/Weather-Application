package view;
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.RenderingHints;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;

import org.json.JSONArray;
import org.json.JSONObject;

import controller.WeatherAPIService;
import model.DatabaseHandler;
import model.WeatherData;

public class WeatherAppUI extends Frame {
    private final WeatherAPIService apiService;
    private final DatabaseHandler dbHandler;
    
    // UI Components
    private TextField searchField;
    private Choice recentSearchesDropdown;
    private Button searchButton;
    private Panel currentWeatherPanel;
    private Panel hourlyForecastPanel;
    private Panel dailyForecastPanel;
    private Panel additionalInfoPanel;
    private Label statusLabel;
    
    // Weather info components
    private Label cityLabel;
    private Label temperatureLabel;
    private Label descriptionLabel;
    private Label humidityLabel;
    private Label windLabel;
    private Label feelsLikeLabel;
    private Label pressureLabel;
    private Label visibilityLabel;
    private Label airQualityLabel;
    
    // Weather icons
    private Map<String, Image> weatherIcons = new HashMap<>();
    
    // Colors
    private final Color BACKGROUND_COLOR = new Color(25, 31, 69); // Dark blue background
    private final Color PANEL_COLOR = new Color(35, 41, 79);      // Slightly lighter blue panels
    private final Color ACCENT_COLOR = new Color(255, 204, 0);    // Yellow accent
    private final Color TEXT_COLOR = new Color(255, 255, 255);    // White text
    private final Color SECONDARY_TEXT_COLOR = new Color(200, 200, 200); // Light gray text
    
    public WeatherAppUI() {
        apiService = new WeatherAPIService();
        dbHandler = new DatabaseHandler();
        
        loadWeatherIcons();
        setupUI();
        loadLastSearchedCity();
        
        // Window closing event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dbHandler.closeConnection();
                dispose();
                System.exit(0);
            }
        });
    }
    
    private void loadWeatherIcons() {
        try {
            // Load weather icons from resources
            String[] iconTypes = {"clear", "clouds", "rain", "snow", "thunderstorm", "mist", "partly_cloudy"};
            for (String type : iconTypes) {
                // Change this line to load from file system instead of resources
                Image icon = ImageIO.read(new File("src/main/resources/icons/" + type + ".png"));
                weatherIcons.put(type, icon);
            }
        } catch (IOException e) {
            System.err.println("Error loading weather icons: " + e.getMessage());
            e.printStackTrace(); // Add this to get more detailed error information
        }
    }
    
    private void setupUI() {
        setTitle("Weather App");
        setSize(800, 700);
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND_COLOR);
        currentWeatherPanel = createRoundedPanel();
        currentWeatherPanel.setLayout(new BorderLayout(20, 20));
        currentWeatherPanel.setBackground(PANEL_COLOR);

        // Add padding to the current weather panel
        currentWeatherPanel = createRoundedPanel();
        currentWeatherPanel.setLayout(new BorderLayout(20, 20));
        currentWeatherPanel.setBackground(PANEL_COLOR);
        
        // Add internal padding to the panel
        Panel currentWeatherInnerPanel = new Panel(new BorderLayout(20, 20));
        currentWeatherInnerPanel.setBackground(PANEL_COLOR);
        currentWeatherInnerPanel.add(Box.createHorizontalStrut(15), BorderLayout.WEST);
        currentWeatherInnerPanel.add(Box.createHorizontalStrut(15), BorderLayout.EAST);
        currentWeatherInnerPanel.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
        currentWeatherInnerPanel.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
        
        // Initialize weather info components
        Panel weatherInfoPanel = new Panel(new BorderLayout(15, 15));
        weatherInfoPanel.setBackground(PANEL_COLOR);
        
        // City and temperature panel
        Panel mainWeatherPanel = new Panel(new BorderLayout(10, 10));
        mainWeatherPanel.setBackground(PANEL_COLOR);
        
        cityLabel = new Label("", Label.LEFT);
        cityLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        cityLabel.setForeground(TEXT_COLOR);
        
        temperatureLabel = new Label("", Label.LEFT);
        temperatureLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        temperatureLabel.setForeground(TEXT_COLOR);
        
        descriptionLabel = new Label("", Label.LEFT);
        descriptionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        descriptionLabel.setForeground(SECONDARY_TEXT_COLOR);
        
        feelsLikeLabel = new Label("", Label.LEFT);
        feelsLikeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        feelsLikeLabel.setForeground(SECONDARY_TEXT_COLOR);
        
        Panel tempPanel = new Panel(new GridLayout(3, 1, 5, 5));
        tempPanel.setBackground(PANEL_COLOR);
        tempPanel.add(cityLabel);
        tempPanel.add(temperatureLabel);
        tempPanel.add(descriptionLabel);
        
        mainWeatherPanel.add(tempPanel, BorderLayout.CENTER);
        mainWeatherPanel.add(feelsLikeLabel, BorderLayout.SOUTH);
        
        // Create a placeholder for the weather icon
        Panel weatherIconPanel = new Panel();
        weatherIconPanel.setPreferredSize(new Dimension(100, 100));
        weatherIconPanel.setBackground(PANEL_COLOR);
        
        // Add components to the weather info panel
        weatherInfoPanel.add(mainWeatherPanel, BorderLayout.CENTER);
        weatherInfoPanel.add(weatherIconPanel, BorderLayout.EAST);
        
        currentWeatherInnerPanel.add(weatherInfoPanel, BorderLayout.CENTER);
        currentWeatherPanel.add(currentWeatherInnerPanel, BorderLayout.CENTER);
        
        // Initialize additional info labels
        humidityLabel = new Label("", Label.CENTER);
        windLabel = new Label("", Label.CENTER);
        pressureLabel = new Label("", Label.CENTER);
        visibilityLabel = new Label("", Label.CENTER);
        airQualityLabel = new Label("", Label.CENTER);
        
        currentWeatherPanel.add(currentWeatherInnerPanel, BorderLayout.CENTER);
        // Define fonts
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 14);
        Font boldFont = new Font("SansSerif", Font.BOLD, 16);
        Font titleFont = new Font("SansSerif", Font.BOLD, 24);
        
        // North panel - Search area
        Panel searchPanel = new Panel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));
        searchPanel.setBackground(BACKGROUND_COLOR);
        
        searchField = new TextField(20);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchField.setBackground(PANEL_COLOR);
        searchField.setForeground(TEXT_COLOR);
        
        searchButton = new Button("Search");
        searchButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchButton.setBackground(ACCENT_COLOR);
        searchButton.setForeground(Color.BLACK);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        recentSearchesDropdown = new Choice();
        recentSearchesDropdown.setFont(new Font("SansSerif", Font.PLAIN, 14));
        recentSearchesDropdown.setBackground(PANEL_COLOR);
        recentSearchesDropdown.setForeground(TEXT_COLOR);
        recentSearchesDropdown.add("Recent Searches");
        
        // Change this variable name to avoid conflict with the class field
        Label cityPromptLabel = new Label("City:");
        cityPromptLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        cityPromptLabel.setForeground(TEXT_COLOR);
        
        searchButton.addActionListener(e -> searchWeather());
        searchField.addActionListener(e -> searchWeather());
        
        recentSearchesDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && recentSearchesDropdown.getSelectedIndex() > 0) {
                String selectedCity = recentSearchesDropdown.getSelectedItem();
                searchField.setText(selectedCity);
                searchWeather();
                recentSearchesDropdown.select(0); // Reset to prompt
            }
        });
        
        // Populate recent searches
        loadRecentSearches();
        
        searchPanel.add(cityPromptLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(recentSearchesDropdown);
        
        // Main content panel with vertical layout and scrolling
        Panel contentPanel = new Panel();
        // Add more padding to the content panel
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);
        
        // Improve scrolling with better padding
        java.awt.ScrollPane scrollPane = new java.awt.ScrollPane(java.awt.ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.setBackground(BACKGROUND_COLOR);
        scrollPane.add(contentPanel);
        scrollPane.setScrollPosition(0, 0);
        scrollPane.setWheelScrollingEnabled(true);
        
        // Create the missing containers
        Panel hourlyContainer = new Panel(new BorderLayout(0, 10));
        hourlyContainer.setBackground(BACKGROUND_COLOR);
        
        Label hourlyTitle = new Label("Hourly forecast", Label.LEFT);
        hourlyTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        hourlyTitle.setForeground(TEXT_COLOR);
        
        hourlyForecastPanel = createRoundedPanel();
        hourlyForecastPanel.setLayout(new GridLayout(1, 8, 10, 10));
        hourlyForecastPanel.setBackground(PANEL_COLOR);
        
        hourlyContainer.add(hourlyTitle, BorderLayout.NORTH);
        hourlyContainer.add(hourlyForecastPanel, BorderLayout.CENTER);
        
        // Create the missing additional container
        Panel additionalContainer = new Panel(new BorderLayout(0, 10));
        additionalContainer.setBackground(BACKGROUND_COLOR);
        
        // Add title for the additional info section
        Label additionalTitle = new Label("Additional Information", Label.LEFT);
        additionalTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        additionalTitle.setForeground(TEXT_COLOR);
        additionalContainer.add(additionalTitle, BorderLayout.NORTH);
        
        additionalInfoPanel = createRoundedPanel();
        additionalInfoPanel.setLayout(new GridLayout(2, 4, 10, 10));
        additionalInfoPanel.setBackground(PANEL_COLOR);
        
        // Add padding around the additional info panel using the same approach
        Panel additionalPaddingPanel = new Panel(new BorderLayout());
        additionalPaddingPanel.setBackground(PANEL_COLOR);
        additionalPaddingPanel.add(additionalInfoPanel, BorderLayout.CENTER);
        
        // Reuse the padding approach (create new panels for each container)
        Panel addWestPadding = new Panel();
        addWestPadding.setPreferredSize(new Dimension(15, 1));
        addWestPadding.setBackground(PANEL_COLOR);
        
        Panel addEastPadding = new Panel();
        addEastPadding.setPreferredSize(new Dimension(15, 1));
        addEastPadding.setBackground(PANEL_COLOR);
        
        Panel addNorthPadding = new Panel();
        addNorthPadding.setPreferredSize(new Dimension(1, 15));
        addNorthPadding.setBackground(PANEL_COLOR);
        
        Panel addSouthPadding = new Panel();
        addSouthPadding.setPreferredSize(new Dimension(1, 15));
        addSouthPadding.setBackground(PANEL_COLOR);
        
        additionalPaddingPanel.add(addWestPadding, BorderLayout.WEST);
        additionalPaddingPanel.add(addEastPadding, BorderLayout.EAST);
        additionalPaddingPanel.add(addNorthPadding, BorderLayout.NORTH);
        additionalPaddingPanel.add(addSouthPadding, BorderLayout.SOUTH);
        
        additionalContainer.add(additionalPaddingPanel, BorderLayout.CENTER);
        
        // 10-day forecast panel
        Panel dailyContainer = new Panel(new BorderLayout(0, 10));
        dailyContainer.setBackground(BACKGROUND_COLOR);
        
        Label dailyTitle = new Label("10-day forecast", Label.LEFT);
        dailyTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        dailyTitle.setForeground(TEXT_COLOR);
        
        dailyForecastPanel = createRoundedPanel();
        dailyForecastPanel.setLayout(new GridLayout(5, 2, 10, 10));
        dailyForecastPanel.setBackground(PANEL_COLOR);
        
        // Add padding around the daily forecast panel using the same approach
        Panel dailyPaddingPanel = new Panel(new BorderLayout());
        dailyPaddingPanel.setBackground(PANEL_COLOR);
        dailyPaddingPanel.add(dailyForecastPanel, BorderLayout.CENTER);
        
        // Create padding panels for daily forecast
        Panel dayWestPadding = new Panel();
        dayWestPadding.setPreferredSize(new Dimension(15, 1));
        dayWestPadding.setBackground(PANEL_COLOR);
        
        Panel dayEastPadding = new Panel();
        dayEastPadding.setPreferredSize(new Dimension(15, 1));
        dayEastPadding.setBackground(PANEL_COLOR);
        
        Panel dayNorthPadding = new Panel();
        dayNorthPadding.setPreferredSize(new Dimension(1, 15));
        dayNorthPadding.setBackground(PANEL_COLOR);
        
        Panel daySouthPadding = new Panel();
        daySouthPadding.setPreferredSize(new Dimension(1, 15));
        daySouthPadding.setBackground(PANEL_COLOR);
        
        dailyPaddingPanel.add(dayWestPadding, BorderLayout.WEST);
        dailyPaddingPanel.add(dayEastPadding, BorderLayout.EAST);
        dailyPaddingPanel.add(dayNorthPadding, BorderLayout.NORTH);
        dailyPaddingPanel.add(daySouthPadding, BorderLayout.SOUTH);
        
        dailyContainer.add(dailyTitle, BorderLayout.NORTH);
        dailyContainer.add(dailyPaddingPanel, BorderLayout.CENTER);
        
        // Add all panels to content panel with spacing
        contentPanel.add(currentWeatherPanel);
        
        Panel spacer1 = new Panel();
        spacer1.setPreferredSize(new Dimension(getWidth(), 30));
        spacer1.setBackground(BACKGROUND_COLOR);
        contentPanel.add(spacer1);
        
        contentPanel.add(hourlyContainer);
        
        Panel spacer2 = new Panel();
        spacer2.setPreferredSize(new Dimension(getWidth(), 30));
        spacer2.setBackground(BACKGROUND_COLOR);
        contentPanel.add(spacer2);
        
        contentPanel.add(additionalContainer);
        
        Panel spacer3 = new Panel();
        spacer3.setPreferredSize(new Dimension(getWidth(), 30));
        spacer3.setBackground(BACKGROUND_COLOR);
        contentPanel.add(spacer3);
        
        contentPanel.add(dailyContainer);
        
        // Status bar
        statusLabel = new Label("Ready", Label.LEFT);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(SECONDARY_TEXT_COLOR);
        
        // Add panels to frame
        add("North", searchPanel);
        add("Center", scrollPane);
        add("South", statusLabel);

        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private Panel createRoundedPanel() {
        return new Panel() {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(PANEL_COLOR);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paint(g);
            }
            
            // Add insets to create padding inside the panel
            @Override
            public Insets getInsets() {
                return new Insets(10, 10, 10, 10);
            }
        };
    }
    
    private Label createInfoCard(String title, String value) {
        Panel card = createRoundedPanel();
        card.setLayout(new GridLayout(2, 1, 5, 5));
        card.setBackground(PANEL_COLOR);
        
        Label titleLabel = new Label(title, Label.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        titleLabel.setForeground(SECONDARY_TEXT_COLOR);
        
        Label valueLabel = new Label(value, Label.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(TEXT_COLOR);
        
        card.add(titleLabel);
        card.add(valueLabel);
        
        additionalInfoPanel.add(card);  // Add the entire card to the panel
        
        return valueLabel;
    }
    
    private void loadRecentSearches() {
        recentSearchesDropdown.removeAll();
        recentSearchesDropdown.add("Recent Searches");
        
        List<String> recentSearches = dbHandler.getRecentSearches(5);
        for (String city : recentSearches) {
            recentSearchesDropdown.add(city);
        }
    }
    
    private void loadLastSearchedCity() {
        String lastCity = dbHandler.getLastSearchedCity();
        if (lastCity != null && !lastCity.isEmpty()) {
            searchField.setText(lastCity);
            searchWeather();
        }
    }
    
    private void searchWeather() {
        String cityName = searchField.getText().trim();
        if (cityName.isEmpty()) {
            statusLabel.setText("Error: Please enter a city name");
            return;
        }
        
        statusLabel.setText("Fetching weather data for " + cityName + "...");
        
        try {
            // Get current weather
            WeatherData weatherData = apiService.getCurrentWeather(cityName);
            
            // Update UI with weather data
            cityLabel.setText(weatherData.getCityName());
            temperatureLabel.setText(String.format("%.0f°", weatherData.getTemperature()));
            descriptionLabel.setText(capitalizeFirst(weatherData.getWeatherDescription()));
            feelsLikeLabel.setText("Feels like " + String.format("%.0f°", weatherData.getFeelsLike()));
            humidityLabel.setText(weatherData.getHumidity() + "%");
            
            // Update additional info
            updateAdditionalInfo(weatherData);
            
            // Get forecast data
            updateHourlyForecast(cityName);
            updateDailyForecast(cityName);
            
            // Update weather icon
            updateWeatherIcon(weatherData.getWeatherDescription());
            
            // Save search to database
            dbHandler.saveSearch(cityName);
            
            // Reload recent searches
            loadRecentSearches();
            
            statusLabel.setText("Weather data updated successfully");
        } catch (IOException e) {
            statusLabel.setText("Error: " + e.getMessage());
            System.err.println("Weather data fetch error: " + e.getMessage());
        }
    }
    
    private void updateAdditionalInfo(WeatherData weatherData) {
        // Clear previous info cards
        additionalInfoPanel.removeAll();
        
        // Create info cards for each piece of additional information
        humidityLabel = createInfoCard("Humidity", weatherData.getHumidity() + "%");
        
        // Update wind (convert from m/s to mph for display)
        double windSpeedMph = weatherData.getWindSpeed() * 2.237;
        windLabel = createInfoCard("Wind", String.format("%.1f mph", windSpeedMph));
        
        // Update pressure (convert from hPa to inHg for display)
        double pressureInHg = weatherData.getPressure() * 0.02953;
        pressureLabel = createInfoCard("Pressure", String.format("%.2f inHg", pressureInHg));
        
        // Update visibility (convert from meters to miles)
        double visibilityMiles = weatherData.getVisibility() / 1609.34;
        visibilityLabel = createInfoCard("Visibility", String.format("%.1f mi", visibilityMiles));
        
        // Air quality (this would need to be added to your API service)
        // For now, we'll use a placeholder
        airQualityLabel = createInfoCard("Air Quality", "Good");
        
        // Add more info cards if needed
        createInfoCard("UV Index", "Low");
        createInfoCard("Sunrise", "6:45 AM");
        createInfoCard("Sunset", "7:30 PM");
        
        additionalInfoPanel.revalidate();
        additionalInfoPanel.repaint();
    }
    
    private void updateWeatherIcon(String description) {
        // Improve icon selection logic to be more accurate
        String iconKey = "clear"; // Default
        
        description = description.toLowerCase();
        
        // First check description for specific weather conditions
        if (description.contains("overcast") || description.contains("broken clouds")) {
            iconKey = "clouds"; // Full cloud cover
        } else if (description.contains("cloud")) {
            if (description.contains("scattered") || description.contains("few")) {
                iconKey = "partly_cloudy";
            } else {
                iconKey = "clouds";
            }
        } else if (description.contains("rain") || description.contains("drizzle")) {
            iconKey = "rain";
        } else if (description.contains("snow")) {
            iconKey = "snow";
        } else if (description.contains("thunder")) {
            iconKey = "thunderstorm";
        } else if (description.contains("mist") || description.contains("fog")) {
            iconKey = "mist";
        } else if (description.contains("clear")) {
            iconKey = "clear";
        }
        
        // For Antarctica or very cold places, override with clouds if temperature is very low
        try {
            WeatherData currentData = apiService.getCurrentWeather(cityLabel.getText());
            if (currentData.getTemperature() < -20) {
                iconKey = "clouds"; // Very cold places typically have cloud cover
            }
        } catch (IOException e) {
            // Fallback to default if we can't get temperature
        }
        
        final String finalIconKey = iconKey;
        
        // Find the weather icon panel inside the currentWeatherInnerPanel
        Panel currentWeatherInnerPanel = (Panel) currentWeatherPanel.getComponent(0);
        Panel weatherInfoPanel = (Panel) currentWeatherInnerPanel.getComponent(4); // Get the weatherInfoPanel
        
        // Create a new weather icon panel
        Panel weatherIconPanel = new Panel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                if (weatherIcons.containsKey(finalIconKey)) {
                    g.drawImage(weatherIcons.get(finalIconKey), 0, 0, 100, 100, this);
                }
            }
        };
        weatherIconPanel.setPreferredSize(new Dimension(100, 100));
        weatherIconPanel.setBackground(PANEL_COLOR);
        
        // Replace the existing icon panel or add a new one
        try {
            weatherInfoPanel.add(weatherIconPanel, BorderLayout.EAST);
        } catch (Exception e) {
            // If there's an error, try to add it to the currentWeatherPanel directly
            currentWeatherPanel.add(weatherIconPanel, BorderLayout.EAST);
        }
        
        // Refresh the UI
        weatherInfoPanel.revalidate();
        weatherInfoPanel.repaint();
        currentWeatherPanel.revalidate();
        currentWeatherPanel.repaint();
    }
    
    private void updateHourlyForecast(String cityName) {
        try {
            JSONObject forecastData = apiService.getForecast(cityName);
            JSONArray forecastList = forecastData.getJSONArray("list");
            
            // Clear previous forecast
            hourlyForecastPanel.removeAll();
            
            // We'll display 8 hourly points
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            
            for (int i = 0; i < 8; i++) {
                if (i < forecastList.length()) {
                    JSONObject timepoint = forecastList.getJSONObject(i);
                    
                    JSONObject main = timepoint.getJSONObject("main");
                    JSONArray weatherArray = timepoint.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    String timestamp = timepoint.getString("dt_txt");
                    
                    // Parse timestamp
                    Date forecastTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
                    String time = timeFormat.format(forecastTime);
                    
                    // Create hourly forecast card
                    Panel card = createRoundedPanel();
                    card.setLayout(new GridLayout(3, 1, 5, 5));
                    card.setBackground(PANEL_COLOR);
                    
                    Label timeLabel = new Label(time, Label.CENTER);
                    timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    timeLabel.setForeground(TEXT_COLOR);
                    
                    // Weather icon panel
                    final String weatherDesc = weather.getString("description").toLowerCase();
                    Panel iconPanel = new Panel() {
                        @Override
                        public void paint(Graphics g) {
                            super.paint(g);
                            String iconKey = getIconKeyFromDescription(weatherDesc);
                            if (weatherIcons.containsKey(iconKey)) {
                                g.drawImage(weatherIcons.get(iconKey), 10, 0, 30, 30, this);
                            }
                        }
                    };
                    iconPanel.setPreferredSize(new Dimension(50, 30));
                    
                    Label tempLabel = new Label(String.format("%.0f°", main.getDouble("temp")), Label.CENTER);
                    tempLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                    tempLabel.setForeground(TEXT_COLOR);
                    
                    card.add(timeLabel);
                    card.add(iconPanel);
                    card.add(tempLabel);
                    
                    hourlyForecastPanel.add(card);
                }
            }
            
            hourlyForecastPanel.revalidate();
            hourlyForecastPanel.repaint();
            
        } catch (Exception e) {
            statusLabel.setText("Error loading hourly forecast: " + e.getMessage());
        }
    }
    
    private void updateDailyForecast(String cityName) {
        try {
            JSONObject forecastData = apiService.getForecast(cityName);
            JSONArray forecastList = forecastData.getJSONArray("list");
            
            // Clear previous forecast
            dailyForecastPanel.removeAll();
            
            // We'll display 10 days (we'll need to group by day)
            SimpleDateFormat dayFormat = new SimpleDateFormat("E");
            SimpleDateFormat dateFormat = new SimpleDateFormat("M/d");
            
            // For demo purposes, we'll just show the same day multiple times
            // In a real app, you'd need to group by day and calculate min/max temps
            for (int i = 0; i < 10; i++) {
                int index = Math.min(i * 8, forecastList.length() - 1);
                if (index < forecastList.length()) {
                    JSONObject timepoint = forecastList.getJSONObject(index);
                    
                    JSONObject main = timepoint.getJSONObject("main");
                    JSONArray weatherArray = timepoint.getJSONArray("weather");
                    JSONObject weather = weatherArray.getJSONObject(0);
                    String timestamp = timepoint.getString("dt_txt");
                    
                    // Parse timestamp
                    Date forecastDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(timestamp);
                    String day = dayFormat.format(forecastDate);
                    String date = dateFormat.format(forecastDate);
                    
                    // Create daily forecast card
                    Panel card = createRoundedPanel();
                    card.setLayout(new GridLayout(1, 4, 15, 5)); // Increase horizontal padding
                    card.setBackground(PANEL_COLOR);
                    
                    Label dayLabel = new Label(day, Label.LEFT);
                    dayLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    dayLabel.setForeground(TEXT_COLOR);
                    
                    Label dateLabel = new Label(date, Label.LEFT);
                    dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
                    dateLabel.setForeground(SECONDARY_TEXT_COLOR);
                    
                    // Weather icon panel
                    final String weatherDesc = weather.getString("description").toLowerCase();
                    Panel iconPanel = new Panel() {
                        @Override
                        public void paint(Graphics g) {
                            super.paint(g);
                            String iconKey = getIconKeyFromDescription(weatherDesc);
                            if (weatherIcons.containsKey(iconKey)) {
                                g.drawImage(weatherIcons.get(iconKey), 0, 0, 30, 30, this);
                            }
                        }
                    };
                    
                    // Temperature range
                    double maxTemp = main.getDouble("temp_max");
                    double minTemp = main.getDouble("temp_min");
                    Label tempLabel = new Label(String.format("%.0f° / %.0f°", maxTemp, minTemp), Label.RIGHT);
                    tempLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
                    tempLabel.setForeground(TEXT_COLOR);
                    
                    Panel dayDatePanel = new Panel(new GridLayout(2, 1));
                    dayDatePanel.setBackground(PANEL_COLOR);
                    dayDatePanel.add(dayLabel);
                    dayDatePanel.add(dateLabel);
                    
                    card.add(dayDatePanel);
                    card.add(iconPanel);
                    card.add(new Label("", Label.CENTER)); // Spacer
                    card.add(tempLabel);
                    
                    dailyForecastPanel.add(card);
                }
            }
            
            dailyForecastPanel.revalidate();
            dailyForecastPanel.repaint();
            
        } catch (Exception e) {
            statusLabel.setText("Error loading daily forecast: " + e.getMessage());
        }
    }
    
    private String getIconKeyFromDescription(String description) {
        if (description.contains("overcast") || description.contains("broken clouds")) {
            return "clouds"; // Full cloud cover
        } else if (description.contains("cloud")) {
            if (description.contains("scattered") || description.contains("few")) {
                return "partly_cloudy";
            } else {
                return "clouds";
            }
        } else if (description.contains("rain") || description.contains("drizzle")) {
            return "rain";
        } else if (description.contains("snow")) {
            return "snow";
        } else if (description.contains("thunder")) {
            return "thunderstorm";
        } else if (description.contains("mist") || description.contains("fog")) {
            return "mist";
        } else {
            return "clear";
        }
    }
    
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}