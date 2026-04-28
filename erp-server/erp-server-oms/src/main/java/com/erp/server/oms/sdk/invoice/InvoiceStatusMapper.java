package com.erp.server.oms.sdk.invoice;

import cn.hutool.core.text.CharSequenceUtil;
import com.erp.model.oms.enums.InvoiceInfoStatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 发票状态映射工具类
 * 用于将第三方API返回的状态字符串映射到系统内部的枚举值
 * 
 * @author system
 * @date 2025/01/XX
 */
public class InvoiceStatusMapper {
    
    private static final Logger log = LoggerFactory.getLogger(InvoiceStatusMapper.class);
    
    /**
     * 第三方状态到系统枚举的映射表
     * Key: 第三方API返回的状态字符串
     * Value: 系统内部的枚举值
     */
    private static final Map<String, InvoiceInfoStatusEnum> STATUS_MAP = new HashMap<>();
    
    static {
        // 初始化状态映射表
        STATUS_MAP.put("Success", InvoiceInfoStatusEnum.INVOICE_SUCCESS);
        STATUS_MAP.put("Processing", InvoiceInfoStatusEnum.INVOICING);
        STATUS_MAP.put("InvoicingFailed", InvoiceInfoStatusEnum.INVOICE_FAILED);
        STATUS_MAP.put("Failed", InvoiceInfoStatusEnum.INVOICE_FAILED);
        STATUS_MAP.put("Canceled", InvoiceInfoStatusEnum.CANCELED);
        STATUS_MAP.put("Voided", InvoiceInfoStatusEnum.VOIDED);
    }
    
    /**
     * 将第三方API返回的状态字符串映射到系统内部的枚举值
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串（如：Success, Processing, Failed等）
     * @return 系统内部的枚举值，如果状态未知则返回 INVOICE_FAILED
     */
    public static InvoiceInfoStatusEnum mapToEnum(String thirdPartyStatus) {
        if (CharSequenceUtil.isBlank(thirdPartyStatus)) {
            log.warn("第三方状态为空，返回默认状态: INVOICE_FAILED");
            return InvoiceInfoStatusEnum.INVOICE_FAILED;
        }
        
        InvoiceInfoStatusEnum statusEnum = STATUS_MAP.get(thirdPartyStatus);
        if (statusEnum == null) {
            log.warn("未知的第三方状态: {}，返回默认状态: INVOICE_FAILED", thirdPartyStatus);
            return InvoiceInfoStatusEnum.INVOICE_FAILED;
        }
        
        return statusEnum;
    }
    
    /**
     * 将第三方API返回的状态字符串映射到系统内部的枚举code值
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return 系统内部的枚举code值（如：invoiceSuccess, invoicing等）
     */
    public static String mapToCode(String thirdPartyStatus) {
        return mapToEnum(thirdPartyStatus).getCode();
    }
    
    /**
     * 判断状态是否为成功状态
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return true表示成功，false表示失败或其他状态
     */
    public static boolean isSuccess(String thirdPartyStatus) {
        return InvoiceInfoStatusEnum.INVOICE_SUCCESS.equals(mapToEnum(thirdPartyStatus));
    }
    
    /**
     * 判断状态是否为处理中状态
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return true表示处理中，false表示其他状态
     */
    public static boolean isProcessing(String thirdPartyStatus) {
        return InvoiceInfoStatusEnum.INVOICING.equals(mapToEnum(thirdPartyStatus));
    }
    
    /**
     * 判断状态是否为失败状态（包括开票失败、已取消、已作废）
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return true表示失败，false表示成功或处理中
     */
    public static boolean isFailed(String thirdPartyStatus) {
        InvoiceInfoStatusEnum statusEnum = mapToEnum(thirdPartyStatus);
        return InvoiceInfoStatusEnum.INVOICE_FAILED.equals(statusEnum)
                || InvoiceInfoStatusEnum.CANCELED.equals(statusEnum)
                || InvoiceInfoStatusEnum.VOIDED.equals(statusEnum);
    }
    
    /**
     * 判断状态是否为已取消状态
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return true表示已取消，false表示其他状态
     */
    public static boolean isCanceled(String thirdPartyStatus) {
        return InvoiceInfoStatusEnum.CANCELED.equals(mapToEnum(thirdPartyStatus));
    }
    
    /**
     * 判断状态是否为已作废状态
     * 
     * @param thirdPartyStatus 第三方API返回的状态字符串
     * @return true表示已作废，false表示其他状态
     */
    public static boolean isVoided(String thirdPartyStatus) {
        return InvoiceInfoStatusEnum.VOIDED.equals(mapToEnum(thirdPartyStatus));
    }
}
