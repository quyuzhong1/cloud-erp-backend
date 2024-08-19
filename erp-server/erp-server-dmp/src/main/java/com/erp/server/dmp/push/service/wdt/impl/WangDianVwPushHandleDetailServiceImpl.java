package com.erp.server.dmp.push.service.wdt.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.annotation.DataIdempotent;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDTO;
import com.erp.model.wms.enums.VirtualWarehouseAllocationSyncStatusEnum;
import com.erp.rpc.wms.feign.VirtualWarehouseAllocationDetailFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.dmp.push.service.CommonService;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.push.service.wdt.WangDianVwPushHandleDetailService;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.sdk.wangdian.sdk.WdtErpException;
import com.sdk.wangdian.sdk.api.virtualWarehouse.VwPushHandleDetailAPI;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 旺店通虚拟仓订单创建处理明细
 */
@Service
@Slf4j
public class WangDianVwPushHandleDetailServiceImpl implements WangDianVwPushHandleDetailService {
    @Resource
    private KingdeeCommonService kingdeeCommonService;
    @Resource
    private WangDianClientService wangDianClientService;
    @Resource
    private CommonService commonService;
    @Resource
    private VirtualWarehouseAllocationDetailFeign allocationDetailFeign;

    @Override
    @DataIdempotent(keyIdName = "pushDTOS.virtual_warehouse_no", waitTime = 10)
    public ApiResult<?> executeConsumer(VwPushHandelDetailPushDTO pushDTOS) {
        PlatformEntity platformEntity = kingdeeCommonService.getPlatformEntity(PlatformEnum.WANGDIAN.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            return ApiResult.error(PlatformEnum.WANGDIAN.getName()+"平台类型未获取到");
        }
        VirtualWarehouseAllocationDTO.SyncUpdateDto dto = new VirtualWarehouseAllocationDTO.SyncUpdateDto();
        String msg = null;
        VwPushHandleDetailAPI api = wangDianClientService.get(VwPushHandleDetailAPI.class);
        log.info("旺店通虚拟仓订单创建：消费者接收数据：{}", pushDTOS);
        Map<String, Object> map = JSON.parseObject(JSON.toJSONString(pushDTOS), new TypeReference<Map<String, Object>>() {
        });
        Object bizType = map.get("bizType");
        Map<String, Object> request = commonService.makeApiFieldMap(map, platformEntity.getId(), ApiModuleTypeEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL.getCode());
        log.info("旺店通虚拟仓订单创建：请求参数：{}", request);
        //审核时间: 仅在order_type=3时生效, 格式: yyyy-MM-dd HH:mm, 时间要大于当前服务器时间2分钟以上
        Object orderTypeObj = request.get("order_type");
        if (Objects.nonNull(orderTypeObj) && !StringUtil.isEmpty(orderTypeObj.toString()) && Objects.equals(orderTypeObj.toString(), "3")) {
            request.put("pre_time", LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        try {
            VwPushHandelDetailResponse pushResult = api.push(request, request.get("detailList"));
            msg = JSONObject.toJSONString(pushResult);
            log.info("旺店通虚拟仓订单创建：响应结果：{}", pushResult);
            if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                dto.setSysType(ThirdSysTypeEnum.WDT.getCode());
                dto.setSysTypeName(ThirdSysTypeEnum.WDT.getName());
                buildResultData(pushResult, dto);
                dto.setHandelDetailId(map.get("sourceId").toString());
                log.info("旺店通虚拟仓订单创建：同步分货单：{}", dto);
                allocationDetailFeign.updateSyncStatus(dto);
            }
        } catch (WdtErpException e) {
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
            msg = e.getMessage();
            if (Objects.equals(bizType, SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode())) {
                dto.setHandelDetailId(map.get("sourceId").toString());
                dto.setFinishDescription(e.getMessage());
                log.info("旺店通虚拟仓订单创建：同步分货单：{}", dto);
                allocationDetailFeign.updateSyncStatus(dto);
            }
        }
        if (VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode().equals(dto.getSyncStatus())){
            return ApiResult.success(msg);
        }else {
            return ApiResult.error(msg);
        }
    }

    /**
     * 构建返回数据
     * @param pushResult
     * @param dto
     */
    private void buildResultData(VwPushHandelDetailResponse pushResult, VirtualWarehouseAllocationDTO.SyncUpdateDto dto) {
        if (Objects.nonNull(pushResult.getStatus()) && 0 == pushResult.getStatus()){
            dto.setThirdCode(pushResult.getMessage());
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.SUCCESS_SYNC.getCode());
            dto.setFinishDescription("");
        }else {
            // 正则表达式匹配模式
            String pattern = "\\bVO\\d{12}\\b";
            // 创建 Pattern 对象
            Pattern r = Pattern.compile(pattern);
            // 创建 Matcher 对象
            Matcher m = r.matcher(pushResult.getMessage());
            // 查找匹配的
            if (m.find()) {
                String extractedString = m.group(0);
                dto.setThirdCode(extractedString);
            }
            dto.setFinishDescription(pushResult.getMessage());
            dto.setSyncStatus(VirtualWarehouseAllocationSyncStatusEnum.FAILED_SYNC.getCode());
        }
    }
}
