package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流-第三方渠道关系表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
*/
@Data
@NoArgsConstructor
public class LogisticsThirdChannelRefDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 是否推送电话
        */
        private Boolean isPushMobile;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
        private String logisticsChannelName;

        /**
        * 渠道代码
        */
        private String logisticsChannelCode;

        /**
        * 第三方物流商编码
        */
        private String thirdSupplierCode;

        /**
        * 第三方物流商名称
        */
        private String thirdSupplierName;


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
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 是否推送电话
        */
        @NotNull(message = "是否推送电话不能为空")
        private Boolean isPushMobile;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 渠道名称
        */
        @NotBlank(message = "渠道名称不能为空")
        @Size(max = 100,message = "渠道名称最大长度不能超过100位")
        private String logisticsChannelName;

        /**
        * 渠道代码
        */
        @NotBlank(message = "渠道代码不能为空")
        @Size(max = 50,message = "渠道代码最大长度不能超过50位")
        private String logisticsChannelCode;

        /**
        * 第三方物流商编码
        */
        @NotBlank(message = "第三方物流商编码不能为空")
        @Size(max = 50,message = "第三方物流商编码最大长度不能超过50位")
        private String thirdSupplierCode;

        /**
        * 第三方物流商名称
        */
        @NotBlank(message = "第三方物流商名称不能为空")
        @Size(max = 100,message = "第三方物流商名称最大长度不能超过100位")
        private String thirdSupplierName;


    }


}