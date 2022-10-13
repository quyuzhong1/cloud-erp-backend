package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

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
public class TaskPagingDTO  implements Serializable {


     //产品id
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
      * 3 全部
      */
     @StateEnumValue(intValues = {0, 1,3}, message = "任务类型有误")
     private Integer taskFlag;


     //赛选条件
     /**
      * 筛选条件
      */
     List<TaskSearchDTO>  searchList;


     /**
      * 搜索关键字
      */
     private String searchKeyword;


}
