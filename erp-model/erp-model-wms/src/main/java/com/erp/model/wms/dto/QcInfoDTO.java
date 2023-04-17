package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Lambda
 * @Classname QcInfoDTO
 * @Description TODO
 * @Date 2023-04-14 15:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class QcInfoDTO {


    /**
     * 暂存 质检信息
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO{

        @NotBlank(message = "质检类型不能为空")
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private QcTypeEnum qcType;

    }

}
