package com.sdk.wms.antu.dto.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.common.business.dto.CleanBaseDTO;
import lombok.*;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AntuUploadFileResp extends CleanBaseDTO implements Serializable {

    //附件主键id
    @JSONField(name = "attach_id")
    private Integer attachId;

    //url
    @JSONField(name = "url")
    private String url;
}
