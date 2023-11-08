package com.sdk.tms.track123.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @ClassName Data
 * @description: TODO
 * @date 2023年11月07日
 * @version: 1.0
 */
@Data
public class ResponseData implements Serializable {
    //收货
    private TrackInfo accepted;
    //拒收
    private TrackInfo rejected;
}
