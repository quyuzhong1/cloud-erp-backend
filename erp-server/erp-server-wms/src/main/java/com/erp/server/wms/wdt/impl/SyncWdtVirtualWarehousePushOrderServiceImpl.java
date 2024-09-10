package com.erp.server.wms.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.wms.service.*;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehousePushOrderService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwPushHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 将erp虚拟仓分货单同步至旺店通
 *
 * @author tanmujin
 * @date 2024-05-16
 */
@Slf4j
@Service
public class SyncWdtVirtualWarehousePushOrderServiceImpl implements SyncWdtVirtualWarehousePushOrderService {

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private VirtualWarehousePushHandleRelationService virtualWarehousePushHandleRelationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;
    @Resource
    private VirtualWarehousePushHandleDetailService virtualWarehousePushHandleDetailService;
    @Resource
    private VirtualWarehousePushHandleService virtualWarehousePushHandleService;
    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Override
    public List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList,
                                                String vwAllocationCode, String operateCode, String sourceType) {

    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> cfgSettingEntityList = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_OUTSTOCK.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
    	List<DmpPushTaskFeignDTO> dmpPushTaskEntityList = new ArrayList<>();
    	List<WmsPushMsgEntity> wmsPushMsgEntityList = new ArrayList<>();
        
        //单据类型:1:锁定分配,2:释放出库,3:虚拟仓间调拨,4:采购入库
        handleDetailList.forEach(handleDetail -> {
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            VwPushHandelDetailPushDTO request = new VwPushHandelDetailPushDTO();
            //获取调出仓 调入仓关联的第三方仓（旺店通）
            if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(3);
            } else if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(2);
            } else if (StringUtils.isEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrder_type(1);
            }

            request.setPre_time(LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            request.setVirtual_warehouse_no(StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseNo()) ? handleDetail.getThirdFromVirtualWarehouseNo() : handleDetail.getThirdToVirtualWarehouseNo());
            request.setTo_virtual_warehouse_no(handleDetail.getThirdToVirtualWarehouseNo());
            request.setBizType(sourceType);
            request.setSourceId(handleDetail.getId());
            List<VwPushHandelDetailPushDTO.DetailList> detailList = new ArrayList<>();
            //获取明细
            List<VirtualWarehousePushHandleRelationEntity> allocationHandleRelationEntities = virtualWarehousePushHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehousePushHandleRelationEntity>()
                            .eq(VirtualWarehousePushHandleRelationEntity::getHandleDetailId, handleDetail.getId()));
            //根据sku和调入虚拟仓进行聚合
            List<String> allocationDetailIds = allocationHandleRelationEntities.stream().map(VirtualWarehousePushHandleRelationEntity::getSourceDetailId).collect(Collectors.toList());
            switch (SourceTypeEnum.getByCode(sourceType)) {
                case VIRTUAL_WAREHOUSE_ALLOCATION:
                    Map<String, List<VirtualWarehouseAllocationDetailEntity>> skuMap = virtualWarehouseAllocationDetailService.listByIds(allocationDetailIds).stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getSkuNo));
                    skuMap.forEach((skuNo, list) -> {
                        VwPushHandelDetailPushDTO.DetailList detail = new VwPushHandelDetailPushDTO.DetailList();
                        detail.setNum(BigDecimal.valueOf(list.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum)));
                        detail.setWarehouse_no(handleDetail.getThirdWarehouseId());
                        detail.setSpec_no(skuNo);
                        detailList.add(detail);
                    });
                    break;
                case REQUISITION_APPLICATION:
                    Map<String, List<RequisitionApplicationDetailEntity>> requireSkuMap = requisitionApplicationDetailService.listByIds(allocationDetailIds).stream().collect(Collectors.groupingBy(RequisitionApplicationDetailEntity::getSkuNo));
                    log.info("获取要货申请明细：{}", requireSkuMap);
                    requireSkuMap.forEach((skuNo, list) -> {
                        VwPushHandelDetailPushDTO.DetailList detail = new VwPushHandelDetailPushDTO.DetailList();
                        detail.setNum(BigDecimal.valueOf(list.stream().map(RequisitionApplicationDetailEntity::getApproveQty).reduce(0, Integer::sum)));
                        detail.setWarehouse_no(handleDetail.getThirdWarehouseId());
                        detail.setSpec_no(skuNo);
                        detailList.add(detail);
                    });
                    //获取要货申请上次推送的id
                    //获取最后一次合单的主单
                    VirtualWarehousePushHandleEntity pushHandleEntity = virtualWarehousePushHandleService.list(new LambdaQueryWrapper<VirtualWarehousePushHandleEntity>()
                            .eq(VirtualWarehousePushHandleEntity::getSourceId, handleDetail.getSourceId())
                            .ne(VirtualWarehousePushHandleEntity::getId, handleDetail.getMainId()).orderByDesc(VirtualWarehousePushHandleEntity::getCreateTime)
                            .last("limit 1")).stream().findFirst().orElse(null);
//                    List<String> newHandleDetailIds = handleDetailList.stream().map(VirtualWarehouseAllocationHandleDetailEntity::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    if (Objects.nonNull(pushHandleEntity)) {
                        List<String> oldHandleDetailIds = virtualWarehousePushHandleDetailService
                                .list(new LambdaQueryWrapper<VirtualWarehousePushHandleDetailEntity>().eq(VirtualWarehousePushHandleDetailEntity::getMainId, pushHandleEntity.getId()))
                                .stream().map(VirtualWarehousePushHandleDetailEntity::getId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
//                    oldHandleDetailIds.removeAll(newHandleDetailIds);
                        dmpSyncTaskDTO.setParentId(oldHandleDetailIds.stream().collect(Collectors.joining(",")));
                    }
                    break;
                default:
                    break;
            }
            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);

            if(CollUtil.isEmpty(cfgSettingEntityList)) {
            	//添加推送任务
                dmpSyncTaskDTO.setSourceId(handleDetail.getId());
                dmpSyncTaskDTO.setSourceCode(vwAllocationCode);
                dmpSyncTaskDTO.setSourceType(sourceType);
                dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
                dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL_TAG.getName());
                dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
                dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
                dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
                dmpSyncTaskDTO.setSyncOperate(operateCode);

                dmpPushTaskEntityList.add(dmpSyncTaskDTO);
            }else {
            	WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.WDT.getCode());
                wmsPushMsgEntity.setSourceType(sourceType);
                wmsPushMsgEntity.setSourceId(handleDetail.getId());
                wmsPushMsgEntity.setSourceCode(vwAllocationCode);
                wmsPushMsgEntity.setSyncOperate(operateCode);
                wmsPushMsgEntity.setPushData(JSON.toJSONString(request));
                wmsPushMsgEntityList.add(wmsPushMsgEntity);
            }
        });
        if(CollUtil.isNotEmpty(wmsPushMsgEntityList)) {
        	wmsPushMsgService.saveBatch(wmsPushMsgEntityList);
        }
        
        return dmpMqFeign.saveTaskList(dmpPushTaskEntityList);
    }
}
