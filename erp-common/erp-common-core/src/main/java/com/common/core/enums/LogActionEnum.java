package com.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.anno.LogAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Stream;


/**
 * 系统操作类型
 *
 * @author Jim
 */
@Getter
@AllArgsConstructor
public enum LogActionEnum {

    INSERT("insert", "插入", false),
    UPDATE("update", "更新", false),
    DELETE("delete", "删除", true),
    GRANT("grant", "授权", false),
    IMPORT("import", "导入", false),
    EXPORT("export", "导出",false),
    CANCEL("cancel", "撤销",true),
    SUBMIT("submit", "提交",true),
    APPROVE("approve", "审核",true),
    DISAPPROVE("disapprove", "反审核",true),
    INVALID("invalid", "作废",true),
    ADD_AND_SUBMIT("addAndSubmit", "新增并提交",false),
    UPDATE_AND_SUBMIT("updateAndSubmit", "更新并提交",false),

    ;

    @EnumValue
    private final String code;
    private final String name;
    /**
     * 默认：是否是批量操作: true=是批量操作, false=非批量操作
     */
    private final boolean isBatchOperation;

    /**
     * 是否属于更新
     */
    public boolean isUpdate(){
        return Arrays.asList(
                LogActionEnum.UPDATE,
                LogActionEnum.UPDATE_AND_SUBMIT
        ).contains(this);
    }

    /**
     * 获取key主键字段名
     */
    public String checkAndGetKeyIdName(String annoKeyIdName){
        // 注解设置优先
        if (StringUtils.isNotBlank(annoKeyIdName)){
            return annoKeyIdName;
        }
        if (this.isBatchOperation){
            // 批量操作默认
            return "ids";
        } else {
            // 单操作默认
            return "id";
        }
    }

    /**
     * 判断是否是批量操作
     * {@link LogAction} 注解isBatchOperationStr优先
     */
    public boolean checkIsBatchOperation(String batchOperationStr) {
        if (StringUtils.isBlank(batchOperationStr)){
            return this.isBatchOperation;
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(batchOperationStr);
    }

    /**
     * 根据code获取枚举
     */
    public static LogActionEnum getByCode(String code) {
        return Arrays.stream(values()).filter(value -> value.getCode().equals(code))
                .findFirst().orElse(null);
    }


}
