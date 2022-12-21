package com.erp.model.bi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 16:38
 */
@Data
@NoArgsConstructor
public class DmpShopChangeLogDTO {

    /**
     * 修改人名称
     */
    private String updateUserName;

    /**
     * 修改时间
     */
    private LocalDate updateTime;

    /**
     * 负责人名称
     */
    private String chargeName;

    /**
     * 店铺负责开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enableTimeBegin;

    /**
     * 店铺负责结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate enableTimeEnd;
}
