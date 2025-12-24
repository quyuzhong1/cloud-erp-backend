package com.erp.model.wms.dto;

import java.time.LocalDate;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
*/
@Data
@NoArgsConstructor
public class AwdOutstockDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }


     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 明细id
         */
        private String  detailId;

        /**
         * 货件单号
         */
        private String code;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;

        /**
         * 发货日期
         */
        private LocalDate billDate;

        /**
         * FBA货件id
         */
        private String fbaShipmentId;

        /**
         * FBA货件号
         */
        private String fbaShipmentCode;

        /**
         * asin
         */
        private String asin;

        /**
         * msku
         */
        private String msku;

        /**
         * fnsku
         */
        private String fnsku;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String productName;

        /**
         * 发货数量
         */
        private Integer qty;

        /**
         * 更新人id
         */
        private String updateUserId;

        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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

        private String code;

        private String shopId;

        private String shopName;

        private LocalDate billDate;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        private List<AwdOutstockDetailDTO.AddDTO> awdDetailList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 发货时间
         */
        @NotNull(message = "发货时间不能为空")
        private LocalDate billDate;

    }

    @Data
    @NoArgsConstructor
    public static class BatchUpdateBillDateViewDTO{

        /**
         * id
         */
        private String id;
        /**
         * AWD出库单号
         */
        private String code;

        /**
         * FBA货件id
         */
        private String fbaShipmentId;

        /**
         * FBA货件编码
         */
        private String fbaShipmentCode;

        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;


    }

    @Data
    @NoArgsConstructor
    public static class GenerateDeliveryDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * AWD出库货件单号
         */
        @NotBlank(message = "编码不能为空")
        private String code;

        /**
         * 店铺id
         */
        @NotBlank(message = "店铺id不能为空")
        private String shopId;

        /**
         * 店铺名称
         */
        @NotBlank(message = "店铺名称不能为空")
        private String shopName;

        /**
         * 发货时间
         */
        @NotNull(message = "发货时间不能为空")
        private LocalDate billDate;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        private String shopId;

        private String shopName;

        private LocalDate billDate;

        private String fbaShipmentId;

        private String fbaShipmentCode;
    }

}