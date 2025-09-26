package com.erp.model.srm.dto;

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
 * 请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-09-25
*/
@Data
@NoArgsConstructor
public class PayableInfoDTO implements Serializable {


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
        * 应付单号
        */
        private String code;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 业务日期
        */
        private LocalDate date;

        /**
        * 组织id
        */
        private String orgId;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 来源类型，取采购对账单
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 备注
        */
        private String remark;

        /**
        * 业务类型,payableType字典
        */
        private String type;

        /**
        * 三方系统id
        */
        private String thirdPayableId;

        /**
        * 三方系统编码
        */
        private String thirdPayableCode;

        /**
        * 审核状态
        */
        private String approveStatus;


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
        * 应付单号
        */
        private String code;

        /**
        * 供应商id
        */
        private String supplierId;

        /**
        * 业务日期
        */
        private LocalDate date;

        /**
        * 组织id
        */
        private String orgId;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 来源类型，取采购对账单
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码
        */
        private String sourceCode;

        /**
        * 备注
        */
        private String remark;

        /**
        * 业务类型,payableType字典
        */
        private String type;

        /**
        * 三方系统id
        */
        private String thirdPayableId;

        /**
        * 三方系统编码
        */
        private String thirdPayableCode;

        /**
        * 审核状态
        */
        private String approveStatus;


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
        * 供应商id
        */
        @NotBlank(message = "供应商id不能为空")
        @Size(max = 19,message = "供应商id最大长度不能超过19位")
        private String supplierId;

        /**
        * 业务日期
        */
        private LocalDate date;

        /**
        * 组织id
        */
        @NotBlank(message = "组织id不能为空")
        @Size(max = 19,message = "组织id最大长度不能超过19位")
        private String orgId;

        /**
        * 来源类型，取采购对账单
        */
        @NotBlank(message = "来源类型，取采购对账单不能为空")
        @Size(max = 64,message = "来源类型，取采购对账单最大长度不能超过64位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编码
        */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 32,message = "来源编码最大长度不能超过32位")
        private String sourceCode;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 业务类型,payableType字典
        */
        @NotBlank(message = "业务类型,payableType字典不能为空")
        @Size(max = 32,message = "业务类型,payableType字典最大长度不能超过32位")
        private String type;

        /**
        * 三方系统id
        */
        @NotBlank(message = "三方系统id不能为空")
        @Size(max = 19,message = "三方系统id最大长度不能超过19位")
        private String thirdPayableId;

        /**
        * 三方系统编码
        */
        @NotBlank(message = "三方系统编码不能为空")
        @Size(max = 255,message = "三方系统编码最大长度不能超过255位")
        private String thirdPayableCode;


    }


}