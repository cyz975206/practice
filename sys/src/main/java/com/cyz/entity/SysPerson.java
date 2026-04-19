package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_person", uniqueConstraints = {
        @UniqueConstraint(name = "uk_staff_num", columnNames = "staff_num")
}, indexes = {
        @Index(name = "idx_person_org_code", columnList = "org_code"),
        @Index(name = "idx_person_status", columnList = "status"),
        @Index(name = "idx_person_id_card", columnList = "id_card")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysPerson extends BaseEntity {

    @Column(name = "surname", length = 32)
    private String surname;

    @Column(name = "surname_pinyin", length = 64)
    private String surnamePinyin;

    @Column(name = "given_name", length = 32)
    private String givenName;

    @Column(name = "given_name_pinyin", length = 64)
    private String givenNamePinyin;

    @Column(name = "full_name", length = 64)
    private String fullName;

    @Column(name = "full_name_pinyin", length = 128)
    private String fullNamePinyin;

    @Column(name = "id_card", length = 256)
    private String idCard;

    @Column(name = "phone", length = 256)
    private String phone;

    @Column(name = "staff_num", length = 32, unique = true)
    private String staffNum;

    @Column(name = "org_code", length = 64)
    private String orgCode;

    @Column(name = "status", length = 32)
    private String status;
}
