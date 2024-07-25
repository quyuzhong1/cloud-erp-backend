package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * PLM系统配置管理请求响应实体
 * </p>
 *
*/
@Data
@NoArgsConstructor
public class PlmCfgSettingDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class MaterialAttributeControlDetail {


        /**
         * 物料属性
         */
        private List<String> materialAttributeList = new ArrayList<>();

        /**
         * 允许采购
         */
        private boolean allowPurchase;

        /**
         * 允许销售
         */
        private boolean allowSale;

        /**
         * 允许库存
         */
        private boolean allowInventory;

        /**
         * 允许生产
         */
        private boolean allowProduction;

        /**
         * 允许委外
         */
        private boolean allowSubContract;

        /**
         * 允许转资产
         */
        private boolean allowTransferAssets;
    }
    @Data
    @NoArgsConstructor
    public static class MaterialAttributeControl {
        /**
         * 明细
         */
        private List<PlmCfgSettingDTO.MaterialAttributeControlDetail> detailList = new ArrayList<>();
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 金蝶推送物料属性控制
         */
        @Valid
        private PlmCfgSettingDTO.MaterialAttributeControl materialAttributeControl = new MaterialAttributeControl();

    }

}