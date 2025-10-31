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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.enums.ClientTypeEnum;
import java.util.Map;

/**
 * <p>
 * 样品转移单主表请求响应实体
 * </p>
 *
 * @author wuhaotian
 * @since 2025-10-28
*/
@Data
@NoArgsConstructor
public class SampleTransferInfoDTO implements Serializable {


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

         public TabListDTO(String tabFlag, Integer count) {
             this.tabFlag = tabFlag;
             this.count = count;
         }

         /**
         * 类型名称
         */
         private String tabFlagName;

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
        * 转移单号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批完成时间
        */
        private LocalDateTime approveTime;

        /**
        * 审批人ID
        */
        private String approveUserId;

        /**
        * 审批人姓名（最新审核人）
        */
        private String approveUserName;

        /**
        * 是否作废
        */
        private Boolean invalidStatus;

        /**
        * 转出人ID
        */
        private String transferOutUserId;

        /**
        * 转出人姓名
        */
        private String transferOutUserName;

        /**
        * 转出人部门ID
        */
        private String transferOutDeptId;

        /**
        * 转出人部门名称
        */
        private String transferOutDeptName;

        /**
        * 转入人ID
        */
        private String transferInUserId;

        /**
        * 转入人姓名
        */
        private String transferInUserName;

        /**
        * 转入人部门ID
        */
        private String transferInDeptId;

        /**
        * 转入人部门名称
        */
        private String transferInDeptName;

        /**
        * 转移日期
        */
        private LocalDate transferDate;

        /**
        * 备注说明
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;


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

        /**
        * 明细ID（用于导出）
        */
        private String detailId;

        /**
        * SKU编号（明细）
        */
        private String skuNo;

        /**
        * SKU ID（明细）
        */
        private String skuId;

        /**
        * 产品名称（明细）
        */
        private String productName;

        /**
        * 转移数量（明细）
        */
        private Integer transferQty;

        /**
        * 明细备注
        */
        private String detailRemark;
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
        * 转移单号
        */
        private String code;

        /**
        * 审批状态
        */
        private String approveStatus;

        /**
        * 审批完成时间
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
        * 转出人ID
        */
        private String transferOutUserId;

        /**
        * 转出人姓名
        */
        private String transferOutUserName;

        /**
        * 转出人部门ID
        */
        private String transferOutDeptId;

        /**
        * 转出人部门名称
        */
        private String transferOutDeptName;

        /**
        * 转入人ID
        */
        private String transferInUserId;

        /**
        * 转入人姓名
        */
        private String transferInUserName;

        /**
        * 转入人部门ID
        */
        private String transferInDeptId;

        /**
        * 转入人部门名称
        */
        private String transferInDeptName;

        /**
        * 转移日期
        */
        private LocalDate transferDate;

        /**
        * 备注说明
        */
        private String remark;

        /**
        * 作废备注
        */
        private String invalidRemark;

        /**
        * 明细列表
        */
        private List<SampleTransferDetailDTO.ViewDTO> detailList;

        /**
        * 附件名称列表
        */
        private List<String> attachmentNameList;

        /**
        * 附件URL列表
        */
        private List<String> attachmentUrlList;

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
        @NotEmpty(message = "明细不能为空")
        private List<SampleTransferDetailDTO.@Valid AddDTO> detailList;

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
        @NotEmpty(message = "明细不能为空")
        private List<SampleTransferDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 客户端类型
        */
        private ClientTypeEnum clientType = ClientTypeEnum.WEB;

        /**
        * 转出人ID
        */
        @NotBlank(message = "转出人ID不能为空")
        @Size(max = 19,message = "转出人ID最大长度不能超过19位")
        private String transferOutUserId;

        /**
        * 转出人姓名
        */
        @Size(max = 32,message = "转出人姓名最大长度不能超过32位")
        private String transferOutUserName;

        /**
        * 转出人部门ID
        */
        @NotBlank(message = "转出人部门ID不能为空")
        @Size(max = 19,message = "转出人部门ID最大长度不能超过19位")
        private String transferOutDeptId;

        /**
        * 转出人部门名称
        */
        @Size(max = 32,message = "转出人部门名称最大长度不能超过32位")
        private String transferOutDeptName;

        /**
        * 转入人ID
        */
        @NotBlank(message = "转入人ID不能为空")
        @Size(max = 19,message = "转入人ID最大长度不能超过19位")
        private String transferInUserId;

        /**
        * 转入人姓名
        */
        @Size(max = 32,message = "转入人姓名最大长度不能超过32位")
        private String transferInUserName;

        /**
        * 转入人部门ID
        */
        @NotBlank(message = "转入人部门ID不能为空")
        @Size(max = 19,message = "转入人部门ID最大长度不能超过19位")
        private String transferInDeptId;

        /**
        * 转入人部门名称
        */
        @Size(max = 32,message = "转入人部门名称最大长度不能超过32位")
        private String transferInDeptName;

        /**
        * 转移日期
        */
        @NotNull(message = "转移日期不能为空")
        private LocalDate transferDate;

        /**
        * 备注说明
        */
        @Size(max = 200,message = "备注说明最大长度不能超过200位")
        private String remark;

        /**
        * 附件名称列表
        */
        private List<String> attachmentNameList;

        /**
        * 附件URL列表
        */
        private List<String> attachmentUrlList;

    }


}