package com.erp.server.mrp.schedule;

import com.common.core.utils.date.LocalDateUtil;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@EnableScheduling
public class DataArchivingJob {

    @Resource
    private DataArchivingService dataArchivingService;

    /**
     * 归档，全量更新数据
     */
    @XxlJob("dataArchiving")
    public ReturnT<String> dataArchiving(LocalDate calculationDate) {
    	if(calculationDate == null) {
    		String calculationDateParam = XxlJobHelper.getJobParam();
    		if(StringUtils.isNotBlank(calculationDateParam)) {
    			calculationDate = LocalDateUtil.parseStrToLocalDate(calculationDateParam);
    		}
    	}
    	LocalDate finCalculationDate = calculationDate;
        XxlJobHelper.log("====开始更新信息====");
        CompletableFuture.runAsync(() -> dataArchivingService.dataArchiving(finCalculationDate));
        return ReturnT.SUCCESS;
    }
}
