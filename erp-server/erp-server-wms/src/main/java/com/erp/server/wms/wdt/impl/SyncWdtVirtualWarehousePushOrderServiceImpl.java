package com.erp.server.wms.wdt.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.WdtSearchHandelDetailDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.WdtVirtualInventoryService;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.wms.dto.VirtualWarehouseAllocationDetailDTO;
import com.erp.model.wms.dto.VirtualWarehousePushHandleDetailDTO;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehousePushHandleRelationEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
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
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    /**
     * 校验分货单调出明细是否存在未同步成功的数据
     * @author will
     * @date 2025/12/29 11:36
     * @param checkDataList
     * @return void
     */
    private void checkHandleDetailListRepeat(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList) {
        if (CollUtil.isEmpty(checkDataList)) {
            return;
        }
        //来源仓库
        List<String> fromWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getWarehouseId).distinct().collect(Collectors.toList());
        //来源虚拟仓
        List<String> fromVirtualWarehouseIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getVirtualWarehouseId).distinct().collect(Collectors.toList());
        //sku
        List<String> skuIdList = checkDataList.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuId).distinct().collect(Collectors.toList());

        //查询已存在未同步成功的调出明细
        List<VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO> oldDetailList = virtualWarehouseAllocationDetailService.listRepeatHandleDetail(fromWarehouseIdList, fromVirtualWarehouseIdList, skuIdList);
        if (CollUtil.isEmpty(oldDetailList)) {
            return;
        }

        Map<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> map = checkDataList.stream().collect(Collectors.groupingBy(obj -> CharSequenceUtil.format("{}-{}",obj.getThirdWarehouseNo(),obj.getThirdVirtualWarehouseNo())));
        for (Map.Entry<String, List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO>> entry : map.entrySet()) {
            List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> value = entry.getValue();
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO fristCheckDataDTO = value.get(0);
            String skuNoList = value.stream().map(VirtualWarehousePushHandleDetailDTO.CheckDataDTO::getSkuNo).collect(Collectors.joining(","));

            WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO detailDTO = new WdtSearchHandelDetailDTO.SearchVirtualInventoryParamDTO();
            detailDTO.setSpec_nos(skuNoList);
            detailDTO.setVirtual_warehouse_no(fristCheckDataDTO.getThirdVirtualWarehouseNo());
            detailDTO.setWarehouse_no(fristCheckDataDTO.getThirdWarehouseNo());
            List<WdtSearchHandelDetailDTO.SearchVirtualInventoryDTO> searchVirtualInventoryDTOS = wdtVirtualInventoryService.searchVirtualInventory(detailDTO);
            if (CollUtil.isEmpty(searchVirtualInventoryDTOS)) {
                throw new ServiceException("调用旺店通虚拟仓库存查询接口无可用库存，仓库编码：{}.虚拟仓库编码：{}，SKU列表：{}" , fristCheckDataDTO.getThirdWarehouseNo(), fristCheckDataDTO.getThirdVirtualWarehouseNo(), skuNoList);
            }
            for (VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO : value) {
                //需要出的数量
                Integer totalPushQty = oldDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), checkDataDTO.getWarehouseId())
                                && CharSequenceUtil.equals(obj.getVirtualWarehouseId(), checkDataDTO.getVirtualWarehouseId())
                                && CharSequenceUtil.equals(obj.getSkuId(), checkDataDTO.getSkuId()))
                        .map(VirtualWarehouseAllocationDetailDTO.RepeatHandleDetailDTO::getQty)
                        .reduce(MathUtil.ZERO, Integer::sum);

                searchVirtualInventoryDTOS.stream().filter(obj -> CharSequenceUtil.equals(checkDataDTO.getSkuNo(),obj.getSkuNo()) && CharSequenceUtil.equals(fristCheckDataDTO.getThirdWarehouseNo(),obj.getWarehouseCode()) && CharSequenceUtil.equals(fristCheckDataDTO.getThirdVirtualWarehouseNo(),obj.getVirtualWarehouseCode()))
                        .findFirst().ifPresent(obj -> {
                            if (MathUtil.compareTo(obj.getQty(),totalPushQty) < 0) {
                                throw new ServiceException("调用旺店通虚拟仓库存查询接口可用库存不足，SKU：【{}】，取消/调出数量：{}，虚拟仓库编码：【{}】，可用库存：{}，未同步完成数：{}", checkDataDTO.getSkuNo() ,totalPushQty ,fristCheckDataDTO.getThirdVirtualWarehouseNo()
                                        , obj.getQty() , checkDataDTO.getQty());
                            }
                        });
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
                        builderCheckDataDTO(checkDataList,list,handleDetail,request,skuNo);
                    });
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


        //校验之前是否存在未同步成功的分货单调出
        checkHandleDetailListRepeat(checkDataList);

        if(CollUtil.isNotEmpty(wmsPushMsgEntityList)) {
        	wmsPushMsgService.saveBatch(wmsPushMsgEntityList);
        }
        
        return dmpMqFeign.saveTaskList(dmpPushTaskEntityList);
    }

    /**
     * 构建校验数据
     * @author will
     * @date 2026/1/30 11:27
     * @param checkDataList
     * @param list
     * @param handleDetail
     * @param request
     * @param skuNo
     * @return void
     */
    private void builderCheckDataDTO(List<VirtualWarehousePushHandleDetailDTO.CheckDataDTO> checkDataList,List<VirtualWarehouseAllocationDetailEntity> list,
                                     VirtualWarehousePushHandleDetailEntity handleDetail,VwPushHandelDetailPushDTO request,String skuNo) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        for (VirtualWarehouseAllocationDetailEntity obj : list) {
            //无调出虚拟仓不校验
            if (CharSequenceUtil.isBlank(handleDetail.getFromVirtualWarehouseId())) {
                continue;
            }
            VirtualWarehousePushHandleDetailDTO.CheckDataDTO checkDataDTO = new VirtualWarehousePushHandleDetailDTO.CheckDataDTO();
            checkDataDTO.setOrderType(request.getOrder_type());
            checkDataDTO.setWarehouseId(handleDetail.getWarehouseId());
            checkDataDTO.setThirdWarehouseNo(handleDetail.getThirdWarehouseId());
            checkDataDTO.setVirtualWarehouseId(handleDetail.getFromVirtualWarehouseId());
            checkDataDTO.setThirdVirtualWarehouseNo(handleDetail.getThirdFromVirtualWarehouseNo());
            checkDataDTO.setSkuId(obj.getSkuId());
            checkDataDTO.setSkuNo(skuNo);
            checkDataDTO.setQty(obj.getQty());
            checkDataDTO.setDetailId(obj.getId());
            checkDataDTO.setHandleDetailId(handleDetail.getId());
            checkDataList.add(checkDataDTO);
        }
    }
}
