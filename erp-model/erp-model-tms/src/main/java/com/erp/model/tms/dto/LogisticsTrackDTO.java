package com.erp.model.tms.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流轨迹表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
*/
@Data
@NoArgsConstructor
public class LogisticsTrackDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 物流单号
         */
        private String trackNo;

        /**
         * 详情
         */
        List<ListDTO> list;



    }




    /**
     * list
     */
    @Data
    public static class ListDTO{

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否最新
         */
        private Boolean isLatest;

        /**
         * 运单号
         */
        private String trackNo;

        /**
         * 运单时间
         */
        private LocalDateTime trackTime;

        /**
         * 状态
         */
        private String status;

        /**
         * 内容
         */
        private String content;

        /**
         * 状态名
         */
        private String statusName;
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

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateTrackDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 跟踪号
         */
        @NotBlank(message = "跟踪号不能为空")
        @Size(max = 30,message = "跟踪号最大长度不能超过30位")
        private String trackNo;
        /**
         * 运单号
         */
        private String transportNo;
        /**
         * 轨迹查询单号（运单号transportNo跟踪号trackNo）
         * TrackQueryTypeEnum
         * 字典接口地址  http://172.16.100.11:3002/project/128/interface/api/25522   key = trackQueryType
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
         * 船司/航司编码
         */
        private String carrierCode;
        /**
         * 电话
         */
        private String telNumber;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 30,message = "运单号最大长度不能超过30位")
        private String trackNo;

        /**
        * 运单时间
        */
        @NotNull(message = "运单时间不能为空")
        private LocalDateTime trackTime;

        /**
        * 状态
        */
        @NotBlank(message = "状态不能为空")
        @Size(max = 30,message = "状态最大长度不能超过30位")
        private String status;

        /**
        * 内容
        */
        @NotBlank(message = "内容不能为空")
        @Size(max = 200,message = "内容最大长度不能超过200位")
        private String content;


    }

    @Data
    @NoArgsConstructor
    public static class TrackWebHookDTO {
        private Track123DTO data;
        private Verify verify;
    }
    @Data
    @NoArgsConstructor
    public static class Verify {
        private String signature;
        private String timestamp;
    }
    @Data
    @NoArgsConstructor
    public static class Track123DTO {
        private LocalDateTime createTime;
        private Integer deliveredDays;
        private LocalDateTime deliveredTime;
        private Long id;
        private LocalDateTime lastTrackingTime;
        private LocalLogisticsInfo localLogisticsInfo;
        private LocalDateTime orderTime;
        private Integer receiptDays;
        private LocalDateTime receiptTime;
        private String shipFrom;
        private LocalDateTime shipTime;
        private String shipTo;
        private String trackNo;
        private String transitStatus;
        private String transitSubStatus;
    }

    @Data
    @NoArgsConstructor
    public static class LocalLogisticsInfo {
        private String courierCode;
        private String courierHomePage;
        private String courierNameCN;
        private String courierNameEN;
        private List<TrackingDetail> trackingDetails;
    }

    @Data
    @NoArgsConstructor
    public static class TrackingDetail {
        private String address;
        private String eventDetail;
        private LocalDateTime eventTime;
        private String transitSubStatus;
        /**
         * 用于计算数据唯一值
         */
        private String md5;
    }
}