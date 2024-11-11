package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jnr.ffi.annotations.In;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PickingListsDTO {


    @Getter
    @Setter
    public static class PagingParam extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        @NotBlank(message = "来源类型不能为空")
        private String sourceType;
    }

    @Getter
    @Setter
    public static class PagingView {

        private String id;
        /**
         * 拣货单号
         */
        private String code;
        /**
         * 来源单号
         */
        private String sourceCode;

        private String warehouseId;
        /**
         * 收货仓库名称
         */
        private String warehouseName;
        /**
         * 产品总数
         */
        private Integer skuTotal;
        /**
         * 仓位总数
         */
        private Integer locationTotal;
        /**
         * 更新人
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    @Getter
    @Setter
    public static class View {
        private String id;
        /**
         * 拣货单号
         */
        private String code;
        /**
         * 来源单号
         */
        private String sourceCode;

        private String warehouseId;
        /**
         * 收货仓库名称
         */
        private String warehouseName;

        private List<PickingDetailDTO.View> details;
    }

    @Getter
    @Setter
    public static class UpdateDTO {
        private String id;
        private List<PickingDetailDTO.View> details;
    }

    @Getter
    @Setter
    public static class ExportDTO extends PagingParam {
        private List<String> ids;
    }

    @Getter
    @Setter
    public static class ExportInfoDTO {
        /**
         * 拣货单号
         */
        private String code;
        /**
         * 来源单号
         */
        private String sourceCode;

        private String warehouseId;
        /**
         * 拣货仓库名称
         */
        private String warehouseName;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * 库区
         */
        private String warehouseAreaName;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 暂存库区
         */
        private String stagingAreaName;
        /**
         * 暂存库位
         */
        private String stagingLocation;
        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }


    @Getter
    @Setter
    public static class CombinationPrintView {

        /**
         * 单据编号
         */
        private String code;
        /**
         * 客户/渠道名
         */
        private String channelName;
        /**
         * 打印时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime printTime;
        /**
         * 打印人
         */
        private String printUserName;
        /**
         * 经办人
         */
        private String handlingUserName;
        /**
         * 打印明细
         */
        private List<CombinationPrintDetailView> printDetailViews;
    }


    @Getter
    @Setter
    public static class PrintView {

        /**
         * 单据编号
         */
        private String code;
        /**
         * 客户/渠道名
         */
        private String channelName;
        /**
         * 打印时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime printTime;
        /**
         * 打印人
         */
        private String printUserName;
        /**
         * 经办人
         */
        private String handlingUserName;
        /**
         * 打印明细
         */
        private List<PrintDetailView> printDetailViews;

        /**
         * 组合品打印明细
         */
        private List<CombinationPrintDetailView> combinationPrintDetailView;
    }

    @Getter
    @Setter
    public static class CombinationPrintDetailView {

        /**
         * 父级sku
         */
        private String parentSku;

        /**
         * 父级sku数量
         */
        private Integer parentSkuQty;

        /**
         * 子级sku
         */
        private String childSku;

        /**
         * 子级sku数量
         */
        private Integer childSkuQty;

        /**
         * 三方sku
         */
        private String thirdSku;

    }

    @Getter
    @Setter
    public static class PrintDetailView {
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品编码
         */
        private String skuNo;
        /**
         * 三方sku
         */
        private String thirdSku;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 拣货数量
         */
        private Integer pickingQty;
        /**
         * 仓库Id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 推荐仓位
         */
        private String warehouseLocation;
        /**
         * 明细来源Id
         */
        private String sourceDetailId;

        public void getPrintView(PickingListsEntity entity, PickingDetailEntity detail, String productName) {
            this.skuId = detail.getSkuId();
            this.skuNo = detail.getSkuNo();
            this.productName = productName;
            this.sourceCode = entity.getSourceCode();
            this.pickingQty = detail.getQty();
            this.warehouseId = entity.getWarehouseId();
            this.warehouseName = entity.getWarehouseName();
            this.warehouseLocation = detail.getWarehouseLocation();
            this.sourceDetailId = detail.getSourceDetailId();
        }
    }

    @Getter
    @Setter
    public static class AddDTO {
        /**
         * 单据类型
         *
         * @see PickingBillTypeEnum
         */
        private String billType;
        /**
         * B2B客户
         */
        private String customerId;

        /**
         * B2B订单收货国家
         */
        private String countryCode;

        /**
         * 头程目的仓库
         */
        private String deliveryWarehouseId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源单据号
         */
        private String sourceCode;

        /**
         * 生成拣货单--拣货规则
         * 其余情况为空
         */
        private List<LocationInventoryResultDTO> ruleOrderMatchResult;
        /**
         * 明细
         */
        private List<PickingDetailDTO.AddDTO> details;
    }

    @Getter
    @Setter
    public static class SourceView {
        private String id;
        /**
         * 拣货单号
         */
        private String code;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 拣货明细id
         */
        private String detailId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 数量
         */
        private Integer qty;
        /**
         * 库位
         */
        private String warehouseLocation;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 暂存仓位
         */
        private String stagingLocation;
        /**
         * 已拣货数量
         */
        private Integer pickedQty;
        /**
         * 已分货数量
         */
        private Integer allocatedQty;
        /**
         * 是否缺货
         */
        private Boolean isOutStock;
    }
    @Data
    @NoArgsConstructor
    public static class DetailPickDTO{
        private String skuId;
        private String skuNo;
        private String fnSku;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 已拣货数量
         */
        private Integer pickedQty;
        /**
         * source_id
         */
        private String sourceId;
    }
}
