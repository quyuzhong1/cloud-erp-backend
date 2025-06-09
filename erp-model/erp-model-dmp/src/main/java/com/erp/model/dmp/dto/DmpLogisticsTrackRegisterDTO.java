package com.erp.model.dmp.dto;

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
 * 物流注册表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-11-12
*/
@Data
@NoArgsConstructor
public class DmpLogisticsTrackRegisterDTO implements Serializable {




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
        * 跟踪号
        */
        private String trackNo;

        /**
        * 运单号
        */
        private String transportNo;

        /**
        * 轨迹查询类型（运单号transportNo跟踪号trackNo）
        */
        private String trackQueryType;

        /**
        * 渠道id
        */
        private String channelId;

        /**
        * 平台订单号
        */
        private String platformOrderNo;

        /**
        * 船司/航司
        */
        private String carrierId;

        /**
        * 电话
        */
        private String telNumber;


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
        * 跟踪号
        */
        @NotBlank(message = "跟踪号不能为空")
        @Size(max = 255,message = "跟踪号最大长度不能超过255位")
        private String trackNo;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 255,message = "运单号最大长度不能超过255位")
        private String transportNo;

        /**
        * 轨迹查询类型（运单号transportNo跟踪号trackNo）
        */
        @NotBlank(message = "轨迹查询类型（运单号transportNo跟踪号trackNo）不能为空")
        @Size(max = 50,message = "轨迹查询类型（运单号transportNo跟踪号trackNo）最大长度不能超过50位")
        private String trackQueryType;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 19,message = "渠道id最大长度不能超过19位")
        private String channelId;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 255,message = "平台订单号最大长度不能超过255位")
        private String platformOrderNo;

        /**
        * 船司/航司
        */
        @NotBlank(message = "船司/航司不能为空")
        @Size(max = 30,message = "船司/航司最大长度不能超过30位")
        private String carrierId;

        /**
        * 电话
        */
        @NotBlank(message = "电话不能为空")
        @Size(max = 50,message = "电话最大长度不能超过50位")
        private String telNumber;


    }


}