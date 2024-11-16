package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName Data
 * @description: TODO
 * @date 2023年11月07日
 * @version: 1.0
 */
@Data
public class OceanResponseData implements Serializable {
    /**
     * 成功数据
     */
    private List<OceanTrackInfo> accepted;
    /**
     * 失败数据
     */
    private List<Rejected> rejected;
}
