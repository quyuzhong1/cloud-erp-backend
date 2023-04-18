package com.erp.model.wms.dto;

import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
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

        private String id;
        /**
         * 质检类型
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890
         * stockIn 入库质检  outsideQc 外检质检 insideQc 在库质检 newProductStockIn 新品入库质检 b2bOutsideQc B2B外检
         */
        @NotNull(message = "质检类型不能为空", groups = {UpdateGroup.class, AddGroup.class})
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private QcTypeEnum qcType;


        /**
         * 总量
         */
        @NotNull(message = "总量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer totalQty;

        /**
         * 质检量
         */
        @NotNull(message = "质检量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcQty;

        /**
         * 质检合格量
         */
        @NotNull(message = "质检合格量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        @NotNull(message = "质检不良量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值为0")
        private Integer qcBadQty;


        /**
         * 问题属性
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=qcProblemType
         */
        private String qcProblemDict;


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
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         */
        @NotBlank(message = "处理措施不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private String handleModeDict;
    }


    /**
     * 质检详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        private String id;


        /**
         * 质检类型
         */
        private QcTypeEnum qcType;

        /**
         * 质检类型名
         */
        private String qcTypeName;


        /**
         * 总量
         */
        private Integer totalQty;

        /**
         * 质检量
         */
        private Integer qcQty;


        /**
         * 抽检比例
         */
        private BigDecimal qcSampleRate;


        /**
         * 是否内检  true 是
         */
        private Boolean isInside;

        /**
         * 质检合格量
         */
        private Integer qcGoodQty;

        /**
         * 质检合格率
         */
        private BigDecimal qcGoodRate;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;


        /**
         * 质检不良率
         */
        private BigDecimal qcBadRate;


        /**
         * 问题属性
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=qcProblemType
         */
        private String qcProblemDict;


        /**
         * 问题属性 名
         */
        private String qcProblemName;


        /**
         * 不良现象
         * 选择不良的时候必填
         */
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
         * 质检结果名
         */
        private String qcResultName;

        /**
         * 处理措施
         */
        private String handleModeDict;

        /**
         * 处理措施名
         *
         */
        private String handleModeName;



        /**
         *仓库id
         */
        private String warehouseId;

        /**
         *仓库名
         */
        private String warehouseName;


        /**
         *备注
         */
        private String remark;

        /**
         *创建人id
         */
        private String createUserId;

        /**
         *创建人名
         */
        private String createUserName;
    }

}
