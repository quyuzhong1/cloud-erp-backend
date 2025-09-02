package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 客户授信请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@NoArgsConstructor
public class CustomerCreditApplyDTO implements Serializable {


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
        * 单据编码
        */
        private String code;

        /**
        * 客户Id
        */
        private String customerId;


        /**
         * 客户编码
         */
        private String customerCode;
        /**
         * 客户名称
         */
        private String customerName;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 授信类型
        */
        private String creditType;
        /**
         * 授信类型名称
         */
        private String creditTypeName;

        /**
        * 授信状态
        */
        private String creditStatus;

        /**
         * 授信状态名称
         */
        private String creditStatusName;

        /**
        * 授信额度
        */
        private BigDecimal creditAmount;

        /**
        * 币种
        */
        private String currency;

        /**
        * 销售员
        */
        private String saleUserId;

        /**
         * 销售员名称
         */
        private String saleUserName;

        /**
        * 销售组织id
        */
        private String saleOrgId;

        /**
         * 销售组织id名称
         */
        private String saleOrgName;

        /**
        * 审核备注

        */
        private String approveRemark;

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
        * 单据编码
        */
        private String code;

        /**
        * 客户Id
        */
        private String customerId;

        /**
         * 客户名称
         */
        private String customerName;
        /**
        * 授信类型
        */
        private String creditType;

        /**
         * 授信类型名称
         */
        private String creditTypeName;

        /**
        * 授信额度
        */
        private BigDecimal creditAmount;

        /**
        * 币种
        */
        private String currency;

        /**
        * 账期
        */
        private String period;

        /**
         * 账期名称
         */
        private String periodName;
        /**
        * 贸易条款
        */
        private String tradeTerm;


        /**
        * 审核备注
        */
        private String approveRemark;

        /**
         * 授信额度测评表
         */
        private List<AttachDTO> evaluationAttachmentList;

        /**
         * 其他附件
         */
        private List<AttachDTO> otherAttachmentList;
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
        * 客户Id
        */
        @NotBlank(message = "客户Id不能为空")
        private String customerId;

        /**
        * 授信类型
        */
        private String creditType;

        /**
        * 授信额度
        */
        @NotNull(message = "授信额度不能为空")
        @Digits(integer = 28, fraction = 4, message = "授信额度整数位不能超过28位，小数位不能超过4位")
        private BigDecimal creditAmount;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 32,message = "币种最大长度不能超过32位")
        private String currency;

        /**
        * 销售员
        */
        private String saleUserId;

        /**
        * 销售组织id
        */
        @NotBlank(message = "销售组织id不能为空")
        @Size(max = 32,message = "销售组织id最大长度不能超过32位")
        private String saleOrgId;

        /**
        * 账期
        */
        @NotBlank(message = "账期不能为空")
        @Size(max = 32,message = "账期最大长度不能超过32位")
        private String period;

        /**
        * 贸易条款
        */
        @NotBlank(message = "贸易条款不能为空")
        @Size(max = 255,message = "贸易条款最大长度不能超过255位")
        private String tradeTerm;

        /**
        * 销售员部门
        */
        private String saleDeptId;

        /**
        * 审核备注
        */
        private String approveRemark;

        /**
         * 授信额度测评表
         */
        private List<AttachDTO> evaluationAttachmentList;

        /**
         * 其他附件
         */
        private List<AttachDTO> otherAttachmentList;

    }


}