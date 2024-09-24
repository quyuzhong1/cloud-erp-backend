package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

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
         * 申报规则
         */
        @Valid
        private List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> logisticsProductDestDeclarePrices;

        /**
         * 通知管理
         */
        @Valid
        private CfgSettingValueDTO.NoticeDTO noticeDTO;

        /**
         * 生成设置
         */
        @Valid
        private CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO;


        /**
         * 单据生成
         */
        @Valid
        private CfgSettingValueDTO.BillAutoAddDTO billAutoAddDTO;
        /**
         * 分摊设置
         */
        @Valid
        private CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO;
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
         * 申报规则
         */
        @Valid
        private List<CfgSettingValueDTO.LogisticsProductDestDeclarePrice> logisticsProductDestDeclarePrices;

        /**
         * 通知管理
         */
        @Valid
        private CfgSettingValueDTO.NoticeDTO noticeDTO;

        /**
         * 生成设置
         */
        @Valid
        private CfgSettingValueDTO.ReconciliationCycleDTO reconciliationCycleDTO;


        /**
         * 单据生成
         */
        @Valid
        private CfgSettingValueDTO.BillAutoAddDTO billAutoAddDTO;
        /**
         * 费用分摊
         */
        @Valid
        private CfgSettingValueDTO.AllocationSettingDTO allocationSettingDTO;
    }


}