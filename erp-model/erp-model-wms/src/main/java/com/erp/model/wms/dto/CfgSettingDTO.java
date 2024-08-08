package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;

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
         * 飞书通知配置
         */
        private CfgSettingValueDTO.FsQcNoticeDTO fsQcNoticeDTO;
        /**
         * 发货拦截设置
         */
        private CfgSettingValueDTO.B2cDeliveryInterceptDTO b2cDeliveryInterceptDTO;
        /**
         * 组包设置
         */
        private CfgSettingValueDTO.PackageSettingDTO packageSettingDTO;
        /**
         * 装箱完成通知
         */
        private CfgSettingValueDTO.FinishPackingNoticeDTO finishPackingNoticeDTO;
        /**
         * 打印配置
         */
        private CfgSettingValueDTO.CfgPrint cfgPrint = new CfgSettingValueDTO.CfgPrint();

        /**
         * 中转设置
         */
        private CfgSettingValueDTO.TransitSettingDTO transitSettingDTO;

    }


}