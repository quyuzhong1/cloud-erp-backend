package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;

/**
 * 虚拟仓设置
 */
@Data
@NoArgsConstructor
public class CfgSettingVirtualDTO implements Serializable {


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
         * 销售看板
         */
        @Valid
        private CfgSettingVirtualValueDTO.SalesDashboardDTO salesDashboardDTO;

        /**
         * 缺货统计
         */
        @Valid
        private CfgSettingVirtualValueDTO.ReportOrderDemandDTO reportOrderDemandDTO;

        /**
         * 规则设置
         */
        @Valid
        private CfgSettingVirtualValueDTO.VirtualRuleDTO virtualRuleDTO;

    }


}