package com.erp.model.plm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Lambda
 * @Classname TasdDTO
 * @Description TODO
 * @Date 2023-06-20 19:56
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public  static class TaskPagingParamDTO extends SortDTO {

        /**
         * 搜索关键字
         */
        private String searchKeyword;

        /**
         * 任务条件
         * 1.待完成
         * 2 全部
         * 3.待审核
         */
        private Integer taskCondition;

        /**
         * 分组名 no 不分组
         * product 产品分组
         * planEndTime  计划结束时间
         */
        @StateEnumValue(strValues = {"no","product","planEndTime"}, message = "分组名标示")
        private String groupNameFlag;

        /**
         * 分组的标示 可能是时间 也可能是产品id
         */
        private String groupFlag;


        /**
         * 高级搜索筛选条件
         */
        private TaskSearchDTO  taskSearchDTO;





    }
}
