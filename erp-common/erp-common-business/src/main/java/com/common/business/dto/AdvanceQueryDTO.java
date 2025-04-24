package com.common.business.dto;

import com.common.business.enums.QueryConditionEnum;
import com.common.business.enums.QueryDataTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 高级筛选通用查询DTO
 * @Author LiuRuiPeng
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdvanceQueryDTO implements Serializable {

    private static final long serialVersionUID = 7251567856692109648L;
    // 前端字段名,也是数据库查询名
    private String field;

    //查询条件
    private String compare;

    //目标值
    private Object value;

    //数据类型
    private String dataType;

    //是否扩展字段
    private Boolean isExtend;

    //分组
    private String groupName;

    //连接符，and,or（保留值，默认and）
    private String compareSymbol = "and";

    //左括号数量(保留值，默认0)
    private int leftBracketCount;

    //右括号数量(保留值，默认0)
    private int rightBracketCount;

    public static AdvanceQueryDTO buildSplicingSQLDTO(String field, QueryConditionEnum queryConditionEnum, Object value, QueryDataTypeEnum dataTypeEnum){
        return AdvanceQueryDTO.builder()
                .field(field)
                .compare(queryConditionEnum.getCompareCode())
                .value(value)
                .dataType(dataTypeEnum.getCode())
                .compareSymbol("and")
                .build();
    }

    public static AdvanceQueryDTO buildDefaultSplicingSQLDTO(String field, Object value){
        return AdvanceQueryDTO.builder()
                .field(field)
                .compare(QueryConditionEnum.IN_LIST.getCompareCode())
                .value(value)
                .dataType(QueryDataTypeEnum.STRING.getCode())
                .compareSymbol("and")
                .build();
    }
}
