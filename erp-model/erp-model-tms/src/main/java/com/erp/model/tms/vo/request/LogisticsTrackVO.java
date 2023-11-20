package com.erp.model.tms.vo.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @ClassName LogisticsQueryVO
 * @description: 查询类
 * @date 2023年11月06日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogisticsTrackVO implements Serializable {
    /**
     * 跟踪号
     */
    private List<String> trackNos;
    /**
     * 授权信息
     */
    private Map<String, String> authMap;
    private List<String> orderNos;
    private String createTimeStart;
    private String createTimeEnd;
    private String cursor;
    private Integer queryPageSize;
}
