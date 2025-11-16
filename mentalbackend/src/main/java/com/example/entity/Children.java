package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 孩子基本信息表
 */

@Data
@TableName("children")
public class Children implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("name")
    private String name;

    @TableField("gender")
    private String gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("ethnicity")
    private String ethnicity;

    @TableField("native_place")
    private String nativePlace;

    @TableField("birth_order")
    private String birthOrder;

    @TableField("birth_location")
    private String birthLocation;

    @TableField("language_environment")
    private String languageEnvironment;

    @TableField("current_school")
    private String currentSchool;

    @TableField("home_address")
    private String homeAddress;

    @TableField("hobbies")
    private String hobbies;

    @TableField("interests")
    private String interests;

    @TableField("health_status")
    private String healthStatus;

    @TableField("health_description")
    private String healthDescription;

    @TableField("past_diseases")
    private String pastDiseases;

    @TableField("father_phone")
    private String fatherPhone;

    @TableField("mother_phone")
    private String motherPhone;

    @TableField("guardian_phone")
    private String guardianPhone;

    @TableField("is_current_operation")
    private Boolean isCurrentOperation;

    @TableField("created_time")
    private LocalDateTime createdTime;

    @TableField("updated_time")
    private LocalDateTime updatedTime;

}