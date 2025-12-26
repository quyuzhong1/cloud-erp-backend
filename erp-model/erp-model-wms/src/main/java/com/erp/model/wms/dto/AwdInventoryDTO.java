package com.erp.model.wms.dto;

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
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-12-26
*/
@Data
@NoArgsConstructor
public class AwdInventoryDTO implements Serializable {



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
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 平台产品id
        */
        private String asin;

        /**
        * 平台sku
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
        * 产品名称
        */
        private String productName;

        /**
        * AWD在库
        */
        private Integer totalOnhandQty;

        /**
        * AWD可用
        */
        private Integer availableDistributableQty;

        /**
        * AWD发FBA在途
        */
        private Integer replenishmentQty;

        /**
        * AWD待发货
        */
        private Integer reservedDistributableQty;

        /**
        * 发AWD在途
        */
        private Integer totalInboundQty;

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

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName ;

        /**
        * 平台产品id
        */
        private String asin;

        /**
        * 平台sku
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
        * 产品名称
        */
        private String productName;

        /**
        * AWD在库
        */
        private Integer totalOnhandQty;

        /**
        * AWD可用
        */
        private Integer availableDistributableQty;

        /**
        * AWD发FBA在途
        */
        private Integer replenishmentQty;

        /**
        * AWD待发货
        */
        private Integer reservedDistributableQty;

        /**
        * 发AWD在途
        */
        private Integer totalInboundQty;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        private String warehouseName ;

        /**
        * 平台产品id
        */
        @NotBlank(message = "平台产品id不能为空")
        private String asin;

        /**
        * 平台sku
        */
        @NotBlank(message = "平台sku不能为空")
        private String msku;

        /**
        * fnsku
        */
        @NotBlank(message = "fnsku不能为空")
        private String fnsku;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * sku编码
         */
        @NotBlank(message = "skuNo不能为空")
        private String skuNo;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        private String productName;

        /**
        * AWD在库
        */
        @NotNull(message = "AWD在库不能为空")
        private Integer totalOnhandQty;

        /**
        * AWD可用
        */
        @NotNull(message = "AWD可用不能为空")
        private Integer availableDistributableQty;

        /**
        * AWD发FBA在途
        */
        @NotNull(message = "AWD发FBA在途不能为空")
        private Integer replenishmentQty;

        /**
        * AWD待发货
        */
        @NotNull(message = "AWD待发货不能为空")
        private Integer reservedDistributableQty;

        /**
        * 发AWD在途
        */
        @NotNull(message = "发AWD在途不能为空")
        private Integer totalInboundQty;


    }


}