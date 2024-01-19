package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 亚马逊报告任务执行状态
 * </p>
 *
 * @author Jim
 * @since 2023-11-08
 */
@Getter
@AllArgsConstructor
public enum AmzReportTaskStatusEnum {

    CREATED("created", "待请求/创建报表(第一步)"),
    QUERY("query","待获取列表(第二步)"),
    DOWNLOAD("download","待下载数据(第三步)"),
    PARSE("parse","待解析(第四步)"),
    FINISH("finish","已完成"),
    STOP("stop","已终止"),

    ;

    /**
     * 代号
     */
    @EnumValue
    private final String code;

    /**
     * 名称
     */
    private final String name;


}
