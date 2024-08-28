package com.erp.model.mrp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 公共配置（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@NoArgsConstructor
public class CfgRuleCommonDTO implements Serializable {


    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 序号
         */
        private String  index;


        /**
         * 主表id
         */
        private String  parentId;

        /**
         * 编码
         */
        private String code;

        /**
        * 名称
        */
        private String name;

        /**
        * 值
        */
        private String value;

        /**
         * 类型，（inventory库存配置，suggest建议配置）
         */
        private String type;

        /**
         * 字段类型，single单选,multiple多选
         */
        private String fieldType;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        private String platformType;

        /**
         * 分类名称
         */
        private String categoryName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 是否默认
         */
        private Boolean isDefault;

        /**
         * 是否必填
         */
        private Boolean isRequired;

        /**
         * 是否变更
         */
        private Boolean isChange;

        /**
         * 子级信息
         */
        List<ViewDTO> childrenList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;

        /**
        * 上级id
        */
        @Size(max = 19,message = "上级id最大长度不能超过19位")
        private String parentId;

        /**
         * 编码
         */
        @NotBlank(message = "编码不能为空")
        private String code;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 值
        */
        @NotBlank(message = "值不能为空")
        private String value;

        /**
        * 类型，（inventory库存配置，suggest建议配置）
        */
        @NotBlank(message = "类型，（inventory库存配置，suggest建议配置）不能为空")
        @Size(max = 32,message = "类型，（inventory库存配置，suggest建议配置）最大长度不能超过32位")
        private String type;

        /**
         * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
         */
        @NotBlank(message = "平台类型不能为空")
        @Size(max = 32,message = "平台类型最大长度不能超过32位")
        private String platformType;

        /**
         * 字段类型，single单选,multiple多选
         */
        @Size(max = 32,message = "字段类型最大长度不能超过32位")
        private String fieldType;

        /**
         * 分类名称
         */
        @Size(max = 32,message = "分类名称最大长度不能超过32位")
        private String categoryName;

        /**
         * 备注
         */
        @TableField("remark")
        private String remark;

        /**
         * 是否默认
         */
        @NotNull(message = "是否默认不能为空")
        private Boolean isDefault;

        /**
         * 是否必填
         */
        @NotNull(message = "是否必填不能为空")
        private Boolean isRequired;

        /**
         * 是否变更
         */
        @NotNull(message = "是否变更不能为空")
        private Boolean isChange;

        /**
         * 子级信息
         */
        List<UpdateDTO> childrenList;
    }


}