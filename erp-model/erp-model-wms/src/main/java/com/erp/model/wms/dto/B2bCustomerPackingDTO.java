package com.erp.model.wms.dto;

import com.common.business.dto.AttachDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * B2B客户装箱
 */
@Data
@NoArgsConstructor
public class B2bCustomerPackingDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 装箱行id（箱级展示时取箱内第一行id）
         */
        private String id;
        /**
         * B2B三方发货单id
         */
        private String mainId;
        /**
         * 箱序号，相同序号表示同一箱
         */
        private Integer boxSeq;
        /**
         * 箱唛号
         */
        private String boxMarkNo;
        /**
         * 箱唛参考号
         */
        private String boxMarkRefNo;
        /**
         * 标签尺寸，如100*150
         */
        private String labelSize;
        /**
         * 贴标要求
         */
        private String labelingRequirement;
        /**
         * 货件标签附件
         */
        private List<WmsAttachmentDTO.UpdateDTO> attachList;
        /**
         * 箱内SKU明细
         */
        private List<LineViewDTO> packingLineList;
    }

    @Data
    @NoArgsConstructor
    public static class LineViewDTO {

        /**
         * 装箱行id
         */
        private String id;
        /**
         * B2B三方发货单id
         */
        private String mainId;
        /**
         * 箱序号
         */
        private Integer boxSeq;
        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品SKU编号
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量（展示用）
         */
        private Integer saleQty;
        /**
         * 当前箱内该SKU装箱数量
         */
        private Integer packingQty;
        /**
         * 三方仓SKU
         */
        private String warehousePlatformSku;
        /**
         * 行序号
         */
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 装箱行id（编辑回传时可为空，落库会重新铺平）
         */
        private String id;

        /**
         * 箱序号，相同序号表示同一箱
         */
        @NotNull(message = "序号不能为空")
        private Integer boxSeq;

        /**
         * 箱唛号
         */
        @Size(max = 50, message = "箱唛号最大长度不能超过50位")
        private String boxMarkNo;

        /**
         * 箱唛参考号
         */
        @Size(max = 50, message = "箱唛参考号最大长度不能超过50位")
        private String boxMarkRefNo;

        /**
         * 标签尺寸，如100*150
         */
        @Size(max = 20, message = "标签尺寸最大长度不能超过20位")
        private String labelSize;

        /**
         * 贴标要求
         */
        @Size(max = 200, message = "贴标要求最大长度不能超过200位")
        private String labelingRequirement;

        /**
         * 货件标签附件
         */
        private List<AttachDTO> attachList;

        /**
         * 箱内SKU明细
         */
        @Valid
        @NotEmpty(message = "装箱SKU不能为空")
        private List<LineAddDTO> packingLineList;
    }

    @Data
    @NoArgsConstructor
    public static class LineAddDTO {

        /**
         * 产品SKU编号
         */
        @NotBlank(message = "SKU不能为空")
        @Size(max = 255, message = "SKU最大长度不能超过255位")
        private String skuNo;

        /**
         * 产品id
         */
        private String skuId;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量（展示用）
         */
        private Integer saleQty;

        /**
         * 当前箱内该SKU装箱数量
         */
        @NotNull(message = "装箱数量不能为空")
        private Integer packingQty;

        /**
         * 三方仓SKU
         */
        private String warehousePlatformSku;

        /**
         * 行序号
         */
        private Integer sort;
    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {

        /**
         * 导入成功的箱级装箱明细
         */
        private List<ViewDTO> successList;
        /**
         * 导入错误文件URL
         */
        private String errorUrl;
    }

}
