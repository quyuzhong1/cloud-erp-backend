package com.erp.model.wms.dto;

import java.math.BigDecimal;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 抽样方案表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@NoArgsConstructor
public class SamplingPlanDTO implements Serializable {



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
         * 质检类型id
         */
        private String  qcTypeId;

        /**
        * 方案编码（CYFA）[可排序]
        */
        private String code;
        /**
         * 质检类型 [可排序]
         * QcTypeEnum
         */
        private String qcType;
        /**
         * 质检类型名称
         */
        private String qcTypeName;

        /**
        * 方案类型[可排序]
        */
        private String planType;
        /**
         * 方案类型名称
         */
        private String planTypeName;

        /**
        * 检验水平[可排序]
        */
        private String qcLevel;
        /**
         * 检验水平名称
         */
        private String qcLevelName;

        /**
        * 严重缺陷AQL[可排序]
        */
        private BigDecimal majorAql;

        /**
        * 一般缺陷AQL[可排序]
        */
        private BigDecimal generalAql;

        /**
        * 备注[可排序]
        */
        private String remark;


        /**
        * 状态[可排序]
        */
        private String disable;


        /**
        * 创建时间[可排序]
        */
        private LocalDateTime createTime;
        /**
         * 更新时间[可排序]
         */
        private LocalDateTime updateTime;

        /**
        * 创建人名称[可排序]
        */
        private String createUserName;
        /**
         * 更新人名称[可排序]
         */
        private String updateUserName;

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
        * 方案编码（CYFA）
        */
        private String code;


        /**
        * 方案类型
        */
        private String planType;
        private String planTypeName;

        /**
        * 检验水平
        */
        private String qcLevel;
        private String qcLevelName;

        /**
        * 严重缺陷AQL
        */
        private BigDecimal majorAql;

        /**
        * 一般缺陷AQL
        */
        private BigDecimal generalAql;

        /**
        * 备注
        */
        private String remark;

        /**
         * 质检类型
         */
        private List<SamplingPlanQcTypeRefDTO.ViewDTO> qcTypeList;

        /**
         * 产品列表
         */
        private List<SamplingPlanSkuRefDTO.ViewDTO> skuRefDTOList;

        /**
         * 抽样详情列表
         */
        private List<SamplingPlanDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 质检类型
         */
        @NotEmpty(message = "质检类型不能为空")
        private List<SamplingPlanQcTypeRefDTO.AddDTO> qcTypeList;
        /**
         * 产品列表
         */
        private List<SamplingPlanSkuRefDTO.AddDTO> skuRefDTOList;
        /**
         * 抽样详情列表
         */
        private List<SamplingPlanDetailDTO.AddDTO> detailList;
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
        private String code;

        /**
         * 质检类型
         */
        @NotEmpty(message = "质检类型不能为空")
        private List<SamplingPlanQcTypeRefDTO.UpdateDTO> qcTypeList;
        /**
         * 产品列表
         */
        private List<SamplingPlanSkuRefDTO.UpdateDTO> skuRefDTOList;
        /**
         * 抽样详情列表
         */
        private List<SamplingPlanDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {


        /**
        * 方案类型
        */
        @NotBlank(message = "方案类型不能为空")
        @Size(max = 30,message = "方案类型最大长度不能超过30位")
        private String planType;

        /**
        * 检验水平
        */
        @Size(max = 30,message = "检验水平最大长度不能超过30位")
        private String qcLevel;

        /**
        * 严重缺陷AQL
        */
        @Digits(integer = 12, fraction = 4, message = "严重缺陷AQL整数位不能超过12位，小数位不能超过4位")
        private BigDecimal majorAql;

        /**
        * 一般缺陷AQL
        */
        @Digits(integer = 8, fraction = 2, message = "一般缺陷AQL整数位不能超过8位，小数位不能超过2位")
        private BigDecimal generalAql;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}