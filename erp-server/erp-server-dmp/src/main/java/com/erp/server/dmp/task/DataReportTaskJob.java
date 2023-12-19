package com.erp.server.dmp.task;

import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.enums.SalesDataReportEnum;
import com.erp.model.dmp.vo.SyncDataReportVO;
import com.erp.server.dmp.service.DmpDateDimensionService;
import com.erp.server.dmp.service.DmpOrderInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    @Resource
    private DmpDateDimensionService dmpDateDimensionService;
    /**
     * 同步销售数据到物理表
     *
     * @return
     */
    @XxlJob("salesDataToPhysical")
    public void salesDataToPhysical() throws InterruptedException, ExecutionException {
        XxlJobHelper.log("=====同步销售数据到物理表 开始=====");
        long start = System.currentTimeMillis();
        SalesDataReportEnum[] values = SalesDataReportEnum.values();
        List<CompletableFuture<SyncDataReportVO>> list = new ArrayList<>(values.length);
        for (SalesDataReportEnum reportEnum : values) {
            CompletableFuture<SyncDataReportVO> future = dmpOrderInfoService.salesDataToPhysical(reportEnum);
            list.add(future);
        }
        for (Future<?> future : list) {
            while (true) {//CPU高速轮询：每个future都并发轮循，判断完成状态然后获取结果，这一行，是本实现方案的精髓所在。即有10个future在高速轮询，完成一个future的获取结果，就关闭一个轮询
                if (future.isDone() && !future.isCancelled()) { //获取future成功完成状态，如果想要限制每个任务的超时时间，取消本行的状态判断+future.get(1000*1, TimeUnit.MILLISECONDS)+catch超时异常使用即可。
                    String result = (String) future.get();//获取结果
                    XxlJobHelper.log("任务i={} 获取完成! {}", result, LocalDateTime.now());
                    break;//当前future获取结果完毕，跳出while
                } else {
                    Thread.sleep(1);//每次轮询休息1毫秒（CPU纳秒级），避免CPU高速轮循耗空CPU---》新手别忘记这个
                }
            }
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====同步销售数据到物理表 结束=====");
    }

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
}
