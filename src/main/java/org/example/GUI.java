package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GUI {
    private static final Logger LOGGER = Logger.getLogger(GUI.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("GUI.log", true);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException("Error setting up log file", e);
        }
    }

    public String[] getLoginCredentials() {
        String email = null;
        String password = null;

        String os = System.getProperty("os.name").toLowerCase();
        String pathname = os.contains("win")
                ? "C:" + File.separator + "temp" + File.separator + "facebook.json"
                : os.contains("linux")
                ? File.separator + "tmp" + File.separator + "facebook.json"
                : null;

        if (pathname == null) {
            throw new RuntimeException("Unsupported OS: " + os);
        }

        Path parentDir = Path.of(pathname).getParent();
        if (!Files.exists(parentDir)) {
            int option = JOptionPane.showConfirmDialog(null,
                    "The directory " + parentDir + " does not exist. Do you want to create it?",
                    "Directory not found", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                try {
                    Files.createDirectories(parentDir);
                    JOptionPane.showMessageDialog(null,
                            "Directory created successfully.");
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to create directory: " + parentDir,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    LOGGER.log(Level.WARNING, "Failed to create directory: " + parentDir, e);
                }
            }
        }

        File loginFile = new File(pathname);
        if (loginFile.exists()) {
            int choice = JOptionPane.showConfirmDialog(null,
                    "Do you want to use saved login credentials?",
                    "Login credentials", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                LOGGER.info("Reading login credentials from file");
                JsonNode credentials = readCredentialsFromFile(pathname);
                if (credentials != null) {
                    email = credentials.get("facebookCredentials").get("email").asText();
                    password = credentials.get("facebookCredentials").get("password").asText();
                } else {
                    LOGGER.warning("Failed to read login credentials from file");
                }
            }
        }

        if (email == null || password == null) {
            LOGGER.info("Reading login credentials from Swing input");
            email = JOptionPane.showInputDialog(null, "Facebook Email:");
            password = JOptionPane.showInputDialog(null, "Facebook Password:");
            int saveChoice = JOptionPane.showConfirmDialog(null, "Do you want to save login credentials?", "Login credentials", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (saveChoice == JOptionPane.YES_OPTION) {
                saveCredentialsToFile(email, password, pathname);
            }
        }


        return new String[]{email, password};
    }

    public String getSearch() {
        return JOptionPane.showInputDialog(null, "What would you like to search for?");
    }

    private JsonNode readCredentialsFromFile(String pathname) {
        try {
            LOGGER.info("Reading login credentials from file");
            File jsonFile = new File(pathname);
            ObjectMapper objectMapper = new ObjectMapper();

            return objectMapper.readTree(jsonFile);
        } catch (IOException e) {
            LOGGER.warning("Failed to read login credentials from file");
            e.printStackTrace();
            return null;
        }
    }

    private void saveCredentialsToFile(String email, String password, String pathname) {
        try {
            LOGGER.info("Saving login credentials to file");
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode credentialsNode = objectMapper.createObjectNode();
            credentialsNode.put("email", email);
            credentialsNode.put("password", password);
            rootNode.set("facebookCredentials", credentialsNode);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(pathname), rootNode);
        } catch (IOException e) {
            LOGGER.warning("Failed to save login credentials to file");
            e.printStackTrace();
        }
    }
}
