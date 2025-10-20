package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachDTO {
    /**
     * 名称
     */
    private String attachName;
    /**
     * 地址
     */
    private String attachUrl;

    /**
     * 业务id
     */
    private String businessId;
}
