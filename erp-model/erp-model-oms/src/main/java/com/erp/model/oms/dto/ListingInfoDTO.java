package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname ListingInfoDTO

 * @Date 2023-08-18 16:13
 * @Created by yl
 */
public class ListingInfoDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class PagingParamDTO implements Serializable{
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 目的仓Id（目的仓Id和店铺Id不能都为空）
         */
        private String warehouseId;

        /**
         * 店铺Id（目的仓Id和店铺Id不能都为空）
         */
        private String shopId;

        /**
         * 授权id
         */
        private String authId;

        /**
         * 服务商code
         */
        @JsonIgnore
        private String  providerCode;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageDTO implements Serializable{
        /**
         * skuMapping id 编辑时传
         */
        private String id;

        /**
         * 第三方sku
         */
        private String platformSku;

        /**
         * 第三方产品名称
         */
        private String platformSkuName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 品名
         */
        private String productName;

        /**
         * 图片
         */
        private String imagesUrl;
        /**
         * mSKU
         */
        private String mSKU;

        /**
         * FNSKU
         */
        private String fnSku;

        /**
         * ASIN
         */
        private String asin;


        /**
         * 计划发货数量
         */
        private Integer qty;

        /**
         * 单箱数量
         */
        private Integer boxQty;

        /**
         * 对照关系是否映射到服务商平台所有仓库: f=否, t=是
         */
        private Boolean hasMappingAll;
        /**
         * 是否组合品
         */
        private Boolean isCombination;
    }

    /**
     * sku映射参数
     */
    @Data
    @NoArgsConstructor
    public static class WarehouseSkuMappingParamDTO {

        /**
         * skuMapping id
         */
        private String id;

        /**
         * erp下拉的sku编号
         */
        private String skuNo;

        /**
         * 仓库Id
         */
        private String warehouseId;

    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable{
        /**
         * id
         */
        private String id;

        /**
         * 平台sku no
         */
        private String platformSkuNo;


        /**
         * 平台产品名
         */
        private String platformProductName;


        /**
         * 库存sku no
         */
        private String warehouseSkuNo;

        /**
         * 库存产品名
         */
        private String warehouseProductName;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseDropDownDTO {
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;

        public static BaseDropDownDTO init(String fieldValue) {
            return new BaseDropDownDTO(fieldValue, fieldValue);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseDropDownParamDTO {
        /**
         * 类型
         * platform=平台
         * warehouse=仓库
         * {@link com.erp.model.oms.enums.RuleTypeEnum}
         */
        @NotBlank(message = "类型不能为空")
        private String type;

        /**
         * 字段名:
         * platformSkuNo=平台SKU号
         * platformSpuNo=平台SPU号
         * platformFnSku =平台关联FNSKU号
         */
        @NotBlank(message = "字段名不能为空")
        private String fieldName;

        public String checkAndGetType(){
            RuleTypeEnum typeEnum = RuleTypeEnum.getByCode(this.getType());
            if (null == typeEnum){
                throw  new ServiceException("类型不存在,仅支持：platform=平台，warehouse=仓库");
            }
            return this.type;
        }

        public String checkAndGetFieldName(){
            List<String> fieldNameList = Arrays.asList("platformSkuNo", "platformSpuNo", "platformFnSku");
            if (fieldNameList.contains(this.fieldName)){
                return this.fieldName;
            }
            throw new ServiceException("字段名不支持");
        }

    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<ListingInfoDTO.PageDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

}
