package com.erp.model.plm.dto;

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
 * 模具关联sku请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-10-14
*/
@Data
@NoArgsConstructor
public class MoldRefSkuDTO implements Serializable {


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
        * 编码
        */
        private String code;
        /**
         * 备注
         */
        private String remark;

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
        * 模具id
        */
        private String moldId;

        /**
        * 模具编码
        */
        private String moldCode;

        /**
        * 模具名称
        */
        private String moldName;
        /**
         *
         */
        private String projectCode;
        /**
         *
         */
        private String projectName;

        /**
        * skuId
        */
        private String skuId;

        /**
        * SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 单模产量
        */
        private Integer outputQty;

        /**
        * sku用量
        */
        private Integer skuQty;

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
        * 备注
        */
        private String remark;

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
        * 模具id
        */
        private String moldId;

        /**
        * 模具编码
        */
        private String moldCode;

        /**
        * 模具名称
        */
        private String moldName;

        /**
        * skuId
        */
        private String skuId;

        /**
        * SKU
        */
        private String skuNo;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 单模产量
        */
        private Integer outputQty;

        /**
        * sku用量
        */
        private Integer skuQty;
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
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 模具id
        */
        @NotBlank(message = "模具id不能为空")
        @Size(max = 19,message = "模具id最大长度不能超过19位")
        private String moldId;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 单模产量
        */
        @NotNull(message = "单模产量不能为空")
        private Integer outputQty;

        /**
        * sku用量
        */
        @NotNull(message = "sku用量不能为空")
        private Integer skuQty;

    }


}