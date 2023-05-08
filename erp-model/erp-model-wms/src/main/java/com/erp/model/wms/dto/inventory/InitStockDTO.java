package com.erp.model.wms.dto.inventory;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @Classname: InventoryInitDTO
 * @Description: 期初库存实体
 * @CreateTime: 2023-05-08  17:05
 * @Author: zhangchunlin
 */
@Data
public class InitStockDTO implements Serializable {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO  {

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;


        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        /**
         * 期初库存产品明细
         */
        @Valid
        @NotNull(message = "产品明细不能为空")
        @Size(min = 1, message = "请至少录入一条产品明细")
        private List<InitStockDetailDTO.AddDTO> details;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
         * 表id
         */
        @NotEmpty(message = "期初库存id不能为空")
        private String id;

        /**
         * 单据日期
         */
        @NotNull(message = "单据日期不能为空")
        private LocalDate billDate;


        /**
         * 仓库id
         */
        @NotEmpty(message = "仓库不能为空")
        private String warehouseId;

        /**
         * 库存组织id
         */
        @NotEmpty(message = "库存组织不能为空")
        private String orgId;

        /**
         * 期初库存产品明细
         */
        @Valid
        @NotNull(message = "产品明细不能为空")
        @Size(min = 1, message = "请至少录入一条产品明细")
        private List<InitStockDetailDTO.UpdateDTO> details;

    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO  {

        /**
         * id
         */
        private String id;

        /**
         * 单据日期
         */
        private LocalDate billDate;


        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 期初库存产品明细
         */
        private List<InitStockDetailDTO.ViewDTO> details;

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * sku编号
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 审核状态
         */
        private String approveStatusName;

        /**
         * spu型号
         */
        private String spuNo;

        /**
         * 品牌名称
         */
        private String brandName;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 销售状态名称
         */
        private String saleStatusName;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 期初数量
         */
        private Integer qty;


        /**
         * 仓位名称
         */
        private String warehouseLocationName;
    }

    /**
     * 查询条件
     */
    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 单据编号
         */
        private String code;

        /**
         * sku编码
         */
        private List<String> skuNoList;


        /**
         * spu编码
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 品牌集合
         */
        private List<String> brandList;

        /**
         * 销售状态集合
         */
        private List<String> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdLList;

    }

    /**
     * 明细导入Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO  {

        /**
         * sku
         */
        @NotEmpty(message = "sku不能为空")
        private String skuNo;

        /**
         * 期初数量
         */
        @NotNull(message = "期初数量不能为空")
        @Min(value = 0, message = "期初数量最小值为0")
        @Max(value = 999999999, message = "期初数量最大值为999999999")
        private Integer qty;

        /**
         * 库位名称
         */
        private String warehouseLocationName;


        @Size(max = 255, message = "备注长度不能超过19位")
        private String remark;

    }

}