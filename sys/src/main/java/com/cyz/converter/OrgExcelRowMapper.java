package com.cyz.converter;

import com.cyz.common.util.ExcelUtil;
import com.cyz.entity.SysOrg;
import org.apache.poi.ss.usermodel.Row;

public class OrgExcelRowMapper {

    private static final String[] HEADERS = {
            "机构号", "机构简称", "机构全称", "机构等级", "上级机构号", "状态", "排序"
    };

    public static String[] getHeaders() {
        return HEADERS;
    }

    public static SysOrg fromRow(Row row) {
        SysOrg org = new SysOrg();
        org.setOrgCode(ExcelUtil.getCellStringValue(row, 0));
        org.setOrgShortName(ExcelUtil.getCellStringValue(row, 1));
        org.setOrgFullName(ExcelUtil.getCellStringValue(row, 2));
        org.setOrgLevel(ExcelUtil.getCellStringValue(row, 3));
        org.setParentOrgCode(ExcelUtil.getCellStringValue(row, 4));
        org.setStatus(ExcelUtil.getCellStringValue(row, 5));
        String sortStr = ExcelUtil.getCellStringValue(row, 6);
        if (!sortStr.isEmpty()) {
            org.setSort(Integer.parseInt(sortStr));
        }
        return org;
    }

    public static void toRow(Row row, SysOrg org) {
        ExcelUtil.setCellValue(row, 0, org.getOrgCode());
        ExcelUtil.setCellValue(row, 1, org.getOrgShortName());
        ExcelUtil.setCellValue(row, 2, org.getOrgFullName());
        ExcelUtil.setCellValue(row, 3, org.getOrgLevel());
        ExcelUtil.setCellValue(row, 4, org.getParentOrgCode());
        ExcelUtil.setCellValue(row, 5, org.getStatus());
        ExcelUtil.setCellValue(row, 6, org.getSort() != null ? String.valueOf(org.getSort()) : "0");
    }
}
