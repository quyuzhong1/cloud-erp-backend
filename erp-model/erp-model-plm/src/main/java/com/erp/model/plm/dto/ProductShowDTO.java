package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname ProductShowDTO
 * @Description TODO
 * @Date 2022-09-17 14:46
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class ProductShowDTO implements Serializable {


    private String productId;
    private String name;
    private String grade;
    private Integer approvalStatus;
    private Integer projectStatus;
    private Date endTime;
    private String projectChargeName;

    private String productChargeName;
    private String brandName;
    private Integer approvalProgress;
    private Integer projectProgress;
    private Integer taskCount;


}
