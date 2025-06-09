package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum MercadolibreBusinessModelEnum {
    MLB("MLB", "巴西（本土）", "br"),
    MLM("MLM", "墨西哥（本土）", "mx"),
    MLC("MLC", "智利（本土）", "cl"),
    MCO("MCO", "哥伦比亚（本土）", "co"),
    MLA("MLA", "阿根廷（本土）", "ar"),
    MPE("MPE", "秘鲁（本土）", "pe"),
    MLU("MLU", "乌拉圭（本土）", "uy"),
    MSV("MSV", "萨尔瓦多（本土）", "sv"),
    MCU("MCU", "古巴（本土）", "cu"),
    MEC("MEC", "厄瓜多尔（本土）", "ec"),
    MPA("MPA", "巴拿马（本土）", "pa"),
    MCR("MCR", "哥斯达黎加（本土）", "cr"),
    MHN("MHN", "洪都拉斯（本土）", "hn"),
    MGT("MGT", "危地马拉（本土）", "gt"),
    MRD("MRD", "多米尼加（本土）", "do"),
    MBO("MBO", "玻利维亚（本土）", "bo"),
    MPY("MPY", "巴拉圭（本土）", "py"),
    MLV("MLV", "委内瑞拉（本土）", "ve"),
    MNI("MNI", "尼加拉瓜（本土）", "ni");
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
     * 国家代号
     */
    private final String countryCode;


    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (MercadolibreBusinessModelEnum bankTypeEnum : MercadolibreBusinessModelEnum.values()) {
            if (code.equals(bankTypeEnum.getCode())) {
                return bankTypeEnum.getName();
            }
        }
        return "";
    }
}
