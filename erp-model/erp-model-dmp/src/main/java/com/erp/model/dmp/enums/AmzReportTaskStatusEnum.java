package com.erp.model.dmp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

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
    QUERY("query","待获取报告(第二步)"),
    DOWNLOAD("download","待下载数据(第三步)"),
    PARSE("parse","待解析(第四步)"),
    FINISH("finish","已完成"),
    STOP("stop","系统终止"),
    MANUAL_STOP("manualStop","人工终止"),
    EXIST_STOP("existStop","最新报告已有终止"),
    DIRECT_QUERY("direct_query","直接获取报表(第一步/第二步)"),
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


    /**
     * 非完成或终止的任务状态
     */
    public static List<String> notFinishOrStopList() {
        return Arrays.asList(
                AmzReportTaskStatusEnum.CREATED.getCode(),
                AmzReportTaskStatusEnum.QUERY.getCode(),
                AmzReportTaskStatusEnum.DOWNLOAD.getCode(),
                AmzReportTaskStatusEnum.PARSE.getCode()
        );
    }
}
