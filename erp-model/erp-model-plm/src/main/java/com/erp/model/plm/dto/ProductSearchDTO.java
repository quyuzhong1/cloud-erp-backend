package com.erp.model.plm.dto;

import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductSearchDTO

 * @Date 2022-09-17 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductSearchDTO extends PermissionsDTO {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {
        /**
         * 我的任务类型
         * all 全部  finished 完成的  unfinished 未完成的
         *
         */
        private String myTaskType;


        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 分类id
         */
        private String categoryId;

        /**
         * 在点击分类可以获取到产品id
         */
        private List<String> productIds;




        /**
         * 阶段集合
         */
        private List<String> phaseNameList;

        /**
         * 状态列表
         *  立项状态 0 待规划 1 调研中  2：ID设计中  3::已立项  4：已暂停  5：已终止
         */
        private List<Integer> stateList;



        /**
         * 产品等级
         */
        private List<String> gradeIdList;

        /**
         * 产品 品牌id 集合
         */
        private List<String> brandIdList;

        /**
         * 产品 属性
         */
        private List<String> propertyIdList;


        /**
         * 产品 经理
         */
        private List<String> productChargeIdList;

        /**
         * 项目 经理
         */
        private List<String> projectChargeIdList;


        /**
         * 项目进展
         * 列表
         */
        private List<String> progressStatusList;


        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


        /**
         * 产品部门类型，/plm/dict/list?type=productDeptType
         */
        private String productDeptType;

        /**
         * 部门id集合
         */
        private List<String> deptIdList;
    }


    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO{
        /**
         * 产品ids
         */
        private List<String> ids;

        /**
         * 导出数据 类型
         * 0，产品列表
         * 1. 任务列表
         */
        @NotNull(message = "导出类型不能为空")
        private List<Integer> exportDataList;
    }


    @Data
    @NoArgsConstructor
    public static class SkuParamDTO extends PermissionsDTO{

        /**
         * 状态集合
         */
        private List<Integer> statusList;

        /**
         * sku编号
         */
        @NotEmpty(message = "SKU不能为空")
        private List<String> skuNoList;
    }

    @Data
    @NoArgsConstructor
    public static class SkuListDTO{

        /**
         * 图片URL
         */
        private String imagesUrl;

        /**
         * 产品id
         */
        private String productId;

        /**
         * spuNo
         */
        private String spuNo;

        /**
         * spu名称
         */
        private String spuName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String productName;

        /**
         * 品牌
         */
        private String brandName;

        /**
         * 销售方式
         */
        private String saleMethod;

        /**
         * 状态
         */
        private Integer status;

        /**
         * 状态名称
         */
        private String statusName;

        /**
         * 单位
         */
        private String unitName;

        /**
         * 变体属性
         */
        private String variantProperty;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关中文名
         */
        private String declareChineseName;

        /**
         * 箱单数量
         */
        private Integer boxQty;

        /**
         *  MOQ(最小起订量)
         */
        private Integer moq;

        /**
         * 供应商
         */
        private String mainSupplier;

        /**
         * 供应商名称
         */
        private String mainSupplierName;
    }

}
