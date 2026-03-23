package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * GB/T2828.1-2012 AQL判定数主表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
*/
@Data
@NoArgsConstructor
public class AqlJudgeMappingDTO implements Serializable {



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
        * 样本量字码（A/B/C...R）
        */
        private String sampleQtyCode;

        /**
        * 样本量（与字码绑定）
        */
        private Integer sampleQty;

        /**
        * AQL值（0.010/0.015/.../100）
        */
        private BigDecimal aqlValue;

        /**
        * 接收数Ac
        */
        private Integer acceptQty;

        /**
        * 拒收数Re
        */
        private Integer rejectQty;

        /**
        * 状态(禁用true启用false)
        */
        private Boolean disabled;


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
        * 样本量字码（A/B/C...R）
        */
        private String sampleQtyCode;

        /**
        * 样本量（与字码绑定）
        */
        private Integer sampleQty;

        /**
        * AQL值（0.010/0.015/.../100）
        */
        private BigDecimal aqlValue;

        /**
        * 接收数Ac
        */
        private Integer acceptQty;

        /**
        * 拒收数Re
        */
        private Integer rejectQty;

        /**
        * 状态(禁用true启用false)
        */
        private Boolean disabled;


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
        * 样本量字码（A/B/C...R）
        */
        @NotBlank(message = "样本量字码（A/B/C...R）不能为空")
        @Size(max = 2,message = "样本量字码（A/B/C...R）最大长度不能超过2位")
        private String sampleQtyCode;

        /**
        * 样本量（与字码绑定）
        */
        @NotNull(message = "样本量（与字码绑定）不能为空")
        private Integer sampleQty;

        /**
        * AQL值（0.010/0.015/.../100）
        */
        @NotNull(message = "AQL值（0.010/0.015/.../100）不能为空")
        @Digits(integer = 3, fraction = 3, message = "AQL值（0.010/0.015/.../100）整数位不能超过3位，小数位不能超过3位")
        private BigDecimal aqlValue;

        /**
        * 接收数Ac
        */
        private Integer acceptQty;

        /**
        * 拒收数Re
        */
        private Integer rejectQty;

        /**
        * 状态(禁用true启用false)
        */
        @NotNull(message = "状态(禁用true启用false)不能为空")
        private Boolean disabled;


    }


}