package com.erp.model.plm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 产品变更信息表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
*/
@Data
@NoArgsConstructor
public class ProductChangeDTO implements Serializable {



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

        private String  detailId;
        /**
        * 变更单号
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * skuid
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 变更原因
        */
        private String reason;

        /**
        * 变更日期
        */
        private LocalDate billDate;

        /**
        * 产品名称
        */
        private String productName;

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
         * 变更字段
         */
        private String field;

        /**
         * 变更字段名称
         */
        private String fieldName;

        /**
         * 变更原值
         */
        private String oldValue;
        /**
         * 变更新值
         */
        private String newValue;
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
        * 变更单号
        */
        private String code;

        /**
        * 审核状态
        */
        private String approveStatus;

        private String approveStatusName;

        /**
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        private String invalidStatusName;

        /**
        * skuid
        */
        private String skuId;

        /**
        * sku编号
        */
        private String skuNo;

        /**
        * 变更原因
        */
        private String reason;

        /**
        * 变更日期
        */
        private LocalDate billDate;

        /**
        * 产品名称
        */
        private String productName;

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

        private List<ProductChangeDetailDTO.ViewDTO> detailDTOList;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        private List<ProductChangeDetailDTO.AddDTO> detailDTOList;
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

        private List<ProductChangeDetailDTO.UpdateDTO> detailDTOList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * skuid
        */
        @NotBlank(message = "skuid不能为空")
        @Size(max = 255,message = "skuid最大长度不能超过255位")
        private String skuId;

        /**
        * 变更原因
        */
        private String reason;

        /**
        * 变更日期
        */
        private LocalDate billDate;

        /**
        * 产品名称
        */
        private String productName;


    }


}