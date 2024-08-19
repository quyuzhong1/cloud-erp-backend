package com.common.core.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.anno.LogAction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


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
    UPDATE_STATUS("updateStatus", "变更状态",true),
    LOGIN("login", "登录",false),
    LOGOUT("logout", "登出",false),
    CUSTOM_UPDATE("customUpdate", "自定义更新",false),
    UPDATE_WITHOUT_PARAMS("updateWithoutParams", "无参更新",false),
    CUSTOM_BATCH_UPDATE("customBatchUpdate", "自定义批量更新",false),
    CUSTOM_BATCH_INSERT("customBatchInsert", "自定义批量插入", false),
    UPLOAD("upload", "上传", false),
    DOWNLOAD("download", "下载", false),
    // 新接口禁止使用
    UNKNOWN_UPDATE("unknownUpdate", "无法识别的参数更新",false),
    CONFIRM("confirm", "确认",true),
    CANCEL_CONFIRM("cancelConfirm", "取消确认",true),
    RECEIVE("receive", "签收",true),
    EXECUTE("execute", "执行",true),
    GET_LOGISTICS_NO("getLogisticsNo", "获取物流跟踪号",true),
    ;

    @EnumValue
    private final String code;
    private final String name;
    /**
     * 是否是批量添加日志: true=是, 默认：false=否
     */
    private final boolean isBatchRecord;

    /**
     * 是否属于对比更新
     */
    public boolean hasCompare(){
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
        if (this.isBatchRecord){
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
            return this.isBatchRecord;
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
