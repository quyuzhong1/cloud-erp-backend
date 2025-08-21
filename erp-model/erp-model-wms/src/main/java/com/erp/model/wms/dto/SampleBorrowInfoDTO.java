package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 借用变更单请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleBorrowInfoDTO implements Serializable {


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
        * 单据编号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 借用人ID
        */
        private String borrowUserId;

        /**
        * 借用人姓名
        */
        private String borrowUserName;

        /**
        * 借用部门ID
        */
        private String borrowDeptId;

        /**
        * 借用部门名称
        */
        private String borrowDeptName;

        /**
        * 借出人ID
        */
        private String lendUserId;

        /**
        * 借出人姓名
        */
        private String lendUserName;

        /**
        * 借出部门ID
        */
        private String lendDeptId;

        /**
        * 借出部门名称
        */
        private String lendDeptName;

        /**
        * 借用日期
        */
        private LocalDate borrowDate;

        /**
        * 预计退回日期
        */
        private LocalDate estimatedReturnDate;

        /**
        * 备注
        */
        private String remark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

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
        * 单据编号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 借用人ID
        */
        private String borrowUserId;

        /**
        * 借用人姓名
        */
        private String borrowUserName;

        /**
        * 借用部门ID
        */
        private String borrowDeptId;

        /**
        * 借用部门名称
        */
        private String borrowDeptName;

        /**
        * 借出人ID
        */
        private String lendUserId;

        /**
        * 借出人姓名
        */
        private String lendUserName;

        /**
        * 借出部门ID
        */
        private String lendDeptId;

        /**
        * 借出部门名称
        */
        private String lendDeptName;

        /**
        * 借用日期
        */
        private LocalDate borrowDate;

        /**
        * 预计退回日期
        */
        private LocalDate estimatedReturnDate;

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
        * 借用人ID
        */
        @NotBlank(message = "借用人ID不能为空")
        @Size(max = 19,message = "借用人ID最大长度不能超过19位")
        private String borrowUserId;

        /**
        * 借用人姓名
        */
        @NotBlank(message = "借用人姓名不能为空")
        @Size(max = 32,message = "借用人姓名最大长度不能超过32位")
        private String borrowUserName;

        /**
        * 借用部门ID
        */
        @NotBlank(message = "借用部门ID不能为空")
        @Size(max = 19,message = "借用部门ID最大长度不能超过19位")
        private String borrowDeptId;

        /**
        * 借用部门名称
        */
        @NotBlank(message = "借用部门名称不能为空")
        @Size(max = 32,message = "借用部门名称最大长度不能超过32位")
        private String borrowDeptName;

        /**
        * 借出人ID
        */
        @NotBlank(message = "借出人ID不能为空")
        @Size(max = 19,message = "借出人ID最大长度不能超过19位")
        private String lendUserId;

        /**
        * 借出人姓名
        */
        @NotBlank(message = "借出人姓名不能为空")
        @Size(max = 32,message = "借出人姓名最大长度不能超过32位")
        private String lendUserName;

        /**
        * 借出部门ID
        */
        @NotBlank(message = "借出部门ID不能为空")
        @Size(max = 19,message = "借出部门ID最大长度不能超过19位")
        private String lendDeptId;

        /**
        * 借出部门名称
        */
        @NotBlank(message = "借出部门名称不能为空")
        @Size(max = 32,message = "借出部门名称最大长度不能超过32位")
        private String lendDeptName;

        /**
        * 借用日期
        */
        private LocalDate borrowDate;

        /**
        * 预计退回日期
        */
        private LocalDate estimatedReturnDate;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;


    }


}