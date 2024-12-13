package com.erp.model.plm.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MouldInfoChangeLogDTO {



    @Getter
    @Setter
    public static class MouldInfoChangeDTO {
        /**
         * 项目编号
         */
        private String projectNo;
        /**
         * 项目名称
         */
        private String name;
        /**
         * 产品经理
         */
        private String productManagerName;
        /**
         * 备注
         */
        private String remark;
        /**
         * 分类
         */
        private String categoryName;
    }

    @Getter
    @Setter
    public static class MouldDetailChangeDTO {
        /**
         * 模具编号
         */
        private String mouldNo;

        /**
         * 外部模具编号(供应商)
         */
        private String thirdMouldNo;

        /**
         * 模具类型
         */
        private String typeId;

        /**
         * 模具穴数
         */
        private String mouldHoles;

        /**
         * 模具长
         */
        private BigDecimal length;

        /**
         * 模具宽
         */
        private BigDecimal width;

        /**
         * 模具高
         */
        private BigDecimal height;

        /**
         * 模具材质
         */
        private String material;

        /**
         * 模具寿命(万)(啤)
         */
        private Integer lifeCycle;

        /**
         * 开模周期(自然日)
         */
        private Integer developCycle;

        /**
         * 启用时间
         */
        private LocalDate enableDate;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 备注
         */
        private String remark;
        /**
         * 产品信息
         */
        private List<MouldProductDTO.UpdateDTO> productList;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 结算方式
         */
        private String payMethodId;

        /**
         * 付款条件
         */
        private String paymentCondition;

        /**
         * 是否费用返还
         */
        private Boolean isNeedRefund;

        /**
         * 返还标准
         */
        private String refundStandard;

        /**
         * 退款单量
         */
        private Integer refundOrderQty;

        /**
         * 返还金额
         */
        private BigDecimal refundAmount;

        /**
         * 关联产品
         */
        private List<MouldRefProductDTO.UpdateDTO> refProductList;

    }

    @Getter
    @Setter
    public static class MouldDocChangeDTO {



    }
}
