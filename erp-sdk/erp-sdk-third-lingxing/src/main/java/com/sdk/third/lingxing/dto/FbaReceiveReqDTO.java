package com.sdk.third.lingxing.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * FBA签收明细请求体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbaReceiveReqDTO {

    /**
     * sid	店铺id	是
     */
    private Integer sid;

    /**
     * event_date	签收日期，格式：Y-m-d	是
     */
    private String event_date;

    /**
     * offset	分页偏移量，默认0	否
     */
    private Integer offset;

    /**
     * length	分页长度，默认1000	否
     */
    private Integer length;


    public FbaReceiveReqDTO(Integer sid, LocalDate eventDate) {
        this.sid = sid;
        this.event_date = eventDate.toString();
        this.offset = 0;
        this.length = 1000;
    }

    public FbaReceiveReqDTO(Integer sid, LocalDate eventDate, Integer offset) {
        this.sid = sid;
        this.event_date = eventDate.toString();
        this.offset = offset;
        this.length = 1000;
    }
}
