package com.code2rich.kydb.ddg.cli;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class CommandLineParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineParser.class);
    
    private final Options options;
    
    public CommandLineParser() {
        options = new Options();
        
        // 必选参数
        options.addRequiredOption("d", "driver", true, "数据库驱动类名");
        options.addRequiredOption("u", "url", true, "JDBC URL");
        options.addRequiredOption("n", "username", true, "数据库用户名");
        options.addRequiredOption("p", "password", true, "数据库密码");
        options.addRequiredOption("o", "output", true, "输出目录路径");
        
        // 可选参数
        options.addOption("s", "schema", true, "数据库schema名称");
        options.addOption("t", "type", true, "输出文件类型 (HTML, WORD, MD)");
        options.addOption("i", "title", true, "文档标题");
        options.addOption("v", "version", true, "文档版本");
        options.addOption("e", "description", true, "文档描述");
        
        // 表过滤选项
        options.addOption(Option.builder("tn").longOpt("table-names").hasArgs().desc("指定表名列表").build());
        options.addOption(Option.builder("tp").longOpt("table-prefixes").hasArgs().desc("指定表前缀列表").build());
        options.addOption(Option.builder("ts").longOpt("table-suffixes").hasArgs().desc("指定表后缀列表").build());
        options.addOption(Option.builder("itn").longOpt("ignore-table-names").hasArgs().desc("忽略表名列表").build());
        options.addOption(Option.builder("itp").longOpt("ignore-table-prefixes").hasArgs().desc("忽略表前缀列表").build());
        options.addOption(Option.builder("its").longOpt("ignore-table-suffixes").hasArgs().desc("忽略表后缀列表").build());

        // 帮助选项
        options.addOption("h", "help", false, "显示帮助信息");
    }
    
    public CommandLineOptions parse(String[] args) {
        CommandLineOptions result = new CommandLineOptions();
        
        try {
            // 创建命令行解析器
            org.apache.commons.cli.CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            
            // 检查帮助选项
            if (cmd.hasOption("h")) {
                printHelp();
                result.setHelp(true);
                return result;
            }
            
            // 解析必选参数
            result.setDriverClassName(cmd.getOptionValue("d"));
            result.setJdbcUrl(cmd.getOptionValue("u"));
            result.setUsername(cmd.getOptionValue("n"));
            result.setPassword(cmd.getOptionValue("p"));
            result.setOutputDir(cmd.getOptionValue("o"));
            
            // 解析可选参数
            if (cmd.hasOption("s")) {
                result.setSchema(cmd.getOptionValue("s"));
            }
            
            if (cmd.hasOption("t")) {
                result.setFileType(cmd.getOptionValue("t"));
            } else {
                result.setFileType("HTML"); // 默认HTML
            }
            
            if (cmd.hasOption("i")) {
                result.setTitle(cmd.getOptionValue("i"));
            } else {
                result.setTitle("Database Documentation");
            }
            
            if (cmd.hasOption("v")) {
                result.setVersion(cmd.getOptionValue("v"));
            } else {
                result.setVersion("1.0.0");
            }
            
            if (cmd.hasOption("e")) {
                result.setDescription(cmd.getOptionValue("e"));
            } else {
                result.setDescription("Generated by KnowYourDB");
            }
            
            // 解析表过滤选项
            if (cmd.hasOption("tn")) {
                result.setDesignatedTableNames(Arrays.asList(cmd.getOptionValues("tn")));
            }
            
            if (cmd.hasOption("tp")) {
                result.setDesignatedTablePrefixes(Arrays.asList(cmd.getOptionValues("tp")));
            }
            
            if (cmd.hasOption("ts")) {
                result.setDesignatedTableSuffixes(Arrays.asList(cmd.getOptionValues("ts")));
            }
            
            if (cmd.hasOption("itn")) {
                result.setIgnoreTableNames(Arrays.asList(cmd.getOptionValues("itn")));
            }
            
            if (cmd.hasOption("itp")) {
                result.setIgnoreTablePrefixes(Arrays.asList(cmd.getOptionValues("itp")));
            }
            
            if (cmd.hasOption("its")) {
                result.setIgnoreTableSuffixes(Arrays.asList(cmd.getOptionValues("its")));
            }
            
        } catch (ParseException e) {
            LOGGER.error("解析命令行参数时出错: {}", e.getMessage());
            printHelp();
            result.setHasErrors(true);
        }
        
        return result;
    }
    
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar know-your-db.jar", options, true);
    }
}

class CommandLineOptions {
    private String driverClassName;
    private String jdbcUrl;
    private String username;
    private String password;
    private String outputDir;
    private String schema;
    private String fileType;
    private String title;
    private String version;
    private String description;
    private List<String> designatedTableNames;
    private List<String> designatedTablePrefixes;
    private List<String> designatedTableSuffixes;
    private List<String> ignoreTableNames;
    private List<String> ignoreTablePrefixes;
    private List<String> ignoreTableSuffixes;
    private boolean help;
    private boolean hasErrors;

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDesignatedTableNames() {
        return designatedTableNames;
    }

    public void setDesignatedTableNames(List<String> designatedTableNames) {
        this.designatedTableNames = designatedTableNames;
    }

    public List<String> getDesignatedTablePrefixes() {
        return designatedTablePrefixes;
    }

    public void setDesignatedTablePrefixes(List<String> designatedTablePrefixes) {
        this.designatedTablePrefixes = designatedTablePrefixes;
    }

    public List<String> getDesignatedTableSuffixes() {
        return designatedTableSuffixes;
    }

    public void setDesignatedTableSuffixes(List<String> designatedTableSuffixes) {
        this.designatedTableSuffixes = designatedTableSuffixes;
    }

    public List<String> getIgnoreTableNames() {
        return ignoreTableNames;
    }

    public void setIgnoreTableNames(List<String> ignoreTableNames) {
        this.ignoreTableNames = ignoreTableNames;
    }

    public List<String> getIgnoreTablePrefixes() {
        return ignoreTablePrefixes;
    }

    public void setIgnoreTablePrefixes(List<String> ignoreTablePrefixes) {
        this.ignoreTablePrefixes = ignoreTablePrefixes;
    }

    public List<String> getIgnoreTableSuffixes() {
        return ignoreTableSuffixes;
    }

    public void setIgnoreTableSuffixes(List<String> ignoreTableSuffixes) {
        this.ignoreTableSuffixes = ignoreTableSuffixes;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }
}