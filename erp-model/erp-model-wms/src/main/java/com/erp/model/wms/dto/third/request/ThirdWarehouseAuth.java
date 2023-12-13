package com.erp.model.wms.dto.third.request;

import com.common.business.enums.OmsPlatformEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * @author liuruipeng
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ThirdWarehouseAuth {

    /**
     * 第三方仓服务商
     * {@link OmsPlatformEnum}
     */
    @NotBlank(message = "第三方仓服务商不能为空")
    private String thirdWarehouseProvideCode;

    @NotBlank(message = "第三方仓授权Id")
    private String authId;

}
