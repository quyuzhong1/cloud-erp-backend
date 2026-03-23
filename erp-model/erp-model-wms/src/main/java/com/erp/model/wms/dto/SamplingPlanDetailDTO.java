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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-03-20
*/
@Data
@NoArgsConstructor
public class SamplingPlanDetailDTO implements Serializable {



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
        * 批量范围从
        */
        private Integer rangFrom;

        /**
        * 批量范围到
        */
        private Integer rangTo;

        /**
        * 抽样比例
        */
        private BigDecimal rate;

        /**
        * 抽样数量
        */
        private Integer qty;

        /**
        * 一般缺陷允收数（Ac）
        */
        private Integer generalAcceptQty;

        /**
        * 一般缺陷拒收数(Re)
        */
        private Integer generalRejectQty;

        /**
        * 严重缺陷允收数（Ac）
        */
        private Integer majorAcceptQty;

        /**
        * 严重缺陷拒收数(Re)
        */
        private Integer majorRejectQty;

        /**
        * 主表id
        */
        private String mainId;


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
        * 批量范围从
        */
        private Integer rangFrom;

        /**
        * 批量范围到
        */
        private Integer rangTo;

        /**
        * 抽样比例
        */
        private BigDecimal rate;

        /**
        * 抽样数量
        */
        private Integer qty;

        /**
        * 一般缺陷允收数（Ac）
        */
        private Integer generalAcceptQty;

        /**
        * 一般缺陷拒收数(Re)
        */
        private Integer generalRejectQty;

        /**
        * 严重缺陷允收数（Ac）
        */
        private Integer majorAcceptQty;

        /**
        * 严重缺陷拒收数(Re)
        */
        private Integer majorRejectQty;

        /**
        * 主表id
        */
        private String mainId;


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
    public static class CommonDTO extends SuperDTO {

        /**
        * 批量范围从
        */
        @NotNull(message = "批量范围从不能为空")
        private Integer rangFrom;

        /**
        * 批量范围到
        */
        @NotNull(message = "批量范围到不能为空")
        private Integer rangTo;

        /**
        * 抽样比例
        */
//        @NotNull(message = "抽样比例不能为空")
        @Digits(integer = 12, fraction = 4, message = "抽样比例整数位不能超过12位，小数位不能超过4位")
        private BigDecimal rate;

        /**
        * 抽样数量
        */
//        @NotNull(message = "抽样数量不能为空")
        private Integer qty;

        /**
        * 一般缺陷允收数（Ac）
        */
        @NotNull(message = "一般缺陷允收数（Ac）不能为空")
        private Integer generalAcceptQty;

        /**
        * 一般缺陷拒收数(Re)
        */
//        @NotNull(message = "一般缺陷拒收数(Re)不能为空")
        private Integer generalRejectQty;

        /**
        * 严重缺陷允收数（Ac）
        */
        @NotNull(message = "严重缺陷允收数（Ac）不能为空")
        private Integer majorAcceptQty;

        /**
        * 严重缺陷拒收数(Re)
        */
//        @NotNull(message = "严重缺陷拒收数(Re)不能为空")
        private Integer majorRejectQty;

        /**
        * 主表id
        */
//        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;


    }


}