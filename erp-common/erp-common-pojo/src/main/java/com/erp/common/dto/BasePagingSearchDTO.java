package com.erp.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @Classname BaseSearchDTO
 * @Description TODO
 * @Date 2022-07-12 11:26
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BasePagingSearchDTO {

    private String searchKeyword;

    //开始时间
    private Date startTime;

    //结束时间
    private Date endTime;

    //状态 1 正常  0 不正常
    private Integer state;
}
