package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 三方渠道表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-12-11
*/
@Data
@NoArgsConstructor
public class ThirdLogisticsDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class PageSelectDTO {
        /**
         * 主键id
         */
        private String thirdId;
        /**
         * 系统类型：lingxing领星
         */
        private String sysType;
        /**
         * 物流商名称
         */
        private String thirdLogisticsSupplierName;
        /**
         * 第三方渠道
         */
        private String thirdLogisticsId;

        /**
         * 第三方渠道名称
         */
        private String thirdLogisticsName;
        /**
         * disabled
         */
        private Boolean disabled;
    }

    /**
     * 远程搜索
     */
    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 系统类型
         */
        @NotBlank(message = "系统类型不能为空")
        private String sysType;

        /**
         * 关键词
         */
        private String searchKeyword;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 是否禁用/停用 true 是 false 不是
        */
        private Boolean disabled;

        /**
        * 平台类型：lingxing领星
        */
        private String platformType;

        /**
        * 物流商类型
        */
        private String type;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流商名称
        */
        private String logisticsSupplierName;

        /**
        * 物流渠道Id
        */
        private String channelId;

        /**
        * 物流渠道名称
        */
        private String channelName;

        /**
        * 平台更新时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;


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
        * 是否禁用/停用 true 是 false 不是
        */
        @NotNull(message = "是否禁用/停用 true 是 false 不是不能为空")
        private Boolean disabled;

        /**
        * 平台类型：lingxing领星
        */
        @NotBlank(message = "平台类型：lingxing领星不能为空")
        @Size(max = 19,message = "平台类型：lingxing领星最大长度不能超过19位")
        private String platformType;

        /**
        * 物流商类型
        */
        @NotBlank(message = "物流商类型不能为空")
        @Size(max = 255,message = "物流商类型最大长度不能超过255位")
        private String type;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 255,message = "物流商id最大长度不能超过255位")
        private String logisticsSupplierId;

        /**
        * 物流商名称
        */
        @NotBlank(message = "物流商名称不能为空")
        @Size(max = 255,message = "物流商名称最大长度不能超过255位")
        private String logisticsSupplierName;

        /**
        * 物流渠道Id
        */
        @NotBlank(message = "物流渠道Id不能为空")
        @Size(max = 255,message = "物流渠道Id最大长度不能超过255位")
        private String channelId;

        /**
        * 物流渠道名称
        */
        @NotBlank(message = "物流渠道名称不能为空")
        @Size(max = 255,message = "物流渠道名称最大长度不能超过255位")
        private String channelName;

        /**
        * 平台更新时间
        */
        private LocalDateTime platformUpdateTime;

        /**
        * 平台创建时间
        */
        private LocalDateTime platformCreateTime;


    }


}