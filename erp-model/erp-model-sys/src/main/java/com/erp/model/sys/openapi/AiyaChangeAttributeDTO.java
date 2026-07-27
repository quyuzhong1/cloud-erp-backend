package com.erp.model.sys.openapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 爱亚转移单反馈（库存状态转化）入参。
 * <p>
 * 业务字段对齐爱亚 {@code changeAttribute4Edi} 裸报文；由爱亚/EDI 推送到
 * {@code POST /webhook/receive/aiyaChangeAttribute}，响应为 MetaResponse。
 * <p>
 * 为减少对方改动，允许继续携带原报文中的可选字段（如 {@code partnerId}、明细批次/效期等），
 * 未知字段忽略；本期落单仅使用仓库/SKU/数量/GOOD↔DAMAGE 状态相关字段。
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiyaChangeAttributeDTO implements Serializable {

    /**
     * 合作伙伴 ID（爱亚原报文可选字段，可继续传；本期不参与路由）
     * <p>
     * 身份依赖网关免登白名单及后续 {@code WebhookHandler#verify}（上线前补齐），不再按 OpenAPI appId 识别。
     */
    private String partnerId;

    /**
     * 客户代码（爱亚原报文必填透传；本期不参与仓/SKU 路由）
     * <p>
     * 身份依赖网关免登白名单及后续 {@code WebhookHandler#verify}（上线前补齐），不再按 OpenAPI Header appId 识别。
     */
    @NotBlank(message = "customerCode不能为空")
    private String customerCode;

    /**
     * 仓库代码（爱亚平台仓码）
     */
    @NotBlank(message = "warehouseCode不能为空")
    private String warehouseCode;

    /**
     * 转移单号（幂等键）
     */
    @NotBlank(message = "changeAttributeNumber不能为空")
    private String changeAttributeNumber;

    /**
     * 转移单确认时间（毫秒时间戳）
     */
    private Long confirmDate;

    /**
     * 转移类型，本期仅处理 CHANGE_STATUS
     */
    @NotBlank(message = "type不能为空")
    private String type;

    /**
     * 转移明细
     */
    @NotEmpty(message = "changeList不能为空")
    @Valid
    private List<ChangeItem> changeList;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChangeItem implements Serializable {

        @NotBlank(message = "sku不能为空")
        private String sku;

        @NotNull(message = "changeQty不能为空")
        private Integer changeQty;

        /**
         * 源货物状态：GOOD / DAMAGE
         */
        private String fromStatus;

        /**
         * 目标货物状态：GOOD / DAMAGE
         */
        private String toStatus;
    }
}
