package com.erp.model.tms.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 中转报关详情请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
*/
@Data
@NoArgsConstructor
public class TransferDeclareDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 销售单id
        */
        private String soId;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道中文
        */
        private String logisticsChannelName;

        /**
        * 物流跟踪号
        */
        private String trackingNo;

        /**
        * 包裹重量
        */
        private BigDecimal packageWeight;

        /**
        * 包裹重量单位
        */
        private String weightUnit;

        /**
        * 出库状态
        */
        private String outstockStatus;

        /**
        * 中转状态
        */
        private String transferStaus;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 销售单id
        */
        @NotBlank(message = "销售单id不能为空")
        @Size(max = 19,message = "销售单id最大长度不能超过19位")
        private String soId;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 64,message = "销售单号最大长度不能超过64位")
        private String soCode;

        /**
        * 物流渠道id
        */
        @NotBlank(message = "物流渠道id不能为空")
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道中文
        */
        @NotBlank(message = "物流渠道中文不能为空")
        @Size(max = 255,message = "物流渠道中文最大长度不能超过255位")
        private String logisticsChannelName;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 255,message = "物流跟踪号最大长度不能超过255位")
        private String trackingNo;

        /**
        * 包裹重量
        */
        @NotNull(message = "包裹重量不能为空")
        @Digits(integer = 6, fraction = 4, message = "包裹重量整数位不能超过6位，小数位不能超过4位")
        private BigDecimal packageWeight;

        /**
        * 包裹重量单位
        */
        @NotBlank(message = "包裹重量单位不能为空")
        @Size(max = 10,message = "包裹重量单位最大长度不能超过10位")
        private String weightUnit;

        /**
        * 出库状态
        */
        @NotBlank(message = "出库状态不能为空")
        @Size(max = 30,message = "出库状态最大长度不能超过30位")
        private String outstockStatus;

        /**
        * 中转状态
        */
        @NotBlank(message = "中转状态不能为空")
        @Size(max = 30,message = "中转状态最大长度不能超过30位")
        private String transferStaus;


    }


}