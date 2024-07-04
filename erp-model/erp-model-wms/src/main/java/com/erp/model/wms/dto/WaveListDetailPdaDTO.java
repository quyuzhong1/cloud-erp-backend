package com.erp.model.wms.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 波次详情DTO（PDA）
 * @date 2024-07-01
 * @author tanmujin
 */
@Data
@NoArgsConstructor
public class WaveListDetailPdaDTO implements Serializable {

    @Data
    public static class ViewDTO{
        /**
         * 波次ID
         */
        private String waveId;

        /**
         * 波次编号
         */
        private String waveCode;

        /**
         * 应拣总数量
         */
        private Integer shouldPickTotalQty;

        /**
         * 已拣总数量
         */
        private Integer pickedTotalQty;

        /**
         * 拣货方式
         */
        private String pickingType;

        /**
         * 仓位拣货列表
         */
        private List<WaveListDetailPdaDTO.PickingLocationDTO> locationPickingDetailList;
    }

    @Data
    public static class PickingLocationDTO {
        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku 编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 应拣总数量
         */
        private Integer shouldPickingTotalQty;

        /**
         * 已拣总数量
         */
        private Integer pickedTotalQty;

        /**
         * 是否缺货
         */
        private Boolean isOutStock;

        /**
         * 投放篮框列表
         */
        private List<WaveListDetailPdaDTO.BasketDTO> basketList;
    }

    @Data
    public static class BasketDTO{
        /**
         * 投放框号
         */
        private String no;

        /**
         * 应拣数量
         */
        private Integer shouldPickingQty;

        /**
         * 已拣数量
         */
        private Integer pickedQty;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class HangUpParamDTO extends ViewDTO{

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class FinishParamDTO extends ViewDTO{

    }

    @Data
    public static class FinishResultDTO {
        /**
         * 波次编码
         */
        private String code;

        /**
         * 商品应拣数量
         */
        private Integer goodsShouldPickingQty;

        /**
         * 商品已拣数量
         */
        private Integer goodsPickedQty;

        /**
         * 商品种类应拣数量
         */
        private Integer skuShouldPickingQty;

        /**
         * 商品种类已拣数量
         */
        private Integer skuPickedQty;
    }
}
