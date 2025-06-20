package com.erp.model.oms.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum MercadolibreBusinessModelEnum {
    MLB("MLB", "巴西（本土）", "br", "https://auth.mercadolivre.com.br/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MLM("MLM", "墨西哥（本土）", "mx", "https://auth.mercadolibre.com.mx/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MLC("MLC", "智利（本土）", "cl", "https://auth.mercadolibre.com.cl/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MCO("MCO", "哥伦比亚（本土）", "co", "https://auth.mercadolibre.com.co/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MLA("MLA", "阿根廷（本土）", "ar", "https://auth.mercadolibre.com.ar/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MPE("MPE", "秘鲁（本土）", "pe", "https://auth.mercadolibre.com.pe/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MLU("MLU", "乌拉圭（本土）", "uy", "https://auth.mercadolibre.com.uy/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MSV("MSV", "萨尔瓦多（本土）", "sv", "https://auth.mercadolibre.com.sv/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MCU("MCU", "古巴（本土）", "cu", "https://auth.mercadolibre.com.cu/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MEC("MEC", "厄瓜多尔（本土）", "ec", "https://auth.mercadolibre.com.ec/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MPA("MPA", "巴拿马（本土）", "pa", "https://auth.mercadolibre.com.pa/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MCR("MCR", "哥斯达黎加（本土）", "cr", "https://auth.mercadolibre.com.cr/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MHN("MHN", "洪都拉斯（本土）", "hn", "https://auth.mercadolibre.com.hn/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MGT("MGT", "危地马拉（本土）", "gt", "https://auth.mercadolibre.com.gt/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MRD("MRD", "多米尼加（本土）", "do", "https://auth.mercadolibre.com.do/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MBO("MBO", "玻利维亚（本土）", "bo", "https://auth.mercadolibre.com.bo/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MPY("MPY", "巴拉圭（本土）", "py", "https://auth.mercadolibre.com.py/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MLV("MLV", "委内瑞拉（本土）", "ve", "https://auth.mercadolibre.com.ve/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s"),
    MNI("MNI", "尼加拉瓜（本土）", "ni", "https://auth.mercadolibre.com.ni/authorization?response_type=code&client_id=%s&redirect_uri=%s&state=%s");
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

    /**
     * 授权路径
     */
    private final String authUrl;


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
