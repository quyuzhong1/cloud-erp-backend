package com.erp.model.tms.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 费用项配置字段基础表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-20
*/
@Data
@NoArgsConstructor
public class CfgLogisticsCostImportFieldDTO implements Serializable {



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
        * 系统分类
        */
        private String sysClassify;

        /**
        * 配置单据
        */
        private String businessType;

        /**
        * 数据表
        */
        private String tableName;

        /**
        * 数据表(中文 )
        */
        private String tableCnName;

        /**
        * class_path
        */
        private String classPath;

        /**
        * 父id
        */
        private String parentId;

        /**
        * ERP字段
        */
        private String field;

        /**
        * ERP字段名称
        */
        private String fieldName;

        /**
        * ERP字段类型：api=API,excel=线下表格:
        */
        private String fieldType;


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
        * 系统分类
        */
        private String sysClassify;

        /**
        * 配置单据
        */
        private String businessType;

        /**
        * 数据表
        */
        private String tableName;

        /**
        * 数据表(中文 )
        */
        private String tableCnName;

        /**
        * class_path
        */
        private String classPath;

        /**
        * 父id
        */
        private String parentId;

        /**
        * ERP字段
        */
        private String field;

        /**
        * ERP字段名称
        */
        private String fieldName;

        /**
        * ERP字段类型：api=API,excel=线下表格:
        */
        private String fieldType;


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
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 系统分类
        */
        @NotBlank(message = "系统分类不能为空")
        @Size(max = 50,message = "系统分类最大长度不能超过50位")
        private String sysClassify;

        /**
        * 配置单据
        */
        @NotBlank(message = "配置单据不能为空")
        @Size(max = 50,message = "配置单据最大长度不能超过50位")
        private String businessType;

        /**
        * 数据表
        */
        @NotBlank(message = "数据表不能为空")
        @Size(max = 100,message = "数据表最大长度不能超过100位")
        private String tableName;

        /**
        * 数据表(中文 )
        */
        @NotBlank(message = "数据表(中文 )不能为空")
        @Size(max = 100,message = "数据表(中文 )最大长度不能超过100位")
        private String tableCnName;

        /**
        * class_path
        */
        @NotBlank(message = "class_path不能为空")
        @Size(max = 100,message = "class_path最大长度不能超过100位")
        private String classPath;

        /**
        * 父id
        */
        @NotBlank(message = "父id不能为空")
        @Size(max = 19,message = "父id最大长度不能超过19位")
        private String parentId;

        /**
        * ERP字段
        */
        @NotBlank(message = "ERP字段不能为空")
        @Size(max = 100,message = "ERP字段最大长度不能超过100位")
        private String field;

        /**
        * ERP字段名称
        */
        @NotBlank(message = "ERP字段名称不能为空")
        @Size(max = 200,message = "ERP字段名称最大长度不能超过200位")
        private String fieldName;

        /**
        * ERP字段类型：api=API,excel=线下表格:
        */
        @NotBlank(message = "ERP字段类型：api=API,excel=线下表格:不能为空")
        @Size(max = 64,message = "ERP字段类型：api=API,excel=线下表格:最大长度不能超过64位")
        private String fieldType;


    }


}