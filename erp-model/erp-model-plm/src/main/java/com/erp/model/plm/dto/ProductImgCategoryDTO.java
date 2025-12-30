package com.erp.model.plm.dto;

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
 * 图片分类表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
*/
@Data
@NoArgsConstructor
public class ProductImgCategoryDTO implements Serializable {



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
        * 分类名称
        */
        private String name;

        /**
        * 父级分类ID，空字符串表示根节点
        */
        private String parentId;

        /**
        * 第几级分类（避免递归计算），1表示第一级
        */
        private Integer level;

        /**
        * 是否是系统自带，系统自带的分类不允许删除
        */
        private Boolean isSystem;

        /**
        * 排序字段，用于同级分类的排序
        */
        private Integer sort;


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
        * 分类名称
        */
        private String name;

        /**
        * 父级分类ID，空字符串表示根节点
        */
        private String parentId;

        /**
        * 第几级分类（避免递归计算），1表示第一级
        */
        private Integer level;

        /**
        * 是否是系统自带，系统自带的分类不允许删除
        */
        private Boolean isSystem;

        /**
        * 排序字段，用于同级分类的排序
        */
        private Integer sort;


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
        * 分类名称
        */
        @NotBlank(message = "分类名称不能为空")
        @Size(max = 100,message = "分类名称最大长度不能超过100位")
        private String name;

        /**
        * 父级分类ID，空字符串表示根节点
        */
        private String parentId;

        /**
        * 第几级分类（避免递归计算），1表示第一级
        */
        @NotNull(message = "第几级分类（避免递归计算），1表示第一级不能为空")
        private Integer level;

        /**
        * 是否是系统自带，系统自带的分类不允许删除
        */
        @NotNull(message = "是否是系统自带，系统自带的分类不允许删除不能为空")
        private Boolean isSystem;

        /**
        * 排序字段，用于同级分类的排序
        */
        @NotNull(message = "排序字段，用于同级分类的排序不能为空")
        private Integer sort;


    }


}