package com.erp.model.tms.vo.request;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName TrackInfoVO

 * @date 2023年11月20日
 * @version: 1.0
 */
@Data
public class TrackInfoVO implements Serializable {
    /**
     * 批量运单号
     */
    private List<String> trackNos;
}
