package com.erp.model.srm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 系统配置管理请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-01-10
*/
@Data
@NoArgsConstructor
public class CfgSettingDTO implements Serializable {




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
        * Key值
        */
        private String key;

        /**
        * json数据
        */
        private String dataJson;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 排序字段
        */
        private Integer index;

        /**
        * 备注
        */
        private String remark;

        /**
        * 供应商id
        */
        private String supplierId;


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
         * 订单接受配置
         */
        private OrderAcceptDTO orderAcceptDTO;

        /**
         * 退货确认配置
         */
        private ReturnConfirmDTO returnConfirmDTO;

    }


}