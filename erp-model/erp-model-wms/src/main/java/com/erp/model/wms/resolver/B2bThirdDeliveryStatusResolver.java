package com.erp.model.wms.resolver;

import com.common.business.enums.PlatformDictEnum;
import com.erp.model.wms.enums.ThirdDeliveryStatusEnum;
import org.apache.commons.lang3.StringUtils;

/**
 * B2B三方发货单状态统一解析器。
 */
public final class B2bThirdDeliveryStatusResolver {

    private B2bThirdDeliveryStatusResolver() {
    }

    public static String resolveErpStatus(String providerCode, String rawStatus) {
        if (StringUtils.isBlank(providerCode) || StringUtils.isBlank(rawStatus)) {
            return null;
        }
        if (PlatformDictEnum.ZHONG_BAO_WAREHOUSE.getCode().equalsIgnoreCase(providerCode)) {
            return resolveZhongBaoStatus(rawStatus);
        }
        if (PlatformDictEnum.DA_MAI.getCode().equalsIgnoreCase(providerCode)) {
            return resolveDaMaiStatus(rawStatus);
        }
        if (PlatformDictEnum.GOOD_CANG.getCode().equalsIgnoreCase(providerCode)) {
            return resolveGoodCangStatus(rawStatus);
        }
        if (PlatformDictEnum.JIFENG.getCode().equalsIgnoreCase(providerCode)) {
            return resolveJiFengStatus(rawStatus);
        }
        if (PlatformDictEnum.IML.getCode().equalsIgnoreCase(providerCode)) {
            return resolveImlStatus(rawStatus);
        }
        if (PlatformDictEnum.ANTU.getCode().equalsIgnoreCase(providerCode)) {
            return resolveAntuStatus(rawStatus);
        }
        if ("eccang".equalsIgnoreCase(providerCode) || "spt".equalsIgnoreCase(providerCode)) {
            return resolveAntuStatus(rawStatus);
        }
        return null;
    }

    private static String resolveZhongBaoStatus(String rawStatus) {
        switch (rawStatus) {
            case "-2":
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            case "-1":
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            case "1":
            case "2":
            case "3":
            case "4":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "5":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            default:
                return null;
        }
    }

    private static String resolveDaMaiStatus(String rawStatus) {
        switch (rawStatus) {
            case "NEW":
                return ThirdDeliveryStatusEnum.CREATING.getCode();
            case "SUBMIT":
            case "WAIT_PROCESSED":
            case "PROCESSED":
            case "WAIT_UPLOAD":
            case "UPLOADED":
            case "BLOCK":
            case "DISCARD_PROCESSED":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "SUCCESS":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case "EXCEPTION":
            case "DISCARD":
            case "PROBLEM":
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            default:
                return null;
        }
    }

    private static String resolveGoodCangStatus(String rawStatus) {
        switch (rawStatus) {
            case "W":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "D":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case "N":
            case "P":
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            case "X":
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            default:
                return null;
        }
    }

    private static String resolveJiFengStatus(String rawStatus) {
        switch (rawStatus) {
            case "1":
            case "2":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "3":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case "4":
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            case "5":
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            default:
                return null;
        }
    }

    private static String resolveImlStatus(String rawStatus) {
        switch (rawStatus) {
            case "DRAFT":
            case "SUBMIT_ORDER":
            case "WAIT_OUTBOUND":
            case "ASSIGNED_SUCCESS":
            case "WAREHOUSE_OPERATION_SUCCESS":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "COMPLETE_OUTBOUND":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case "ORDER_FAIL":
            case "EXCEPTION":
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            default:
                return null;
        }
    }

    private static String resolveAntuStatus(String rawStatus) {
        switch (rawStatus) {
            case "C":
            case "W":
                return ThirdDeliveryStatusEnum.WAIT_SHIPPED.getCode();
            case "D":
                return ThirdDeliveryStatusEnum.SHIPPED.getCode();
            case "N":
                return ThirdDeliveryStatusEnum.EXCEPTION_ORDER.getCode();
            case "X":
                return ThirdDeliveryStatusEnum.CANCEL_DELIVERY.getCode();
            default:
                return null;
        }
    }
}
