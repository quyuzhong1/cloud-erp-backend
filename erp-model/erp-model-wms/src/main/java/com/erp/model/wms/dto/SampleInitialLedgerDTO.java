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
 * 样品期初台账请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
*/
@Data
@NoArgsConstructor
public class SampleInitialLedgerDTO implements Serializable {


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
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 期初台账单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 归属人ID
        */
        private String userId;

        /**
        * 归属人姓名
        */
        private String userName;

        /**
        * 归属部门ID
        */
        private String deptId;

        /**
        * 业务日期
        */
        private LocalDate billDate;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 数量
        */
        private Integer qty;


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
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名
        */
        private String approveUserName;

        /**
        * 审批时间
        */
        private LocalDateTime approveTime;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 期初台账单号
        */
        private String code;

        /**
        * 单据状态
        */
        private String status;

        /**
        * 归属人ID
        */
        private String userId;

        /**
        * 归属人姓名
        */
        private String userName;

        /**
        * 归属部门ID
        */
        private String deptId;

        /**
        * 业务日期
        */
        private LocalDate billDate;

        /**
        * SKU编码
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 数量
        */
        private Integer qty;


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
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 单据状态
        */
        @NotBlank(message = "单据状态不能为空")
        @Size(max = 50,message = "单据状态最大长度不能超过50位")
        private String status;

        /**
        * 归属人ID
        */
        @NotBlank(message = "归属人ID不能为空")
        @Size(max = 19,message = "归属人ID最大长度不能超过19位")
        private String userId;

        /**
        * 归属人姓名
        */
        @NotBlank(message = "归属人姓名不能为空")
        @Size(max = 50,message = "归属人姓名最大长度不能超过50位")
        private String userName;

        /**
        * 归属部门ID
        */
        @NotBlank(message = "归属部门ID不能为空")
        @Size(max = 19,message = "归属部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 业务日期
        */
        private LocalDate billDate;

        /**
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 200,message = "产品名称最大长度不能超过200位")
        private String productName;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;


    }


}