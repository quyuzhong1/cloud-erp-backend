package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskOperateDTO

 * @Date 2022-10-19 14:51
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskOperateDTO  implements Serializable {

    /**
     * 任务id 集合
     */
    @NotNull(message = "任务id集合不能为空")
    @Size(min = 1,message = "勾选任务必须选一个")
    private List<TaskHandleDataDTO>  taskDataList;

    /**
     * 产品id 不能为空
     */
    @NotBlank(message = "产品id不能为空")
    private String productId;

    /**
     * 意见
     */
    private String comment;


    /**
     * 附件url集合
     */
    private List<String> attachUrlList;

    /**
     * 附件名集合
     */
    private List<String> attachNameList;


}
