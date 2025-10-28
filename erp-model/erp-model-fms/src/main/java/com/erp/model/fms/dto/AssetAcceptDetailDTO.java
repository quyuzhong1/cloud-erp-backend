package com.erp.model.fms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import com.erp.model.fms.enums.AssetCardStatusEnum;

/**
 * <p>
 * 资产验收表明细表请求响应实体
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
*/
@Data
@NoArgsConstructor
public class AssetAcceptDetailDTO implements Serializable {




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
        * SKU编号
        */
        private String skuNo;

        /**
        * SKU ID
        */
        private String skuId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 验收数量
        */
        private Integer acceptQty;

        /**
        * 资产卡片关联状态（已生成、未生成）
        */
        private String assetCardStatus;

        /**
        * 待验收数量
        */
        private Integer pendingQty;

        /**
        * 已验收数量
        */
        private Integer acceptedQty;

        /**
        * 可验收数量
        */
        private Integer acceptableQty;

        /**
        * 资产位置ID
        */
        private String assetLocationId;

        /**
        * 使用部门名称
        */
        private String useDeptName;

        /**
        * 使用部门ID
        */
        private String useDeptId;

        /**
        * 费用项目
        */
        private String costType;

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
        @NotBlank(message = "主表ID不能为空")
        @Size(max = 19,message = "主表ID最大长度不能超过19位")
        private String mainId;

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        @Size(max = 19,message = "SKU ID最大长度不能超过19位")
        private String skuId;

        /**
        * 产品名称
        */
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 验收数量
        */
        @NotNull(message = "验收数量不能为空")
        private Integer acceptQty;

        /**
        * 资产卡片关联状态（已生成、未生成）
        */
        @Size(max = 20,message = "资产卡片关联状态（已生成、未生成）最大长度不能超过20位")
        private String assetCardStatus;

        /**
         * 获取资产卡片关联状态枚举
         * @return 枚举
         */
        public AssetCardStatusEnum getAssetCardStatusEnum() {
            return AssetCardStatusEnum.getByStatus(this.assetCardStatus);
        }

        /**
        * 待验收数量
        */
        private Integer pendingQty;

        /**
        * 已验收数量
        */
        private Integer acceptedQty;

        /**
        * 可验收数量
        */
        private Integer acceptableQty;

        /**
        * 资产位置ID
        */
        @NotBlank(message = "资产位置ID不能为空")
        @Size(max = 19,message = "资产位置ID最大长度不能超过19位")
        private String assetLocationId;

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
        * 费用项目 dict/drop/down?costType
        */
        @NotBlank(message = "费用项目不能为空")
        @Size(max = 50,message = "费用项目最大长度不能超过50位")
        private String costType;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}