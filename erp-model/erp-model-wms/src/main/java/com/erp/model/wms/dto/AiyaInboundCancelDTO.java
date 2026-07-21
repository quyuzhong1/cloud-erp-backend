package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * AIYA（爱亚/百世 GLINK）取消入库单（serviceType={@code GLINK_CANCEL_ASN_NOTIFY}）请求报文。
 * <p>
 * 按爱亚取消入库单接口，业务必填参数仅 {@code asnNumbers[]}（ASN 编码，即我方下发的
 * {@code asnNumber}=发货单号）；{@code customerCode}（用户编码）虽为必填，但由
 * {@code AiyaOpenApiService} 从授权信息统一注入 bizData，本 DTO 不重复承载。
 * <p>
 * 接口文档中的 {@code warehouseCode}/{@code asnType}/{@code page}/各类时间范围等均为可选字段，
 * 取消场景无需传，按「非必填不纳入 DTO」的约定不再承载。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiyaInboundCancelDTO implements Serializable {

    /**
     * ASN 编码列表（必填），单个长度 ≤ 64，即我方下发的 {@code asnNumber}（=发货单号）。
     */
    @NotEmpty(message = "asnNumbers不能为空")
    private List<String> asnNumbers;
}
