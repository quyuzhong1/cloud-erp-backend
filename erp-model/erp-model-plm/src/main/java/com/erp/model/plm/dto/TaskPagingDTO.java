package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskPagingDTO
 * @Description TODO
 * @Date 2022-09-21 14:41
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskPagingDTO extends SortDTO implements Serializable {

     /**
      * 产品id
      */
     @NotBlank(message = "产品id不能为空")
     private String productId;

     /**
      * 阶段id 如果没有就是全部
      */
     private String phaseId;

     /**
      * 任务类型
      * 0 待我完成
      * 1 待我审核
      * 2全部
      */
     @StateEnumValue(intValues = {0,1,2}, message = "任务类型有误")
     private Integer taskFlag;

     /**
      * 高级搜索筛选条件
      */
     TaskSearchDTO  taskSearchDTO;

     /**
      * 搜索关键字
      */
     private String searchKeyword;

     /**
      *  状态值 0:待发布 1:未开始 2:进行中 3 已完成, 4.完成待确认 5.审核中  6 审核通过 7 审核不通过
      */
     private List<Integer> statusList;


}
