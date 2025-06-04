package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.format.NumberFormat;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.excel.FbaTransitExcelDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * <p>
 * FBA在途核算报表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-12-12
*/
@Data
@NoArgsConstructor
public class FbaTransitCalculateReportDTO implements Serializable {




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
        * 月份（YYYY-MM）
        */
        private LocalDate reportMonth;

        /**
        * 货件ID
        */
        private String shipmentId;

        /**
        * 货件单号
        */
        private String shipmentCode;

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
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 货件状态
        */
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private LocalDateTime shipmentCreateTime;

        /**
        * 货件签收时间
        */
        private LocalDateTime shipmentReceiveTime;

        /**
        * 在途调整时间
        */
        private LocalDateTime transitAdjustTime;


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
        * 月份（YYYY-MM）
        */
        private LocalDate reportMonth;

        /**
        * 货件ID
        */
        @NotBlank(message = "货件ID不能为空")
        @Size(max = 19,message = "货件ID最大长度不能超过19位")
        private String shipmentId;

        /**
        * 货件单号
        */
        @NotBlank(message = "货件单号不能为空")
        @Size(max = 255,message = "货件单号最大长度不能超过255位")
        private String shipmentCode;

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
        * 客户id
        */
        @NotBlank(message = "客户id不能为空")
        @Size(max = 19,message = "客户id最大长度不能超过19位")
        private String customerId;

        /**
        * 客户姓名
        */
        @NotBlank(message = "客户姓名不能为空")
        @Size(max = 255,message = "客户姓名最大长度不能超过255位")
        private String customerName;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 30,message = "仓库id最大长度不能超过30位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 255,message = "仓库名称最大长度不能超过255位")
        private String warehouseName;

        /**
        * 货件状态
        */
        @NotBlank(message = "货件状态不能为空")
        @Size(max = 255,message = "货件状态最大长度不能超过255位")
        private String shipmentStatus;

        /**
        * 货件创建时间
        */
        private LocalDateTime shipmentCreateTime;

        /**
        * 货件签收时间
        */
        private LocalDateTime shipmentReceiveTime;

        /**
        * 在途调整时间
        */
        private LocalDateTime transitAdjustTime;


    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 月份【可排序】
         */
        private LocalDate reportMonth;
        /**
         * 月份 页面展示
         */
        private String reportMonthStr;
        /**
         * 货件单号【可排序】
         */
        private String shipmentCode;
        /**
         * 店铺【可排序】
         */
        private String shopName;
        /**
         * 客户【可排序】
         */
        private String customerName;
        /**
         * 货件状态【可排序】
         */
        private String shipmentStatus;
        /**
         * 明细id
         */
        private String detailId;
        private String asin;
        private String msku;
        /**
         * fnSku
         */
        private String fnSku;
        /**
         * 系统SKU【可排序】
         */
        private String skuNo;
        /**
         * FBA仓库
         */
        private String warehouseName;
        /**
         * 申报量【可排序】
         */
        private Integer declareQty;
        /**
         * 已发货【可排序】
         */
        private Integer deliveryQty;
        /**
         * 签收量【可排序】
         */
        private Integer receiveQty;
        /**
         * 收发差异【可排序】
         */
        private Integer diffQty;
        /**
         * 货件创建时间【可排序】
         */
        private LocalDateTime shipmentCreateTime;
        /**
         * 货件签收时间【可排序】
         */
        private LocalDateTime shipmentReceiveTime;
        /**
         * 在途调整时间【可排序】
         */
        private LocalDateTime adjustTime;
        /**
         * 期初在途【可排序】
         */
        private Integer initTransitQty;
        /**
         * 本期发货【可排序】
         */
        private Integer currentDeliveryQty;
        /**
         * 本期签收【可排序】
         */
        private Integer currentReceiveQty;
        /**
         * 期末在途【可排序】
         */
        private Integer endPeriodTransitQty;
        /**
         * 期末在途调整【可排序】
         */
        private Integer endPeriodTransitAdjustQty;
        /**
         * 期末在途（调整后）【可排序】
         */
        private Integer afterEndPeriodTransitQty;
        /**
         * 调整原因【可排序】
         */
        private String adjustReason;
    }

    /**
     * 列表分页查询
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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
     * 在途报表
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class CalculateDTO extends FbaTransitCalculateAbstractDTO{
        private String id;
        //月份
        private LocalDate reportMonth;
        //货件id
        private String shipmentId;
        //货件编码
        private String shipmentCode;
        //报表明细id
        private String detailId;
        private String asin;
        private String msku;
        private String fnSku;
        //erp-skuId
        private String skuId;
        private String skuNo;
        //期末在途调整后数量
        private Integer afterEndPeriodTransitQty;
    }

    @Data
    @NoArgsConstructor
    public static class FbaTransitCalculateAbstractDTO{
        //货件编码
        private String shipmentCode;
        //asin
        private String asin;
        //msku
        private String msku;
        //fnSku
        private String fnSku;
    }
    /**
     * 头程发货单
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class DeliveryDTO extends FbaTransitCalculateAbstractDTO{
        private String id;
        private String code;
        //月份
        private LocalDateTime approveTime;
        //货件id
        private String shipmentId;
        //店铺
        private String shopId;
        private String shopName;
        //仓库
        private String warehouseId;
        private String warehouseName;
        //报表明细id
        private String detailId;
        //erp-skuId
        private String skuId;
        private String skuNo;
        //申报数量
        private Integer declareQty;
        //发货数量
        private Integer deliveryQty;
        //明细关联的fba货件编码
        private String fbaShipmentCode;
    }

    /**
     * FBA签收
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class FbaReceiveDTO extends FbaTransitCalculateAbstractDTO{
        //货件id
        private String shipmentId;
        //货件编码
        private String shipmentCode;
        //店铺
        private String shopId;
        private String shopName;
        //平台货件状态
        private String platformShipmentStatus;
        //货件明细Id
        private String detailId;
        //erp-skuId
        private String skuId;
        private String skuNo;
        //签收明细id
        private String receiveId;
        //申报数量
        private Integer declareQty;
        //发货数量
        private Integer deliveryQty;
        //申报差异
        private Integer diffQty;
        //签收数量
        private Integer receiveQty;
        //签收日期
        private LocalDate receiveDate;
        //明细关联的fba货件平台来源
        private String sourcePlatform;
    }
    /**
     * 在途报表
     */
    @Data
    @NoArgsConstructor
    public static class TransitDTO {
        //唯一值 shipmentCode+asin+msku
        private String transitKey;
        //货件编码
        private String shipmentCode;
        private String asin;
        private String msku;
        private String fnSku;
        //月份
        private LocalDate reportMonth;
    }


    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<FbaTransitExcelDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {
        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;
    }

    @Data
    @NoArgsConstructor
    public static class AdjustDTO {
        //主表id
        @NotBlank(message = "主表id不能为空")
        private String id;
        //明细Id
        @NotBlank(message = "明细id不能为空")
        private String detailId;
        //调整数量
        @NotNull(message = "期末在途调整数量不能为空")
        @Min(value = -999999999,message = "期末在途调整数量最小值为-999999999")
        @Max(value = 999999999,message = "期末在途调整数量最大值为999999999")
        private Integer adjustQty;
        //调整原因
        @NotBlank(message = "调整原因不能为空")
        private String adjustReason;
    }
}