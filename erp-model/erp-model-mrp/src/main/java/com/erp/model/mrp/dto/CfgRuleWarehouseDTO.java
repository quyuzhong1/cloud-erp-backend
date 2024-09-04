package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
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
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 本地仓设置
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.AddDTO> cfgLocalWarehouseList;

        /**
         * 海外仓设置
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.AddDTO> cfgOverseasWarehouseList;
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
        private String id;

        /**
         * 本地仓设置（实体仓设置）
         */
        @Valid
        private List<CfgRuleWarehouseDetailDTO.UpdateDTO> cfgLocalWarehouseList;

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

}