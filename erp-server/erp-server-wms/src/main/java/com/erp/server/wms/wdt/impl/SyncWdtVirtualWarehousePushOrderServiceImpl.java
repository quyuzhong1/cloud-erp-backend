package com.erp.server.wms.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.WdtVirtualInventoryService;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    @Resource
    private WdtVirtualInventoryService wdtVirtualInventoryService;
    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    /**
     * 校验分货单调出明细是否存在未同步成功的数据
     * @author will
     * @date 2025/12/29 11:36
     * @param checkDataList
     * @return void
     */
    private void checkHandleDetailListRepeat(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList) {
        //来源仓库
        List<String> fromWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //来源虚拟仓
        List<String> fromVirtualWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //sku
        List<String> skuIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuId).distinct().collect(Collectors.toList());
        //明细id集合
        List<String> detailIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getDetailId)
                .distinct().collect(Collectors.toList());

        //查询已存在未同步成功的调出明细
        List<VirtualWarehouseAllocationDetailEntity> oldDetailList = virtualWarehouseAllocationDetailService.listRepeatHandleDetail(fromWarehouseIdList, fromVirtualWarehouseIdList, skuIdList, detailIdList);
        if (CollUtil.isEmpty(oldDetailList)) {
            return;
        }
        List<String> oldHandleDetailIdList = oldDetailList.stream().map(VirtualWarehouseAllocationDetailEntity::getHandleDetailId).distinct().collect(Collectors.toList());
        List<DmpPushTaskDTO.SyncInfoDTO>  syncInfoList = dmpInoutTaskFeign.listErrorData(new DmpSyncTaskDTO.ListDTO(SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode(),
                oldHandleDetailIdList, PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc()));

        for (VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO : checkDataList) {
            //获取对应的已存在未同步成功的调出明细
            List<VirtualWarehouseAllocationDetailEntity> warehouseAllocationDetailList = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), checkDataDTO.getWarehouseId()) && CharSequenceUtil.equals(obj.getFromVirtualWarehouseId(), checkDataDTO.getVirtualWarehouseId()) && CharSequenceUtil.equals(obj.getSkuId(), checkDataDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(warehouseAllocationDetailList)) {
               continue;
            }
            for (VirtualWarehouseAllocationDetailEntity entity : warehouseAllocationDetailList) {
                List<DmpPushTaskDTO.SyncInfoDTO> thisSyncInfoList = syncInfoList.stream().filter(obj -> CharSequenceUtil.equals(entity.getHandleDetailId(), obj.getSourceId()))
                        .collect(Collectors.toList());
                long failCount = syncInfoList.stream().filter(obj -> CharSequenceUtil.equals(entity.getHandleDetailId(), obj.getSourceId())
                                && Arrays.asList("cosumererror","mqerror","error").contains(obj.getStatus()))
                        .count();
                //未找到同步记录或者存在失败同步记录则报错
                if (CollUtil.isEmpty(thisSyncInfoList) || failCount > 0) {
                    throw new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_ALLOCATION_NOT_REPEAT,
                            entity.getWarehouseName(),
                            entity.getFromVirtualWarehouseName(),
                            entity.getSkuNo());
                }
            }
        }
    }

    @Override
    public List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehousePushHandleDetailEntity> handleDetailList,
                                                String vwAllocationCode, String operateCode, String sourceType) {

    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> cfgSettingEntityList = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, sourceType)
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
    	List<DmpPushTaskFeignDTO> dmpPushTaskEntityList = new ArrayList<>();
    	List<WmsPushMsgEntity> wmsPushMsgEntityList = new ArrayList<>();
        List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList = new ArrayList<>();

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

                        //添加校验数据
                        list.forEach(obj -> {
                            VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO = new VirtualWarehousePushHandleDetailDTO.CheckDataDTO();
                            checkDataDTO.setWarehouseId(handleDetail.getWarehouseId());
                            checkDataDTO.setVirtualWarehouseId(handleDetail.getFromVirtualWarehouseId());
                            checkDataDTO.setSkuId(obj.getSkuId());
                            checkDataDTO.setDetailId(obj.getId());
                            checkDataList.add(checkDataDTO);
                        });
                    });
                    break;
                default:
                    break;
            }
            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);

            //查询旺店通可用库存是否足够
            checkWdtUseInventoryQty(request,detailList);

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
        if (CollUtil.isNotEmpty(checkDataList)) {
            //校验之前是否存在未同步成功的分货单调出
            checkHandleDetailListRepeat(checkDataList);
        }
        if(CollUtil.isNotEmpty(wmsPushMsgEntityList)) {
        	wmsPushMsgService.saveBatch(wmsPushMsgEntityList);
        }
        
        return dmpMqFeign.saveTaskList(dmpPushTaskEntityList);
    }


    /**
     * 校验旺店通库存
     * @author will
     * @date 2025/12/22 09:45
     * @param request
     * @param detailList
     * @return void
     */
    private void checkWdtUseInventoryQty(VwPushHandelDetailPushDTO request,List<VwPushHandelDetailPushDTO.DetailList> detailList) {
        if (CollUtil.isEmpty(detailList)) {
            return;
        }
        if (request.getOrder_type() == 1) {
            return;
        }
        Map<String, List<VwPushHandelDetailPushDTO.DetailList>> map = detailList.stream().collect(Collectors.groupingBy(VwPushHandelDetailPushDTO.DetailList::getWarehouse_no));
        for (Map.Entry<String, List<VwPushHandelDetailPushDTO.DetailList>> entry : map.entrySet()) {
            String warehouseNo = entry.getKey();
            List<VwPushHandelDetailPushDTO.DetailList> value = entry.getValue();
            String skuNoList = value.stream().map(VwPushHandelDetailPushDTO.DetailList::getSpec_no).collect(Collectors.joining(","));

            WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO = new WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO();
            detailDTO.setSpec_nos(skuNoList);
            detailDTO.setVirtual_warehouse_no(request.getVirtual_warehouse_no());
            detailDTO.setWarehouse_no(warehouseNo);
            List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventoryDTOS = wdtVirtualInventoryService.searchVirtualInventory(detailDTO);
            if (CollUtil.isEmpty(searchVirtualInventoryDTOS)) {
                throw new ServiceException("调用旺店通虚拟仓库存查询接口无可用库存，仓库编码：{}.虚拟仓库编码：{}，SKU列表：{}" , warehouseNo, request.getVirtual_warehouse_no(), skuNoList);
            }
            for (VwPushHandelDetailPushDTO.DetailList detailPush : value) {
                searchVirtualInventoryDTOS.stream().filter(obj -> CharSequenceUtil.equals(detailPush.getSpec_no(),obj.getSkuNo()) && CharSequenceUtil.equals(warehouseNo,obj.getWarehouseCode()) && CharSequenceUtil.equals(request.getVirtual_warehouse_no(),obj.getVirtualWarehouseCode()))
                        .findFirst().ifPresent(obj -> {
                            if (MathUtil.compareTo(new BigDecimal(obj.getQty()),detailPush.getNum()) < 0) {
                                throw new ServiceException("调用旺店通虚拟仓库存查询接口可用库存不足，仓库编码：{}.虚拟仓库编码：{}，SKU：{}，可用库存：{}，需求数量：{}" , warehouseNo ,request.getVirtual_warehouse_no(), detailPush.getSpec_no()
                                        , obj.getQty() , detailPush.getNum());
                            }
                });
            }
        }

    }

}
