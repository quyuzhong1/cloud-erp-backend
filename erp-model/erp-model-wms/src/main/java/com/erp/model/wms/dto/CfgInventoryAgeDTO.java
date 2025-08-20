package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 库龄配置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-08-20
*/
@Data
@NoArgsConstructor
public class CfgInventoryAgeDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 库龄统计天数集合
         */
        private List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 库龄统计天数集合
         */
        @NotEmpty(message = "库龄统计天数配置不能为空")
        @Valid
        private List<CfgSettingVirtualValueDTO.InventoryAgeDateTO> list;
    }

}