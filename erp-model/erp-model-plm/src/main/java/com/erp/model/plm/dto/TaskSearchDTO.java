package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;

/**
 * @Classname TaskSearchDTO
 * @Description TODO
 * @Date 2022-09-21 14:20
 * @Created by yl
 */
@Data
@NoArgsConstructor
@Validated
public class TaskSearchDTO implements Serializable {

    //搜索关键字
    private String searchKeyword;

    //搜索类型
    @StateEnumValue(strValues = {"name", "priority", "personInCharge", "status",  "createTime"}, message = "搜索类型有误")
    private String searchType;

    //优先级
    private Integer priority;

    private String personInChargeId;

    //状态
    private Integer status;

    private Date startTime;

    private Date endTime;




}
