package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 *  启动任务
 * @Classname StartTaskDTO
 * @Description TODO
 * @Date 2022-10-20 12:09
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class OperateBaseTaskDTO implements Serializable {

    /**
     * 任务id 集合
     */
    private List<String> taskIdList;

    private String productId;


}
