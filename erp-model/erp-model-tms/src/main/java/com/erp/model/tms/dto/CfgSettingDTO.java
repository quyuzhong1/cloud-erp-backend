package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-02-29
 */
@Data
@NoArgsConstructor
public class CfgSettingDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 物流产品信息-目的国申报价
         */
        @Valid
        private List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> logisticsProductDestDeclarePrices;

    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 物流产品信息-目的国申报价
         */
        @Valid
        private List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> logisticsProductDestDeclarePrices;

    }


}