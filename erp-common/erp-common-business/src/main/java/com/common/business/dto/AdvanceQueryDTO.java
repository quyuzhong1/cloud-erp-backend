package com.common.business.dto;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 高级筛选通用查询DTO
 * @Author LiuRuiPeng
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvanceQueryDTO {

    // 前端字段名,也是数据库查询名
    private String field;

    //查询条件
    private String compare;

    //目标值
    private Object value;

    //数据类型
    private String dataType;

    //分组
    private String group;

    //连接符，and,or（保留值，默认and）
    private String compareSymbol = "and";

    //左括号数量(保留值，默认0)
    private int leftBracketCount;

    //右括号数量(保留值，默认0)
    private int rightBracketCount;

    public static AdvanceQueryDTO buildSplicingSQL(String field, QueryConditionEnum queryConditionEnum, Object value, QueryDataTypeEnum dataTypeEnum){
        return AdvanceQueryDTO.builder()
                .field(field)
                .compare(queryConditionEnum.getCompareCode())
                .value(value)
                .dataType(dataTypeEnum.getCode())
                .compareSymbol("and")
                .build();
    }
}
