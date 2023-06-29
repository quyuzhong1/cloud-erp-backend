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
         * 搜索类型
         * alL 全部
         * yes 已匹配
         * no 未匹配
         */
        @StateEnumValue(strValues = {"all", "yes", "no"}, message = "搜索类型有误")
        @NotBlank(message = "搜索类型不能为空")
        private String  searchType;



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
}
