package com.erp.server.wms.sdk.delivery;

import cn.hutool.core.lang.Tuple;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.IPlatformService;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cRefEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cSourcePlatformEnum;
import com.erp.model.sys.entity.SysAccountingCompanyEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class AbstractShipOrder implements IPlatformService {

    @Resource
    private SoB2cFeign soB2cFeign;

    /**
     * 处理拆分（拆单，拆BOM）相关逻辑
     * 非手动标发：
     *   拆单逻辑：直接标发，数量取明细数量
     *   捆绑拆分逻辑：查询关联订单发货状态，当关联订单都是已发货，并且全部相同明细来源的明细是否标发标识是否 ，找到原单明细做标发，数量取原单数量
     * 手动标发：
     *   拆单逻辑：直接标发，数量取明细数量
     *   捆绑拆分逻辑：查询关联订单发货状态，当全部的相同明细来源的明细是否标发标识是否 ，找到原单明细做标发，数量取原单数量
     * @param detailList
     * @param falseDeliveryFlag
     * @return
     */
    public List<SoB2cDetailEntity> handleSplit(List<SoB2cDetailEntity> detailList, boolean falseDeliveryFlag){
        List<SoB2cDetailEntity> allDetailList = detailList;
        detailList = detailList.stream().filter(v -> CharSequenceUtil.isNotBlank(v.getSplitDetailId())).collect(Collectors.toList());
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
        allDetailList = allDetailList.stream()
                .filter(v -> !filterDetailList.contains(v.getId()))
                .filter(v -> SoB2cSourcePlatformEnum.ENUM_THIRD_PLATFORM.getCode().equalsIgnoreCase(v.getSourcePlatform()))
                .collect(Collectors.toList());
        //将allDetailList 相同的来源明细id去重
        allDetailList = allDetailList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SoB2cDetailEntity::getSourceDetailId))), ArrayList::new));
        return allDetailList;
    }

    /**
     * 合并前的所有源订单
     */
    public Tuple allSourceOrderInfo(PlatformShipOrderDTO dto) {
        List<SoB2cEntity> sourceOrderList;
        Map<String, List<SoB2cDetailEntity>> soB2cDetailEntityListMap = new HashMap<>();
        // 原平台订单信息
        Map<String, List<SoB2cDetailEntity>> sourcePlatformOrderMap = new HashMap<>();

        // 查询合并来源关系
        List<SoB2cRefEntity> refEntityList = soB2cFeign.findMergeByTargetId(dto.getSoB2cId());
        if (CollectionUtils.isEmpty(refEntityList)){
            // 无合并
            //检查销售订单是否存在
            SoB2cEntity mainEntity = soB2cFeign.getById(dto.getSoB2cId());
            if (ObjectUtil.isEmpty(mainEntity)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            sourceOrderList = Collections.singletonList(mainEntity);
            //检查销售订单详情是否存在
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
            if (CollectionUtils.isEmpty(soB2cDetailEntityList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
            }
            soB2cDetailEntityListMap.put(dto.getSoB2cId(),soB2cDetailEntityList);
            //  查询拆分前的原单信息
            if (dto.isHasFindSourcePlatformOrder()){
                // 按最早创建日期的平台仓订单作为原单
                sourcePlatformOrderMap = this.findSourcePlatformOrder(mainEntity);
            }
        } else {
            // 有合并
            List<String> mainIds = refEntityList.stream().map(SoB2cRefEntity::getSourceId).distinct().collect(Collectors.toList());
            List<String> detailIds = refEntityList.stream().map(SoB2cRefEntity::getSourceDetailId).distinct().collect(Collectors.toList());
            sourceOrderList = soB2cFeign.listByIds(mainIds);
            //检查销售订单是否存在
            if (CollectionUtils.isEmpty(sourceOrderList)) {
                throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
            }
            // 查询所有明细
            List<SoB2cDetailEntity> allDetailList = soB2cFeign.listDetailByIds(detailIds);
            soB2cDetailEntityListMap = allDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId));
        }
        //检查销售订单物流信息是否存在（按当前订单的物流信息）
        SoB2cLogisticsEntity logisticsEntity = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(dto.getSoB2cId()))
                .stream().findFirst().orElse(null);
        if (null == logisticsEntity) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }
        return new Tuple(sourceOrderList, soB2cDetailEntityListMap, logisticsEntity, sourcePlatformOrderMap);
    }

    /**
     * 查询最早创建日期的平台仓订单作为原单
     */
    private Map<String, List<SoB2cDetailEntity>> findSourcePlatformOrder(SoB2cEntity mainEntity) {
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.getByPlatformCode(
                Collections.singletonList(mainEntity.getPlatformCode()),
                mainEntity.getDictPlatform(),
                mainEntity.getShopId(),
                SourceTypeEnum.SO_B2C.getCode());
        SoB2cEntity soB2cEntity = soB2cEntityList.stream().min(Comparator.comparing(SoB2cEntity::getCreateTime)).orElse(null);
        Map<String, List<SoB2cDetailEntity>> resultMap = new HashMap<>();
        if (null == soB2cEntity){
            return resultMap;
        }
        List<SoB2cDetailEntity> detailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(mainEntity.getId()));
        resultMap.put(soB2cEntity.getPlatformCode(), detailEntityList);
        return resultMap;
    }

}
