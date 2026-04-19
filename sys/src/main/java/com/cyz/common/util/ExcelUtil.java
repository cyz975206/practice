package com.cyz.common.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 通用Excel读写工具类
 */
public class ExcelUtil {

    /**
     * 读取Excel文件
     *
     * @param inputStream 输入流
     * @param headerRow   表头所在行号（从0开始）
     * @param dataStartRow 数据起始行号
     * @param rowMapper   行映射函数
     */
    public static <T> List<T> read(InputStream inputStream, int headerRow, int dataStartRow,
                                    Function<Row, T> rowMapper) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<T> result = new ArrayList<>();
            for (int i = dataStartRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }
                try {
                    T item = rowMapper.apply(row);
                    if (item != null) {
                        result.add(item);
                    }
                } catch (Exception e) {
                    // 跳过解析失败的行
                }
            }
            return result;
        }
    }

    /**
     * 写入Excel文件
     *
     * @param outputStream 输出流
     * @param sheetName    Sheet名称
     * @param headers      表头数组
     * @param data         数据列表
     * @param rowWriter    行写入函数
     */
    public static <T> void write(OutputStream outputStream, String sheetName,
                                  String[] headers, List<T> data,
                                  BiConsumer<Row, T> rowWriter) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // 写入表头
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 写入数据
            for (int i = 0; i < data.size(); i++) {
                Row row = sheet.createRow(i + 1);
                rowWriter.accept(row, data.get(i));
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
        }
    }

    /**
     * 获取单元格字符串值
     */
    public static String getCellStringValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * 设置单元格值
     */
    public static void setCellValue(Row row, int cellIndex, String value) {
        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(value != null ? value : "");
    }

    private static boolean isRowEmpty(Row row) {
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(row, i);
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }
}
