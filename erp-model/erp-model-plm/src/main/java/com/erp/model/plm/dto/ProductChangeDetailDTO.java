package com.erp.model.plm.dto;

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
 * 产品变更信息表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
*/
@Data
@NoArgsConstructor
public class ProductChangeDetailDTO implements Serializable {



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
        * 主表id
        */
        private String mainId;

        /**
        * 变更字段
        */
        private String field;

        /**
        * 变更原值
        */
        private Object oldValue;

        /**
        * 变更新值
        */
        private Object newValue;

        /**
        * 备注
        */
        private String remark;

        /**
        * 分组信息
        */
        private String groupName;


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
        * 主表id
        */
        private String mainId;

        /**
        * 变更字段
        */
        private String field;

        /**
        * 变更原值
        */
        private Object oldValue;

        /**
        * 变更新值
        */
        private Object newValue;

        /**
        * 备注
        */
        private String remark;

        /**
        * 分组信息
        */
        private String groupName;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 255,message = "主表id最大长度不能超过255位")
        private String mainId;

        /**
        * 变更字段
        */
        @NotBlank(message = "变更字段不能为空")
        @Size(max = 255,message = "变更字段最大长度不能超过255位")
        private String field;

        /**
        * 变更原值
        */
        @NotBlank(message = "变更原值不能为空")
        @Size(max = 255,message = "变更原值最大长度不能超过255位")
        private Object oldValue;

        /**
        * 变更新值
        */
        @NotBlank(message = "变更新值不能为空")
        @Size(max = 255,message = "变更新值最大长度不能超过255位")
        private Object newValue;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 分组信息
        */
        @NotBlank(message = "分组信息不能为空")
        @Size(max = 255,message = "分组信息最大长度不能超过255位")
        private String groupName;


    }


}