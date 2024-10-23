package com.erp.model.wms.dto;

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
 * 发货通知变更单请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-10-23
*/
@Data
@NoArgsConstructor
public class SoDeliveryNoticeChangeDTO implements Serializable {


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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 销售订单编号
        */
        private String soCode;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 客户表id
        */
        private String customerId;

        /**
        * 客户名称
        */
        private String customerName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 变更原因
        */
        private String changeReason;


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
        * 审核状态 
        */
        private String approveStatus;

        /**
        * 来源单号
        */
        private String sourceCode;

        /**
        * 来源Id
        */
        private String sourceId;

        /**
        * 销售订单编号
        */
        private String soCode;

        /**
        * 销售订单id
        */
        private String soId;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 客户表id
        */
        private String customerId;

        /**
        * 客户名称
        */
        private String customerName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 审核时间
        */
        private LocalDateTime approveTime;

        /**
        * 变更原因
        */
        private String changeReason;


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
        * 来源单号
        */
        @NotBlank(message = "来源单号不能为空")
        @Size(max = 50,message = "来源单号最大长度不能超过50位")
        private String sourceCode;

        /**
        * 来源Id
        */
        @NotBlank(message = "来源Id不能为空")
        @Size(max = 50,message = "来源Id最大长度不能超过50位")
        private String sourceId;

        /**
        * 销售订单编号
        */
        @NotBlank(message = "销售订单编号不能为空")
        @Size(max = 50,message = "销售订单编号最大长度不能超过50位")
        private String soCode;

        /**
        * 销售订单id
        */
        @NotBlank(message = "销售订单id不能为空")
        @Size(max = 50,message = "销售订单id最大长度不能超过50位")
        private String soId;

        /**
        * 客户表id
        */
        @NotBlank(message = "客户表id不能为空")
        @Size(max = 19,message = "客户表id最大长度不能超过19位")
        private String customerId;

        /**
        * 客户名称
        */
        @NotBlank(message = "客户名称不能为空")
        @Size(max = 255,message = "客户名称最大长度不能超过255位")
        private String customerName;

        /**
        * 变更原因
        */
        @NotBlank(message = "变更原因不能为空")
        @Size(max = 255,message = "变更原因最大长度不能超过255位")
        private String changeReason;


    }


}