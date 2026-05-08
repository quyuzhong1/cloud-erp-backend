package com.erp.server.dmp.push.consumer.wangdian;

import com.common.core.controller.vo.ApiResult;
import com.sdk.wangdian.enums.WdtExtInStockStatusEnum;
import com.sdk.wangdian.enums.WdtExtOutStockStatusEnum;
import com.sdk.wangdian.enums.WdtInStockStatusEnum;
import com.sdk.wangdian.enums.WdtOutStockStatusEnum;
import com.sdk.wangdian.sdk.api.wms.external.in.StockExternalInResponse;
import com.sdk.wangdian.sdk.api.wms.external.out.StockExternalOutResponse;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.OtherStockinResponse;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.StockoutOtherQueryResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 旺店通其他出入库审核状态校验结果构建工具
 */
public final class WdtOtherStockAuditHelper {

    private static final String NO_DETAIL_MSG = "单据创建成功，但旺店通未返回单据详情，无法确认审核结果";

    private WdtOtherStockAuditHelper() {
    }

    public static ApiResult<Object> buildNoDetailResult() {
        return ApiResult.error(NO_DETAIL_MSG);
    }

    public static ApiResult<Object> buildOutStockAuditResult(StockoutOtherQueryResponse.OrderItem order) {
        if (order != null && order.getStatus() != null && order.getStatus().equals(110)) {
            return ApiResult.success();
        }
        String status = order == null ? "" : String.valueOf(order.getStatus());
        String statusName = WdtOutStockStatusEnum.getName(status);
        return buildAuditFailedResult(statusName, status, order == null ? "" : firstNonBlank(order.getReason(), order.getRemark()));
    }

    public static ApiResult<Object> buildInStockAuditResult(OtherStockinResponse.OrderInfoDto order) {
        if (order != null && order.getStatus() != null && order.getStatus().equals(80)) {
            return ApiResult.success();
        }
        String status = order == null ? "" : String.valueOf(order.getStatus());
        String statusName = WdtInStockStatusEnum.getName(status);
        return buildAuditFailedResult(statusName, status, order == null ? "" : firstNonBlank(order.getMessage(), order.getReason(), order.getRemark()));
    }

    public static ApiResult<Object> buildSelfOutStockAuditResult(StockExternalOutResponse.Order order) {
        if (order != null && WdtExtOutStockStatusEnum.finish().contains(order.getStatus())) {
            return ApiResult.success();
        }
        String status = order == null ? "" : order.getStatus();
        String statusName = WdtExtOutStockStatusEnum.getName(status);
        return buildAuditFailedResult(statusName, status, order == null ? "" : firstNonBlank(order.getReason(), order.getRemark()));
    }

    public static ApiResult<Object> buildSelfInStockAuditResult(StockExternalInResponse.Order order) {
        if (order != null && WdtExtInStockStatusEnum.finish().contains(order.getStatus())) {
            return ApiResult.success();
        }
        String status = order == null ? "" : order.getStatus();
        String statusName = WdtExtInStockStatusEnum.getName(status);
        return buildAuditFailedResult(statusName, status, order == null ? "" : firstNonBlank(order.getReason(), order.getRemark()));
    }

    private static ApiResult<Object> buildAuditFailedResult(String statusName, String status, String wdtMessage) {
        String format = String.format("单据创建成功，但旺店通审核未通过，当前状态：%s，错误信息：%s",
                StringUtils.isBlank(statusName) ? status : statusName,
                StringUtils.defaultIfBlank(wdtMessage, "无"));
        return ApiResult.error(format);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }
}
