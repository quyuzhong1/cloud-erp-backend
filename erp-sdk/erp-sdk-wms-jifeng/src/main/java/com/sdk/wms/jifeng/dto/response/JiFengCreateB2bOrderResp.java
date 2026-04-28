package com.sdk.wms.jifeng.dto.response;

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
public class JiFengCreateB2bOrderResp {

    @JSONField(name = "erpNo")
    private String erpNo;
    @JSONField(name = "outboundNo")
    private String outboundNo;
}
