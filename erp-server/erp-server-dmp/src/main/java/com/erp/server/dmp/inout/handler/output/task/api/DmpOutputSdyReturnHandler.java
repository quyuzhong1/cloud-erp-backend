package com.erp.server.dmp.inout.handler.output.task.api;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.entity.*;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.enums.DmpReturnInfoStatusEnum;
import com.erp.server.dmp.inout.dto.request.DmpOutputTaskRequest;
import com.erp.server.dmp.inout.dto.response.DmpOutputTaskResponse;
import com.erp.server.dmp.inout.handler.output.task.DmpOutputTaskHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@Scope("prototype")
public class DmpOutputSdyReturnHandler extends DmpOutputTaskHandler {
    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Override
    protected List<DmpOutputTaskRecordEntity> outputData(DmpOutputTaskRequest dmpRequest, DmpOutputTaskResponse dmpResponse) {
        Map<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMaps = dmpRequest.getConvertInputDmpBaseEntityListMaps();
        Map<String, DmpSoReturnInfoEntity> dmpSoReturnInfoEntityMap = new HashMap<>();
        Map<String, List<DmpSoReturnDetailEntity>> dmpSoReturnDetailEntityMap = new HashMap<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> convertInputDmpBaseEntityListMap : convertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = convertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = convertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnInfoEntity dmpSoInfoEntity = (DmpSoReturnInfoEntity) v;
                        dmpSoReturnInfoEntityMap.put(dmpSoInfoEntity.getId(), dmpSoInfoEntity);
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
                        String mainId = dmpSoReturnDetailEntity.getMainId();
                        List<DmpSoReturnDetailEntity> list = dmpSoReturnDetailEntityMap.get(mainId);
                        if (CollUtil.isEmpty(list)) {
                            list = new ArrayList<>();
                        }
                        list.add(dmpSoReturnDetailEntity);
                        dmpSoReturnDetailEntityMap.put(mainId, list);
                    }
                }
            }
        }

        Map<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMaps = dmpRequest.getChangeConvertInputDmpBaseEntityListMaps();
        Set<String> changeIds = new HashSet<>();
        for (Map.Entry<DmpCfgInputConvertEntity, List<BaseEntity>> changeConvertInputDmpBaseEntityListMap : changeConvertInputDmpBaseEntityListMaps.entrySet()) {
            List<BaseEntity> value = changeConvertInputDmpBaseEntityListMap.getValue();
            if (CollUtil.isNotEmpty(value)) {
                String storageName = changeConvertInputDmpBaseEntityListMap.getKey().getStorageName();
                if ("dmp_so_return_info".equals(storageName)) {
                    for (BaseEntity v : value) {
                        changeIds.add(v.getId());
                    }
                } else if ("dmp_so_return_detail".equals(storageName)) {
                    for (BaseEntity v : value) {
                        DmpSoReturnDetailEntity dmpSoReturnDetailEntity = (DmpSoReturnDetailEntity) v;
                        changeIds.add(dmpSoReturnDetailEntity.getMainId());
                    }
                }
            }
        }

        Map<String, String> map = new HashMap<>();
        String cfgOutputId = dmpResponse.getDmpCfgOutputEntity().getId();
        for(String changId : changeIds) {
            List<ShudiyunB2cOrderDTO> sdyDtoList = this.convert(dmpSoReturnInfoEntityMap.get(changId), dmpSoReturnDetailEntityMap.get(changId), cfgOutputId);
            if(CollUtil.isEmpty(sdyDtoList)) {
                map.put(changId, JSON.toJSONString(sdyDtoList));
            }
        }
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = new ArrayList<>();
/*
        if(CollUtil.isNotEmpty(dmpPushMsgEntityList)) {
            dmpPushMsgEntityList.sort((d1 , d2) -> d1.getMessageUpdateTime().compareTo(d2.getMessageUpdateTime()));
            LocalDateTime now = LocalDateTime.now();
            int i = 0;
            for(DmpPushMsgEntity dmpPushMsgEntity : dmpPushMsgEntityList) {
                String dataId = dmpPushMsgEntity.getId();
                String pushData = dmpPushMsgEntity.getPushData();
                DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
                String id = identifierGenerator.nextId(dmpOutputTaskRecordEntity).toString();
                dmpOutputTaskRecordEntity.setId(id);
                dmpOutputTaskRecordEntity.setMainId(dmpRequest.getOutputTaskId());
                dmpOutputTaskRecordEntity.setDataId(dataId);
                dmpOutputTaskRecordEntity.setSourceCode(dmpPushMsgEntity.getSourceCode());
                dmpOutputTaskRecordEntity.setRequestData(pushData);
                dmpOutputTaskRecordEntity.setStatus(DmpOutputTaskRecordStatusEnum.INIT.getCode());
                LocalDateTime insertTime = now.plus(i, ChronoUnit.MILLIS);
                dmpOutputTaskRecordEntity.setCreateTime(insertTime);
                dmpOutputTaskRecordEntity.setUpdateTime(insertTime);
                dmpOutputTaskRecordEntityList.add(dmpOutputTaskRecordEntity);
                i = i + 1;
            }
        }*/

        return dmpOutputTaskRecordEntityList;
    }

    @Override
    protected void pushData(DmpCfgOutputEntity dmpCfgOutputEntity, DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {

    }

    /**
     * 解析订单数据
     **/
    public List<ShudiyunB2cOrderDTO> convert(DmpSoReturnInfoEntity dmpSoReturnEntity, List<DmpSoReturnDetailEntity> dmpSoReturnDetailEntityList, String cfgOutputId) {
        List<ShudiyunB2cOrderDTO> sdyListDTO = new ArrayList<>();

        for (DmpSoReturnDetailEntity dmpSoReturnDetailEntity : dmpSoReturnDetailEntityList) {
            ShudiyunB2cOrderDTO sdyDTO = new ShudiyunB2cOrderDTO();
            sdyDTO.setTransaction_unique_key(dmpSoReturnEntity.getId() + dmpSoReturnDetailEntity.getId());
            sdyDTO.setBiz_no(dmpSoReturnEntity.getThirdCode());
            sdyDTO.setBiz_time(dmpSoReturnEntity.getReturnTime());

            if ("refund".equals(dmpSoReturnDetailEntity.getSolutionType())) {
                //仅退款
                sdyDTO.setTransaction_type("CC");
                sdyDTO.setTransaction_type("110.10.02");
            } else if ("replacement".equals(dmpSoReturnDetailEntity.getSolutionType())) {
                //RMA.退换货
                sdyDTO.setTransaction_type("110.20");
                //换货退货
                sdyDTO.setTransaction_type("110.20.01");
            } else {
                //RMA.退货单
                sdyDTO.setTransaction_type("110.10");
                //退货退款
                sdyDTO.setTransaction_sub_type("110.10.01");
            }

            if (StrUtil.isNotBlank(dmpSoReturnEntity.getStatus()) && StrUtil.isNotBlank(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())))) {
                sdyDTO.setBiz_status(DmpReturnInfoStatusEnum.getName(Integer.valueOf(dmpSoReturnEntity.getStatus())));
            } else {
                sdyDTO.setBiz_status("已完成");
            }
            int qtyTotal = dmpSoReturnDetailEntityList.stream().mapToInt(DmpSoReturnDetailEntity::getQty).sum();
            sdyDTO.setOnline_appled_return_quanty(qtyTotal);
            sdyDTO.setCustomer_refundable_quantity(qtyTotal);
            sdyDTO.setQuantity_buyer_returned(qtyTotal);

            BigDecimal amountTotal = dmpSoReturnDetailEntityList.stream().map(DmpSoReturnDetailEntity::getAmount).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
            sdyDTO.setOnline_applied_amount(amountTotal);
            sdyDTO.setOrder_seller_payed(amountTotal);
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dmpSoReturnEntity.getShopId());
            sdyListDTO.add(sdyDTO);
        }

        return sdyListDTO;

    }
}
