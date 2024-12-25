package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.DmpSoInfoService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class DmpOrderInfoJob {
    @Resource
    private DmpSoInfoService dmpSoInfoService;
    @Resource
    private MongoService mongoService;

    @XxlJob("syncGyyOrder")
    public void syncGyyOrder() {
        DmpSoInfoDTO.addGyyOrderDTO dto = new DmpSoInfoDTO.addGyyOrderDTO();
        String jobParam = XxlJobHelper.getJobParam();
        String startTime = null;
        String endTime = null;
        Integer page = 1;// 每页记录数
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            startTime = jsonParam.getStr("startTime");
            endTime = jsonParam.getStr("endTime");
            page = jsonParam.getInt("page", 1);
        }

        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setPage(page);

        XxlJobHelper.log("===========开始时间：" + startTime + "  结束时间：" + endTime);

        while(true) {
            XxlJobHelper.log("===========当前页数：" + page);

            List<GyyOrderEntity> mongoData = mongoService.findMongoData(dto, page, 1000, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
            if (CollUtil.isEmpty(mongoData)) {
                return;
            }
            dmpSoInfoService.addGyyOrder(mongoData);
            page++;
        }

    }
}