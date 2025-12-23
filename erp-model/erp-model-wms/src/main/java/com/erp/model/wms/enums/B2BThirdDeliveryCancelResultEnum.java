package com.erp.model.wms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.stream.Stream;

/**
 * @author Lambda
 * @Classname DeliverTypeEnum
 * @Description 发货类型枚举
 * @Date 2023-12-29 10:30
 * @Created by yl
 */
@Getter
public enum B2BThirdDeliveryCancelResultEnum implements EnumMessage {
    NEW("NEW","草稿"),
    SUBMIT("SUBMIT","已提交"),
    PROCESSED("PROCESSED","出库中"),
    WAIT_UPLOAD("WAIT_UPLOAD","待上传"),
    UPLOADED("UPLOADED","已上传"),
    BLOCK("BLOCK","订单拦截中"),
    EXCEPTION("EXCEPTION","出库异常"),
    SUCCESS("SUCCESS","已出库"),
    DISCARD_PROCESSED("DISCARD_PROCESSED","作废中"),
    DISCARD("DISCARD","已作废"),
    PROBLEM("PROBLEM","问题件"),
    ;

    B2BThirdDeliveryCancelResultEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
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

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * 通过code查询
     * DeliverTypeEnum
     * 枚举
     */
    public static B2BThirdDeliveryCancelResultEnum getByCode(String code) {
        return Stream.of(B2BThirdDeliveryCancelResultEnum.values())
                .filter(e -> e.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
