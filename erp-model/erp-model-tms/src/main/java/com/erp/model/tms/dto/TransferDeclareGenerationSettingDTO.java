package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 预报设置-自动生成请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@NoArgsConstructor
public class TransferDeclareGenerationSettingDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String id;

        /**
        * 发货物流商id集合
        */
        private List<String> deliveryLogisticsSupplierIdList;

        /**
        * 中转物流商id
        */
        private String transferLogisticsSupplierId;

        /**
        * 中转渠道id
        */
        private String transferChannelId;
    }

    /**
     * 预报设置入参
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {
        /**
         * 主键id（报关设置id）
         */
        private String id;

        /**
         * 发货物流商id
         */
        @NotEmpty(message = "发货物流商id不能为空")
        private List<String> deliveryLogisticsSupplierIdList;

        /**
         * 中转物流商/中转渠道Id
         */
        @NotBlank(message = "中转渠道Id不能为空")
        private String transferChannelId;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class SaveOrUpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 中转渠道id
         */
        @NotBlank(message = "中转渠道id不能为空")
        @Size(max = 19,message = "中转渠道id最大长度不能超过19位")
        private String transferChannelId;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 中转物流商id
        */
        @NotBlank(message = "中转物流商id不能为空")
        @Size(max = 19,message = "中转物流商id最大长度不能超过19位")
        private String transferLogisticsSupplierId;

        /**
        * 中转物流服务商
        */
        @NotBlank(message = "中转物流服务商不能为空")
        @Size(max = 255,message = "中转物流服务商最大长度不能超过255位")
        private String transferLogisticsSupplierName;

        /**
        * 中转渠道id
        */
        @NotBlank(message = "中转渠道id不能为空")
        @Size(max = 19,message = "中转渠道id最大长度不能超过19位")
        private String transferChannelId;

        /**
        * 中转渠道中文
        */
        @NotBlank(message = "中转渠道中文不能为空")
        @Size(max = 255,message = "中转渠道中文最大长度不能超过255位")
        private String transferChannelName;


    }


}