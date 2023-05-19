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
@NoArgsConstructor
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
         * 期初库存产品明细
         */
        @Valid
        @NotNull(message = "期初库存明细不能为空")
        @Size(min = 1, message = "请至少录入一条期初库存明细")
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
         * 期初库存产品明细
         */
        @Valid
        @NotNull(message = "期初库存明细不能为空")
        @Size(min = 1, message = "请至少录入一条期初库存明细")
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
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 期初库存主单id
         */
        private String id;

        /**
         * 期初库存明细id
         */
        private String detailId;

        /**
         * 单据编号
         */
        private String code;

        /**
         * sku id
         */
        private String skuId;


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
        private String approveStatus;

        /**
         * 审核状态
         */
        private String approveStatusName;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatusName;

        /**
         * spu型号
         */
        private String spuNo;

        /**
         * 品牌名称
         */
        private String brandName;

        /**
         * 库存组织id
         */
        private String orgId;

        /**
         * 库存组织名称
         */
        private String orgName;

        /**
         * 销售状态编码
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 期初数量
         */
        private Integer qty;

        /**
         * 仓位
         */
        private String warehouseLocation;

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
         * spu编码 接口地址：/wms/drop/down/product/spuNo/list（一次性返回所有）
         */
        private List<String> spuNoList;

        /**
         * 仓库id集合
         */
        private List<String> warehouseIdList;

        /**
         * 销售状态集合 接口地址：plm/common/enumDropDown?type=SaleState
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdList;

    }

    /**
     * 导出Excel查询条件
     */
    @Data
    @NoArgsConstructor
    public static class ExportSearchParamDTO extends SortDTO {

        /**
         * 勾选的单据id集合
         */
        private List<String> ids;

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
         * 销售状态集合
         */
        private List<Integer> saleStatusList;

        /**
         * 库存组织集合
         */
        private List<String> orgIdList;

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
         * 库位
         */
        private String warehouseLocation;


        @Size(max = 255, message = "备注长度不能超过19位")
        private String remark;

    }

    /**
     * 按条件查询期初库存数据
     */
    @Data
    @NoArgsConstructor
    public static class ConditionDTO {

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * sku id
         */
        private String skuId;

        /**
         * 日期范围
         */
        private List<LocalDate> dateList;

    }

}