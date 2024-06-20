package com.erp.server.dmp.push.service.wdt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.rpc.wms.feign.VirtualWarehouseAllocationDetailFeign;
import com.erp.rpc.wms.feign.WmsVirtualWarehouseFeign;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianVwAllocationHandleDetailService;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.Result;
import com.sdk.wangdian.sdk.api.virtualWarehouse.VwAllocationHandleDetailAPI;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwAllocationHandelDetailPushDTO;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwAllocationHandelDetailResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分货单处理明细
 */
@Service
@Slf4j
public class WangDianVwAllocationHandleDetailServiceImpl implements WangDianVwAllocationHandleDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;
    @Resource
    private VirtualWarehouseAllocationDetailFeign allocationDetailFeign;

    @Resource
    private RedissonClient redissonClient;

    private static final String LOCK = "wdt:push:virtualWarehouseSync:";


    @Override
    public void executeConsumer(VwAllocationHandelDetailPushDTO pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return;
        }
        RLock lock = redissonClient.getLock(LOCK + pushDTOS.getVirtual_warehouse_no());
        try {
            boolean locked = lock.tryLock(10, 30, TimeUnit.SECONDS);
            if (locked) {
                VwAllocationHandleDetailAPI api = wangDianClientService.get(VwAllocationHandleDetailAPI.class);
                log.info("旺店通虚拟仓订单创建：消费者接收数据：{}",pushDTOS);
                Map<String, Object> map = JSON.parseObject(JSON.toJSONString(pushDTOS), new TypeReference<Map<String, Object>>() {});
                Object bizType = map.get("bizType");
                Map<String, Object> request = commonService.makeApiFieldMap(map, platformEntity.getId(), ApiModuleTypeEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL.getCode());
                log.info("旺店通虚拟仓订单创建：请求参数：{}",request);
                String msg = null;
                VirtualWarehouseAllocationDTO.SyncUpdateDto dto = new VirtualWarehouseAllocationDTO.SyncUpdateDto();
                try {
                    VwAllocationHandelDetailResponse pushResult = api.push(request, request.get("detailList"));
                    log.info("旺店通虚拟仓订单创建：响应结果：{}", pushResult);
                    if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                        dto.setSysType(ThirdSysTypeEnum.WDT.getCode());
                        dto.setSysTypeName(ThirdSysTypeEnum.WDT.getName());
                        dto.setThirdCode(pushResult.getMessage());
                        dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode());
                        dto.setHandelDetailId(map.get("sourceId").toString());
                        log.info("旺店通虚拟仓订单创建：成功同步分货单：{}",dto);
                        allocationDetailFeign.updateSyncStatus(dto);
                    }
                }catch (WdtErpException e){
                    if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                        dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
                        dto.setHandelDetailId(map.get("sourceId").toString());
                        log.info("旺店通虚拟仓订单创建：失败同步分货单：{}",dto);
                        allocationDetailFeign.updateSyncStatus(dto);
                    }
                    throw new ServiceException((e.getMessage()));
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ApiError.ERROR_1026);
        } finally {
            lock.unlock();
        }
    }
}
