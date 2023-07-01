package com.erp.model.oms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname SkuMapingDTO
 * @Description TODO
 * @Date 2023-06-28 17:24
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SkuMapingDTO implements Serializable {


    /**
     * tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        private String searchType;

        private Integer count;

    }

    @Data
    @NoArgsConstructor
    public static class MatchCountDTO {
        /**
         * 匹配结果
         */
        private Boolean matchResult;

        /**
         * 数量
         */
        private Integer count;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 店铺id集合
         */
        private List<String> shopIdList;

        /**
         * 店铺id集合
         */
        private List<String> skuNoList;

        /**
         * 搜索类型
         * alL 全部
         * already 已匹配
         * not 未匹配
         */
        @StateEnumValue(strValues = {"all", "already", "not"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String searchType;


        /**
         * 平台code 集合
         */
        private List<String> platformList;


        /**
         * 创建人 id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


    }


    /**
     * 导出仓库
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * id
         */
        @NotBlank(message = "sku对照不存在")
        private String id;


        /**
         * 平台
         */
        @NotBlank(message = "平台不能为空")
        private String platformDict;

        /**
         * 店铺
         */
        private String shopId;

        /**
         * 产品sku
         */
        @NotBlank(message = "产品sku不能为空")
        private String productSkuId;

        /**
         * 平台sku no
         */
        @NotBlank(message = "平台sku不能为空")
        private String platformSkuNo;

        /**
         * 平台sku 名
         */
        private String platformSkuName;



    }

    /**
     * 导出sku 对照表
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    /**
     * 分页数据
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * id
         */
        private String id;

        /**
         * 平台
         */
        private String platformDict;

        /**
         * 平台名称
         */
        private String platformName;


        /**
         * 店铺id
         */
        private String shopId;

        /**
         * 店铺名称
         */
        private String shopName;


        /**
         * 平台sku no
         */
        private String platformSkuNo;

        /**
         * 平台sku 名称
         */
        private String platformSkuName;

        /**
         * 匹配结果
         */
        private Boolean matchResult;

        /**
         * 匹配结果
         */
        private String matchResultStr;

        /**
         * 产品sku id
         */
        private String productSkuId;

        /**
         * 产品sku no
         */
        private String productSkuNo;

        /**
         * 产品sku名称
         */
        private String productSkuName;


        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;


    }


    @Data
    @NoArgsConstructor
    public static class ProductSkuInfoDTO{

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku no
         */
        private String skuNo;

        private String skuName;

        /**
         * 图片地址
         */
        private String imagesUrl;

        /**
         * 平台sku
         */
        private String platformSkuNo;

        /**
         * 平台sku
         */
        private String platformSkuName;


    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO extends SortDTO {


        /**
         * sku编号
         */
        private String no;


        @NotBlank(message = "客户不能为空")
        private String customerId;


    }
}
