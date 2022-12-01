package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/18 16:45
 */
@Data
@NoArgsConstructor
public class ProductMilepostDateDTO implements Serializable {

    /**
     * 实际结束时间
     */
    private Date realityEndTime;

    /**
     *预计结束时间
     */
    private Date planEndTime;
}
