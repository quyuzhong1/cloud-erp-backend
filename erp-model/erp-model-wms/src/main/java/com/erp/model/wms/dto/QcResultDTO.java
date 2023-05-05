package com.erp.model.wms.dto;

import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.wms.enums.QcResultEnum;
import com.erp.model.wms.enums.QcTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class QcResultDTO {


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
        @NotBlank(message = "质检类型不能为空", groups = {UpdateGroup.class, AddGroup.class})
        @StateEnumValue(clazz = QcTypeEnum.class, message = "质检类型有误")
        private String qcType;


        /**
         * 总量
         */
        @NotNull(message = "总量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "1", message = "质检总量必须大于0", groups = {AddGroup.class})
        private Integer totalQty;

        /**
         * 质检量
         */
        @NotNull(message = "质检量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "1", message = "质检量必须大于0", groups = {AddGroup.class})
        private Integer qcQty;

        /**
         * 质检合格量
         */
        @NotNull(message = "质检合格量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值必须大于0", groups = {AddGroup.class})
        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        @NotNull(message = "质检不良量不能为空")
        @DecimalMax(value = "99999999", message = "最大值为99999999", groups = {UpdateGroup.class, AddGroup.class})
        @DecimalMin(value = "0", message = "最小值为0", groups = {AddGroup.class})
        private Integer qcBadQty;


        /**
         * 采购订单明细id 不能为空
         */
        //@NotBlank(message = "采购订单明细id 不能为空")
        private String purchaseOrderDetailId;


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
        @NotBlank(message = "质检结果不能为空", groups = {UpdateGroup.class, AddGroup.class})
        @StateEnumValue(clazz = QcResultEnum.class, message = "质检结果有误")
        private String qcResult;

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
        private String qcType;

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
        private String qcResult;

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
         */
        private String handleModeName;


        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 质检完成时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime qcFinishTime;

    }


    /**
     * 质检数量
     */
    @Data
    @NoArgsConstructor
    public static class QcQtyDTO {


        /**
         * 质检单id
         */
        private String mainId;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * skuId
         */
        private String skuId;

        /**
         * 质检类型
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890
         * stockIn 入库质检  outsideQc 外检质检 insideQc 在库质检 newProductStockIn 新品入库质检 b2bOutsideQc B2B外检
         */
        private QcTypeEnum qcType;


        /**
         * 总量
         */

        private Integer totalQty;

        /**
         * 质检量
         */

        private Integer qcQty;

        /**
         * 质检合格量
         */

        private Integer qcGoodQty;

        /**
         * 质检不良量
         */
        private Integer qcBadQty;


    }

    @Data
    @NoArgsConstructor
    public static class UpdateHandleModeDTO {

        /**
         * 处理措施
         * 来源 http://172.16.100.11:3002/project/92/interface/api/8890 type=handleModeType
         * 取value
         */
        @NotBlank(message = "处理措施不能为空", groups = {UpdateGroup.class, AddGroup.class})
        private String handleModeDict;

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;
    }

    /**
     * 需要入库的参数
     */
    @Data
    @NoArgsConstructor
    public static class StockInDTO {

        /**
         * 表id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;


        /**
         * 采购订单id
         */
        private String purchaseOrderId;


        /**
         * 采购订单详情id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购订单 交付仓库id
         */
        private String warehouseId;

        /**
         * 质检总量
         */
        private Integer totalQty;

        /**
         * 质检类型
         */
        private String qcType;


    }


    /**
     * 质检通知内容
     */
    @Data
    @NoArgsConstructor
    public static class QcNoticeDTO {


        private String id;

        /**
         * 采购订单code
         */
        private String purchaseOrderCode;


        /**
         * skuid
         */
        private String skuId;


        /**
         * skuNo
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String skuName;

        /**
         * 质检人员
         */
        private String qcUserName;

        /**
         * 操作人
         */
        private String userName;

        /**
         * 质检类型
         */
        private String qcType;

        /**
         * 质检类型名
         */
        private String qcTypeName;



        /**
         * 质检完成时间
         */
        private String qcFinishTime;

        /**
         * 处理措施
         */
        private String handleModeName;

        /**
         * 处理措施
         */
        private String handleModeDict;




    }
}
