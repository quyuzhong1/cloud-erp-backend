package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 海外仓库存库龄明细表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2024-12-06
*/
@Data
@NoArgsConstructor
public class OverseasInventoryAgeDetailDTO implements Serializable {

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AgeRangeViewDTO {

        /**
         * overseas_inventory主表id
         */
        private String mainId;

        /**
         * 库龄 0-30 天的可售商品数量
         */
        private Integer inventoryAge0To30Days = 0 ;

        /**
         * 库龄 31-60 天的可售商品数量
         */
        private Integer inventoryAge31To60Days = 0 ;

        /**
         * 库龄 61-90 天的可售商品数量
         */
        private Integer inventoryAge61To90Days = 0 ;

        /**
         * 库龄 91-180 天的可售商品数量
         */
        private Integer inventoryAge91To180Days = 0 ;

        /**
         * 库龄 181-270 天的可售商品数量
         */
        private Integer inventoryAge181To270Days = 0 ;

        /**
         * 库龄 271-365 天的可售商品数量
         */
        private Integer inventoryAge271To365Days = 0 ;

        /**
         * 库龄 365 天以上的可售商品数量
         */
        private Integer inventoryAge365PlusDays = 0 ;

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
        * overseas_inventory主表id
        */
        private String mainId;

        /**
        * 拉取日期
        */
        private LocalDate pullDate;

        /**
        * 上架日期
        */
        private LocalDate putAwayDate;

        /**
        * 在库库存
        */
        private Integer inventoryQty;

        /**
        * 库龄 = 拉取日期 - 上架日期
        */
        private Integer inventoryAge;


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
        * overseas_inventory主表id
        */
        @NotBlank(message = "overseas_inventory主表id不能为空")
        @Size(max = 19,message = "overseas_inventory主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 拉取日期
        */
        private LocalDate pullDate;

        /**
        * 上架日期
        */
        private LocalDate putAwayDate;

        /**
        * 在库库存
        */
        @NotNull(message = "在库库存不能为空")
        private Integer inventoryQty;

        /**
        * 库龄 = 拉取日期 - 上架日期
        */
        @NotNull(message = "库龄 = 拉取日期 不能为空")
        private Integer inventoryAge;


    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
        * overseas_inventory主表id
        */
        private String id;

        /**
        * 拉取日期
        */
        private LocalDate pullDate;

        /**
        * 上架日期
        */
        private LocalDate putAwayDate;

        /**
        * 在库库存
        */
        private Integer inventoryQty;

        /**
        * 库龄 = 拉取日期 - 上架日期
        */
        private Integer inventoryAge;
    }

    /**
     * 列表查询入参
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {
        /**
         * overseas_inventory主表id
         */
        @NotBlank(message = "主表id不能为空")
        private String mainId;

        /**
         * age0To30Days
         * age31To60Days
         * age61To90Days
         * age91To180Days
         * age181To270Days
         * age271To365Days
         * age365PlusDays
         */
        @NotBlank(message = "库龄范围不能为空")
        private String ageRangeType;
    }

}