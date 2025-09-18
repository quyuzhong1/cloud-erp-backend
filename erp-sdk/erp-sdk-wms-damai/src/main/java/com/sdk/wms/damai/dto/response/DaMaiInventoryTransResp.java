package com.sdk.wms.damai.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class DaMaiInventoryTransResp {

    @JsonProperty("billNo")
    private String billNo;
    @JsonProperty("billTypeName")
    private String billTypeName;
    @JsonProperty("operationTypeName")
    private String operationTypeName;
    @JsonProperty("skuCode")
    private String skuCode;
    @JsonProperty("skuName")
    private String skuName;
    @JsonProperty("customerSkuCode")
    private String customerSkuCode;
    @JsonProperty("whCode")
    private String whCode;
    @JsonProperty("transQty")
    private Integer transQty;
    @JsonProperty("fmTotalQty")
    private Integer fmTotalQty;
    @JsonProperty("toTotalQty")
    private Integer toTotalQty;
    @JsonProperty("remark")
    private String remark;
    @JsonProperty("operationTime")
    private String operationTime;
    private String authId;

    private String uniqueKey;
    /**
     * 生成基于 billNo、skuCode、operationTime 的32位唯一标识
     * 采用SHA-256哈希算法，取前32位确保唯一性和长度限制
     */
    public String getUniqueKey() {
        try {
            // 1. 拼接三个核心字段（处理null值）
            String combined = String.join("|",
                    Objects.toString(billNo, ""),
                    Objects.toString(skuCode, ""),
                    Objects.toString(operationTime, ""));

            // 2. 使用SHA-256哈希算法生成固定长度哈希值
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            // 3. 转换为16进制字符串（64位），取前32位满足长度限制
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            // 4. 截取前32位，确保长度为32字符
            return hexString.substring(0, 32);

        } catch (NoSuchAlgorithmException e) {
            // 哈希算法不支持时的降级处理（理论上不会发生）
            throw new RuntimeException("生成唯一标识失败", e);
        }
    }
}
