package com.erp.model.tms.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;

/**
 * @description: track123对接请求、响应DTO
 * @author Will
 * @date: 2024/4/8 11:38
 */
@Data
@NoArgsConstructor
public class LogisticsTrackBaseDTO implements Serializable {


    /**
     * 海运轨迹查询请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    public static class OceanTrackRequestDTO {

        /**
         * 唯一订单号(由track123生成的订单编号，从注册接口的响应参数获取)
         */
        @NotBlank(message = "订单号不能为空")
        private String orderNo;

        /**
         * 查询单号
         */
        @NotBlank(message = "跟踪单号不能为空")
        private String trackingNo;

        /**
         * 单号类型（1.订舱号 2.提单号 3.箱号）
         */
        @NotNull(message = "单号类型不能为空")
        private Integer type;

        /**
         * 授权信息
         */
        private Map<String, String> authMap;
    }

    /**
     * 海运轨迹查询响应DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    public static class OceanTrackResponseDTO {

        /**
         * 查询单号
         */
        private String trackingNo;

        /**
         * 运输状态
         */
        private String transitStatus;

        /**
         * 轨迹发生的时间
         */
        private String eventTime;

        /**
         * 轨迹发生的时间
         */
        private String eventDetail;


    }

    /**
     * 海运注册请求DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    public static class OceanRegisterRequestDTO {

        /**
         * 物流明细id
         */
        @NotBlank(message = "物流明细id")
        private String id;

        /**
         * 跟踪单号
         */
        @NotBlank(message = "跟踪单号不能为空")
        private String trackNo;

        /**
         * 单号类型（1.订舱号 2.提单号 3.箱号）
         */
        @NotNull(message = "单号类型不能为空")
        private Integer type ;

        /**
         * 客户邮箱,由商家/平台填写的,订单关联的客户邮箱
         */
        private String customerEmail ;

        /**
         * 授权信息
         */
        private Map<String, String> authMap;
    }


}