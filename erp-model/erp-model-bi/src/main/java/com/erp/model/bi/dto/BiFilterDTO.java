package com.erp.model.bi.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * BI 筛选条件
 * @Classname
 * @Description TODO
 * @Date 2022-12-16 9:27
 * @Created by yl
 */
@Data
public class BiFilterDTO implements Serializable {


    /**
     * 日期标示
     * today 今天
     * yesterday 昨天
     * lastSevenDays 最近7天
     * lastFifteenDays 最近15 天
     * lastThirtyDays 最近30 天
     */
    private String dateFlag;


    /**
     * 开始时间
     */
    private LocalDate startTime;

    /**
     * 结束时间
     */
    private LocalDate endTime;

    /**
     * 高级搜索的内容
     */
    private List<AdvanceSearchDTO> searchList;
}
