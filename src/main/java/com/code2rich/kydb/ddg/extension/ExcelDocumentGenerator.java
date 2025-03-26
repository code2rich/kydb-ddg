package com.code2rich.kydb.ddg.extension;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel文档生成器
 */
public class ExcelDocumentGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcelDocumentGenerator.class);

    // 定义样式颜色常量 - 根据要求调整
    private static final byte[] TITLE_BACKGROUND_RGB = new byte[] { (byte) 204, (byte) 255, (byte) 255 }; // CCFFFF 蓝色
    private static final byte[] HEADER_BACKGROUND_RGB = new byte[] { (byte) 255, (byte) 255, (byte) 153 }; // FFFF99 黄色
    private static final byte[] HEADER_FONT_RGB = new byte[] { (byte) 0, (byte) 0, (byte) 0 }; // 黑色字体
    private static final byte[] TABLE_ROW_EVEN_RGB = new byte[] { (byte) 204, (byte) 255, (byte) 255 }; // CCFFFF 蓝色
    private static final byte[] TABLE_HEADER_RGB = new byte[] { (byte) 204, (byte) 255, (byte) 255 }; // CCFFFF 蓝色
    private static final byte[] SUB_HEADER_RGB = new byte[] { (byte) 255, (byte) 255, (byte) 153 }; // FFFF99 黄色
    private static final byte[] HYPERLINK_RGB = new byte[] { (byte) 0, (byte) 0, (byte) 255 }; // 超链接蓝色

    /**
     * 生成Excel文档
     * @param outputDir 输出目录
     * @param fileName 文件名
     * @param dbType 数据库类型
     * @param dbName 数据库名称
     * @param tableList 表列表，每个表是一个Map，包含表名、注释和列信息
     */
    public void generate(String outputDir, String fileName, String dbType, String dbName, List<Map<String, Object>> tableList) {
        LOGGER.info("Generating Excel documentation...");
        
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // 创建各种样式
            Map<String, CellStyle> styles = createStyles(workbook);
            
            // 创建封面页
            createCoverSheet(workbook, styles, dbType, dbName);
            
            // 创建修订记录页
            createRevisionSheet(workbook, styles);
            
            // 创建表目录页
            createTableListSheet(workbook, tableList, styles);
            
            // 为每个表创建工作表
            for (Map<String, Object> table : tableList) {
                createTableSheet(workbook, table, styles);
            }
            
            // 保存Excel文件
            String filePath = outputDir + File.separator + fileName + ".xlsx";
            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }
            
            LOGGER.info("Excel documentation generated successfully: {}", filePath);
        } catch (IOException e) {
            LOGGER.error("Failed to generate Excel documentation", e);
        }
    }
    
    /**
     * 创建样式
     */
    private Map<String, CellStyle> createStyles(XSSFWorkbook workbook) {
        Map<String, CellStyle> styles = new HashMap<>();
        
        // 封面标题样式 - 大字体
        XSSFCellStyle coverTitleStyle = workbook.createCellStyle();
        coverTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        coverTitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        coverTitleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        coverTitleStyle.setFillForegroundColor(new XSSFColor(TITLE_BACKGROUND_RGB, null));
        
        XSSFFont coverTitleFont = workbook.createFont();
        coverTitleFont.setFontHeightInPoints((short) 20);
        coverTitleFont.setBold(true);
        coverTitleStyle.setFont(coverTitleFont);
        styles.put("coverTitle", coverTitleStyle);
        
        // 主标题样式
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setAlignment(HorizontalAlignment.CENTER);
        titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleStyle.setFillForegroundColor(new XSSFColor(TITLE_BACKGROUND_RGB, null));
        
        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 16);
        titleFont.setBold(true);
        titleStyle.setFont(titleFont);
        styles.put("title", titleStyle);
        
        // 表头样式
        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(new XSSFColor(HEADER_BACKGROUND_RGB, null));
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        
        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        styles.put("header", headerStyle);
        
        // 子表头样式
        XSSFCellStyle subHeaderStyle = workbook.createCellStyle();
        subHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
        subHeaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        subHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        subHeaderStyle.setFillForegroundColor(new XSSFColor(SUB_HEADER_RGB, null));
        subHeaderStyle.setBorderTop(BorderStyle.THIN);
        subHeaderStyle.setBorderRight(BorderStyle.THIN);
        subHeaderStyle.setBorderBottom(BorderStyle.THIN);
        subHeaderStyle.setBorderLeft(BorderStyle.THIN);
        
        XSSFFont subHeaderFont = workbook.createFont();
        subHeaderFont.setFontHeightInPoints((short) 11);
        subHeaderFont.setBold(true);
        subHeaderStyle.setFont(subHeaderFont);
        styles.put("subHeader", subHeaderStyle);
        
        // 普通单元格样式
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        styles.put("cell", cellStyle);
        
        // 隔行变色样式
        XSSFCellStyle alternatingRowStyle = workbook.createCellStyle();
        alternatingRowStyle.cloneStyleFrom(cellStyle);
        alternatingRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        alternatingRowStyle.setFillForegroundColor(new XSSFColor(TABLE_ROW_EVEN_RGB, null));
        styles.put("alternatingRow", alternatingRowStyle);
        
        // 链接样式
        XSSFCellStyle hyperlinkStyle = workbook.createCellStyle();
        hyperlinkStyle.cloneStyleFrom(cellStyle);
        hyperlinkStyle.setAlignment(HorizontalAlignment.CENTER);
        
        XSSFFont hyperlinkFont = workbook.createFont();
        hyperlinkFont.setUnderline(FontUnderline.SINGLE);
        hyperlinkFont.setColor(new XSSFColor(HYPERLINK_RGB, null));
        hyperlinkStyle.setFont(hyperlinkFont);
        styles.put("hyperlink", hyperlinkStyle);
        
        return styles;
    }
    
    /**
     * 创建封面页
     */
    private void createCoverSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles, String dbType, String dbName) {
        Sheet sheet = workbook.createSheet("封面");
        
        // 设置默认列宽
        sheet.setDefaultColumnWidth(15);
        
        // 创建标题行 - 居中并占据足够空间
        Row titleRow1 = sheet.createRow(10);
        Cell titleCell1 = titleRow1.createCell(4);
        titleCell1.setCellValue("数据库设计说明书");
        titleCell1.setCellStyle(styles.get("coverTitle"));
        
        // 合并单元格使标题居中
        sheet.addMergedRegion(new CellRangeAddress(10, 10, 4, 8));
        
        // 设置整个工作表为浅蓝色背景
        for (int i = 0; i < 100; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }
            for (int j = 0; j < 20; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    cell = row.createCell(j);
                }
                CellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(new XSSFColor(TITLE_BACKGROUND_RGB, null));
                cell.setCellStyle(style);
            }
        }
        
        // 在封面上添加数据库信息（放在浅蓝背景上，使其更加明显）
        Row dbTypeRow = sheet.getRow(12);
        Cell dbTypeHeaderCell = dbTypeRow.getCell(4);
        dbTypeHeaderCell.setCellValue("数据库类型: " + (dbType != null ? dbType : ""));
        
        Row dbNameRow = sheet.getRow(13);
        Cell dbNameHeaderCell = dbNameRow.getCell(4);
        dbNameHeaderCell.setCellValue("数据库名称: " + (dbName != null ? dbName : ""));
        
        // 设置生成日期
        Row dateRow = sheet.getRow(14);
        Cell dateCell = dateRow.getCell(4);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        dateCell.setCellValue("生成日期: " + sdf.format(new Date()));
        
        // 设置标题行样式
        CellStyle titleRowStyle = workbook.createCellStyle();
        titleRowStyle.setAlignment(HorizontalAlignment.CENTER);
        Font titleRowFont = workbook.createFont();
        titleRowFont.setBold(true);
        titleRowFont.setFontHeightInPoints((short) 12);
        titleRowStyle.setFont(titleRowFont);
        titleRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        titleRowStyle.setFillForegroundColor(new XSSFColor(TITLE_BACKGROUND_RGB, null));
        
        dbTypeHeaderCell.setCellStyle(titleRowStyle);
        dbNameHeaderCell.setCellStyle(titleRowStyle);
        dateCell.setCellStyle(titleRowStyle);
        
        // 设置打印区域和分页符，确保封面独立一页
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short)1); 
        sheet.getPrintSetup().setFitHeight((short)1);
    }
    
    /**
     * 创建修订记录页
     */
    private void createRevisionSheet(XSSFWorkbook workbook, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("修订记录");
        
        // 设置列宽
        for (int i = 0; i < 10; i++) {
            sheet.setColumnWidth(i, 15 * 256);
        }
        
        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("修订记录");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));
        
        // 创建表头行
        Row headerRow = sheet.createRow(2);
        String[] headers = new String[] { "版本号", "修订日期", "修订内容", "修改人", "审核/负责人", "备注" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // 添加初始版本信息
        Row dataRow = sheet.createRow(3);
        
        Cell versionCell = dataRow.createCell(0);
        versionCell.setCellValue("V1.0");
        versionCell.setCellStyle(styles.get("cell"));
        
        Cell dateCell = dataRow.createCell(1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        dateCell.setCellValue(sdf.format(new Date()));
        dateCell.setCellStyle(styles.get("cell"));
        
        Cell contentCell = dataRow.createCell(2);
        contentCell.setCellValue("初始版本");
        contentCell.setCellStyle(styles.get("cell"));
        
        // 其他单元格
        for (int i = 3; i < 6; i++) {
            Cell cell = dataRow.createCell(i);
            cell.setCellStyle(styles.get("cell"));
        }
        
        // 为工作表添加浅蓝色背景
        for (int i = 5; i < 100; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                row = sheet.createRow(i);
            }
            for (int j = 0; j < 10; j++) {
                Cell cell = row.getCell(j);
                if (cell == null) {
                    cell = row.createCell(j);
                }
                CellStyle style = workbook.createCellStyle();
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setFillForegroundColor(new XSSFColor(TITLE_BACKGROUND_RGB, null));
                cell.setCellStyle(style);
            }
        }
        
        // 设置打印区域和分页符，确保修订记录独立一页
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setFitWidth((short)1);
        sheet.getPrintSetup().setFitHeight((short)1);
    }
    
    /**
     * 创建表目录页
     */
    private void createTableListSheet(XSSFWorkbook workbook, List<Map<String, Object>> tableList, Map<String, CellStyle> styles) {
        Sheet sheet = workbook.createSheet("表目录");
        
        // 设置列宽
        sheet.setColumnWidth(0, 10 * 256); // 序号
        sheet.setColumnWidth(1, 30 * 256); // 表名
        sheet.setColumnWidth(2, 50 * 256); // 表备注
        
        // 创建标题行
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("表目录");
        titleCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        
        // 创建表头行
        Row headerRow = sheet.createRow(2);
        String[] headers = new String[] { "序号", "表名", "表备注" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // 填充表数据，交替背景色
        int tableIndex = 1;
        for (Map<String, Object> table : tableList) {
            Row tableRow = sheet.createRow(tableIndex + 2);
            
            // 使用交替背景色
            CellStyle rowStyle = (tableIndex % 2 == 0) ? styles.get("alternatingRow") : styles.get("cell");
            
            // 序号
            Cell indexCell = tableRow.createCell(0);
            indexCell.setCellValue(tableIndex++);
            indexCell.setCellStyle(rowStyle);
            
            // 表名（添加超链接）
            Cell nameCell = tableRow.createCell(1);
            String tableName = table.get("tableName") != null ? table.get("tableName").toString() : "";
            nameCell.setCellValue(tableName);
            
            // 创建工作表内部超链接
            Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
            
            // 安全处理工作表名称，避免超出限制或特殊字符
            String safeSheetName = getSafeSheetName(tableName);
            link.setAddress("'" + safeSheetName + "'!A1");
            
            nameCell.setHyperlink(link);
            nameCell.setCellStyle(styles.get("hyperlink"));
            
            // 表备注
            Cell remarksCell = tableRow.createCell(2);
            remarksCell.setCellValue(table.get("remarks") != null ? table.get("remarks").toString() : "");
            remarksCell.setCellStyle(rowStyle);
        }
        
        // 设置打印区域和分页符，确保表目录独立一页
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setLandscape(true);
        sheet.getPrintSetup().setFitWidth((short)1);
        sheet.getPrintSetup().setFitHeight((short)0);
        
        // 设置页面边距
        sheet.setMargin(Sheet.LeftMargin, 0.5);
        sheet.setMargin(Sheet.RightMargin, 0.5);
        sheet.setMargin(Sheet.TopMargin, 0.5);
        sheet.setMargin(Sheet.BottomMargin, 0.5);
    }
    
    /**
     * 创建表工作表
     */
    private void createTableSheet(Workbook workbook, Map<String, Object> table, Map<String, CellStyle> styles) {
        // 限制工作表名长度，避免超出Excel限制
        String tableName = table.get("tableName") != null ? table.get("tableName").toString() : "未命名表";
        String sheetName = getSafeSheetName(tableName);
        
        Sheet sheet = workbook.createSheet(sheetName);
        
        // 自动适应列宽
        sheet.setColumnWidth(0, 15 * 256);  // 序号
        sheet.setColumnWidth(1, 25 * 256);  // 列名
        sheet.setColumnWidth(2, 20 * 256);  // 数据类型
        sheet.setColumnWidth(3, 10 * 256);  // 长度
        sheet.setColumnWidth(4, 10 * 256);  // 精度
        sheet.setColumnWidth(5, 10 * 256);  // 主键
        sheet.setColumnWidth(6, 10 * 256);  // 非空
        sheet.setColumnWidth(7, 20 * 256);  // 默认值
        sheet.setColumnWidth(8, 40 * 256);  // 注释
        
        // 表基本信息
        int rowNum = 0;
        
        // 标题行
        Row tableNameRow = sheet.createRow(rowNum++);
        Cell nameCell = tableNameRow.createCell(0);
        nameCell.setCellValue("表名: " + tableName);
        nameCell.setCellStyle(styles.get("title"));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));
        
        rowNum++; // 空行
        
        String remarks = table.get("remarks") != null ? table.get("remarks").toString() : "";
        if (!remarks.isEmpty()) {
            Row tableRemarkRow = sheet.createRow(rowNum++);
            Cell remarkCell = tableRemarkRow.createCell(0);
            remarkCell.setCellValue("表注释: " + remarks);
            remarkCell.setCellStyle(styles.get("subHeader"));
            sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 8));
            
            rowNum++; // 空行
        } else {
            rowNum++;
        }
        
        // 添加返回链接
        Row backLinkRow = sheet.createRow(rowNum++);
        Cell backLinkCell = backLinkRow.createCell(0);
        backLinkCell.setCellValue("返回表目录");
        Hyperlink backLink = workbook.getCreationHelper().createHyperlink(HyperlinkType.DOCUMENT);
        backLink.setAddress("'表目录'!A1");
        backLinkCell.setHyperlink(backLink);
        backLinkCell.setCellStyle(styles.get("hyperlink"));
        
        rowNum++; // 空行
        
        // 列信息表头
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = new String[] { "序号", "列名", "数据类型", "长度", "精度", "主键", "非空", "默认值", "注释" };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(styles.get("header"));
        }
        
        // 填充列数据
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> columns = (List<Map<String, Object>>) table.get("columns");
        if (columns != null) {
            int columnIndex = 1;
            for (Map<String, Object> column : columns) {
                Row row = sheet.createRow(rowNum++);
                
                // 使用交替背景色
                CellStyle rowStyle = (columnIndex % 2 == 0) ? styles.get("alternatingRow") : styles.get("cell");
                
                createCell(row, 0, columnIndex++, rowStyle);
                createCell(row, 1, getStringValue(column, "name"), rowStyle);
                createCell(row, 2, getStringValue(column, "typeName"), rowStyle);
                createCell(row, 3, getStringValue(column, "length"), rowStyle);
                createCell(row, 4, getStringValue(column, "scale"), rowStyle);
                createCell(row, 5, getBooleanValue(column, "primaryKey") ? "是" : "", rowStyle);
                createCell(row, 6, getBooleanValue(column, "nullable") ? "" : "是", rowStyle);
                createCell(row, 7, getStringValue(column, "defaultValue"), rowStyle);
                createCell(row, 8, getStringValue(column, "remarks"), rowStyle);
            }
        }
        
        // 设置打印区域和分页符，确保每个表的设计明细都放在一页
        sheet.setFitToPage(true);
        sheet.getPrintSetup().setLandscape(true);  // 横向打印
        sheet.getPrintSetup().setFitWidth((short)1); // 调整为一页宽
        sheet.getPrintSetup().setFitHeight((short)0); // 高度可以跨多页
        
        // 设置页面边距
        sheet.setMargin(Sheet.LeftMargin, 0.5);
        sheet.setMargin(Sheet.RightMargin, 0.5);
        sheet.setMargin(Sheet.TopMargin, 0.5);
        sheet.setMargin(Sheet.BottomMargin, 0.5);
    }
    
    /**
     * 创建单元格并设置样式
     */
    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value != null) {
            cell.setCellValue(value.toString());
        }
        cell.setCellStyle(style);
    }
    
    /**
     * 获取安全的工作表名称（避免超出Excel限制）
     */
    private String getSafeSheetName(String tableName) {
        String sheetName = tableName;
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 31);
        }
        // 替换Excel工作表名中不允许的字符
        return sheetName.replaceAll("[\\\\/?*\\[\\]]", "_");
    }
    
    /**
     * 安全获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    /**
     * 安全获取布尔值
     */
    private boolean getBooleanValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "true".equalsIgnoreCase(String.valueOf(value));
    }
}