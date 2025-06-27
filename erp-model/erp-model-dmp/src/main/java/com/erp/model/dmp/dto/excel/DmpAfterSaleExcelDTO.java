package com.erp.model.dmp.dto.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 售后申请表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-04-06
*/
@Data
@NoArgsConstructor
public class DmpAfterSaleExcelDTO implements Serializable {
        /**
         * 工单号
         */
//        @ExcelProperty(value = "工单号", index = 0)
        private String code;

        /**
         * 订单编号
         */
//        @ExcelProperty(value = "订单编号", index = 1)
        private String platformCode;

        /**
         * 审核状态
         */
//        @ExcelProperty(value = "审核状态", index = 2)
        private String approveStatusName;

        /**
         * 单据状态
         */
//        @ExcelProperty(value = "单据状态", index = 3)
        private String statusName;

        /**
         * 作废状态
         */
//        @ExcelProperty(value = "作废状态", index = 4)
        private String invalidStatusName;

        /**
         * 作废备注
         */
//        @ExcelProperty(value = "作废备注", index = 5)
        private String invalidRemark;

        /**
         * 客户名称
         */
//        @ExcelProperty(value = "客户名称", index = 6)
        private String thridUserName;

        /**
         * 手机号
         */
//        @ExcelProperty(value = "手机号", index = 7)
        private String phoneNumber;

        /**
         * 客户地址
         */
//        @ExcelProperty(value = "客户地址", index = 8)
        private String address;

        /**
         * SKU
         */
//        @ExcelProperty(value = "SKU", index = 9)
        private String skuNo;

        /**
         * 产品名称
         */
//        @ExcelProperty(value = "产品名称", index = 10)
        private String productName;

        /**
         * 数量
         */
//        @ExcelProperty(value = "数量", index = 11)
        private String skuQty;

        /**
         * 货值
         */
//        @ExcelProperty(value = "货值", index = 12)
        private String price;

        /**
         * 维修金额
         */
//        @ExcelProperty(value = "维修金额", index = 13)
        private String totalRepairAmount;


        /**
         * 买家寄出快递单号
         */
//        @ExcelProperty(value = "买家寄出快递单号", index = 15)
        private String returnTrackNo;

        /**
         * 商家寄出快递单号
         */
//        @ExcelProperty(value = "商家寄出快递单号", index = 16)
        private String outboundTrackNo;

        /**
         * 故障描述
         */
//        @ExcelProperty(value = "故障描述", index = 17)
        private String faultDesc;

        /**
         * 备注
         */
//        @ExcelProperty(value = "备注", index = 18)
        private String remark;
        /**
         * 客服备注
         */
        private String csrRemark;

        /**
         * 维修备注
         */
        private String rmaRemark;

        /**
         * 申请日期
         */
//        @ExcelProperty(value = "申请日期", converter = LocalDateStringConverter.class, index = 14)
        private LocalDate billDate;

        /**
         * 审核人
         */
//        @ExcelProperty(value = "审核人", index = 19)
        private String approveUserName;

        /**
         * 审核完成时间
         */
//        @ExcelProperty(value = "审核完成时间", converter = LocalDateStringConverter.class, index = 20)
        private LocalDateTime approveTime;

        /**
         * 创建人
         */
//        @ExcelProperty(value = "创建人", index = 21)
        private String createUserName;

    @ExcelIgnore
    private String status;
    @ExcelIgnore
    private String approveStatus;
    @ExcelIgnore
    private Boolean invalidStatus;


}