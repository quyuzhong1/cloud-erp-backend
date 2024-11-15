package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * 亚马逊 创建报告结果参数 DTO
 *
 * @Author Jim
 **/
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class AmazonCreateReportResultDTO {

    /**
     * 查询是否有处理中的报告
     * (计算预估等待时间：0=不等待)
     * 创建报告响应代号429,根据1/0.0167约等于60秒
     * 存在处理中的报告/创建必定失败:根据历史报告预估完成时间.
     */
    private Long estimatedWaitSecond;

    /**
     * 创建成功后响应的报告ID
     */
    private String reportId;

    /**
     * 创建成功
     */
    private boolean createdSuccess;

    public static AmazonCreateReportResultDTO success(String reportId) {
        return new AmazonCreateReportResultDTO(0L, reportId, true);
    }

    public static AmazonCreateReportResultDTO wait(Long estimatedWaitSecond) {
        return new AmazonCreateReportResultDTO(estimatedWaitSecond, "", false);
    }


}
