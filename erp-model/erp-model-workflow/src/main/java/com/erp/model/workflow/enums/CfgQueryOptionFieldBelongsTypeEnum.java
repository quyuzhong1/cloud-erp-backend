package com.erp.model.workflow.enums;

import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 字段所属单据类型 枚举
 * </p>
 *
 * @author hcg
 * @since 2025-05-15 12:14:18
 */
public enum CfgQueryOptionFieldBelongsTypeEnum implements EnumMessage {
	MAIN("main", "主表"),
	DETAIL("detailList", "明细"),
	COMMON("common", "共用"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    CfgQueryOptionFieldBelongsTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgQueryOptionFieldBelongsTypeEnum statusEnum : CfgQueryOptionFieldBelongsTypeEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }

    /**
     * 是否是主表数据
     */
    public static Boolean isFieldMain(String code) {
       if (CharSequenceUtil.isBlank(code) || CharSequenceUtil.equals(code,MAIN.getCode()) || CharSequenceUtil.equals(code,COMMON.getCode())) {
           return Boolean.TRUE;
       }
        return Boolean.FALSE;
    }
}
