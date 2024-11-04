package com.sdk.wms.antu.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AntuGetReturnReq extends AntuBaseRequest {

    //	状态 0:已作废 1:待确认 2:在途 3:到货 4:到货异常 5:已完成
    @JSONField(name = "spo_status")
//    @Builder.Default
    private Integer spoStatus;

    //更新开始时间， 格式YYYY-MM-DD HH:II:SS
    @JSONField(name = "spo_update_time_from")
    private String modifyDateFrom;

    //更新结束时间， 格式YYYY-MM-DD HH:II:SS
    @JSONField(name = "spo_update_time_to")
    private String modifyDateTo;

}
