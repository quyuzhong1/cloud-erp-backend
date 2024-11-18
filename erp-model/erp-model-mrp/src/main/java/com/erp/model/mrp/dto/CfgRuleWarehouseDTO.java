package com.erp.model.mrp.dto;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 仓库（规则设置）请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-24
*/
@Data
@NoArgsConstructor
public class CfgRuleWarehouseDTO implements Serializable {




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
        * 是否启禁用虚拟仓，true启用，false禁用
        */
        private Boolean isEnableVirtual;

        /**
        * 是否启禁用海外仓，true启用，false禁用
        */
        private Boolean isEnableOverseas;

        /**
        * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
        */
        private String platformType;

        /**
         * 本地仓设置（实体仓数据）
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgLocalWarehouseList;

        /**
         * 本地仓设置（虚拟仓数据）
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgLocalVirtualWarehouseList;

        /**
         * 海外仓设置
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgOverseasWarehouseList;
    }


    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 本地仓设置（实体仓设置）
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList;

        /**
         * 本地仓设置（虚拟仓数据）
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalVirtualWarehouseList;

        /**
         * 海外仓设置
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgOverseasWarehouseList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 是否启禁用虚拟仓，true启用，false禁用
        */
        @NotNull(message = "是否启禁用虚拟仓，true启用，false禁用不能为空")
        private Boolean isEnableVirtual;

        /**
        * 是否启禁用海外仓，false启用，true禁用
        */
        @NotNull(message = "是否启禁用海外仓，false启用，true禁用不能为空")
        private Boolean isEnableOverseas;

        /**
        * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
        */
        @NotBlank(message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)不能为空")
        @Size(max = 32,message = "平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)最大长度不能超过32位")
        private String platformType;


    }

    @Data
    @NoArgsConstructor
    public static class ParamDTO {
        /**
         * 平台类型
         */
        private String platformType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrategyDTO {

        /**
         * 平台类型
         */
        private String platformType;
        /**
         * 平台
         */
        private String platform;
        /**
         * 店铺id
         */
        private String shopId;
    }

    @Getter
    @Setter
    public static class StrategyResultDTO {
        /**
         * 是否启禁用虚拟仓，true启用，false禁用
         */
        private Boolean isEnableVirtual;
        /**
         * 是否启禁用海外仓，true启用，false禁用
         */
        private Boolean isEnableOverseas;

        /**
         * 本地仓
         */
        private List<StrategyDetailResultDTO> localWarehouseList = new ArrayList<>();
        /**
         * 海外仓
         */
        private List<StrategyDetailResultDTO> overseasWarehouseList = new ArrayList<>();

    }

    @Getter
    @Setter
    public static class StrategyDetailResultDTO {
        /**
         * id
         */
        private String id;
        /**
         * 实体仓id
         */
        private String warehouseId;
        /**
         * 虚拟仓id
         */
        private String virtualWarehouseId;
        /**
         * 仓库类型，local本地，overseas海外
         */
        private String warehouseType;
        /**
         * 关联店铺类型，platform按平台，shop按店铺
         */
        private String channelType;
        /**
         * 渠道（店铺）id的json
         */
        private JSONArray channelIdJson;
        /**
         * 库存分配类型
         */
        private String inventoryAllocateType;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 平台
         */
        private String dictPlatform;

        public static StrategyDetailResultDTO buildStrategyDetailResultDTO(CfgRuleWarehouseDetailEntity entity) {
            StrategyDetailResultDTO detailResultDTO = new StrategyDetailResultDTO();
            detailResultDTO.setId(entity.getId());
            detailResultDTO.setWarehouseId(entity.getWarehouseId());
            detailResultDTO.setVirtualWarehouseId(entity.getVirtualWarehouseId());
            detailResultDTO.setWarehouseType(entity.getWarehouseType());
            detailResultDTO.setChannelType(entity.getChannelType());
            detailResultDTO.setChannelIdJson(entity.getChannelIdJson());
            detailResultDTO.setInventoryAllocateType(entity.getInventoryAllocateType());
            detailResultDTO.setMainId(entity.getMainId());
            detailResultDTO.setDictPlatform(entity.getDictPlatform());
            return detailResultDTO;
        }
    }

    @Data
    @NoArgsConstructor
    public static class WarehouseShopDTO {
        /**
         * 实体仓店铺
         */
        private List<String> shopNameList;
        /**
         * 虚拟仓店铺
         */
        private List<String> virtualShopNameList;
        /**
         * 海外仓店铺
         */
        private List<String> overseasShopNameList;
    }
}