package com.sdk.wms.weishi.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiWarehouseResp {

    @JSONField(name = "id")
    private Integer id;
    @JSONField(name = "whsCode")
    private String whsCode;
    @JSONField(name = "whsNameEn")
    private String whsNameEn;
    @JSONField(name = "whsName")
    private String whsName;
}
