package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: 组合产品DTO
 * @date 2023/8/16 9:40
 */
@Data
@NoArgsConstructor
public class BomCombinationDTO implements Serializable {



    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 组合产品编码
         */
        private String parentSkuNo;
        /**
         * 组合产品名称
         */
        private String name;

        /**
         * 产品信息（子sku合并展示）
         */
        private String childSkuNos;
        /**
         * 组合成本
         */
        private BigDecimal childSkuCost;
        /**
         * 更新人
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 子级SKu
         */
        private List<ChildDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class ChildDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * skuid
         */
        private String childSkuId;
        /**
         * sku编号
         */
        private String childSkuNo;
        /**
         * 用量
         */
        private Integer qty;
        /**
         * 实际成本
         */
        private BigDecimal actualTaxCost;
        /**
         * 目标成本
         */
        private BigDecimal targetTaxCost;

        /**
         * 一级供应商id
         */
        private String mainSupplierId;
    }


    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 组合产品名称
         */
        private String name;
        /**
         * 子级sku编号
         */
        private String childSkuNo;
        /**
         * 更新人id集合
         */
        private List<String> updateUserIdList;
        /**
         * 更新时间集合
         */
        private List<LocalDate> updateTimeList;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 组合产品编码
         */
        @NotBlank(message = "组合产品编码不能为空")
        @Size(max = 64, message = "组合产品编码最大64个字符")
        private String skuNo;
        /**
         * 组合产品名称
         */
        @NotBlank(message = "组合产品名称不能为空")
        @Size(max = 100, message = "组合产品名称最大100个字符")
        private String name;

    }

        @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{

        /**
         * 明细
         */
        @NotEmpty(message = "产品信息不能为空")
        @Valid
        private List<BomCombinationDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细
         */
        @NotEmpty(message = "产品信息不能为空")
        @Valid
        private List<BomCombinationDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO{

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细
         */
        private List<BomCombinationDetailDTO.ViewDTO> detailList;
    }
}
