package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskFinishSkuDTO
 * @Description TODO
 * @Date 2022-11-29 14:45
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskFinishSkuDTO implements Serializable {

    /**
     * 任务id
     * @author yl
     * @date 2022-11-29 14:47
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    /**
     * 产品id
     */
    private String productId;

    /**
     * skuId 集合
     */
    private List<String> skuIdList;

    /**
     * 全部的集合
     */
    private List<String> allList;
}
