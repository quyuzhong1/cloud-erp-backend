package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方仓发货单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
*/
@Data
@NoArgsConstructor
public class ThirdWarehouseDeliveryDTO implements Serializable {



    /**
     * 分页视图
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 表id
         */
        private String id;

        /**
         * 发货单号
         */
        private String code;

        /**
         * 平台
         */
        private String platform;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 平台订单号
         */
        private String platformOrderCode;
        /**
         * 三方仓订单号
         */
        private String thirdCode;
        /**
         * 状态
         */
        private String status;
        /**
         * 状态名
         */
        private String statusName;
        /**
         * 渠道id
         */
        private String channelId;
        /**
         * 渠道名称
         */
        private String channelName;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 库存sku
         */
        private String platformSkuNo;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 发货仓库Id
         */
        private String warehouseId;

        /**
         * 发货仓库名称
         */
        private String warehouseName;
        /**
         * 异常原因
         */
        private String abnormalProblemReason;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 发货时间
         */
        private LocalDate outstockDate;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList = new ArrayList<>();
        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
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
        * 三方仓出库单号
        */
        private String code;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 销售id
        */
        private String soId;

        /**
        * 平台
        */
        private String dictPlatform;

        /**
        * 平台订单号
        */
        private String platformCode;

        /**
        * 三方仓平台
        */
        private String thirdWarehousePlatform;


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
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 50,message = "销售单号最大长度不能超过50位")
        private String soCode;

        /**
        * 销售id
        */
        @NotBlank(message = "销售id不能为空")
        @Size(max = 50,message = "销售id最大长度不能超过50位")
        private String soId;

        /**
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 50,message = "平台最大长度不能超过50位")
        private String dictPlatform;

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 100,message = "平台订单号最大长度不能超过100位")
        private String platformCode;

        /**
        * 三方仓平台
        */
        @NotBlank(message = "三方仓平台不能为空")
        @Size(max = 255,message = "三方仓平台最大长度不能超过255位")
        private String thirdWarehousePlatform;


    }


}