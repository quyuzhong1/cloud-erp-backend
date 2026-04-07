package com.erp.model.dmp.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 输入信息 输入维度类型 枚举
 * </p>
 *
 */
@Getter
public enum DmpInputNextLevelType implements EnumMessage {

	PLATFORM_STATUS("platformStatus", "平台单据状态","中台调度维度:平台单据状态"),
	AUTH_ID("authId", "海外仓授权ID","中台调度维度:wms.overseas_provider.id"),
	SHOP_ID("shopId", "店铺ID","中台调度维度:oms.shop_info.id"),
    OMS_ACCOUNT("omsAccount", "店铺账号","中台调度维度:相同的店铺账号platform_shop_code，next_level_id=其中一个店铺ID"),
    ;

    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private final String code;
    /**
     * 名称
     */
    private final String name;
    /**
     * 备注
     */
    private final String remark;

    DmpInputNextLevelType(String code, String name, String remark) {
        this.code = code;
        this.name = name;
        this.remark = remark;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (DmpInputNextLevelType statusEnum : DmpInputNextLevelType.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
}
