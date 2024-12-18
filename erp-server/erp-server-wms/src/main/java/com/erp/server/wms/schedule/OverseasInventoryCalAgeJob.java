package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.wms.entity.OverseasInventoryAgeDetailEntity;
import com.erp.server.wms.service.OverseasInventoryAgeDetailService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 三方仓库龄重算
 * @Author jack
 * @Date 2024-12-18
 **/
@Component
@Slf4j
@EnableScheduling
public class OverseasInventoryCalAgeJob {
    @Resource
    private OverseasInventoryAgeDetailService overseasInventoryAgeDetailService;

    /**
     *
     * @Author jack
     * @Date 2024-12-18
     * @return com.xxl.job.core.biz.model.ReturnT<java.lang.String>
     **/
    @XxlJob("overseasInventoryCalAgeJob")
    public ReturnT<String> poReturnAutoConfirmJob() {
        XxlJobHelper.log("=====三方仓库龄重算 开始任务=====");
        long start = System.currentTimeMillis();
        List<OverseasInventoryAgeDetailEntity> ageDetail = overseasInventoryAgeDetailService.getAgeDetail();
        if(CollUtil.isNotEmpty(ageDetail)) {
            LocalDate now = LocalDate.now();
            for (OverseasInventoryAgeDetailEntity ageDTO : ageDetail) {
                // 计算日期差
                int daysBetween = (int) ChronoUnit.DAYS.between(ageDTO.getPutAwayDate(), now);
                ageDTO.setInventoryAge(daysBetween);
            }

            overseasInventoryAgeDetailService.updateBatchById(ageDetail);
        }
        long end = System.currentTimeMillis();
        XxlJobHelper.log("主线程花费时间：{}", (end - start));
        XxlJobHelper.log("=====三方仓库龄重算 结束任务=====");
        return ReturnT.SUCCESS;
    }
}
