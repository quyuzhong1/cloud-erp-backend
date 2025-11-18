package com.erp.model.oms.dto;

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
 * B2B订单面单表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-02-24
*/
@Data
@NoArgsConstructor
public class SoLabelDTO implements Serializable {




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
        * 订单主表id
        */
        private String mainId;

        /**
        * 平台物流面单base64
        */
        private String logisticsLabelBase64;

        /**
        * 来源类型
        */
        private String sourceType;


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
        * 订单主表id
        */
        @NotBlank(message = "订单主表id不能为空")
        @Size(max = 19,message = "订单主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 平台物流面单base64
        */
        @NotBlank(message = "平台物流面单base64不能为空")
        private String logisticsLabelBase64;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 255,message = "来源类型最大长度不能超过255位")
        private String sourceType;


    }


    @Data
    @NoArgsConstructor
    public static class PrintLabelDTO {
        //发货通知单id
        private String id;
        //发货通知单号
        private String code;
        //销售单id
        private String soId;
        //销售单号
        private String soCode;
        //客户名称
        private String customerName;
        //文件编码
        private String logisticsLabelUrl;
        //是否存在物流单
        private Boolean hasLabel;
    }
}