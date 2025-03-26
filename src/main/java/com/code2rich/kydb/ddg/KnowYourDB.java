package com.code2rich.kydb.ddg;

import com.code2rich.kydb.ddg.cli.CommandLineParser;
import com.code2rich.kydb.ddg.gui.KnowYourDBGUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.Map;

public class KnowYourDB {
    private static final Logger LOGGER = LoggerFactory.getLogger(KnowYourDB.class);

    public static void main(String[] args) {
        // If no arguments are provided, launch GUI mode
        if (args.length == 0) {
            launchGUI();
            return;
        }
        
        // Otherwise, proceed with CLI mode
        try {
            CommandLineParser parser = new CommandLineParser();
            Map<String, String> options = (Map<String, String>) parser.parse(args);

            if (options.isEmpty()) {
                return;
            }

            DatabaseDocumentGenerator generator = new DatabaseDocumentGenerator();
            generator.generate(
                    options.get("driver"),
                    options.get("url"),
                    options.get("username"),
                    options.get("password"),
                    options.get("schema"),
                    options.get("output"),
                    options.get("type"),
                    options.get("title"),
                    options.get("version"),
                    options.get("description")
            );

            LOGGER.info("Documentation generated successfully!");
        } catch (Exception e) {
            LOGGER.error("Error generating documentation: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    private static void launchGUI() {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.error("Could not set look and feel: {}", e.getMessage());
        }
        
        SwingUtilities.invokeLater(KnowYourDBGUI::new);
    }
}