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
import java.util.Map;

/**
 * <p>
 * 样品归还单主表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleReturnInfoDTO implements Serializable {


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
         private String tabFlagName;

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


         /**
          * 勾选的id集合
          */
         private List<String> ids;

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
        * 归还单号
        */
        private String code;

        /**
        * 来源单据ID
        */
        private String sourceId;

        /**
        * 来源单据编号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

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
        * 归还人ID
        */
        private String returnUserId;

        /**
        * 归还人姓名
        */
        private String returnUserName;

        /**
        * 归还人部门ID
        */
        private String returnDeptId;

        /**
        * 归还人部门名称
        */
        private String returnDeptName;

        /**
        * 接收人ID
        */
        private String receiverUserId;

        /**
        * 接收人姓名
        */
        private String receiverUserName;

        /**
        * 接收人部门ID
        */
        private String receiverDeptId;

        /**
        * 接收人部门名称
        */
        private String receiverDeptName;

        /**
        * 归还日期
        */
        private LocalDate returnDate;

        /**
        * 备注说明
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

        /**
         * 明细id
         */
        private String  detailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 归还数量
         */
        private Integer returnQty;

        /**
         * 备注
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
        * 归还单号
        */
        private String code;

        /**
        * 来源单据ID
        */
        private String sourceId;

        /**
        * 来源单据编号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 审批状态
        */
        private String approveStatus;
        private String approveStatusName;

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
        private String invalidStatusName;

        /**
        * 归还人ID
        */
        private String returnUserId;

        /**
        * 归还人姓名
        */
        private String returnUserName;

        /**
        * 归还人部门ID
        */
        private String returnDeptId;

        /**
        * 归还人部门名称
        */
        private String returnDeptName;

        /**
        * 接收人ID
        */
        private String receiverUserId;

        /**
        * 接收人姓名
        */
        private String receiverUserName;

        /**
        * 接收人部门ID
        */
        private String receiverDeptId;

        /**
        * 接收人部门名称
        */
        private String receiverDeptName;

        /**
        * 归还日期
        */
        private LocalDate returnDate;

        /**
        * 备注说明
        */
        private String remark;

        /**
         * 明细
         */
        private List<SampleReturnDetailDTO.ViewDTO> detailList;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<SampleReturnDetailDTO.@Valid AddDTO> detailList;

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
        private String id;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<SampleReturnDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 来源单据ID
        */
        @NotBlank(message = "来源单据不能为空")
        private String sourceId;

        /**
        * 来源单据编号
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 归还人ID
        */
        @NotBlank(message = "归还人不能为空")
        private String returnUserId;

        /**
        * 归还人姓名
        */
        private String returnUserName;

        /**
        * 归还人部门ID
        */
        @NotBlank(message = "归还人部门不能为空")
        private String returnDeptId;

        /**
        * 归还人部门名称
        */
        private String returnDeptName;

        /**
        * 接收人ID
        */
        @NotBlank(message = "接收人不能为空")
        private String receiverUserId;

        /**
        * 接收人姓名
        */
        private String receiverUserName;

        /**
        * 接收人部门ID
        */
        @NotBlank(message = "接收人部门不能为空")
        private String receiverDeptId;

        /**
        * 接收人部门名称
        */
        private String receiverDeptName;

        /**
        * 归还日期
        */
        @NotNull(message = "归还日期不能为空")
        private LocalDate returnDate;

        /**
        * 备注说明
        */
        @Size(max = 200,message = "备注说明最大长度不能超过200位")
        private String remark;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;


    }


}