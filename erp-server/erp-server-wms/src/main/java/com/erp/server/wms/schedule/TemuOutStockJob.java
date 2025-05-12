package com.erp.server.wms.schedule;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.SoOutstockService;
import com.sdk.oms.temu.dto.TemuOrderDTO;
import com.sdk.oms.temu.dto.TemuOrderReq;
import com.sdk.oms.temu.dto.TemuResp;
import com.sdk.oms.temu.service.TemuClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import io.seata.common.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Temu半托管出库
 */
@Component
@Slf4j
public class TemuOutStockJob {

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private TemuClient temuClient;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @XxlJob("TemuOutStockJob")
    public void temuOutStockJob() {
        //查询temu半托管平台，状态是已发货，平台仓的订单,并且销售出库时间为空
        List<SoB2cEntity> soB2cEntityList = FeignQuery.create(SoB2cEntity.class)
                .eq(SoB2cEntity::getDictPlatform, PlatformDictEnum.TE_MU.getCode())
                .eq(SoB2cEntity::getBillStatus, SoB2cBillStatusEnum.ENUM_SHIPPED.getCode())
                .isNull(SoB2cEntity::getSoOutstockDate)
                .list();
        soB2cEntityList = soB2cEntityList.stream().filter(SoB2cEntity::hasPlatformWarehouseOrder).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            XxlJobHelper.log("没有需要处理的订单");
            return;
        }
        //查询仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> warehouseMappingDTOS = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(),PlatformDictEnum.TE_MU.getCode());
        if(CollectionUtils.isEmpty(warehouseMappingDTOS)){
            XxlJobHelper.log("没查到仓库映射");
            return;
        }
        //查询店铺授权信息
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        if(CollectionUtils.isEmpty(shopIds)){
            XxlJobHelper.log("没查到店铺信息");
            return;
        }
        List<ShopInfoEntity> shopInfoEntityList = shopInfoFeign.listShopInfoByIds(shopIds);
        shopInfoEntityList = shopInfoEntityList.stream().filter(v->!v.getDisabled() && v.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(shopInfoEntityList)){
            XxlJobHelper.log("没查到店铺有效授权信息");
            return;
        }
        List<TemuOrderDTO.ResultDTO.PageItemsDTO> allTemuList = new ArrayList<>();
        //根据店铺分组处理
        Map<String,List<SoB2cEntity>> groupShopMap = soB2cEntityList.stream().collect(Collectors.groupingBy(SoB2cEntity::getShopId));
        for (Map.Entry<String, List<SoB2cEntity>> entry : groupShopMap.entrySet()) {
            ShopInfoEntity shopInfoEntity = shopInfoEntityList.stream().filter(v->v.getId().equals(entry.getKey())).findFirst().orElse(null);
            if(shopInfoEntity == null){
                XxlJobHelper.log("店铺信息不存在"+entry.getKey());
                continue;
            }
            Map<String,Object> extendMap = shopInfoEntity.getExtendData();
            List<String> allPlatformCodes = entry.getValue().stream().map(SoB2cEntity::getPlatformCode).collect(Collectors.toList());
            //分成20一组
            List<List<String>> partitionPlatformCodes = ListUtil.partition(allPlatformCodes, 20);
            for (List<String> platformCodeList : partitionPlatformCodes) {
                TemuOrderReq temuOrderReq = new TemuOrderReq();
                temuOrderReq.setAreaCode(shopInfoEntity.getDictAreaCode());
                temuOrderReq.setParentOrderSnList(platformCodeList);
                temuOrderReq.setToken(shopInfoEntity.getAccessToken());
                temuOrderReq.setAppKey(extendMap.get("clientId").toString());
                temuOrderReq.setAppSecret(extendMap.get("clientSecret").toString());
                TemuResp<TemuOrderDTO> temuResp = temuClient.getOrderList(temuOrderReq);
                if(!temuResp.getSuccess()){
                    XxlJobHelper.log("查询temu订单数据响应失败,{}",temuResp.getErrorMsg());
                    log.error("查询temu订单数据响应失败,{}",temuResp.getErrorMsg());
                    continue;
                }
                TemuOrderDTO temuOrderDTO = temuResp.getResult();
                if(!temuOrderDTO.getSuccess()){
                    XxlJobHelper.log("查询temu订单数据失败,{}",temuOrderDTO.getErrorMsg());
                    log.error("查询temu订单数据失败,{}",temuResp.getErrorMsg());
                    continue;
                }
                if(CollectionUtils.isEmpty(temuOrderDTO.getResult().getPageItems())){
                    XxlJobHelper.log("没有查询到订单数据");
                    continue;
                }
                List<TemuOrderDTO.ResultDTO.PageItemsDTO> pageItemsDTOList = temuOrderDTO.getResult().getPageItems();
                //过滤掉没有发货时间和没有发货仓库id的数据
                pageItemsDTOList = pageItemsDTOList.stream().filter(v->{
                    Integer shippingTimeInt = v.getParentOrderMap().getParentShippingTime();
                    List<String> warehouseIds =  v.getOrderList().stream().map(TemuOrderDTO.ResultDTO.PageItemsDTO.OrderListDTO::getInventoryDeductionWarehouseId).filter(StringUtils::isNotBlank).collect(Collectors.toList());
                    v.setWarehouseIdList(warehouseIds);
                    return CollectionUtils.isNotEmpty(warehouseIds) && Objects.nonNull(shippingTimeInt);
                }).collect(Collectors.toList());
                allTemuList.addAll(pageItemsDTOList);
            }
        }
        if(CollectionUtils.isEmpty(allTemuList)){
            XxlJobHelper.log("没有查询到Temu已发货订单数据");
            return;
        }
        List<String> temuPlatformCode = allTemuList.stream().map(v->v.getParentOrderMap().getParentOrderSn()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> handleSoB2cList = soB2cEntityList.stream().filter(v->temuPlatformCode.contains(v.getPlatformCode())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(handleSoB2cList)){
            XxlJobHelper.log("没有需要处理的订单");
            return;
        }
        List<String> soIds = handleSoB2cList.stream().map(SoB2cEntity::getId).distinct().collect(Collectors.toList());
        List<SoB2cDetailEntity> allSoB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soIds);
        //将仓库能匹配上的数据出库
        for (SoB2cEntity soB2cEntity : handleSoB2cList) {
            TemuOrderDTO.ResultDTO.PageItemsDTO temuDto = allTemuList.stream().filter(v->v.getParentOrderMap().getParentOrderSn().equals(soB2cEntity.getPlatformCode())).findFirst().orElse(null);
            if(temuDto == null){
                continue;
            }
            List<String> temuWarehouseIds = temuDto.getWarehouseIdList();
            List<String> erpWarehouseIds = warehouseMappingDTOS.stream().filter(v->temuWarehouseIds.contains(v.getThirdWarehouseId())).map(ThirdMappingDTO.WarehouseMappingDTO::getSysWarehouseId).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(erpWarehouseIds)){
                XxlJobHelper.log("没有匹配的仓库,{}",temuWarehouseIds);
                continue;
            }
            List<SoB2cDetailEntity> soB2cDetailEntityList = allSoB2cDetailEntityList.stream().filter(v->v.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
            soB2cDetailEntityList = soB2cDetailEntityList.stream().filter(v-> erpWarehouseIds.contains(v.getWarehouseId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(soB2cDetailEntityList)){
                XxlJobHelper.log("没有匹配的订单明细,{}",soB2cEntity.getId());

                continue;
            }
            Integer outTimeInt = temuDto.getParentOrderMap().getParentShippingTime();
            Instant instant = Instant.ofEpochSecond(outTimeInt);
            // 获取系统默认时区
            ZoneId zoneId = ZoneId.systemDefault();
            // 将 Instant 对象转换为 LocalDateTime 对象
            LocalDateTime outTime = LocalDateTime.ofInstant(instant, zoneId);
            //出库
            soOutstockService.generateB2cSoOutstock(soB2cEntity,soB2cDetailEntityList,outTime);
        }
    }
}
