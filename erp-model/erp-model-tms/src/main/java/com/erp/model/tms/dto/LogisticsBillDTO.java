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
 * 物流单请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
*/
@Data
@NoArgsConstructor
public class LogisticsBillDTO implements Serializable {


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO{
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabName;

        /**
         * 数量
         */
        private Integer count;
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
        * 销售平台
        */
        private String salesPlatform;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id 销售订单
        */
        private String sourceId;

        /**
        * 来源code
        */
        private String sourceCode;

        /**
        * 出库id
        */
        private String outstockId;

        /**
        * 出库code
        */
        private String outstockCode;

        /**
        * 渠道id
        */
        private String channelId;

        /**
        * 下单时间
        */
        private LocalDateTime orderTime;

        /**
        * 发货时间
        */
        private LocalDateTime deliveryTime;

        /**
        * 运输单号
        */
        private String transportNo;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 物流明细信息
         */
        @NotNull(message = "物流明细信息不能为空")
        private List<LogisticsBillDetailDTO.AddDTO> detailList;
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

        /**
         * 物流明细信息
         */
        @NotNull(message = "物流明细信息不能为空")
        private List<LogisticsBillDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 销售平台
        */
        @NotBlank(message = "销售平台不能为空")
        @Size(max = 30,message = "销售平台最大长度不能超过30位")
        private String salesPlatform;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 50,message = "店铺名称最大长度不能超过50位")
        private String shopName;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 30,message = "来源类型最大长度不能超过30位")
        private String sourceType;

        /**
        * 来源id 销售订单
        */
        @NotBlank(message = "来源id 销售订单不能为空")
        @Size(max = 19,message = "来源id 销售订单最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源code
        */
        @NotBlank(message = "来源code不能为空")
        @Size(max = 30,message = "来源code最大长度不能超过30位")
        private String sourceCode;

        /**
        * 出库id
        */
        @NotBlank(message = "出库id不能为空")
        @Size(max = 19,message = "出库id最大长度不能超过19位")
        private String outstockId;

        /**
        * 出库code
        */
        @NotBlank(message = "出库code不能为空")
        @Size(max = 30,message = "出库code最大长度不能超过30位")
        private String outstockCode;

        /**
        * 渠道id
        */
        @NotBlank(message = "渠道id不能为空")
        @Size(max = 19,message = "渠道id最大长度不能超过19位")
        private String channelId;

        /**
        * 下单时间
        */
        @NotNull(message = "下单时间不能为空")
        private LocalDateTime orderTime;

        /**
        * 发货时间
        */
        @NotNull(message = "发货时间不能为空")
        private LocalDateTime deliveryTime;

        /**
        * 运输单号
        */
        @NotBlank(message = "运输单号不能为空")
        @Size(max = 50,message = "运输单号最大长度不能超过50位")
        private String transportNo;


    }

    /**
     * 查询物流信息
     */
    @Data
    @NoArgsConstructor
    public static class LogisticsBillVo {
        /**
         * 店铺id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 运输单号
         */
        private String transportNo;
        /**
         * 运输状态
         */
        private String trackStatus;
        /**
         * 跟踪单号
         */
        private String trackNo;
    }

}