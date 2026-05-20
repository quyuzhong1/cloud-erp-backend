package com.erp.model.sys.dto;

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
 * 部门关联表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-12-29
*/
@Data
@NoArgsConstructor
public class CfgDeptRelationDTO implements Serializable {



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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 部门id
        */
        private String deptId;

        /**
        * 军区id
        */
        private String partitionId;

        /**
        * 平台编码
        */
        private String dictPlatform;


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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 部门id
        */
        private String deptId;

        /**
        * 军区id
        */
        private String partitionId;

        /**
        * 平台编码
        */
        private String dictPlatform;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 部门id
        */
        @NotBlank(message = "部门id不能为空")
        @Size(max = 19,message = "部门id最大长度不能超过19位")
        private String deptId;

        /**
        * 军区id
        */
        @NotBlank(message = "军区id不能为空")
        @Size(max = 255,message = "军区id最大长度不能超过255位")
        private String partitionId;

        /**
        * 平台编码
        */
        @NotBlank(message = "平台编码不能为空")
        @Size(max = 255,message = "平台编码最大长度不能超过255位")
        private String dictPlatform;


    }


}