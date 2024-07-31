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
 * 中台销售订单出库库位详情请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-07-30
*/
@Data
@NoArgsConstructor
public class DmpLogisticInfoDTO implements Serializable {




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
        * 主id
        */
        private String mainId;

        /**
        * 物流单号
        */
        private String logisticsNo;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流服务商
        */
        private String logisticsServiceName;

        /**
        * 物流类型代码
        */
        private String logisticsTypeCode;

        /**
        * 发货状态
        */
        private String receiveStatus;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 主id
        */
        @NotBlank(message = "主id不能为空")
        @Size(max = 255,message = "主id最大长度不能超过255位")
        private String mainId;

        /**
        * 物流单号
        */
        @NotBlank(message = "物流单号不能为空")
        @Size(max = 255,message = "物流单号最大长度不能超过255位")
        private String logisticsNo;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 物流服务商
        */
        @NotBlank(message = "物流服务商不能为空")
        @Size(max = 255,message = "物流服务商最大长度不能超过255位")
        private String logisticsServiceName;

        /**
        * 物流类型代码
        */
        @NotBlank(message = "物流类型代码不能为空")
        @Size(max = 255,message = "物流类型代码最大长度不能超过255位")
        private String logisticsTypeCode;

        /**
        * 发货状态
        */
        @NotBlank(message = "发货状态不能为空")
        @Size(max = 50,message = "发货状态最大长度不能超过50位")
        private String receiveStatus;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}