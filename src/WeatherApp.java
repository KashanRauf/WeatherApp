/**
 * Class for GUI
 * Sources for assets: Vitaly Gorbachev, Kirill Kazacheck, and iconpacks.net
 */

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.json.simple.JSONObject;

public class WeatherApp extends JFrame {
    private JSONObject data;

    // Instantiates the app and GUI
    public WeatherApp() {
        super("Weather");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        setVisible(true);

        addGUI();
    }

    // Adds GUI components
    private void addGUI() {
        // Search bar
        JTextField search = new JTextField();
        search.setBounds(15, 15, 300, 45);
        search.setFont(new Font("Arial", Font.PLAIN, 20));
        add(search);
        
        // Icon representing current condition
        JLabel weatherIcon = new JLabel(loadImage("src/Assets/sunny.png"));
        weatherIcon.setBounds(36, 70, 300, 300);
        add(weatherIcon);
        
        // Temperature in degrees celsius
        JLabel temperature = new JLabel("15 C");
        temperature.setBounds(-9, 380, 400, 50);
        temperature.setFont(new Font("Arial", Font.BOLD, 40));
        temperature.setHorizontalAlignment(SwingConstants.CENTER);
        add(temperature);
        
        // A description of the condition (e.g. sunny, cloudy, rainy, snowy)
        JLabel condition = new JLabel("Sunny");
        condition.setBounds(-9, 435, 400, 35);
        condition.setFont(new Font("Arial", Font.PLAIN, 30));
        condition.setHorizontalAlignment(SwingConstants.CENTER);
        add(condition);
        
        // Humidity
        JLabel humidityIcon = new JLabel(loadImage("src/Assets/humidity.png"));
        humidityIcon.setBounds(10, 480, 80, 80);
        add(humidityIcon);
        
        JLabel humidity = new JLabel("<html><b>Humidity</b> 80%</html>");
        humidity.setBounds(84, 480, 80, 80);
        humidity.setFont(new Font("Arial", Font.PLAIN, 16));
        add(humidity);
        
        // Windspeed    
        JLabel windSpeedIcon = new JLabel(loadImage("src/Assets/wind.png"));
        windSpeedIcon.setBounds(169, 480, 80, 80);
        add(windSpeedIcon);
        
        JLabel windSpeed = new JLabel("<html><b>Wind Speed</b> 5km/h</html>");
        windSpeed.setBounds(234, 480, 100, 80);
        windSpeed.setFont(new Font("Arial", Font.PLAIN, 16));
        add(windSpeed);
        
        // Search button, put at end since other GUI components have to be initialized first to update data
        JButton searchButton = new JButton(loadImage("src/Assets/search.png"));
        searchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchButton.setBounds(330, 15, 45, 45);
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = search.getText();

                // Validate input from search bar
                if (input.isBlank()) {
                    return;
                }

                // Gets data
                data = WeatherData.getWeatherData(input);

                // Update GUI components:
                // Update weatherIcon
                String condition = (String) data.get("condition");
                weatherIcon.setIcon(loadImage("src/Assets/" + condition + ".png"));

                // Temperature
                temperature.setText(String.valueOf(data.get("temperature")) + " C");

                // Humidity
                humidity.setText("<html><b>Humidity</b> " + String.valueOf(data.get("humidity")) + "%</html>");

                // Wind speed
                windSpeed.setText("<html><b>Wind Speed</b> " + String.valueOf(data.get("windspeed")) + "km/h</html>");
            }
        });
        add(searchButton);
    }

    private ImageIcon loadImage(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            return new ImageIcon(image);
        } catch(IOException e) {
            System.err.println(e);
            e.printStackTrace();
            return null;
        }
    }
}

