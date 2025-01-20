package com.erp.model.mrp.dto;

import com.erp.model.mrp.entity.CfgRuleOrderStrategyEntity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <p>
 * 策略（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-10-12
*/
@Data
@NoArgsConstructor
public class CfgRuleOrderStrategyDTO implements Serializable {




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
        * 采购建议策略,true是，false否
        */
        private Boolean isSplit;
        /**
         * 是否合并SKU集中采购,true是，false否
         */
        private Boolean isMergeSku;

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


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否拆分组合品,true是，false否
        */
        @NotNull(message = "是否拆分组合品不能为空")
        private Boolean isSplit;

        /**
         * 是否合并SKU集中采购,true是，false否
         */
        @NotNull(message = "是否合并SKU集中采购不能为空")
        private Boolean isMergeSku;
    }



    @Getter
    @Setter
    public static class StrategyResultDTO {
        /**
         * 采购建议策略,true是，false否
         */
        private Boolean isSplit;
        /**
         * 是否合并SKU集中采购,true是，false否
         */
        private Boolean isMergeSku;

        /**
         * 数据格式化
         */
        public static CfgRuleOrderStrategyDTO.StrategyResultDTO buildStrategyResultDTO(CfgRuleOrderStrategyEntity entity) {
            CfgRuleOrderStrategyDTO.StrategyResultDTO strategyResultDTO = new CfgRuleOrderStrategyDTO.StrategyResultDTO();
            strategyResultDTO.setIsSplit(entity.getIsSplit());
            strategyResultDTO.setIsMergeSku(entity.getIsMergeSku());
            return strategyResultDTO;
        }
    }


}