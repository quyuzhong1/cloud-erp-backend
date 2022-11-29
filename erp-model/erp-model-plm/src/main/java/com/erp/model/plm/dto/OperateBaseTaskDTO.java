package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
    @NotNull(message = "任务id集合不能为空")
    @Size(min = 1,message = "勾选任务必须选一个")
    private List<String> taskIdList;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id 不能为空")
    private String productId;


    /**
     *是否确定完成
     * true 表示确定
     *
     */
    private Boolean isConfirmFinish=false;


}
