package com.erp.model.wms.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.util.List;

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
    public static class AddDTO {


        /**
         * 质检类型
         */
        @NotBlank(message = "质检类型不能为空")
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private QcTypeEnum qcType;


        /**
         * 总量
         */
        @NotNull(message = "总量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999")
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer totalQty;

        /**
         * 质检量
         */
        @NotNull(message = "质检量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999")
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcQty;

        /**
         * 质检合格量
         */
        @NotNull(message = "质检合格量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999")
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        @NotNull(message = "质检不良量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999")
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcBadQty;


        /**
         * 问题属性
         */
        private String qcProblemKey;


        /**
         * 不良现象
         * 选择不良的时候必填
         */
        @Size(max = 250, message = "最大250个字符")
        private String badDescription;


        /**
         * 不良图片地址集合
         */
        private List<String> badImageUrlList;

        /**
         * 不良图片名称地址集合
         */
        private List<String> badImageNameList;


        /**
         * 质检结果
         */
        private QcResultEnum qcResult;

        /**
         * 处理措施
         */
        @NotBlank(message = "处理措施不能为空")
        private String handleModeKey;
    }

}
