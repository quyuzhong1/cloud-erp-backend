package com.sdk.wms.goodcang.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class GoodCangUploadFileResp extends CleanBaseDTO implements Serializable {

    //附件ID（整型）
    @JSONField(name = "attachment_id")
    private Integer attachmentId;
}
