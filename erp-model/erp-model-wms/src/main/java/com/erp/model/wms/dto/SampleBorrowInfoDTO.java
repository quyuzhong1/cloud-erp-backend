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
import javax.validation.constraints.*;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 样品借用单请求响应实体
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
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;


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
         * 归还周期
         */
        private String returnPeriod;

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
         * 明细id
         */
        private String  detailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 借用数量
         */
        private Integer borrowQty;

        /**
         * 待归还数量
         */
        private Integer waitReturnQty;
        /**
         * 已归还数量
         */
        private Integer returnQty;

        /**
         * 明细备注
         */
        private String detailRemark;
        /**
         * 商品种类
         */
        private Integer skuCount;
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
        private String approveStatusName;

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
        private String invalidStatusName;

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
         * 明细
         */
        private List<SampleBorrowDetailDTO.ViewDTO> detailList;

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
        private List<SampleBorrowDetailDTO.@Valid AddDTO> detailList;
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
        private List<SampleBorrowDetailDTO.@Valid UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 借用人ID
        */
        @NotBlank(message = "借用人ID不能为空")
        private String borrowUserId;

        /**
        * 借用人姓名
        */
        private String borrowUserName;

        /**
        * 借用部门ID
        */
        @NotBlank(message = "借用部门ID不能为空")
        private String borrowDeptId;

        /**
        * 借用部门名称
        */
        private String borrowDeptName;

        /**
        * 借出人ID
        */
        @NotBlank(message = "借出人ID不能为空")
        private String lendUserId;

        /**
        * 借出人姓名
        */
        private String lendUserName;

        /**
        * 借出部门ID
        */
        @NotBlank(message = "借出部门ID不能为空")
        private String lendDeptId;

        /**
        * 借出部门名称
        */
        private String lendDeptName;

        /**
        * 借用日期
        */
        @NotNull(message = "借用日期不能为空")
        private LocalDate borrowDate;

        /**
        * 预计退回日期
        */
        @NotNull(message = "预计退回日期不能为空")
        private LocalDate estimatedReturnDate;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
         * 附件集合
         */
        private List<String> attachmentNameList;
        private List<String> attachmentUrlList;

    }


    @Data
    @NoArgsConstructor
    public static class SampleReturnView {

        private String sourceId;

        private String sourceCode;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU NO
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 借用人ID
         */
        private String returnUserId;

        /**
         * 借用人姓名
         */
        private String returnUserName;

        /**
         * 借用部门ID
         */
        private String returnDeptId;

        /**
         * 借用部门名称
         */
        private String returnDeptName;

        /**
         * 借出人ID
         */
        private String receiverUserId;

        /**
         * 借出人姓名
         */
        private String receiverUserName;

        /**
         * 借出部门ID
         */
        private String receiverDeptId;

        /**
         * 借出部门名称
         */
        private String receiverDeptName;

        /**
         * 待归还数量=借用数量-已归还数量
         */
        private Integer waitReturnQty;
        /**
         * 已归还数量=关联的已审核样品归还单归还数量
         */
        private Integer returnedQty;

        /**
         * 可归还数量=待归还数量-关联的待提交、审核中、审核不通过样品归还单归还数量
         */
        private Integer canReturnQty;

        /**
         * 归还日期
         */
        private LocalDate returnDate;

        /**
         * 归还数量
         */
        private Integer returnQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审批状态
         */
        private String approveStatus;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SearchDTO {

        private String notId;

        private String skuNo;

        private List<String> skuNos;

        /**
         * 样品借用单编号
         */
        @NotBlank(message = "样品借用单编号不能为空")
        private String sourceCode;

    }


    @Data
    @NoArgsConstructor
    public static class SkuAvailableQtyDTO {
        private String sourceId;

        private String sourceCode;

        /**
         * 来源明细ID
         */
        private String sourceDetailId;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU NO
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 借用数量
         */
        private Integer borrowQty;
        /**
         * 待归还数量=借用数量-已归还数量
         */
        private Integer waitReturnQty;
        /**
         * 已归还数量=关联的已审核样品归还单归还数量
         */
        private Integer returnedQty;

        /**
         * 可归还数量=待归还数量-关联的待提交、审核中、审核不通过样品归还单归还数量
         */
        private Integer canReturnQty;
        /**
         * 备注
         */
        private String remark;

    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 归还人 ID
         */
        private String borrowUserId;

        /**
         * 归还人姓名
         */
        private String borrowUserName;

        /**
         * 归还部门ID
         */
        private String borrowDeptId;

        /**
         * 归还部门名称
         */
        private String borrowDeptName;

        /**
         * 接收人ID
         */
        private String lendUserId;

        /**
         * 接收人姓名
         */
        private String lendUserName;

        /**
         * 接收部门ID
         */
        private String lendDeptId;

        /**
         * 接收部门名称
         */
        private String lendDeptName;
    }
}