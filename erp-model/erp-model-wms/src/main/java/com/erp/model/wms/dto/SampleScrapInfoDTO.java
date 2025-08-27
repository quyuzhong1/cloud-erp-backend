package com.erp.model.wms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.Valid;
import javax.validation.constraints.*;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 样品报废单主表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class SampleScrapInfoDTO implements Serializable {


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
          * 类型
          */
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
        * 报废单编号
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
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 报废操作人ID
        */
        private String scrapUserId;

        /**
        * 报废操作人姓名
        */
        private String scrapUserName;

        /**
        * 报废人部门ID
        */
        private String scrapDeptId;

        /**
        * 报废人部门名称
        */
        private String scrapDeptName;

        /**
        * 报废日期
        */
        private LocalDate scrapDate;

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

        /**
         * id
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
         * 报废数量
         */
        private Integer scrapQty;

        /**
         * 明细备注
         */
        private String detailRemark;

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
        * 报废单编号
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
        * 作废状态
        */
        private Boolean invalidStatus;
        private String invalidStatusName;

        /**
        * 报废操作人ID
        */
        private String scrapUserId;

        /**
        * 报废操作人姓名
        */
        private String scrapUserName;

        /**
        * 报废人部门ID
        */
        private String scrapDeptId;

        /**
        * 报废人部门名称
        */
        private String scrapDeptName;

        /**
        * 报废日期
        */
        private LocalDate scrapDate;

        /**
        * 备注
        */
        private String remark;

        /**
         * 明细
         */
        private List<SampleScrapDetailDTO.ViewDTO> detailList;

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
        private List<SampleScrapDetailDTO.@Valid AddDTO> detailList;

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
         * 明细
         */
        @NotEmpty(message = "明细不能为空" )
        private List<SampleScrapDetailDTO. @Valid UpdateDTO> detailList;

    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 序号
         */
        private String no;
        /**
         * 报废操作人ID
         */
        private String scrapUserId;

        /**
         * 报废操作人姓名
         */
        private String scrapUserName;

        /**
         * 报废人部门ID
         */
        private String scrapDeptId;

        /**
         * 报废人部门名称
         */
        private String scrapDeptName;

        /**
         * 报废日期
         */
        private LocalDate scrapDate;

        /**
         * 备注
         */
        private String remark;

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
         * 使用方名称
         */
        private String useUserName;

        /**
         * 报废数量
         */
        private Integer scrapQty;

        /**
         * 备注
         */
        private String detailRemark;
        /**
         * 台账id
         */
        private String sampleLedgerId;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 报废操作人ID
        */
        @NotBlank(message = "报废人不能为空")
        private String scrapUserId;

        /**
        * 报废操作人姓名
        */
        private String scrapUserName;

        /**
        * 报废人部门ID
        */
        @NotBlank(message = "报废人部门不能为空")
        private String scrapDeptId;

        /**
        * 报废人部门名称
        */
        private String scrapDeptName;

        /**
        * 报废日期
        */
        @NotNull(message = "报废日期不能为空")
        private LocalDate scrapDate;

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




}