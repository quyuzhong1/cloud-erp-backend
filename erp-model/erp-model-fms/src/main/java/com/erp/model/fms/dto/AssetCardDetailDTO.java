package com.erp.model.fms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 资产卡片明细表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetCardDetailDTO implements Serializable {




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
        * 来源明细ID
        */
        private String sourceDetailId;

        /**
        * 主表ID
        */
        private String mainId;

        /**
        * 资产编码
        */
        private String assetCode;

        /**
        * 资产位置ID
        */
        private String assetLocationId;

        /**
        * 资产位置名称
        */
        private String assetLocationName;

        /**
        * 数量
        */
        private Integer qty;

        /**
        * 供应商ID
        */
        private String supplierId;

        /**
        * 供应商名称
        */
        private String supplierName;

        /**
        * 使用部门名称
        */
        private String useDeptName;

        /**
        * 使用部门ID
        */
        private String useDeptId;

        /**
        * 费用项目（折旧费）
        */
        private String costType;

        /**
        * 费用项目名称
        */
        private String costTypeName;

        /**
        * 备注
        */
        private String remark;


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


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * id 新增这个字段为空
         */
        private String id;

        /**
        * 来源明细ID
        */
        @Size(max = 19,message = "来源明细ID最大长度不能超过19位")
        private String sourceDetailId;

        /**
        * 主表ID
        */
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * 资产编码
        */
        @NotBlank(message = "资产编码不能为空")
        @Size(max = 50,message = "资产编码最大长度不能超过50位")
        private String assetCode;

        /**
        * 资产位置ID
        */
        @NotBlank(message = "资产位置ID不能为空")
        @Size(max = 19,message = "资产位置ID最大长度不能超过19位")
        private String assetLocationId;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 供应商ID
        */
        @NotBlank(message = "供应商ID不能为空")
        @Size(max = 19,message = "供应商ID最大长度不能超过19位")
        private String supplierId;

        /**
        * 供应商名称
        */
        @Size(max = 100,message = "供应商名称最大长度不能超过100位")
        private String supplierName;

        /**
        * 使用部门名称
        */
        @Size(max = 50,message = "使用部门名称最大长度不能超过50位")
        private String useDeptName;

        /**
        * 使用部门ID
        */
        @NotBlank(message = "使用部门ID不能为空")
        @Size(max = 19,message = "使用部门ID最大长度不能超过19位")
        private String useDeptId;

        /**
        * 费用项目（折旧费）
        */
        @NotBlank(message = "费用项目（折旧费）不能为空")
        @Size(max = 50,message = "费用项目（折旧费）最大长度不能超过50位")
        private String costType;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


    /**
     * 模糊搜索参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {
        /**
         * 卡片主表id
         */
        private List<String> cardIds;
        /**
         * 模糊搜索参数
         */
        private String searchKeyword;
    }

    /**
     * 模糊搜索参数
     */
    @Data
    @NoArgsConstructor
    public static class SearchCardDetailDTO {
        /**
         * id
         */
        private String id;

        /**
         * 主表ID
         */
        private String mainId;
        /**
         * 资产编码
         */
        private String assetCode;
        /**
         * 资产位置ID
         */
        private String assetLocationId;
        private String assetLocationName;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 供应商ID
         */
        private String supplierId;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 使用部门名称
         */
        private String useDeptName;
        /**
         * 使用部门ID
         */
        private String useDeptId;
        /**
         * 费用项目（折旧费）
         */
        private String costType;
    }


}