package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname ProductDTO
 * @Description TODO
 * @Date 2022-09-16 16:12
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductDTO implements Serializable {

    /**
     * 产品id
     */
    private String id;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名不能为空")
    @Size(max = 50, message = "最大50字符")
    private String name;


    /**
     * 产品分类
     */
    private String category;


    /**
     * 产品分类id
     */
    private String categoryId;


    /**
     * 产品属性
     */
    @NotBlank(message = "产品属性不能为空")
    private String property;


    /**
     * 产品属性id
     */
    private String propertyId;

    /**
     * 产品负责人
     */
    private List<String> chargeNames;

    /**
     * 产品负责人id
     */
    @NotNull(message = "产品负责人id不能为空")
    @Size(min = 1, message = "产品负责人id 不能为空")
    private List<String> chargeIds;


    /**
     * 产品等级
     */
    private String grade;

    /**
     * 产品等级id
     */
    @NotBlank(message = "产品等级不能为空")
    private String gradeId;

    /**
     * 产品品牌
     */
    @NotBlank(message = "产品品牌不能为空")
    private String brandName;

    /**
     * 产品品牌id
     */
    private String brandId;


    /**
     * 产品类型 1 新产品 2 迭代产品
     */
    @NotNull(message = "产品类型不能为空")
    @StateEnumValue(intValues = {2, 1}, message = "产品类型有误")
    private Integer type;

    /**
     * 关联产品id
     */
    private String relevanceProductId;
    /**
     * 关联产品名称
     */
    private String relevanceProductName;

    //产品分类id集合
    private List<String> categoryIdList;

    /**
     * 禁止修改的字段
     */
    private List<String> disableFieldList;

    /**
     * 产品规划id
     */
    private String productPlanId;


    /**
     * 辅助字段：产品经理
     */
    private String chargeName;

    /**
     * 产品示意图url
     */
    private String imageUrl;

    /**
     * 项目经理
     */
    private String projectChargeId;

    /**
     * 模板id
     */
    private String templateId;


    /**
     * 模板名
     */
    private String templateName;


    /**
     * 产品统计信息
     */
    @Data
    @NoArgsConstructor
    public static class ProductCountDTO {

        /**
         * 新项目数
         */
        private Integer totalCount;

        /**
         * 未立项数
         */
        private Integer notApprovalCount;

        /**
         * 已立项数
         */
        private Integer approvalCount;


        /**
         * 启动的数
         */
        private Integer startCount;

        /**
        * 进行中的数
         */
        private Integer doingCount;


        /**
         * 完成的数
         */
        private Integer finishCount;

        /**
         * 暂停的数
         */
        private Integer suspendCount;

        /**
         * 终止的数
         */
        private Integer terminateCount;


        /**
         * 延期的数
         */
        private Integer delayCount;



    }

    /**
     * 产品统计信息
     */
    @Data
    @NoArgsConstructor
    public static class CountBaseDTO {

        /**
         * 状态
         */
        private Integer status;

        /**
         *数量
         */
        private Integer count;



    }


}
