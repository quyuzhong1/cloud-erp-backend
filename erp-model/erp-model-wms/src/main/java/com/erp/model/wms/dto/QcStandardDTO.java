package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.wms.entity.WmsAttachmentEntity;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 质检标准 DTO
 *
 * @author jack
 * @since 2026-03-22
 */
public class QcStandardDTO {

    /**
     * 公共业务字段
     */
    @Data
    public static class CommonDTO implements Serializable {

        /**
         * SKU内部ID
         */
        @NotBlank(message = "SKU内部ID不能为空")
        private String skuId;

        /**
         * SKU编码
         */
        private String skuNo;

        /**
         * 备注
         */
        private String remark;


        /**
         * 质检项目明细列表
         */
        @NotEmpty(message = "质检项目明细不能为空")
        private List<@Valid DetailDTO> detailList;

        /**
         * 参考图片
         */
        private List<AttachDTO> attachmentList;

        private  List<WmsAttachmentEntity> wmsAttachmentEntities;

    }

    /**
     * 新增 DTO
     */
    @Data
    public static class AttachDTO{

        /**
         * 图片类型
         */
        private String type ;
        private String typeName ;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;

        private List<String> attachmentUrlList;

    }
    /**
     * 新增 DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class AddDTO extends CommonDTO {
    }

    /**
     * 修改 DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateDTO extends CommonDTO {
        /**
         * 质检标准主键ID
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

    /**
     * 明细 DTO
     */
    @Data
    public static class DetailDTO implements Serializable {
        /**
         * 明细ID
         */
        private String id;

        /**
         * 排序
         */
        private Integer sort;

        /**
         * 质检项目名称
         */
        @NotBlank(message = "质检项目不能为空")
        private String inspectItemName;

        /**
         * 质检要求描述
         */
        @NotBlank(message = "质检要求不能为空")
        private String inspectRequirement;
    }



    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class PagingParamDTO extends SortDTO {
        /**
         * 页面高级查询解析后的条件列表
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 注入容器，默认key为 default
         */
        private Map<String, String> sqlMap;

        /**
         * 页面勾选的记录ID集合
         */
        private List<String> ids;
    }

    /**
     * 列表返回 DTO
     */
    @Data
    public static class ListDTO implements Serializable {
        /**
         * 质检标准ID
         */
        private String id;
        
        /**
         * SKU内部ID
         */
        private String skuId;
        
        /**
         * SKU编码
         */
        private String skuNo;
        
        /**
         * 产品名称
         */
        private String productName;
        
        /**
         * 备注
         */
        private String remark;
        
        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 是否禁用
         */
        private String disabledName;
        
        /**
         * 创建人姓名id
         */
        private String createUserId;
        /**
         * 创建人姓名
         */
        private String createUserName;
        
        /**
         * 创建时间
         */
        private String createTime;
        
        /**
         * 更新人姓名id
         */
        private String updateUserId;
        /**
         * 更新人姓名
         */
        private String updateUserName;
        
        /**
         * 更新时间
         */
        private String updateTime;
    }


    /**
     * 列表返回 DTO
     */
    @Data
    public static class ExportDTO extends ListDTO implements Serializable {
        /**
         * 质检项目
         */
        private String inspectItemName;

        /**
         * 质检要求
         */
        private String inspectRequirement;

        /**
         * 产品实物
         */
        private String productPhysicalUrl;

        /**
         * 包装配件
         */
        private String packagingAccessoriesUrl;
    }
    /**
     * 详情返回 DTO
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ViewDTO extends CommonDTO {
        /**
         * 质检标准ID
         */
        private String id;
        
        /**
         * 是否禁用
         */
        private Boolean disabled;


        /**
         * 创建人ID
         */
        private String productName;
    }

    /**
     * 状态统计返回 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 标签标识：all=全部, ENABLE=已启用, DISABLE=已禁用
         */
        private String tabFlag;

        /**
         * 标签名称
         */
        private String tabFlagName;

        /**
         * 对应状态的数量计数
         */
        private Integer count;
    }

    /**
     * 状态更新 DTO
     */
    @Data
    public static class UpdateStatusDTO implements Serializable {
        /**
         * 记录ID
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 目标状态：false=启用, true=禁用
         */
        @NotNull(message = "状态必须指定")
        private Boolean disabled;
    }

    /**
     * 批量状态更新 DTO
     */
    @Data
    public static class UpdateStatusBatchDTO implements Serializable {
        /**
         * 记录ID结合
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;

        /**
         * 目标状态
         */
        @NotNull(message = "状态必须指定")
        private Boolean disabled;
    }
}
