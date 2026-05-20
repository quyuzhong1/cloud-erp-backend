package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 费用项配置字段配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@NoArgsConstructor
public class CfgLogisticsCostImportDetailDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }


     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 物流商抬头字段
        */
        private String sourceField;

        /**
        * 物流商明细字段
        */
        private String sourceDetailField;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue;

        /**
        * ERP字段id
        */
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

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
        * 物流商抬头字段
        */
        private String sourceField;

        /**
        * 物流商明细字段
        */
        private String sourceDetailField;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue;

        /**
        * ERP字段id
        */
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;


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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 主表id
         */
        private String mainId;

        /**
        * 物流商抬头字段
        */
        @NotBlank(message = "物流商抬头字段不能为空")
        @Size(max = 200,message = "物流商抬头字段最大长度不能超过200位")
        private String sourceField;

        /**
        * 物流商明细字段
        */
        @Size(max = 200,message = "物流商明细字段最大长度不能超过200位")
        private String sourceDetailField;

        /**
        * 是否唯一
        */
        private Boolean isUniqueKey = false;

        /**
        * 是否绝对值
        */
        private Boolean isAbsoluteValue = false;

        /**
        * ERP字段id
        */
        @NotBlank(message = "ERP字段id不能为空")
        private String targetFieldId;

        /**
        * ERP字段
        */
        private String targetField;

        /**
        * ERP字段名称
        */
        private String targetFieldName;

        /**
        * ERP字段类型
        */
        private String targetFieldType;
        /**
         * 费用项id
         */
        private String targetDetailFieldId;
        /**
         * 费用项
         */
        private String targetDetailField;
        /**
         * 费用项名称
         */
        private String targetDetailFieldName;
        /**
         * 排序
         */
        private Integer index;


    }


}