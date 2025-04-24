package com.erp.server.dmp.task;

import com.common.core.utils.date.DateUtil;
import com.erp.server.dmp.service.DmpDateDimensionService;
import com.erp.server.dmp.service.BiOrderInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author zdy
 * @ClassName DataReportTaskJob
 * @description: 数据报表任务
 * @date 2023年12月05日
 * @version: 1.0
 */
@Slf4j
@Component
public class DataReportTaskJob {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Resource
    private BiOrderInfoService biOrderInfoService;
    @Resource
    private DmpDateDimensionService dmpDateDimensionService;
    /**
     *
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @XxlJob("createTimeDimension")
    public void createTimeDimension() throws InterruptedException, ExecutionException {
        XxlJobHelper.log("createTimeDimension start");
        String jobParam = XxlJobHelper.getJobParam();
        int year = 2023;
        if (StringUtils.isNotBlank(jobParam)){
            year = Integer.parseInt(jobParam);
        }else {
            LocalDateTime now = LocalDateTime.now();
            year = now.getYear();
        }
        //获取天列表
        List<LocalDateTime> datesInYear = DateUtil.getDatesInYear(year);
        //删除当前年维度的数据
        dmpDateDimensionService.deleteByYear(String.valueOf(year));
        //批量新增
        dmpDateDimensionService.batchInsertDateDimensions(datesInYear);
        XxlJobHelper.log("createTimeDimension end");
    }

    /**
     * 同步销售数据(根据平台创建时间)到物理表
     *
     * @return
     */
    @XxlJob("dmpOrderCreateTimeToPhysical")
    public void DmpOrderCreateTimeToPhysical(){
        XxlJobHelper.log("DmpOrderCreateTimeToPhysical start :" + LocalDateTime.now());
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // 准备调用存储过程
            String sql = "{call dmp_order_create_report_physical() }";
            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                // 执行存储过程
                cstmt.execute();
                XxlJobHelper.log("DmpOrderCreateTimeToPhysical success");
            }
        } catch (SQLException e) {
            XxlJobHelper.log("DmpOrderCreateTimeToPhysical error");
        }
        XxlJobHelper.log("DmpOrderCreateTimeToPhysical end :" + LocalDateTime.now());
    }
    /**
     * 同步销售数据(根据平台创建时间)到物理表
     *
     * @return
     */
    @XxlJob("dmpOrderDeliveryTimeToPhysical")
    public void DmpOrderDeliveryTimeToPhysical(){
        XxlJobHelper.log("DmpOrderDeliveryTimeToPhysical start :" + LocalDateTime.now());
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            // 准备调用存储过程
            String sql = "{call dmp_order_delivery_report_physical() }";
            try (CallableStatement cstmt = conn.prepareCall(sql)) {
                // 执行存储过程
                cstmt.execute();
                XxlJobHelper.log("DmpOrderDeliveryTimeToPhysical success");
            }
        } catch (SQLException e) {
            XxlJobHelper.log("DmpOrderDeliveryTimeToPhysical error");
        }
        XxlJobHelper.log("DmpOrderDeliveryTimeToPhysical end :" + LocalDateTime.now());
    }
}
