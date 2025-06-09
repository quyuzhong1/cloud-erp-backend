package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import com.erp.model.wms.entity.WmsDataCompareTaskEntity;
import com.erp.model.wms.enums.WmsDataCompareTaskStatusEnum;
import com.erp.server.wms.service.WmsDataCompareTaskService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
@Slf4j
public class DataCompareJob {

    @Resource
    private WmsDataCompareTaskService wmsDataCompareTaskService;

    /**
     * 仓库缓存清除
     */
    @XxlJob("dataCompareRetry")
    public ReturnT<String> dataCompareRetry() {
        List<WmsDataCompareTaskEntity> list = wmsDataCompareTaskService.lambdaQuery()
        		.eq(WmsDataCompareTaskEntity::getStatus, WmsDataCompareTaskStatusEnum.DOING.getCode()).select(WmsDataCompareTaskEntity::getId).list();
        if(CollUtil.isNotEmpty(list)) {
        	for(WmsDataCompareTaskEntity l : list) {
        		wmsDataCompareTaskService.dealParseTask(l.getId());
        	}
        }
        return ReturnT.SUCCESS;
    }

}