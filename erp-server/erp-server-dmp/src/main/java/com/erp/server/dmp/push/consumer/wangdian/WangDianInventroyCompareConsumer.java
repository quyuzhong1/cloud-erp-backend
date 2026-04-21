package com.erp.server.dmp.push.consumer.wangdian;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.DmpSyncTaskIdDTO;
import com.common.business.enums.SyncStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.message.handler.AbstractPlatformConsumerHandler;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.wms.dto.WdtCompareInventoryDTO;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.rpc.wms.feign.VirtualWarehouseAllocationDetailFeign;
import com.erp.server.dmp.push.service.wdt.WangDianInventoryCompareService;
import com.erp.server.dmp.push.service.wdt.WangDianVwPushHandleDetailService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

@Component
@Slf4j
public class WangDianInventroyCompareConsumer<T extends DmpSyncTaskIdDTO> extends AbstractPlatformConsumerHandler<T> {
    @Resource
    private DmpPushTaskService dmpPushTaskService;
    @Resource
    private WangDianInventoryCompareService wangDianInventoryCompareService;

    @Override
    public void updateSyncTaskStatus(DmpSyncMqDTO.ParamDTO paramDTO) {
        dmpPushTaskService.updateStatus(paramDTO);
    }

    @Override
    public void updateMongodbData(String platform, String uniqueId, Integer isClean) {

    }

    @Override
    public void sendWarnMsg(String syncTaskId, String msg) {
        dmpPushTaskService.sendWarnMsg(syncTaskId);
    }

    @Override
    public ApiResult<?> handle(Object ext) {
        log.warn("分货单触发旺店通同步库存->获取消费数据：{}", ext);
        WdtCompareInventoryDTO pushDTOS = JSON.parseObject(ext.toString(), WdtCompareInventoryDTO.class);
        return wangDianInventoryCompareService.executeConsumer(pushDTOS);
    }
}
