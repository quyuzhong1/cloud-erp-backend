package com.sdk.wms.weishi.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * @author liuruipeng
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class WeiShiCreateInboundResp {

    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "inboundNo")
    private String inboundNo;
    @JSONField(name = "boxNoList")
    private List<String> boxNoList;
}
