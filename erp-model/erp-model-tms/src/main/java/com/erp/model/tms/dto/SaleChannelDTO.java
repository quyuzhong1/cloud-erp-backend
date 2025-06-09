package com.erp.model.tms.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName SaleChannelDTO

 * @date 2023年11月15日
 * @version: 1.0
 */
@Data
public class SaleChannelDTO implements Serializable {
    /**
     * 主键id
     */
    private String  id;

    /**
     * 渠道id(物流平台原始id)
     */
    private String platformChannelId;

    /**
     * 渠道名称(默认中文)
     */
    private String cnName;

    /**
     * 渠道编码
     */
    private String code;
}
