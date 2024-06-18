package com.erp.server.wms.wdt.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.wms.entity.VirtualWarehouseAllocationHandleDetailEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
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

    @Override
    public List<DmpPushTaskEntity> saveTaskList(List<VirtualWarehouseAllocationHandleDetailEntity> handleDetailList, String vwAllocationCode, String operateCode) {
//        request.setOuterNo(entity.getCode());

        //查询推送任务表，如果有了相同的来源单据号，则序号累加
//        DmpSyncTaskDTO.ListDTO param = new DmpSyncTaskDTO.ListDTO(handleDetailList.stream()
//                .map(VirtualWarehouseAllocationHandleDetailEntity::getId).collect(Collectors.toList()), PlatformEnum.WANGDIAN.getDesc(), PlatformEnum.ERP.getDesc());
//        List<DmpPushTaskEntity> taskList = dmpMqFeign.listByParam(param);
//        Optional<CreateOtherStockoutRequest> optional = taskList.stream()
//                .filter(task -> task.getSyncOperate().equalsIgnoreCase(operateCode))
//                .map(task -> JSON.parseObject(task.getMqData(), CreateOtherStockoutRequest.class))
//                .max((o1, o2) -> ObjectUtil.compare(o1.getOuterNo(), o2.getOuterNo()));
//        if(optional.isPresent()){
//            String maxOuterNo = optional.get().getOuterNo();
//            if(maxOuterNo.contains("_")){
//                String[] split = maxOuterNo.split("_");
//                Integer seq = Integer.parseInt(split[1]) + 1;
//                request.setOuterNo(split[0] + "_" + String.format("%03d", seq));
//            }else {
//                request.setOuterNo(entity.getCode() + "_001");
//            }
//        }

        //根据发货仓库ID查询旺店通仓库编号
//        ThirdMappingEntity thirdMappingEntity = dmpThirdMappingFeign.getBySysId(entity.getWarehouseId());
//        request.setWarehouseNo(Optional.ofNullable(thirdMappingEntity).orElse(new ThirdMappingEntity("")).getThirdInfoId());
//        ThirdWarehouseEntity thirdWarehouse = dmpThirdMappingFeign.getBySysId(entity.getWarehouseId());

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
            VwAllocationHandelDetailPushDTO.DetailList detail = new VwAllocationHandelDetailPushDTO.DetailList();
            detail.setNum(BigDecimal.valueOf(handleDetail.getQty()));
            detail.setWarehouseNo("1");
            detail.setSpecNo(handleDetail.getId());

            request.setDetailList(detailList);
            request.setRemark("原始单据号：" + vwAllocationCode);

            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(handleDetail.getId());
            dmpSyncTaskDTO.setSourceCode(vwAllocationCode);
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.VIRTUAL_WAREHOUSE_ALLOCATION.getCode());
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
