package com.erp.model.oms.dto;

import cn.hutool.core.util.ReflectUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.core.anno.StateEnumValue;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.enums.RuleTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
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
         * 服务商code
         */
        @JsonIgnore
        private String  providerCode;
    }

    @Data
    @NoArgsConstructor
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
         * MSKU/FNSKU
         */
        private String fnSku;

        /**
         * ASIN
         */
        private String asin;
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



}
