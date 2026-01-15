package com.sdk.third.lingxing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @Author: wtr
 * @Date: 2025/12/22 9:13
 * @Param:
 * @Return:
 * @Description: FBA签收明细请求体
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbaShipmentReqDTO {

    /**
     * sid	店铺id	是
     */
    private String sid;

    /**
     * 货件创建开始日期，格式：Y-m-d，左闭右开
     */
    private String start_date;

    /**
     * 货件创建截止日期，格式：Y-m-d，左闭右开
     */
    private String end_date;

    /**
     * offset	分页偏移量，默认0	否
     */
    private Integer offset;

    /**
     * length	分页长度，默认1000	否
     */
    private Integer length;

    public FbaShipmentReqDTO(String sid, String start_date, String end_date) {
        this.sid = sid;
        this.start_date = start_date;
        this.end_date = end_date;
        this.offset = 0;
        this.length = 1000;
    }

    public FbaShipmentReqDTO(String sid, String start_date, String end_date, Integer offset) {
        this.sid = sid;
        this.start_date = start_date;
        this.end_date = end_date;
        this.offset = offset;
        this.length = 1000;
    }
}