package com.erp.model.wms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * WEGO 2C 出库单分页查询（interfaceType=2c.order.queryPage）请求参数。
 *
 * <p>查询过滤条件（均为可选）：</p>
 * <ul>
 *     <li>{@code noList}：按 WEGO 出库单号列表过滤，最多 100 条；</li>
 *     <li>{@code finishDateBegin/End}：按完结日期范围过滤；</li>
 *     <li>{@code orderDateBegin/End}：按订单日期范围过滤。</li>
 * </ul>
 *
 * <p>响应 {@code result} 为分页对象（{@link com.sdk.wms.wego.dto.response.WegoOutboundResp.PageResultDTO}），
 * 通过 {@code pages / emptyFlag} 判断是否还有下一页。</p>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoOutboundQueryPageDTO implements Serializable {

    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 2C 出库单分页查询请求
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QueryReqDTO implements Serializable {

        /**
         * WEGO accessToken（取自 overseas_provider.auth_json.appToken）
         */
        @NotBlank(message = "accessToken不能为空")
        private String accessToken;

        /**
         * WEGO secret（仅参与本地签名，不会透传到第三方）
         */
        @NotBlank(message = "secret不能为空")
        private String secret;

        /**
         * WEGO 出库单号列表（可选，最多 100 条；为空则按日期范围过滤）
         */
        private List<String> noList;

        /**
         * 完结日期开始（格式：YYYY-MM-DD HH:mm:ss，可选）
         */
        private String finishDateBegin;

        /**
         * 完结日期结束（格式：YYYY-MM-DD HH:mm:ss，可选）
         */
        private String finishDateEnd;

        /**
         * 订单日期开始（格式：YYYY-MM-DD HH:mm:ss，可选）
         */
        private String orderDateBegin;

        /**
         * 订单日期结束（格式：YYYY-MM-DD HH:mm:ss，可选）
         */
        private String orderDateEnd;

        /**
         * 页码（从 1 开始）
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 每页数量（最大 100）
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        @Max(value = MAX_PAGE_SIZE, message = "pageSize最大为" + MAX_PAGE_SIZE)
        private Integer pageSize;
    }
}
