package com.erp.server.wms.sdk.delivery;

import com.common.business.service.IPlatformService;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    /**
     * 处理拆分（拆单，拆BOM）相关逻辑
     * 非虚假发货：
     *   拆单逻辑：直接标发，数量取明细数量
     *   捆绑拆分逻辑：查询关联订单发货状态，当关联订单都是已发货，并且全部相同明细来源的明细是否标发标识是否 ，找到原单明细做标发，数量取原单数量
     * 虚假发货：
     *   拆单逻辑：直接标发，数量取明细数量
     *   捆绑拆分逻辑：查询关联订单发货状态，当全部的相同明细来源的明细是否标发标识是否 ，找到原单明细做标发，数量取原单数量
     * @param detailList
     * @param falseDeliveryFlag
     * @return
     */
    public List<SoB2cDetailEntity> handleSplit(List<SoB2cDetailEntity> detailList, boolean falseDeliveryFlag){
        List<SoB2cDetailEntity> allDetailList = detailList;
        detailList = detailList.stream().filter(v -> StringUtils.isNotBlank(v.getSplitDetailId())).collect(Collectors.toList());
        //没有捆绑商品拆分，直接返回
        if (CollectionUtils.isEmpty(detailList)) {
            return allDetailList;
        }
        String mainId = detailList.get(0).getMainId();
        Map<String, List<SoB2cDetailEntity>> splitDetailMap = detailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getSplitDetailId));
        List<String> filterDetailList = new ArrayList<>();
        splitDetailMap.forEach((key, value) -> {
            //查询关联的捆绑商品对应明细
            SoB2cDTO.CombinationDTO soCombinationDTO = soB2cFeign.listRefBomSplit(key);
            //不包括本身 并且未作废的其他订单主表
            List<SoB2cEntity> otherMainList = soCombinationDTO.getSoB2cEntityList().stream().filter(v -> !mainId.equals(v.getId())&& !v.getInvalidStatus()).collect(Collectors.toList());
            List<String> otherMainIds = otherMainList.stream().map(v->v.getId()).collect(Collectors.toList());
            //不包括本身 并且未作废的其他明细
            List<SoB2cDetailEntity> otherDetailList = soCombinationDTO.getSoB2cDetailEntityList().stream().filter(v -> otherMainIds.contains(v.getMainId())).collect(Collectors.toList());
            otherMainList = otherMainList.stream().filter(v -> !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus())).collect(Collectors.toList());
            if(falseDeliveryFlag){
                // 有一个别的明细已发货，则过滤
                if (otherDetailList.stream().anyMatch(SoB2cDetailEntity::getIsSignShipped)) {
                    filterDetailList.addAll(value.stream().map(v -> v.getId()).collect(Collectors.toList()));
                } else {
                    //将数量设置为拆分前的数量
                    List<SoB2cDetailEntity> soB2cDetailEntity = soB2cFeign.listDetailContainDeleted(Arrays.asList(key));
                    if (CollectionUtils.isNotEmpty(soB2cDetailEntity)) {
                        value.forEach(v -> v.setQty(soB2cDetailEntity.get(0).getQty()));
                    }
                }
            }else{
                //有别的订单未发货或者 有别的明细已发货
                if (CollectionUtils.isNotEmpty(otherMainList) || otherDetailList.stream().anyMatch(SoB2cDetailEntity::getIsSignShipped)) {
                    filterDetailList.addAll(value.stream().map(v -> v.getId()).collect(Collectors.toList()));
                } else {
                    //将数量设置为拆分前的数量
                    List<SoB2cDetailEntity> soB2cDetailEntity = soB2cFeign.listDetailContainDeleted(Arrays.asList(key));
                    if (CollectionUtils.isNotEmpty(soB2cDetailEntity)) {
                        value.forEach(v -> v.setQty(soB2cDetailEntity.get(0).getQty()));
                    }
                }
            }
        });
        allDetailList = allDetailList.stream().filter(v -> !filterDetailList.contains(v.getId())).collect(Collectors.toList());
        //将allDetailList 相同的来源明细id去重
        allDetailList = allDetailList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SoB2cDetailEntity::getSourceDetailId))), ArrayList::new));
        return allDetailList;
    }

}
