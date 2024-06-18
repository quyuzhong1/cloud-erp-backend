package com.erp.server.wms.wdt.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleDetailEntity;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleRelationEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.server.wms.service.VirtualWarehouseAllocationHandleRelationService;
import com.erp.server.wms.wdt.SyncWdtVirtualWarehouseAllocationOrderService;
import com.sdk.wangdian.sdk.api.virtualWarehouse.dto.VwAllocationHandelDetailPushDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehouseAllocationHandleDetailEntity> handleDetailList,
                                                String vwAllocationCode, String operateCode,String sourceType) {

        List<DmpPushTaskFeignDTO> dmpPushTaskEntityList = new ArrayList<>();
        handleDetailList.forEach(handleDetail -> {
            VwAllocationHandelDetailPushDTO request = new VwAllocationHandelDetailPushDTO();
            //获取调出仓 调入仓关联的第三方仓（旺店通）
            if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrderType(3);
            } else if (StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrderType(2);
            } else if (StringUtils.isEmpty(handleDetail.getThirdFromVirtualWarehouseId())
                    && StringUtils.isNotEmpty(handleDetail.getThirdToVirtualWarehouseId())) {
                request.setOrderType(1);
            }

            request.setVirtualWarehouseNo(StringUtils.isNotEmpty(handleDetail.getThirdFromVirtualWarehouseId()) ? handleDetail.getThirdFromVirtualWarehouseId() : handleDetail.getThirdToVirtualWarehouseId());
            request.setToVirtualWarehouseNo(handleDetail.getThirdToVirtualWarehouseId());
            List<VwAllocationHandelDetailPushDTO.DetailList> detailList = new ArrayList<>();
//            switch (SourceTypeEnum.getByCode(sourceType)){
//                case VIRTUAL_WAREHOUSE_ALLOCATION:
//                    //获取明细
//                    List<VirtualWarehouseAllocationHandleRelationEntity> allocationHandleRelationEntities = virtualWarehouseAllocationHandleRelationService.list(new LambdaQueryWrapper<VirtualWarehouseAllocationHandleRelationEntity>()
//                            .eq(VirtualWarehouseAllocationHandleRelationEntity::getHandleDetailId, handleDetail.getId()));
//                    //根据sku和调出虚拟仓进行聚合
//                    allocationHandleRelationEntities.forEach(allocationHandleRelationEntity->{
//                        VwAllocationHandelDetailPushDTO.DetailList detail = new VwAllocationHandelDetailPushDTO.DetailList();
//                        detail.setNum(BigDecimal.valueOf(handleDetail.getQty()));
//                        detail.setWarehouseNo(handleDetail.getThirdWarehouseId());
//                        detail.setSpecNo(handleDetail.getId());
//                        detailList.add(detail);
//                    });
//                    break;
//                case REQUISITION_APPLICATION:
//                    break;
//                default:
//                   break;
//            }
            //获取对应的产品信息




            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);

            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
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
        });

        return dmpMqFeign.saveTaskList(dmpPushTaskEntityList);
    }
}
