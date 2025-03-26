package com.code2rich.kydb.ddg.gui;

import com.code2rich.kydb.ddg.DatabaseDocumentGenerator;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class KnowYourDBGUI extends JFrame {
    private final JTextField jdbcUrlField;
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JTextField schemaField;
    private final JTextField outputDirField;
    private final JTextField titleField;
    private final JTextField versionField;
    private final JTextArea descriptionField;
    private final JComboBox<String> driverComboBox;
    private final JComboBox<String> fileTypeComboBox;
    private final JButton generateButton;
    private final JButton browseButton;

    // 表过滤相关组件
    private final JTextArea designatedTableNamesArea;
    private final JTextArea designatedTablePrefixesArea;
    private final JTextArea designatedTableSuffixesArea;
    private final JTextArea ignoreTableNamesArea;
    private final JTextArea ignoreTablePrefixesArea;
    private final JTextArea ignoreTableSuffixesArea;

    // Common JDBC drivers
    private final Map<String, String> driverClassMapping = new HashMap<String, String>() {{
        put("MySQL", "com.mysql.cj.jdbc.Driver");
        put("PostgreSQL", "org.postgresql.Driver");
        put("Oracle", "oracle.jdbc.driver.OracleDriver");
        put("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        put("MariaDB", "org.mariadb.jdbc.Driver");
    }};

    // Output file types
    private final String[] fileTypes = {"HTML", "Word", "Markdown", "Excel"};
    public KnowYourDBGUI() {
        // Set up the main frame
        super("KnowYourDB - Database Documentation Generator");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top panel with logo and title
        JPanel topPanel = new JPanel();
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel titleLabel = new JLabel("KnowYourDB", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel);

        // Main form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Database connection section
        JPanel connectionPanel = createSectionPanel("数据库连接");
        GridBagLayout connectionLayout = new GridBagLayout();
        connectionPanel.setLayout(connectionLayout);

        // Driver selection
        addLabelAndComponent(connectionPanel, "数据库类型:",
                driverComboBox = new JComboBox<>(driverClassMapping.keySet().toArray(new String[0])), 0);

        // JDBC URL
        addLabelAndComponent(connectionPanel, "JDBC URL:",
                jdbcUrlField = new JTextField(25), 1);
        jdbcUrlField.setToolTipText("Example: jdbc:mysql://localhost:3306/mydb");

        // Username
        addLabelAndComponent(connectionPanel, "用户名:",
                usernameField = new JTextField(15), 2);

        // Password
        addLabelAndComponent(connectionPanel, "密码:",
                passwordField = new JPasswordField(15), 3);

        // Schema
        addLabelAndComponent(connectionPanel, "Schema (选填):",
                schemaField = new JTextField(15), 4);

        // Output options section
        JPanel outputPanel = createSectionPanel("输出选项");
        GridBagLayout outputLayout = new GridBagLayout();
        outputPanel.setLayout(outputLayout);

        // Output directory with browse button
        JPanel outputDirPanel = new JPanel(new BorderLayout(5, 0));
        outputDirField = new JTextField(25);
        outputDirField.setText(System.getProperty("user.dir") + File.separator + "docs");
        browseButton = new JButton("浏览...");
        outputDirPanel.add(outputDirField, BorderLayout.CENTER);
        outputDirPanel.add(browseButton, BorderLayout.EAST);
        addLabelAndComponent(outputPanel, "输出目录:", outputDirPanel, 0);

        // File type
        addLabelAndComponent(outputPanel, "文件格式:",
                fileTypeComboBox = new JComboBox<>(fileTypes), 1);

        // Document information section
        JPanel docInfoPanel = createSectionPanel("文档信息");
        GridBagLayout docInfoLayout = new GridBagLayout();
        docInfoPanel.setLayout(docInfoLayout);

        // Title
        addLabelAndComponent(docInfoPanel, "标题:",
                titleField = new JTextField("Database Documentation"), 0);

        // Version
        addLabelAndComponent(docInfoPanel, "版本:",
                versionField = new JTextField("1.0.0"), 1);

        // Description
        JLabel descLabel = new JLabel("描述:");
        descLabel.setHorizontalAlignment(JLabel.RIGHT);
        GridBagConstraints descLabelGbc = new GridBagConstraints();
        descLabelGbc.gridx = 0;
        descLabelGbc.gridy = 2;
        descLabelGbc.anchor = GridBagConstraints.NORTHEAST;
        descLabelGbc.insets = new Insets(5, 5, 5, 5);
        docInfoPanel.add(descLabel, descLabelGbc);

        descriptionField = new JTextArea(3, 25);
        descriptionField.setText("数据库文档由KnowYourDB生成");
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionField);

        GridBagConstraints descFieldGbc = new GridBagConstraints();
        descFieldGbc.gridx = 1;
        descFieldGbc.gridy = 2;
        descFieldGbc.fill = GridBagConstraints.BOTH;
        descFieldGbc.insets = new Insets(5, 5, 5, 5);
        docInfoPanel.add(descScroll, descFieldGbc);

        // 表过滤选项面板
        JPanel tableFilterPanel = createSectionPanel("表过滤选项");
        tableFilterPanel.setLayout(new GridBagLayout());

        // 分为两列：指定表和忽略表
        JPanel designatedPanel = new JPanel(new GridBagLayout());
        designatedPanel.setBorder(BorderFactory.createTitledBorder("指定生成的表"));
        JPanel ignorePanel = new JPanel(new GridBagLayout());
        ignorePanel.setBorder(BorderFactory.createTitledBorder("忽略的表"));

        // 指定表名
        designatedTableNamesArea = createFilterTextArea("每行一个表名");
        addFilterComponent(designatedPanel, "表名:", new JScrollPane(designatedTableNamesArea), 0);

        // 指定表前缀
        designatedTablePrefixesArea = createFilterTextArea("每行一个前缀");
        addFilterComponent(designatedPanel, "表前缀:", new JScrollPane(designatedTablePrefixesArea), 1);

        // 指定表后缀
        designatedTableSuffixesArea = createFilterTextArea("每行一个后缀");
        addFilterComponent(designatedPanel, "表后缀:", new JScrollPane(designatedTableSuffixesArea), 2);

        // 忽略表名
        ignoreTableNamesArea = createFilterTextArea("每行一个表名");
        addFilterComponent(ignorePanel, "表名:", new JScrollPane(ignoreTableNamesArea), 0);

        // 忽略表前缀
        ignoreTablePrefixesArea = createFilterTextArea("每行一个前缀");
        addFilterComponent(ignorePanel, "表前缀:", new JScrollPane(ignoreTablePrefixesArea), 1);

        // 忽略表后缀
        ignoreTableSuffixesArea = createFilterTextArea("每行一个后缀");
        addFilterComponent(ignorePanel, "表后缀:", new JScrollPane(ignoreTableSuffixesArea), 2);

        // 将两个面板添加到表过滤面板
        GridBagConstraints filterGbc = new GridBagConstraints();
        filterGbc.gridx = 0;
        filterGbc.gridy = 0;
        filterGbc.fill = GridBagConstraints.BOTH;
        filterGbc.weightx = 1.0;
        filterGbc.weighty = 1.0;
        filterGbc.insets = new Insets(5, 5, 5, 5);
        tableFilterPanel.add(designatedPanel, filterGbc);

        filterGbc.gridx = 1;
        tableFilterPanel.add(ignorePanel, filterGbc);

        // Add all sections to the main form panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        formPanel.add(connectionPanel, gbc);

        gbc.gridy = 1;
        formPanel.add(outputPanel, gbc);

        gbc.gridy = 2;
        formPanel.add(docInfoPanel, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        formPanel.add(tableFilterPanel, gbc);

        // Generate button panel
        JPanel buttonPanel = new JPanel();
        generateButton = new JButton("生成文档");
        generateButton.setPreferredSize(new Dimension(200, 40));
        generateButton.setFont(new Font("Arial", Font.BOLD, 14));
        buttonPanel.add(generateButton);

        // Add components to the main frame
        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(formPanel), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        addActionListeners();

        // Set default values
        driverComboBox.setSelectedItem("MySQL");
        jdbcUrlField.setText("jdbc:mysql://localhost:3306/mydb");
        fileTypeComboBox.setSelectedItem("HTML");

        // Set visible
        setVisible(true);
    }

    private JTextArea createFilterTextArea(String tooltip) {
        JTextArea area = new JTextArea(3, 15);
        area.setToolTipText(tooltip);
        area.setLineWrap(true);
        return area;
    }

    private void addFilterComponent(JPanel panel, String labelText, JComponent component, int row) {
        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(JLabel.RIGHT);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.NORTHEAST;
        labelGbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.BOTH;
        fieldGbc.weightx = 1.0;
        fieldGbc.weighty = 1.0;
        fieldGbc.insets = new Insets(5, 5, 5, 5);
        panel.add(component, fieldGbc);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }

    private void addLabelAndComponent(JPanel panel, String labelText, JComponent component, int row) {
        JLabel label = new JLabel(labelText);
        label.setHorizontalAlignment(JLabel.RIGHT);

        GridBagConstraints labelGbc = new GridBagConstraints();
        labelGbc.gridx = 0;
        labelGbc.gridy = row;
        labelGbc.anchor = GridBagConstraints.EAST;
        labelGbc.insets = new Insets(5, 5, 5, 5);
        panel.add(label, labelGbc);

        GridBagConstraints fieldGbc = new GridBagConstraints();
        fieldGbc.gridx = 1;
        fieldGbc.gridy = row;
        fieldGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldGbc.weightx = 1.0;
        fieldGbc.insets = new Insets(5, 5, 5, 5);
        panel.add(component, fieldGbc);
    }

    private List<String> getTextAreaLines(JTextArea textArea) {
        List<String> lines = new ArrayList<>();
        if (textArea.getText() == null || textArea.getText().trim().isEmpty()) {
            return lines;
        }

        String[] linesArray = textArea.getText().split("\n");
        for (String line : linesArray) {
            if (line != null && !line.trim().isEmpty()) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    private void addActionListeners() {
        // Browse button action
        browseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择输出目录");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                outputDirField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Driver selection change action
        driverComboBox.addActionListener(e -> {
            String selected = (String) driverComboBox.getSelectedItem();
            if ("MySQL".equals(selected)) {
                jdbcUrlField.setText("jdbc:mysql://localhost:3306/mydb");
            } else if ("PostgreSQL".equals(selected)) {
                jdbcUrlField.setText("jdbc:postgresql://localhost:5432/mydb");
            } else if ("Oracle".equals(selected)) {
                jdbcUrlField.setText("jdbc:oracle:thin:@localhost:1521:orcl");
            } else if ("SQL Server".equals(selected)) {
                jdbcUrlField.setText("jdbc:sqlserver://localhost:1433;databaseName=mydb");
            } else if ("MariaDB".equals(selected)) {
                jdbcUrlField.setText("jdbc:mariadb://localhost:3306/mydb");
            }
        });

        // Generate button action
        generateButton.addActionListener(e -> generateDocumentation());
    }

    private void generateDocumentation() {
        try {
            // Get driver class from selection
            final String driverClass = driverClassMapping.get((String) driverComboBox.getSelectedItem());

            // Get file type
            final String fileType = getFileTypeString();

            // 从文本框中获取表过滤配置
            final List<String> designatedTableNames = getTextAreaLines(designatedTableNamesArea);
            final List<String> designatedTablePrefixes = getTextAreaLines(designatedTablePrefixesArea);
            final List<String> designatedTableSuffixes = getTextAreaLines(designatedTableSuffixesArea);
            final List<String> ignoreTableNames = getTextAreaLines(ignoreTableNamesArea);
            final List<String> ignoreTablePrefixes = getTextAreaLines(ignoreTablePrefixesArea);
            final List<String> ignoreTableSuffixes = getTextAreaLines(ignoreTableSuffixesArea);

            // Disable the button during generation
            generateButton.setEnabled(false);
            generateButton.setText("生成中...");

            // 获取其他参数
            final String jdbcUrl = jdbcUrlField.getText();
            final String username = usernameField.getText();
            final String password = new String(passwordField.getPassword());
            final String schema = schemaField.getText();
            final String outputDir = outputDirField.getText();
            final String title = titleField.getText();
            final String version = versionField.getText();
            final String description = descriptionField.getText();

            // Run in background thread to avoid UI freeze
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    DatabaseDocumentGenerator generator = new DatabaseDocumentGenerator();
                    generator.generate(
                            driverClass,
                            jdbcUrl,
                            username,
                            password,
                            schema,
                            outputDir,
                            fileType,
                            title,
                            version,
                            description,
                            designatedTableNames,
                            designatedTablePrefixes,
                            designatedTableSuffixes,
                            ignoreTableNames,
                            ignoreTablePrefixes,
                            ignoreTableSuffixes
                    );
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get(); // Check for exceptions
                        JOptionPane.showMessageDialog(
                                KnowYourDBGUI.this,
                                "文档生成成功!",
                                "成功",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(
                                KnowYourDBGUI.this,
                                "生成文档出错: " + ex.getMessage(),
                                "错误",
                                JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    } finally {
                        // Re-enable the button
                        generateButton.setEnabled(true);
                        generateButton.setText("生成文档");
                    }
                }
            };

            worker.execute();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "错误: " + ex.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private String getFileTypeString() {
        String selectedType = (String) fileTypeComboBox.getSelectedItem();
        if (selectedType == null) {
            return "HTML";
        }
        if (selectedType.equalsIgnoreCase("Word")) {
            return "WORD";
        } else if (selectedType.equalsIgnoreCase("Markdown")) {
            return "MD";
        } else if (selectedType.equalsIgnoreCase("Excel")) {
            return "EXCEL";
        } else {
            return "HTML";
        }
    }

    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(KnowYourDBGUI::new);
    }
}