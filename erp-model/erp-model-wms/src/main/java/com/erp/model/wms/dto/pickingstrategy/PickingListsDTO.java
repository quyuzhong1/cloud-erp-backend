package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.dto.PickingDetailDTO;
import com.erp.model.wms.entity.PickingDetailEntity;
import com.erp.model.wms.entity.PickingListsEntity;
import com.erp.model.wms.enums.PickingBillTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PickingListsDTO {
    private PickingListsDTO() {
        throw new IllegalStateException("Utility PickingListsDTO class");
    }

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
         * 打印人
         */
        private String printUserName;
        /**
         * 打印状态
         */
        private String printStatus;
        /**
         * 打印状态名称
         */
        private String printStatusName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 打印时间
         */
        private LocalDateTime printTime;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
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
         * 打印状态
         * PackagePrintStatusEnum
         */
        private String printStatus;
        /**
         * 打印状态名称
         */
        private String printStatusName;
        /**
         * 暂存库位
         */
        private String stagingLocation;
        /**
         * 更新人
         */
        private String updateUserName;
        private String printUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        private LocalDateTime printTime;
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
         * 打单人
         */
        private String printUserName;
        /**
         * 经办人
         */
        private String handlingUserName;
        /**
         * 拣货清单
         */
        private List<PrintDetailView> printDetailViews;

        /**
         * 发货清单
         */
        private List<CombinationPrintDetailView> combinationPrintDetailView;
    }
    @Data
    @NoArgsConstructor
    public static class PrintCombinationView implements Serializable {

        /**
         * 单据编号
         */
        private String code;
        /**
         * 来源单号
         */
        private String sourceCode;
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
         * 打单人
         */
        private String printUserName;
        /**
         * 经办人
         */
        private String handlingUserName;
        /**
         * 单品拣货清单
         */
        private List<PrintSkuView> printSkuSingleViewList;

        /**
         * 组合品拣货清单
         */
        private List<PrintSkuView> printSkuCombinationViewList;
    }
    @Data
    @NoArgsConstructor
    public static class PrintSkuView {
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
        /**
         * 是否是捆绑商品:true=是，false=否
         */
        private Boolean isCombination;
        /**
         * 组合sku
         */
        private String parentSkuNo;

        /**
         * 发货数量
         */
        private Integer parentSkuQty;

        /**
         * 子级sku
         */
        private String childSkuNo;

        /**
         * 子级sku数量
         */
        private Integer childSkuQty;
        /**
         * 客户PO
         */
        private String customerPO;
        /**
         * 目的地
         */
        private String toCountry;
        /**
         * 分组字段
         */
        private String groupName;
        /**
         * 拣货备注
         */
        private String pickRemark;
        public void getPrintView(PickingListsEntity entity, PickingDetailEntity detail, String productName, String customerPO, String toCountry, String pickRemark) {
            this.parentSkuNo = detail.getSkuNo();
            this.productName = productName;
            this.sourceCode = entity.getSourceCode();
            this.parentSkuQty = detail.getQty();
            this.warehouseId = entity.getWarehouseId();
            this.warehouseName = entity.getWarehouseName();
            this.warehouseLocation = detail.getWarehouseLocation();
            this.sourceDetailId = detail.getSourceDetailId();
            this.customerPO = customerPO;
            this.toCountry = toCountry;
            this.pickRemark = pickRemark;
        }
    }

    @Getter
    @Setter
    public static class CombinationPrintDetailView {

        /**
         * 是否是捆绑商品:true=是，false=否
         */
        private Boolean isCombination;
        /**
         * sku
         */
        private String parentSku;

        /**
         * 发货数量
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
        /**
         * 客户PO号
         */
        private String customerPo;

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
        /**
         * 客户PO
         */
        private String customerPO;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddChangeDTO {

        private List<PickingDetailEntity> addList = new ArrayList<>();
        private List<PickingDetailEntity> updateList = new ArrayList<>();
        private List<PickingDetailEntity> removeList = new ArrayList<>();

    }
}
