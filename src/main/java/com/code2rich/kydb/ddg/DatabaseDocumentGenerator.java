package com.code2rich.kydb.ddg;

import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.code2rich.kydb.ddg.extension.ExcelDocumentGenerator;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseDocumentGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseDocumentGenerator.class);

    public void generate(
            String driverClassName,
            String jdbcUrl,
            String username,
            String password,
            String schema,
            String outputDir,
            String fileType,
            String title,
            String version,
            String description,
            List<String> designatedTableNames,
            List<String> designatedTablePrefixes,
            List<String> designatedTableSuffixes,
            List<String> ignoreTableNames,
            List<String> ignoreTablePrefixes,
            List<String> ignoreTableSuffixes) {

        LOGGER.info("Starting documentation generation...");
        LOGGER.info("JDBC URL: {}", jdbcUrl);
        LOGGER.info("Output directory: {}", outputDir);
        LOGGER.info("File type: {}", fileType);

        // Create output directory if it doesn't exist
        File directory = new File(outputDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Configure datasource
        DataSource dataSource = getDataSource(driverClassName, jdbcUrl, username, password, schema);

        // 判断是否为Excel格式
        boolean isExcelFormat = "EXCEL".equalsIgnoreCase(fileType);
        
        // Configure the engine (只有在非Excel格式时才需要)
        EngineConfig engineConfig = null;
        if (!isExcelFormat) {
            engineConfig = getEngineConfig(outputDir, title, getFileType(fileType));
        }

        // Configure process with table filters
        ProcessConfig processConfig = getProcessConfig(
                designatedTableNames,
                designatedTablePrefixes,
                designatedTableSuffixes,
                ignoreTableNames,
                ignoreTablePrefixes,
                ignoreTableSuffixes);

        if (isExcelFormat) {
            // 使用自定义Excel生成器
            try {
                // 直接从数据库连接获取元数据，避免使用screw的内部类
                generateExcelDocument(dataSource, schema, outputDir, title == null ? "database-document" : title, processConfig);
            } catch (Exception e) {
                LOGGER.error("Failed to generate Excel documentation", e);
            }
        } else {
            // 将原来的代码放入else块
            // Configure documentation (只有在非Excel格式时才需要)
            Configuration config = Configuration.builder()
                    .version(version)
                    .description(description)
                    .dataSource(dataSource)
                    .engineConfig(engineConfig)
                    .produceConfig(processConfig)
                    .build();
        
            // Execute documentation generation
            new DocumentationExecute(config).execute();
        }
        LOGGER.info("Documentation generated at: {}", outputDir);
    }

    // 新增方法：直接使用JDBC生成Excel文档
    private void generateExcelDocument(DataSource dataSource, String schema, String outputDir, String title, ProcessConfig processConfig) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 获取数据库类型和名称
            String dbType = metaData.getDatabaseProductName();
            String dbName = schema != null ? schema : connection.getCatalog();
            
            // 获取所有表
            List<Map<String, Object>> tableList = new ArrayList<>();
            
            // 表类型数组，通常包括 "TABLE", "VIEW" 等
            String[] types = {"TABLE"};
            
            ResultSet tablesResultSet = metaData.getTables(
                    connection.getCatalog(), 
                    schema, 
                    "%", 
                    types);
            
            while (tablesResultSet.next()) {
                String tableName = tablesResultSet.getString("TABLE_NAME");
                
                // 表过滤逻辑 - 简化版本，实际应该参考processConfig
                if (shouldSkipTable(tableName, processConfig)) {
                    continue;
                }
                
                Map<String, Object> tableMap = new HashMap<>();
                tableMap.put("tableName", tableName);
                tableMap.put("remarks", tablesResultSet.getString("REMARKS"));
                
                // 获取列信息
                List<Map<String, Object>> columnList = new ArrayList<>();
                ResultSet primaryKeys = metaData.getPrimaryKeys(
                        connection.getCatalog(), 
                        schema, 
                        tableName);
                
                // 收集主键信息
                List<String> pkColumns = new ArrayList<>();
                while (primaryKeys.next()) {
                    pkColumns.add(primaryKeys.getString("COLUMN_NAME"));
                }
                primaryKeys.close();
                
                // 获取列信息
                ResultSet columnsResultSet = metaData.getColumns(
                        connection.getCatalog(), 
                        schema, 
                        tableName, 
                        "%");
                
                while (columnsResultSet.next()) {
                    Map<String, Object> columnMap = new HashMap<>();
                    String columnName = columnsResultSet.getString("COLUMN_NAME");
                    
                    columnMap.put("name", columnName);
                    columnMap.put("typeName", columnsResultSet.getString("TYPE_NAME"));
                    columnMap.put("length", columnsResultSet.getInt("COLUMN_SIZE"));
                    columnMap.put("scale", columnsResultSet.getInt("DECIMAL_DIGITS"));
                    columnMap.put("primaryKey", pkColumns.contains(columnName));
                    columnMap.put("nullable", columnsResultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    columnMap.put("defaultValue", columnsResultSet.getString("COLUMN_DEF"));
                    columnMap.put("remarks", columnsResultSet.getString("REMARKS"));
                    
                    columnList.add(columnMap);
                }
                columnsResultSet.close();
                
                tableMap.put("columns", columnList);
                tableList.add(tableMap);
            }
            tablesResultSet.close();
            
            // 使用重构后的Excel生成器生成文档
            ExcelDocumentGenerator excelGenerator = new ExcelDocumentGenerator();
            excelGenerator.generate(outputDir, title, dbType, dbName, tableList);
            
        } catch (SQLException e) {
            LOGGER.error("Error while fetching database metadata", e);
            throw new RuntimeException("Failed to generate Excel documentation", e);
        }
    }
    
    // 辅助方法：根据过滤规则判断是否应该跳过某个表
    private boolean shouldSkipTable(String tableName, ProcessConfig processConfig) {
        if (processConfig == null) {
            return false;
        }
        
        // 获取表过滤配置
        List<String> designatedNames = processConfig.getDesignatedTableName();
        List<String> designatedPrefixes = processConfig.getDesignatedTablePrefix();
        List<String> designatedSuffixes = processConfig.getDesignatedTableSuffix();
        List<String> ignoreNames = processConfig.getIgnoreTableName();
        List<String> ignorePrefixes = processConfig.getIgnoreTablePrefix();
        List<String> ignoreSuffixes = processConfig.getIgnoreTableSuffix();
        
        // 如果指定了表名/前缀/后缀，只包含指定的表
        if (!designatedNames.isEmpty() || !designatedPrefixes.isEmpty() || !designatedSuffixes.isEmpty()) {
            boolean matched = false;
            
            // 检查表名是否匹配
            if (designatedNames.contains(tableName)) {
                matched = true;
            }
            
            // 检查表名前缀是否匹配
            if (!matched) {
                for (String prefix : designatedPrefixes) {
                    if (tableName.startsWith(prefix)) {
                        matched = true;
                        break;
                    }
                }
            }
            
            // 检查表名后缀是否匹配
            if (!matched) {
                for (String suffix : designatedSuffixes) {
                    if (tableName.endsWith(suffix)) {
                        matched = true;
                        break;
                    }
                }
            }
            
            // 如果没有匹配，跳过此表
            if (!matched) {
                return true;
            }
        }
        
        // 检查是否应该忽略此表
        if (ignoreNames.contains(tableName)) {
            return true;
        }
        
        // 检查表名前缀是否应该忽略
        for (String prefix : ignorePrefixes) {
            if (tableName.startsWith(prefix)) {
                return true;
            }
        }
        
        // 检查表名后缀是否应该忽略
        for (String suffix : ignoreSuffixes) {
            if (tableName.endsWith(suffix)) {
                return true;
            }
        }
        
        return false;
    }

    // 支持旧接口，避免破坏兼容性
    public void generate(
            String driverClassName,
            String jdbcUrl,
            String username,
            String password,
            String schema,
            String outputDir,
            String fileType,
            String title,
            String version,
            String description) {

        // 调用新接口，传入空列表
        generate(
                driverClassName,
                jdbcUrl,
                username,
                password,
                schema,
                outputDir,
                fileType,
                title,
                version,
                description,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private DataSource getDataSource(String driverClassName, String jdbcUrl, String username, String password, String schema) {
        LOGGER.info("Setting up data source...");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(driverClassName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Set schema if provided
        if (schema != null && !schema.isEmpty()) {
            hikariConfig.setSchema(schema);
        }

        // Enable metadata retrieval for proper documentation
        hikariConfig.addDataSourceProperty("useInformationSchema", "true");
        hikariConfig.addDataSourceProperty("remarks", "true");
        hikariConfig.addDataSourceProperty("remarksReporting", "true");

        // Connection pool settings
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setMaximumPoolSize(5);

        return new HikariDataSource(hikariConfig);
    }

    private EngineConfig getEngineConfig(String outputDir, String title, EngineFileType fileType) {
        LOGGER.info("Setting up engine configuration...");

        return EngineConfig.builder()
                .fileOutputDir(outputDir)
                .openOutputDir(true)
                .fileType(fileType)
                .produceType(EngineTemplateType.freemarker)
                .fileName(title == null ? "database-document" : title)
                .build();
    }

    // 重构的表过滤配置方法
    private ProcessConfig getProcessConfig(
            List<String> designatedTableNames,
            List<String> designatedTablePrefixes,
            List<String> designatedTableSuffixes,
            List<String> ignoreTableNames,
            List<String> ignoreTablePrefixes,
            List<String> ignoreTableSuffixes) {

        LOGGER.info("Setting up process configuration with table filters...");

        // 日志记录过滤配置
        if (!designatedTableNames.isEmpty()) {
            LOGGER.info("Designated table names: {}", designatedTableNames);
        }
        if (!designatedTablePrefixes.isEmpty()) {
            LOGGER.info("Designated table prefixes: {}", designatedTablePrefixes);
        }
        if (!designatedTableSuffixes.isEmpty()) {
            LOGGER.info("Designated table suffixes: {}", designatedTableSuffixes);
        }
        if (!ignoreTableNames.isEmpty()) {
            LOGGER.info("Ignore table names: {}", ignoreTableNames);
        }
        if (!ignoreTablePrefixes.isEmpty()) {
            LOGGER.info("Ignore table prefixes: {}", ignoreTablePrefixes);
        }
        if (!ignoreTableSuffixes.isEmpty()) {
            LOGGER.info("Ignore table suffixes: {}", ignoreTableSuffixes);
        }

        // 防止空指针
        List<String> designatedNames = designatedTableNames != null ? designatedTableNames : new ArrayList<>();
        List<String> designatedPrefixes = designatedTablePrefixes != null ? designatedTablePrefixes : new ArrayList<>();
        List<String> designatedSuffixes = designatedTableSuffixes != null ? designatedTableSuffixes : new ArrayList<>();
        List<String> ignoreNames = ignoreTableNames != null ? ignoreTableNames : new ArrayList<>();
        List<String> ignorePrefixes = ignoreTablePrefixes != null ? ignoreTablePrefixes : new ArrayList<>();
        List<String> ignoreSuffixes = ignoreTableSuffixes != null ? ignoreTableSuffixes : new ArrayList<>();

        return ProcessConfig.builder()
                .designatedTableName(new ArrayList<>(designatedNames))
                .designatedTablePrefix(new ArrayList<>(designatedPrefixes))
                .designatedTableSuffix(new ArrayList<>(designatedSuffixes))
                .ignoreTableName(new ArrayList<>(ignoreNames))
                .ignoreTablePrefix(new ArrayList<>(ignorePrefixes))
                .ignoreTableSuffix(new ArrayList<>(ignoreSuffixes))
                .build();
    }

    // 向后兼容的方法
    private ProcessConfig getProcessConfig(String schema) {
        return getProcessConfig(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private EngineFileType getFileType(String fileType) {
        if (fileType == null || fileType.isEmpty()) {
            return EngineFileType.HTML;
        }
    
        switch (fileType.toUpperCase()) {
            case "HTML":
                return EngineFileType.HTML;
            case "WORD":
                return EngineFileType.WORD;
            case "MD":
            case "MARKDOWN":
                return EngineFileType.MD;
            // Excel类型在调用此方法前已经被处理，不应该走到这里
            // 但是为了完整性，依然保留
            case "EXCEL":
                LOGGER.info("Excel format will be handled by custom implementation");
                // 返回一个默认类型，实际上不会被使用到
                return EngineFileType.HTML;
            default:
                LOGGER.warn("Unknown file type '{}', defaulting to HTML", fileType);
                return EngineFileType.HTML;
        }
    }
}