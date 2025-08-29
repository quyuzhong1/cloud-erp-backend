package com.erp.model.oms.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AttachDTO;
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
 * 收款单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-08-28
*/
@Data
@NoArgsConstructor
public class SoReceiptDTO implements Serializable {


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SoInfoAndReceiptDTO {

        /**
         * 订单id
         */
        private String soId;

        /**
         * 订单编号
         */
        private String soCode;

        /**
         * 订单审核状态
         */
        private String approveStatus;

        /**
         * 订单审核状态名称
         */
        private String approveStatusName;

        /**
         * 剩余收款金额
         */
        private String remainReceiptAmount;

        /**
         * 收款账号
         */
        private String receiptAccount;

    }

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
        * 第三方单据编号
        */
        private String thirdCode;

        /**
        * 客户Id
        */
        private String customerId;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 币种
        */
        private String currency;

        /**
        * 是否入账
        */
        private Boolean isPosted;

        /**
        * 入账账户
        */
        private String postedAccount;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
        */
        private String approveUserName;

        /**
        * 备注
        */
        private String remark;


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
     * 查询销售订单收款信息dto
     */
    @Data
    @NoArgsConstructor
    public static class SoSearchDTO {

        /**
         * 客户id
         */
        private String customerId;

        /**
         * 单个模糊搜索销售订单号
         */
        private String soCode;

        /**
         * 快粘贴销售订单号
         */
        private List<String> soCodeList;
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
        * 第三方单据编号
        */
        private String thirdCode;

        /**
        * 客户Id
        */
        private String customerId;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 币种
        */
        private String currency;

        /**
        * 是否入账
        */
        private Boolean isPosted;

        /**
        * 入账账户
        */
        private String postedAccount;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人姓名
        */
        private String approveUserName;

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

        /**
         * 明细
         */
        @Valid
        private List<SoReceiptDetailDTO.AddDTO> detailList;

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
        @Valid
        private List<SoReceiptDetailDTO.UpdateDTO> detailList;


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 第三方单据编号
        */
        private String thirdCode;

        private String thirdSystem;
        /**
        * 客户Id
        */
        @NotBlank(message = "客户Id不能为空")
        @Size(max = 32,message = "客户Id最大长度不能超过32位")
        private String customerId;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        private String currency;

        /**
        * 是否入账
        */
        @NotNull(message = "是否入账不能为空")
        private Boolean isPosted;

        /**
        * 入账账户
        */
        @NotBlank(message = "入账账户不能为空")
        private String postedAccount;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 备注
        */
        private String remark;

        /**
         * 附件列表
         */
        private List<AttachDTO> attachmentList;

    }


}