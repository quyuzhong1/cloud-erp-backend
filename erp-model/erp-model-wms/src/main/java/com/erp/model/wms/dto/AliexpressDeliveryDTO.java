package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 速卖通发货单请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-26
*/
@Data
@NoArgsConstructor
public class AliexpressDeliveryDTO implements Serializable {




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
        * 平台订单号
        */
        private String platformCode;

        /**
        * 销售单id
        */
        private String soId;

        /**
        * 销售单编号
        */
        private String soCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 物流跟踪号
        */
        private String trackNo;

        /**
        * 订单创建时间
        */
        private LocalDateTime tradeCreateTime;

        /**
        * 订单出库时间
        */
        private LocalDateTime outBoundTime;

        /**
        * 平台发货仓库
        */
        private String warehouseName;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        private List<AliexpressDeliveryDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 平台订单号
        */
        @NotBlank(message = "平台订单号不能为空")
        @Size(max = 64,message = "平台订单号最大长度不能超过64位")
        private String platformCode;

        /**
        * 销售单id
        */
        @NotBlank(message = "销售单id不能为空")
        @Size(max = 64,message = "销售单id最大长度不能超过64位")
        private String soId;

        /**
        * 销售单编号
        */
        @NotBlank(message = "销售单编号不能为空")
        @Size(max = 255,message = "销售单编号最大长度不能超过255位")
        private String soCode;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 64,message = "店铺id最大长度不能超过64位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 255,message = "店铺名称最大长度不能超过255位")
        private String shopName;

        /**
        * 物流跟踪号
        */
        @NotBlank(message = "物流跟踪号不能为空")
        @Size(max = 255,message = "物流跟踪号最大长度不能超过255位")
        private String trackNo;

        /**
        * 订单创建时间
        */
        private LocalDateTime tradeCreateTime;

        /**
        * 订单出库时间
        */
        private LocalDateTime outBoundTime;

        /**
        * 平台发货仓库
        */
        private String warehouseName;


    }

    /**
     * 列表信息
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;

        /**
         * 明细id
         */
        private String detailId;
        /**
         * 平台订单号
         */
        private String platformCode;
        /**
         * 销售单id
         */
        private String soId;
        /**
         * 销售单编号
         */
        private String soCode;
        /**
         * 店铺Id
         */
        private String shopId;
        /**
         * 店铺名称
         */
        private String shopName;
        /**
         * 物流跟踪号
         */
        private String trackNo;
        /**
         * 订单创建时间
         */
        private LocalDateTime tradeCreateTime;
        /**
         * 订单出库时间
         */
        private LocalDateTime outBoundTime;
        /**
         * 平台发货仓库
         */
        private String warehouseName;

        /**
         * 平台SKU
         */
        private String platformSku;

        /**
         * 系统SKU
         */
        private String skuNo;

        /**
         * 数量
         */
        private String qty;

        /**
         * 系统已出库
         */
        private Boolean isSystemOut;
    }

    /**
     * 列表查询条件
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO implements Serializable{
        private static final long serialVersionUID = 1905122041950251207L;
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }
}