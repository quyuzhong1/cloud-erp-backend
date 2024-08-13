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
 * 请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
*/
@Data
@NoArgsConstructor
public class DmpLogisticsTrackDTO implements Serializable {




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
        * 承运商编码
        */
        private String courierCode;

        /**
        * 承运商名称
        */
        private String courierName;

        /**
        * 运单号
        */
        private String trackNo;

        /**
        * 运单时间
        */
        private LocalDateTime trackTime;

        /**
        * 状态 notFind  查询不到 waitCollect 等待揽收trackIng 运输途中 arriveWaitTake 到达待取deliveryIng 派送途中 deliveryFail 投递失败
sign 成功签收 maybeException 可能异常transportLong  运输过久
        */
        private String status;

        /**
        * 内容
        */
        private String content;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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
        * 承运商编码
        */
        @NotBlank(message = "承运商编码不能为空")
        @Size(max = 255,message = "承运商编码最大长度不能超过255位")
        private String courierCode;

        /**
        * 承运商名称
        */
        @NotBlank(message = "承运商名称不能为空")
        @Size(max = 255,message = "承运商名称最大长度不能超过255位")
        private String courierName;

        /**
        * 运单号
        */
        @NotBlank(message = "运单号不能为空")
        @Size(max = 255,message = "运单号最大长度不能超过255位")
        private String trackNo;

        /**
        * 运单时间
        */
        private LocalDateTime trackTime;

        /**
        * 状态 notFind  查询不到 waitCollect 等待揽收trackIng 运输途中 arriveWaitTake 到达待取deliveryIng 派送途中 deliveryFail 投递失败
sign 成功签收 maybeException 可能异常transportLong  运输过久
        */
        @NotBlank(message = "状态")
        @Size(max = 255,message = "状态最大长度不能超过255位")
        private String status;

        /**
        * 内容
        */
        @NotBlank(message = "内容不能为空")
        @Size(max = 500,message = "内容最大长度不能超过500位")
        private String content;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;

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


    }


}