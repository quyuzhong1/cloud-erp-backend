package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName HandoverCommitResult
 * @date 2024年02月21日
 * @version: 1.0
 */
@Data
public class HandoverCommitResult implements Serializable {
    @JSONField(name = "data")
    private HandoverCommitResponse response;
    @JSONField(name = "success")
    private Boolean success;
}
