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
         * 归属部门
         */
        private String deptName;

        /**
        * 业务日期
        */
        private LocalDate billDate;

        /**
        * SKU编码
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
         * 详情
         */
        private List<SampleInitialLedgerDTO.DetailDTO>detailList;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
        * 明细列表
        */
        @NotEmpty(message = "明细列表不能为空")
        private List<DetailDTO> detailList;

    }

    /**
    * 明细DTO
    */
    @Data
    @NoArgsConstructor
    public static class DetailDTO {

        /**
        * SKU ID
        */
        @NotBlank(message = "SKU ID不能为空")
        private String skuId;

        /**
        * SKU编码
        */
        @NotBlank(message = "SKU编码不能为空")
        private String skuNo;

        /**
        * 数量
        */
        @NotNull(message = "数量不能为空")
        private Integer qty;

        /**
        * 备注
        */
        private String remark;

        /**
        * 产品名称（通过feign获取，不需要前端传递）
        */
        private String productName;
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

        /**
        * 明细列表
        */
        @NotEmpty(message = "明细列表不能为空")
        private List<DetailDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 归属人ID
        */
        @NotBlank(message = "归属人ID不能为空")
        @Size(max = 19,message = "归属人ID最大长度不能超过19位")
        private String userId;

        /**
        * 归属部门ID
        */
        @NotBlank(message = "归属部门ID不能为空")
        @Size(max = 19,message = "归属部门ID最大长度不能超过19位")
        private String deptId;

        /**
        * 业务日期
        */
        @NotEmpty(message = "业务日期不能为空")
        private LocalDate billDate;


    }


}