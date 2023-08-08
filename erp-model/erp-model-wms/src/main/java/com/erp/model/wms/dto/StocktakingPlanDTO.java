package com.erp.model.wms.dto;

import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;

/**
 * <p>
 * 盘点计划表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-08-08
*/
@Data
@NoArgsConstructor
public class StocktakingPlanDTO implements Serializable {


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
         * 搜索类型
         */
         private String  tabFlag;

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
        * 盘点计划单号
        */
        private String code;

        /**
        * 盘点计划名称
        */
        private String name;

        /**
        * 单据审核状态
        */
        private String approveStatus;

        /**
        * 盘点状态
        */
        private String status;

        /**
        * 盘点方式
        */
        private String mode;

        /**
        * 分单规则
        */
        private String separateRule;

        /**
        * 盘点类型
        */
        private String type;

        /**
        * 提交审核时间
        */
        private LocalDateTime submitTime;

        /**
        * 提交审核人id
        */
        private String submitUserId;

        /**
        * 提交审核人名称
        */
        private String submitUserName;

        /**
        * 最后审核人id
        */
        private String approveUserId;

        /**
        * 最后审核人名称
        */
        private String approveUserName;

        /**
        * 最后审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 动销开始时间
        */
        private LocalDateTime startTime;

        /**
        * 动销结束时间
        */
        private LocalDateTime endTime;


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
        * 盘点计划单号
        */
        private String code;

        /**
        * 盘点计划名称
        */
        private String name;

        /**
        * 单据审核状态
        */
        private String approveStatus;

        /**
        * 盘点状态
        */
        private String status;

        /**
        * 盘点方式
        */
        private String mode;

        /**
        * 分单规则
        */
        private String separateRule;

        /**
        * 盘点类型
        */
        private String type;

        /**
        * 提交审核时间
        */
        private LocalDateTime submitTime;

        /**
        * 提交审核人id
        */
        private String submitUserId;

        /**
        * 提交审核人名称
        */
        private String submitUserName;

        /**
        * 最后审核人id
        */
        private String approveUserId;

        /**
        * 最后审核人名称
        */
        private String approveUserName;

        /**
        * 最后审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 动销开始时间
        */
        private LocalDateTime startTime;

        /**
        * 动销结束时间
        */
        private LocalDateTime endTime;


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
    public static class CommonDTO {

        /**
        * 盘点计划名称
        */
        @NotBlank(message = "盘点计划名称不能为空")
        @Size(max = 80,message = "盘点计划名称最大长度不能超过80位")
        private String name;

        /**
        * 盘点状态
        */
        @NotBlank(message = "盘点状态不能为空")
        @Size(max = 30,message = "盘点状态最大长度不能超过30位")
        private String status;

        /**
        * 盘点方式
        */
        @NotBlank(message = "盘点方式不能为空")
        @Size(max = 50,message = "盘点方式最大长度不能超过50位")
        private String mode;

        /**
        * 分单规则
        */
        @NotBlank(message = "分单规则不能为空")
        @Size(max = 50,message = "分单规则最大长度不能超过50位")
        private String separateRule;

        /**
        * 盘点类型
        */
        @NotBlank(message = "盘点类型不能为空")
        @Size(max = 50,message = "盘点类型最大长度不能超过50位")
        private String type;

        /**
        * 提交审核时间
        */
        private LocalDateTime submitTime;

        /**
        * 提交审核人id
        */
        @NotBlank(message = "提交审核人id不能为空")
        @Size(max = 19,message = "提交审核人id最大长度不能超过19位")
        private String submitUserId;

        /**
        * 提交审核人名称
        */
        @NotBlank(message = "提交审核人名称不能为空")
        @Size(max = 50,message = "提交审核人名称最大长度不能超过50位")
        private String submitUserName;

        /**
        * 动销开始时间
        */
        private LocalDateTime startTime;

        /**
        * 动销结束时间
        */
        private LocalDateTime endTime;


    }


}