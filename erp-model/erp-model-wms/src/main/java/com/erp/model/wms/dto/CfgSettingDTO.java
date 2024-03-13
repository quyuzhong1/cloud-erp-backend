package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-01-08
*/
@Data
@NoArgsConstructor
public class CfgSettingDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {


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

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 委外发料单设置
         */
        @Valid
        private CfgSettingValueDTO.SubcontractIssueSettingDTO subcontractIssueSettingDTO;

        /**
         * 采购退货单设置
         */
        @Valid
        private CfgSettingValueDTO.PoReturnSettingDTO poReturnSettingDTO;

        /**
         * 采购对账单设置
         */
        @Valid
        private CfgSettingValueDTO.PoReconciliationSettingDTO poReconciliationSettingDTO;
        /**
         * 物流产品信息-目的国申报价
         */
        @Valid
        private List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> logisticsProductDestDeclarePrices;
    }


}