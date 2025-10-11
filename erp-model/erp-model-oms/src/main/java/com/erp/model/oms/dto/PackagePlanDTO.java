package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 组包计划主表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
*/
@Data
@NoArgsConstructor
public class PackagePlanDTO implements Serializable {




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
        * 单据编号
        */
        private String code;

        /**
        * 平台
        */
        private String dictPlatform;

        /**
        * 店铺id
        */
        private String shopId;

        /**
        * 大包单号
        */
        private String packageNo;

        /**
        * 组包状态 PackageStatusEnum
        */
        private String packageStatus;

        /**
        * 交接标签下载
        */
        private Boolean isHandoverDownload;

        /**
        * 箱数
        */
        private Integer boxNum;

        /**
        * 发货仓库id
        */
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        private String deliveryWarehouseName;

        /**
        * 打印交接单状态  not 未打印  already 已打印
        */
        private String printHandoverStatus;

        /**
        * 打印订单状态  not 未打印  already 已打印
        */
        private String printOrderStatus;


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
        * 平台
        */
        @NotBlank(message = "平台不能为空")
        @Size(max = 50,message = "平台最大长度不能超过50位")
        private String dictPlatform;

        /**
        * 店铺id
        */
        @NotBlank(message = "店铺id不能为空")
        @Size(max = 19,message = "店铺id最大长度不能超过19位")
        private String shopId;

        /**
        * 大包单号
        */
        @NotBlank(message = "大包单号不能为空")
        @Size(max = 50,message = "大包单号最大长度不能超过50位")
        private String packageNo;

        /**
        * 组包状态 PackageStatusEnum
        */
        @NotBlank(message = "组包状态 PackageStatusEnum不能为空")
        @Size(max = 50,message = "组包状态 PackageStatusEnum最大长度不能超过50位")
        private String packageStatus;

        /**
        * 交接标签下载
        */
        @NotNull(message = "交接标签下载不能为空")
        private Boolean isHandoverDownload;

        /**
        * 箱数
        */
        @NotNull(message = "箱数不能为空")
        private Integer boxNum;

        /**
        * 发货仓库id
        */
        @NotBlank(message = "发货仓库id不能为空")
        @Size(max = 19,message = "发货仓库id最大长度不能超过19位")
        private String deliveryWarehouseId;

        /**
        * 发货仓库名称
        */
        @NotBlank(message = "发货仓库名称不能为空")
        @Size(max = 50,message = "发货仓库名称最大长度不能超过50位")
        private String deliveryWarehouseName;

        /**
        * 打印交接单状态  not 未打印  already 已打印
        */
        @NotBlank(message = "打印交接单状态  not 未打印  already 已打印不能为空")
        @Size(max = 32,message = "打印交接单状态  not 未打印  already 已打印最大长度不能超过32位")
        private String printHandoverStatus;

        /**
        * 打印订单状态  not 未打印  already 已打印
        */
        @NotBlank(message = "打印订单状态  not 未打印  already 已打印不能为空")
        @Size(max = 32,message = "打印订单状态  not 未打印  already 已打印最大长度不能超过32位")
        private String printOrderStatus;
        /**
        * 组包计划明细
        */
        private List<PackagePlanDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 组包计划单号 [可排序]
         */
        private String code;
        /**
         * 平台[可排序]
         */
        private String dictPlatform;
        private String dictPlatformName;
        /**
         * 店铺id[可排序]
         */
        private String shopId;
        private String shopName;
        /**
         * 大包单号[可排序]
         */
        private String packageNo;
        /**
         * 组包状态 PackageStatusEnum[可排序]
         */
        private String packageStatus;
        private String packageStatusName;
        /**
         * 交接标签下载[可排序]
         */
        private Boolean isHandoverDownload;
        private String handoverDownload;
        /**
         * 箱数[可排序]
         */
        private Integer boxNum;
        /**
         * 发货仓库id[可排序]
         */
        private String deliveryWarehouseId;
        /**
         * 发货仓库名称[可排序]
         */
        private String deliveryWarehouseName;
        /**
         * 打印交接单状态  not 未打印  already 已打印[可排序]
         * PackagePrintStatusEnum
         */
        private String printHandoverStatus;
        private String printHandoverStatusName;
        /**
         * 打印订单状态  not 未打印  already 已打印[可排序]
         * PackagePrintStatusEnum
         */
        private String printOrderStatus;
        private String printOrderStatusName;
        /**
         * 创建时间[可排序]
         */
        private LocalDateTime createTime;
        /**
         * 创建人[可排序]
         */
        private String createUserName;
        /**
         * 组包计划明细
         */
        private List<PackagePlanDetailDTO.ViewDTO> detailList;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingViewDTO{

        /**
         * 销售订单code
         */
        private String soCode;

        /**
         * 销售订单id
         */
        private String soId;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道名
         */
        private String logisticsChannelName;

        /**
         * 物流跟踪号
         */
        private String trackNo;

        /**
         * 运输单号
         */
        private String transportNo;
        /**
         * 条码
         */
        private String barcode;
    }
}