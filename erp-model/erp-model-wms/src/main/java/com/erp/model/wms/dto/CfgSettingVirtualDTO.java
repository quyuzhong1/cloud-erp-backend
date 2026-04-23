package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

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

        /**
         * 虚拟仓调拨设置
         */
        @Valid
        private CfgSettingVirtualValueDTO.VirtualTransferSettingDTO virtualTransferSettingDTO;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchVirtualTransferResultDTO{

        /**
         * 是否匹配
         */
        private Boolean isMatch;

        /**
         * 调拨方向
         */
        private String transferDirection;

    }



    /**
     * 匹配中转规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchVirtualTransferRuleDTO{

        /**
         * 调入仓库
         */
        private String inWarehouseCode;

        /**
         * 调出仓库
         */
        private String outWarehouseCode;

    }

}