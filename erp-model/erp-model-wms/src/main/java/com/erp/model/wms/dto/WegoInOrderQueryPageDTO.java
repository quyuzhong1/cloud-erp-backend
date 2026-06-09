package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * WEGO 入库订单分页查询（interfaceType=inorder.queryPage）请求参数。
 *
 * <p>WEGO 服务端约束：</p>
 * <ul>
 *     <li>{@code pageSize} 最大支持 100；</li>
 *     <li>{@code pageNum} 从 1 开始；</li>
 *     <li>所有日期范围字段均可选，调用方按业务场景任选其一传入。</li>
 * </ul>
 *
 * <p>公共参数 {@code accessToken / interfaceType / sign} 由 SDK 统一拼装，
 * {@code secret} 仅参与本地签名计算，不会透传到 WEGO。</p>
 */
@Data
@NoArgsConstructor
public class WegoInOrderQueryPageDTO implements Serializable {

    /**
     * WEGO inorder.queryPage 单页最大条数（由服务端约束）。
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 入库订单分页查询请求
     */
    @Data
    @NoArgsConstructor
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
         * 完结开始日期，格式 yyyy-MM-dd
         */
        private String finishDateBegin;

        /**
         * 完结结束日期，格式 yyyy-MM-dd
         */
        private String finishDateEnd;

        /**
         * 订单开始日期，格式 yyyy-MM-dd
         */
        private String orderDateBegin;

        /**
         * 订单结束日期，格式 yyyy-MM-dd
         */
        private String orderDateEnd;

        /**
         * 上架开始日期，格式 yyyy-MM-dd
         */
        private String upDateBegin;

        /**
         * 上架结束日期，格式 yyyy-MM-dd
         */
        private String upDateEnd;

        /**
         * 页码，从 1 开始
         */
        @NotNull(message = "pageNum不能为空")
        @Min(value = 1, message = "pageNum最小为1")
        private Integer pageNum;

        /**
         * 每页条数，WEGO 服务端最大 100
         */
        @NotNull(message = "pageSize不能为空")
        @Min(value = 1, message = "pageSize最小为1")
        @Max(value = MAX_PAGE_SIZE, message = "pageSize最大为" + MAX_PAGE_SIZE)
        private Integer pageSize;
    }
}
