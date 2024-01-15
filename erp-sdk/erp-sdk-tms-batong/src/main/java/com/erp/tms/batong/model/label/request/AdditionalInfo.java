package com.erp.tms.batong.model.label.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @author Lambda
 * @Classname additionalInfo
 * @Description TODO
 * @Date 2024-01-15 11:19
 * @Created by yl
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdditionalInfo implements Serializable {

    /**
     *标签上打印配货信息
     * (Y:打印 N:不打印) 默认 N:不打印
     */
    @JSONField(name = "lable_print_invoiceinfo")
    private String labelPrintInvoiceInfo;

    /**
     *标签上是否打印买家ID
     * (Y:打印 N:不打印) 默认 N:不打印
     */
    @JSONField(name = "lable_print_buyerid")
    private String labelPrintBuyerid;

    /**
     *标签上是否打印日期
     *  (Y:打印 N:不打印) 默认 Y:打印
     */
    @JSONField(name = "lable_print_datetime")
    private String labelPrintDatetime;

    /**
     *报关单上是否打印实际重量
     * (Y:打印 N:不打印) 默认 N:不打印
     */
    @JSONField(name = "customsdeclaration_print_actualweight")
    private String customsDeclarationPrintActualWeight;
}
