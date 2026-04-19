package com.cyz.converter;

import com.cyz.common.util.ExcelUtil;
import com.cyz.common.util.PinyinUtil;
import com.cyz.common.util.Sm4Util;
import com.cyz.config.Sm4Properties;
import com.cyz.entity.SysPerson;
import org.apache.poi.ss.usermodel.Row;

public class PersonExcelRowMapper {

    private static String sm4Key;

    public static void setSm4Key(String key) {
        sm4Key = key;
    }

    private static final String[] HEADERS = {
            "姓", "名", "身份证号码", "手机号码", "员工工号", "所属机构号", "人员状态"
    };

    public static String[] getHeaders() {
        return HEADERS;
    }

    /**
     * 从Excel行解析人员数据（含SM4加密处理）
     */
    public static SysPerson fromRow(Row row) {
        String surname = ExcelUtil.getCellStringValue(row, 0);
        String givenName = ExcelUtil.getCellStringValue(row, 1);
        String idCard = ExcelUtil.getCellStringValue(row, 2);
        String phone = ExcelUtil.getCellStringValue(row, 3);
        String staffNum = ExcelUtil.getCellStringValue(row, 4);
        String orgCode = ExcelUtil.getCellStringValue(row, 5);
        String status = ExcelUtil.getCellStringValue(row, 6);

        String fullName = surname + givenName;
        SysPerson person = new SysPerson();
        person.setSurname(surname);
        person.setSurnamePinyin(PinyinUtil.toPinyin(surname));
        person.setGivenName(givenName);
        person.setGivenNamePinyin(PinyinUtil.toPinyin(givenName));
        person.setFullName(fullName);
        person.setFullNamePinyin(PinyinUtil.toPinyin(fullName));
        person.setIdCard(Sm4Util.encrypt(idCard, sm4Key));
        person.setPhone(Sm4Util.encrypt(phone, sm4Key));
        person.setStaffNum(staffNum);
        person.setOrgCode(orgCode);
        person.setStatus(status);
        return person;
    }

    /**
     * 将人员数据写入Excel行（含SM4解密处理）
     */
    public static void toRow(Row row, SysPerson person) {
        ExcelUtil.setCellValue(row, 0, person.getSurname());
        ExcelUtil.setCellValue(row, 1, person.getGivenName());
        ExcelUtil.setCellValue(row, 2, Sm4Util.decrypt(person.getIdCard(), sm4Key));
        ExcelUtil.setCellValue(row, 3, Sm4Util.decrypt(person.getPhone(), sm4Key));
        ExcelUtil.setCellValue(row, 4, person.getStaffNum());
        ExcelUtil.setCellValue(row, 5, person.getOrgCode());
        ExcelUtil.setCellValue(row, 6, person.getStatus());
    }
}
