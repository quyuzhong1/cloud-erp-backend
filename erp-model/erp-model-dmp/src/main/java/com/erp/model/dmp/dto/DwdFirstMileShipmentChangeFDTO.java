package com.erp.model.dmp.dto;

import java.util.Date;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * DWD头程发货签收变更记录(包含期初/调整)请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-11-28
*/
@Data
@NoArgsConstructor
public class DwdFirstMileShipmentChangeFDTO implements Serializable {



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
        * 来源系统：amazon
        */
        private String sourceSystem;

        /**
        * 来源平台
        */
        private String sourcePlatform;

        /**
        * 平台账号编码
        */
        private String accountCode;

        /**
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对周期(时间格式)
        */
        private Date checkMonthQuery;

        /**
        * 店铺ID/授权ID
        */
        private String nextLevelId;

        /**
        * 业务类型:firstMileInit=期初,firstMileAdjust=调整
        */
        private String billTopic;

        /**
        * 平台货件ID
        */
        private String platformShipmentId;

        /**
        * 平台货件单号
        */
        private String platformShipmentCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 客户id
        */
        private String customerId;

        /**
        * 客户姓名
        */
        private String customerName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 目的仓库id
        */
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        private String warehouseName;

        /**
        * 在途仓库id
        */
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        private String platformStockSku;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * ERP SKU编码
        */
        private String skuNo;

        /**
        * 变更数量
        */
        private Integer changeQty;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

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
        * 来源系统：amazon
        */
        private String sourceSystem;

        /**
        * 来源平台
        */
        private String sourcePlatform;

        /**
        * 平台账号编码
        */
        private String accountCode;

        /**
        * 核对周期
        */
        private String checkMonth;

        /**
        * 核对周期(时间格式)
        */
        private Date checkMonthQuery;

        /**
        * 店铺ID/授权ID
        */
        private String nextLevelId;

        /**
        * 业务类型:firstMileInit=期初,firstMileAdjust=调整
        */
        private String billTopic;

        /**
        * 平台货件ID
        */
        private String platformShipmentId;

        /**
        * 平台货件单号
        */
        private String platformShipmentCode;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 店铺名称
        */
        private String shopName;

        /**
        * 客户id
        */
        private String customerId;

        /**
        * 客户姓名
        */
        private String customerName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 目的仓库id
        */
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        private String warehouseName;

        /**
        * 在途仓库id
        */
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        private String platformStockSku;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * ERP SKU编码
        */
        private String skuNo;

        /**
        * 变更数量
        */
        private Integer changeQty;


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
        * 来源系统：amazon
        */
        @NotBlank(message = "来源系统：amazon不能为空")
        @Size(max = 192,message = "来源系统：amazon最大长度不能超过192位")
        private String sourceSystem;

        /**
        * 来源平台
        */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 192,message = "来源平台最大长度不能超过192位")
        private String sourcePlatform;

        /**
        * 平台账号编码
        */
        @NotBlank(message = "平台账号编码不能为空")
        @Size(max = 192,message = "平台账号编码最大长度不能超过192位")
        private String accountCode;

        /**
        * 核对周期
        */
        @NotBlank(message = "核对周期不能为空")
        @Size(max = 50000,message = "核对周期最大长度不能超过50,000位")
        private String checkMonth;

        /**
        * 核对周期(时间格式)
        */
        private Date checkMonthQuery;

        /**
        * 店铺ID/授权ID
        */
        @NotBlank(message = "店铺ID/授权ID不能为空")
        @Size(max = 192,message = "店铺ID/授权ID最大长度不能超过192位")
        private String nextLevelId;

        /**
        * 业务类型:firstMileInit=期初,firstMileAdjust=调整
        */
        @NotBlank(message = "业务类型:firstMileInit=期初,firstMileAdjust=调整不能为空")
        @Size(max = 192,message = "业务类型:firstMileInit=期初,firstMileAdjust=调整最大长度不能超过192位")
        private String billTopic;

        /**
        * 平台货件ID
        */
        @NotBlank(message = "平台货件ID不能为空")
        @Size(max = 192,message = "平台货件ID最大长度不能超过192位")
        private String platformShipmentId;

        /**
        * 平台货件单号
        */
        @NotBlank(message = "平台货件单号不能为空")
        @Size(max = 384,message = "平台货件单号最大长度不能超过384位")
        private String platformShipmentCode;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 192,message = "店铺id最大长度不能超过192位")
        private String shopId;

        /**
        * 店铺名称
        */
        @NotBlank(message = "店铺名称不能为空")
        @Size(max = 384,message = "店铺名称最大长度不能超过384位")
        private String shopName;

        /**
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 192,message = "客户id最大长度不能超过192位")
        private String customerId;

        /**
        * 客户姓名
        */
        @NotBlank(message = "客户姓名不能为空")
        @Size(max = 384,message = "客户姓名最大长度不能超过384位")
        private String customerName;

        /**
        * 货件状态
        */
        @NotBlank(message = "货件状态不能为空")
        @Size(max = 192,message = "货件状态最大长度不能超过192位")
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private Date shipmentCreateTime;

        /**
        * 目的仓库id
        */
        @NotBlank(message = "目的仓库id不能为空")
        @Size(max = 192,message = "目的仓库id最大长度不能超过192位")
        private String warehouseId;

        /**
        * 目的仓库名称
        */
        @NotBlank(message = "目的仓库名称不能为空")
        @Size(max = 384,message = "目的仓库名称最大长度不能超过384位")
        private String warehouseName;

        /**
        * 在途仓库id
        */
        @NotBlank(message = "在途仓库id不能为空")
        @Size(max = 192,message = "在途仓库id最大长度不能超过192位")
        private String intransitWarehouseId;

        /**
        * 在途仓库名称
        */
        @NotBlank(message = "在途仓库名称不能为空")
        @Size(max = 384,message = "在途仓库名称最大长度不能超过384位")
        private String intransitWarehouseName;

        /**
        * 平台产品id（ASIN）
        */
        @NotBlank(message = "平台产品id（ASIN）不能为空")
        @Size(max = 192,message = "平台产品id（ASIN）最大长度不能超过192位")
        private String platformSpuNo;

        /**
        * 平台sku（MSKU）/销售平台SKU
        */
        @NotBlank(message = "平台sku（MSKU）/销售平台SKU不能为空")
        @Size(max = 192,message = "平台sku（MSKU）/销售平台SKU最大长度不能超过192位")
        private String platformSkuNo;

        /**
        * FNSKU/平台库存SKU
        */
        @NotBlank(message = "FNSKU/平台库存SKU不能为空")
        @Size(max = 192,message = "FNSKU/平台库存SKU最大长度不能超过192位")
        private String platformStockSku;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 192,message = "SKU ID最大长度不能超过192位")
        private String skuId;

        /**
        * 变更数量
        */
        @NotNull(message = "变更数量不能为空")
        private Integer changeQty;


    }


}