package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Classname 任务文档统计
 * @Description TODO
 * @Date 2022-09-22 9:55
 * @Created by yl
 */
@Data
public class CountDTO implements Serializable {


    //任务id 或者产品id
    private String flagId;

    //数量
    private Integer count;
}
