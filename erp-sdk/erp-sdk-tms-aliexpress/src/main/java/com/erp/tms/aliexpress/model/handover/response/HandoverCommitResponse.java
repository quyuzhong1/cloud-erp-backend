package com.erp.tms.aliexpress.model.handover.response;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName HandoverCommitResponse
 * @date 2024年02月05日
 * @version: 1.0
 */
@Data
public class HandoverCommitResponse implements Serializable {
    /**
     * 未组包成功的小包信息
     */
    @JSONField(name = "not_commit_parcel_detail")
    private String notCommitParcelDetail;
    /**
     * 交接物id，即大包id
     */
    @JSONField(name = "handover_content_id")
    private Long handoverContentId;
    /**
     * 交接批次号，即交接单id
     */
    @JSONField(name = "handover_order_id")
    private Long handoverOrderId;
    /**
     * 交接物编码，即大包LP号
     */
    @JSONField(name = "handover_content_code")
    private String handoverContentCode;
}
