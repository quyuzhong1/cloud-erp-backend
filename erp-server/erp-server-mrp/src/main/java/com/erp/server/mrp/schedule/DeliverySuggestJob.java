package com.erp.server.mrp.schedule;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.server.mrp.service.DeliverySuggestService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Component
@EnableScheduling
public class DeliverySuggestJob {

    @Resource
    private DeliverySuggestService deliverySuggestService;

    /**
     * 归档，全量更新数据
     */
    @XxlJob("dataArchiving")
    public ReturnT dataArchiving(String param) {
        Map<String, String> map = JSONObject.parseObject(param, new TypeReference<Map<String,String>>(){});
        LocalDate calculationDate = LocalDateUtil.parseStrToLocalDate(map.get("calculationDate"));
        int days = Integer.parseInt(map.get("cleanDays"));
    	LocalDate finCalculationDate = calculationDate;
        XxlJobHelper.log("====开始更新信息====");
        return ReturnT.SUCCESS;
    }
}
