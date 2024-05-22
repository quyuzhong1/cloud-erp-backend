package com.erp.server.wms.sdk.delivery;

import com.common.business.service.IPlatformService;
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


    public List<SoB2cDetailEntity> handleBomSplit(List<SoB2cDetailEntity> detailList){
        List<SoB2cDetailEntity> allDetailList = detailList;
        detailList = detailList.stream().filter(v-> StringUtils.isNotBlank(v.getSplitDetailId())).collect(Collectors.toList());
        //没有捆绑商品拆分，直接返回
        if(CollectionUtils.isEmpty(detailList)){
            return allDetailList;
        }
        String mainId = detailList.get(0).getMainId();
        Map<String, List<SoB2cDetailEntity>> splitDetailMap = detailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getSplitDetailId));
        List<String> filterDetailList = new ArrayList<>();
        splitDetailMap.forEach((key,value)->{
            //查询关联的捆绑商品对应明细
            List<SoB2cEntity> refDetailList = soB2cFeign.listRefBomSplit(key);
            refDetailList = refDetailList.stream().filter(v->!mainId.equals(v.getId()) && !SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(v.getBillStatus())&& !v.getInvalidStatus()).collect(Collectors.toList());
            //不为空，说明别的订单未发货，过滤掉对应明细
            if(CollectionUtils.isNotEmpty(refDetailList)){
                filterDetailList.addAll(value.stream().map(v->v.getId()).collect(Collectors.toList()));
            }else{
                //将数量设置为拆分前的数量
                List<SoB2cDetailEntity> soB2cDetailEntity = soB2cFeign.listDetailByIds(Arrays.asList(key));
                if(CollectionUtils.isNotEmpty(soB2cDetailEntity)){
                    value.forEach(v->v.setQty(soB2cDetailEntity.get(0).getQty()));
                }
            }
        });
        allDetailList = allDetailList.stream().filter(v->!filterDetailList.contains(v.getId())).collect(Collectors.toList());
        //将allDetailList 相同的splitDetailId去重
        allDetailList = allDetailList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SoB2cDetailEntity::getSplitDetailId))), ArrayList::new));
        return allDetailList;
    }
}
