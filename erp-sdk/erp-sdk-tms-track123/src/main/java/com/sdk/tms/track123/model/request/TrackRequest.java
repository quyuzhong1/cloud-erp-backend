package com.sdk.tms.track123.model.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName TrackRequest
 * @description: TODO
 * @date 2023年11月07日
 * @version: 1.0
 */
@Data
@Builder
public class TrackRequest implements Serializable {
    private List<String> trackNos;
    private List<String> orderNos;
    private String createTimeStart;
    private String createTimeEnd;
    private String cursor;
    private Integer queryPageSize;
}
