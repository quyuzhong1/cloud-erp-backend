package com.erp.server.wms.wdt.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.RequisitionApplicationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleRelationEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.wms.service.RequisitionApplicationDetailService;
import com.erp.server.wms.service.VirtualWarehouseAllocationDetailService;
import com.erp.server.wms.service.VirtualWarehouseAllocationHandleRelationService;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehouseAllocationOrderService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwAllocationHandelDetailPushDTO;
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
public class SyncWdtVirtualWarehouseAllocationOrderServiceImpl implements SyncWdtVirtualWarehouseAllocationOrderService {

    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private VirtualWarehouseAllocationHandleRelationService virtualWarehouseAllocationHandleRelationService;
    @Resource
    private VirtualWarehouseAllocationDetailService virtualWarehouseAllocationDetailService;
    @Resource
    private RequisitionApplicationDetailService requisitionApplicationDetailService;

    @Override
    public List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehouseAllocationHandleDetailEntity> handleDetailList,
                                                String vwAllocationCode, String operateCode, String sourceType) {

        List<DmpPushTaskFeignDTO> dmpPushTaskEntityList = new ArrayList<>();
        handleDetailList.forEach(handleDetail -> {
            VwAllocationHandelDetailPushDTO request = new VwAllocationHandelDetailPushDTO();
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

            String type = handleDetail.getType();
            request.setPre_time(LocalDateTime.now().minusMinutes(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            request.setVirtual_warehouse_no(StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId()) ? handleDetail.getThirdFromVirtualWarehouseId() : handleDetail.getThirdToVirtualWarehouseId());
            request.setTo_virtual_warehouse_no(handleDetail.getThirdToVirtualWarehouseId());
            List<VwAllocationHandelDetailPushDTO.DetailList> detailList = new ArrayList<>();
            //获取明细
            List<VirtualWarehouseAllocationHandleRelationEntity> allocationHandleRelationEntities = virtualWarehouseAllocationHandleRelationService
                    .list(new LambdaQueryWrapper<VirtualWarehouseAllocationHandleRelationEntity>()
                            .eq(VirtualWarehouseAllocationHandleRelationEntity::getHandleDetailId, handleDetail.getId()));
            //根据sku和调入虚拟仓进行聚合
            List<String> allocationDetailIds = allocationHandleRelationEntities.stream().map(VirtualWarehouseAllocationHandleRelationEntity::getAllocationDetailId).collect(Collectors.toList());

            switch (SourceTypeEnum.getByCode(sourceType)) {
                case VIRTUAL_WAREHOUSE_ALLOCATION:
//                    switch (VirtualWarehouseAllocationTypeEnum.getByCode(type)) {
//                        case ALLOCATION:
                    Map<String, List<VirtualWarehouseAllocationDetailEntity>> skuMap = virtualWarehouseAllocationDetailService.listByIds(allocationDetailIds).stream().collect(Collectors.groupingBy(VirtualWarehouseAllocationDetailEntity::getSkuNo));
                    skuMap.forEach((skuNo, list) -> {
                        VwAllocationHandelDetailPushDTO.DetailList detail = new VwAllocationHandelDetailPushDTO.DetailList();
                        detail.setNum(BigDecimal.valueOf(list.stream().map(VirtualWarehouseAllocationDetailEntity::getQty).reduce(0, Integer::sum)));
                        detail.setWarehouse_no(handleDetail.getThirdWarehouseId());
                        detail.setSpecNo(skuNo);
                        detailList.add(detail);
                    });
                    break;
                case REQUISITION_APPLICATION:
                    Map<String, List<RequisitionApplicationDetailEntity>> requireSkuMap = requisitionApplicationDetailService.listByIds(allocationDetailIds).stream().collect(Collectors.groupingBy(RequisitionApplicationDetailEntity::getSkuNo));
                    requireSkuMap.forEach((skuNo, list) -> {
                        VwAllocationHandelDetailPushDTO.DetailList detail = new VwAllocationHandelDetailPushDTO.DetailList();
                        detail.setNum(BigDecimal.valueOf(list.stream().map(RequisitionApplicationDetailEntity::getApproveQty).reduce(0, Integer::sum)));
                        detail.setWarehouse_no(handleDetail.getThirdWarehouseId());
                        detail.setSpecNo(skuNo);
                        detailList.add(detail);
                    });
                    break;
                default:
                    break;
            }
//            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);
            String detailStr = JSONUtil.toJsonStr(detailList);
            String requestStr = JSONUtil.toJsonStr(request);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("["+requestStr+","+detailStr+"]");

            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(handleDetail.getId());
            dmpSyncTaskDTO.setSourceCode(vwAllocationCode);
            dmpSyncTaskDTO.setSourceType(sourceType);
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_VIRTUAL_ALLOCATION_HANDLE_DETAIL_TAG.getName());
            dmpSyncTaskDTO.setMqData(stringBuilder.toString());
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operateCode);

            dmpPushTaskEntityList.add(dmpSyncTaskDTO);
        });

        return dmpMqFeign.saveTaskList(dmpPushTaskEntityList);
    }
}
