package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.dto.DmpPushWdtDTO;
import com.erp.model.dmp.dto.DmpPushWdtDetailDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.wms.entity.WdtWarehouseLocationMappingEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpPushWdtFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.service.WdtWarehouseLocationMappingService;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CommonCreateBillGoodsReq;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 旺店通抽象类
 */
@Slf4j
@Service
public class AbstractWdtService <T extends CommonCreateBillGoodsReq>{

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DmpPushWdtFeign dmpPushWdtFeign;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private WdtWarehouseLocationMappingService locationMappingService;
    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;
    @Resource
    private DmpMqFeign dmpMqFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    protected List<T> handleGoodsList(List<T> goodsList) {
        if(CollectionUtils.isEmpty(goodsList)){
            return new ArrayList<>();
        }
        List<T> handlerGoodsList = new ArrayList<>();
        //根据SKU查询BOM判断是否是组合SKU，组合SKU需要拆分
        List<String> skuNoList = goodsList.stream().map(CommonCreateBillGoodsReq::getSpecNo).collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenList = plmTaskFeign.listBomChildBySkuNos(skuNoList);
        for (T goods : goodsList) {
            List<BomChildrenSkuDTO> bomChildrenSkuDTOList = allBomChildrenList.stream().filter(req -> req.getParentSkuNo().equals(goods.getSpecNo()) && BomTypeEnum.COMBINATION.getType().equals(req.getType())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomChildrenSkuDTOList)){
                handlerGoodsList.add(goods);
            }else{
                bomChildrenSkuDTOList.forEach(v->{
                    T handlerGoods = BeanUtil.copyProperties(goods, (Class<T>) goods.getClass());
                    handlerGoods.setSpecNo(v.getSkuNo());
                    handlerGoods.setPositionNo(goods.getPositionNo());
                    handlerGoods.setNum(goods.getNum().multiply(new BigDecimal(v.getQuantity())));
                    handlerGoodsList.add(handlerGoods);
                });
            }
        }
        return handlerGoodsList;
    }

    private BusinessNoTypeEnum getBusinessNoType(SourceTypeEnum sourceType) {
        if(sourceType.compareTo(SourceTypeEnum.OTHER_INSTOCK) == 0){
            return BusinessNoTypeEnum.CODE_QTRK;
        }
        if(sourceType.compareTo(SourceTypeEnum.OTHER_OUTSTOCK) == 0){
            return BusinessNoTypeEnum.CODE_QTCK;
        }
        return null;
    }

    public void transfer(SyncOperateEnum operateEnum, String sourceId, String sourceCode, List<T> goodsList, SourceTypeEnum sourceTypeEnum) {
        BusinessNoTypeEnum businessNoTypeEnum = getBusinessNoType(sourceTypeEnum);

        //拆分组合bom
        List<T> splitBomGoodsList = handleGoodsList(goodsList);
        Map<String, List<T>> groupByWarehouse = splitBomGoodsList.stream().collect(Collectors.groupingBy(item -> item.getWarehouseId()));
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(groupByWarehouse.keySet()), "wdt");
        if(mappingList.isEmpty()){
            log.error("没有找到第三方仓库映射, 取消推送: {}, {}", sourceCode, operateEnum);
            return;
        }
        Map<String, String> thirdWarehouseMap = mappingList.stream().collect(Collectors.toMap(item1 -> item1.getSysWarehouseId(), item2 -> item2.getThirdWarehouseCode()));
        //按仓库维度构建单据
        for (Map.Entry<String, List<T>> entry : groupByWarehouse.entrySet()) {
            String warehouseId = entry.getKey();
            List<T> goodsLists = entry.getValue();
            String thirdWarehouseCode = thirdWarehouseMap.get(warehouseId);
            if(StringUtils.isEmpty(thirdWarehouseCode)){
                log.error("没有找到第三方仓库映射, 取消推送: {}", warehouseId);
                return;
            }
            if(goodsLists.isEmpty()){
                log.error("sku明细不能为空: {}", sourceCode);
                throw new ServiceException("sku明细不能为空");
            }
            //保存原始数据
            saveMiddleData(sourceId, sourceCode, sourceTypeEnum, goodsLists, null, warehouseId, thirdWarehouseCode, operateEnum, "0");
            //仓位转换,然后拆分为有映射的和没有映射的
            Pair<List<T>, List<T>> pair = handleTransfer(goodsLists, warehouseId);
            if(! pair.getKey().isEmpty()){
                List<T> combinationList = combinationSku(pair.getKey(), operateEnum);
                String codeWithPush = docNoGenHelper.generateCode(businessNoTypeEnum);
                String idWithPush = saveMiddleData(sourceId, sourceCode, sourceTypeEnum, combinationList, codeWithPush, warehouseId, thirdWarehouseCode, operateEnum, "1");
                List<DmpPushTaskEntity> pushTaskList = generateTask(combinationList, operateEnum, codeWithPush, thirdWarehouseCode, sourceCode, idWithPush, SyncStatusEnum.IN_SYNC, sourceTypeEnum);
                if(CollectionUtils.isNotEmpty(pushTaskList)){
                    dmpMqFeign.sendTask(pushTaskList);
                }
            }

            if(! pair.getValue().isEmpty()){
                List<T> combinationListWithNoPush = combinationSku(pair.getValue(), operateEnum);
                String codeWithNoPush = docNoGenHelper.generateCode(businessNoTypeEnum);
                String idWithNoPush = saveMiddleData(sourceId, sourceCode, sourceTypeEnum, combinationListWithNoPush, codeWithNoPush, warehouseId, thirdWarehouseCode, operateEnum, "1");
                List<DmpPushTaskEntity> pushTaskListNoPush = generateTask(combinationListWithNoPush, operateEnum, codeWithNoPush, thirdWarehouseCode, sourceCode, idWithNoPush, SyncStatusEnum.NO_NEED_SYNC, sourceTypeEnum);
            }
        }
    }

    /**
     * 构建其它出入库单据
     *
     * @param operateEnum
     * @param midTableId 中间表ID
     * @param businessNoTypeEnum
     * @param sourceTypeEnum
     * @param goodsLists
     * @param thirdWarehouseCode
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    public void structBill(SyncOperateEnum operateEnum, String midTableId, BusinessNoTypeEnum businessNoTypeEnum, SourceTypeEnum sourceTypeEnum, List<T> goodsLists, String thirdWarehouseCode) {
        List<DmpPushWdtDTO.ViewDTO> viewDTOList = dmpPushWdtFeign.listByIds(Collections.singletonList(midTableId));
        DmpPushWdtDTO.ViewDTO viewDTO = viewDTOList.get(0);
        String warehouseId = viewDTO.getWarehouseId();
        String sourceCode = viewDTO.getSourceCode();
        Pair<List<T>, List<T>> pair = handleTransfer(goodsLists, warehouseId);
        if(pair.getKey().isEmpty()){
            log.error("查询&同步时没有找到仓位映射, 取消推送: {} {}", midTableId, warehouseId);
            return;
        }
        boolean removeSuccess = dmpTaskFeign.deletePushTaskBySourceId(midTableId);
        if(! pair.getKey().isEmpty()){
            String codeWithPush = docNoGenHelper.generateCode(businessNoTypeEnum);
            String idWithPush = saveMiddleData(viewDTO.getSourceId(), sourceCode, sourceTypeEnum, pair.getKey(), codeWithPush, warehouseId, thirdWarehouseCode, operateEnum, "1");
            List<DmpPushTaskEntity> pushTaskList = generateTask(pair.getKey(), operateEnum, codeWithPush, thirdWarehouseCode, sourceCode, idWithPush, SyncStatusEnum.IN_SYNC, sourceTypeEnum);
            if(CollectionUtils.isNotEmpty(pushTaskList)){
                dmpMqFeign.sendTask(pushTaskList);
            }
        }

        if(! pair.getValue().isEmpty()){
            String codeWithNoPush = docNoGenHelper.generateCode(businessNoTypeEnum);
            String idWithNoPush = saveMiddleData(viewDTO.getSourceId(), sourceCode, sourceTypeEnum, pair.getValue(), codeWithNoPush, warehouseId, thirdWarehouseCode, operateEnum, "0");
            List<DmpPushTaskEntity> pushTaskListNoPush = generateTask(pair.getValue(), operateEnum, codeWithNoPush, thirdWarehouseCode, sourceCode, idWithNoPush, SyncStatusEnum.NO_NEED_SYNC, sourceTypeEnum);
        }
    }

    /**
     * 处理仓位转换
     * @param goodsLists  sku明细
     * @param warehouseId 仓库ID
     * @return 有仓位映射的列表,没有仓位映射的列表
     * @date: 2024-08-15
     * @author: tanmujin
     */
    private <T extends CommonCreateBillGoodsReq> Pair<List<T>, List<T>> handleTransfer(List<T> goodsLists, String warehouseId) {
        List<WdtWarehouseLocationMappingEntity> mappingList = locationMappingService.list(new LambdaQueryWrapper<WdtWarehouseLocationMappingEntity>().eq(WdtWarehouseLocationMappingEntity::getSysWarehouseId, warehouseId));
        Map<String, String> wdtLocationMap = mappingList.stream().collect(Collectors.toMap(item -> item.getSysWarehouseId() + "#" + item.getSysWarehouseLocation(), item1 -> item1.getThirdWarehouseLocation()));
        List<T> needPushList = new ArrayList<>();
        List<T> noNeedPushList = new ArrayList<>();
        for (T goods : goodsLists) {
            String key = warehouseId + "#" + goods.getPositionNo();
            if(wdtLocationMap.containsKey(key)){
                goods.setPositionNo(wdtLocationMap.get(key));
                needPushList.add(goods);
            }else {
                noNeedPushList.add(goods);
            }
        }
        return new Pair<>(needPushList, noNeedPushList);
    }

    /**
     * 保存原始数据
     * @param
     * @return 中间表ID
     * @date: 2024-08-15
     * @author: tanmujin
     */
    private String saveMiddleData(String sourceId, String sourceCode, SourceTypeEnum sourceTypeEnum, List<T> goodsLists, String outerCode, String sysWarehouseId, String thirdWarehouseCode, SyncOperateEnum operateEnum, String type) {
        DmpPushWdtDTO.AddDTO dto = new DmpPushWdtDTO.AddDTO();
        dto.setSourceId(sourceId);
        dto.setSourceCode(sourceCode);
        dto.setThirdCode(outerCode);
        dto.setThirdType(sourceTypeEnum.getCode());
        dto.setWarehouseId(sysWarehouseId);
        dto.setThirdWarehouseCode(thirdWarehouseCode);
        dto.setOperateType(operateEnum.getCode());
        List<DmpPushWdtDetailDTO> detailDTOList = BeanMapper.copyList(goodsLists, DmpPushWdtDetailDTO.class);
        dto.setDetailDTOList(detailDTOList);
        dto.setType(type);
        return dmpPushWdtFeign.add(dto);
    }

    /**
     * 合并相同仓位的sku
     * @param goodsList
     * @param operateEnum
     * @return
     * @date: 2024-08-15
     * @author: tanmujin
     */
    public List<T> combinationSku(List<T> goodsList, SyncOperateEnum operateEnum) {
        Map<String, List<T>> collect = goodsList.stream().collect(Collectors.groupingBy(item -> item.getSpecNo() + "#" + item.getPositionNo()));
        List<T> combinationList = new ArrayList<>();
        for (Map.Entry<String, List<T>> entry : collect.entrySet()) {
            List<T> collectGoodsList = entry.getValue();
            T goods = BeanUtil.copyProperties(collectGoodsList.get(0), (Class<T>) collectGoodsList.get(0).getClass());
            String[] split = entry.getKey().split("#");
            goods.setSpecNo(split[0]);
            if(split.length == 1){
                goods.setPositionNo("");
            }else {
                goods.setPositionNo(split[1]);
            }
            int sum = collectGoodsList.stream().mapToInt(item -> item.getNum().intValue()).sum();
            goods.setNum(BigDecimal.valueOf(sum));
            combinationList.add(goods);
        }
        return combinationList;
    }

    private List<DmpPushTaskEntity> generateTask(List<T> combinationList, SyncOperateEnum operateEnum, String outerCode, String thirdWarehouseCode, String sourceCode, String sourceId, SyncStatusEnum syncStatusEnum, SourceTypeEnum sourceTypeEnum) {
        List<DmpPushTaskEntity> list = new ArrayList<>();
        if(sourceTypeEnum.compareTo(SourceTypeEnum.OTHER_INSTOCK) == 0){
            List<DmpPushTaskEntity> dmpPushTaskEntityList = generateStockInTask(combinationList, operateEnum, outerCode, thirdWarehouseCode, sourceCode, sourceId, syncStatusEnum);
            list.addAll(dmpPushTaskEntityList);
        }
        if(sourceTypeEnum.compareTo(SourceTypeEnum.OTHER_OUTSTOCK) == 0){
            List<DmpPushTaskEntity> dmpPushTaskEntityList = generateStockOutTask(combinationList, operateEnum, outerCode, thirdWarehouseCode, sourceCode, sourceId, syncStatusEnum);
            list.addAll(dmpPushTaskEntityList);
        }
        return list;
    }

    private List<DmpPushTaskEntity> generateStockOutTask(List<T> combinationList, SyncOperateEnum operateEnum, String outerCode, String thirdWarehouseCode, String sourceCode, String sourceId, SyncStatusEnum syncStatusEnum) {
        CreateOtherStockoutRequest request = new CreateOtherStockoutRequest();
        request.setOuterNo(outerCode);
        request.setWarehouseNo(thirdWarehouseCode);
        request.setIsCheck(Boolean.TRUE);
        request.setGoodsList((List<CreateOtherStockoutRequest.GoodsList>) combinationList);
        request.setSourceId(outerCode);
        request.setOperateCode(operateEnum.getCode());
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());
        request.setRemark("原始单据号：" + sourceCode);

        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(sourceId);
        dmpSyncTaskDTO.setSourceCode(sourceCode);
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_OUTSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_OUT_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateEnum.getCode());
        dmpSyncTaskDTO.setThirdCode(outerCode);
        dmpSyncTaskDTO.setStatus(syncStatusEnum.getCode());

        return dmpMqFeign.saveTaskList(Collections.singletonList(dmpSyncTaskDTO));
    }

    private List<DmpPushTaskEntity> generateStockInTask(List<T> combinationList, SyncOperateEnum operateEnum, String outerCode, String thirdWarehouseCode, String sourceCode, String sourceId, SyncStatusEnum syncStatusEnum) {
        CreateOtherStockinRequest request = new CreateOtherStockinRequest();
        request.setOuterNo(outerCode);
        request.setWarehouseNo(thirdWarehouseCode);
        request.setIsCheck(Boolean.TRUE);
        request.setGoodsList((List<CreateOtherStockinRequest.GoodsList>) combinationList);
        request.setSourceId(outerCode);
        request.setOperateCode(operateEnum.getCode());
        request.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        request.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        request.setCreateTime(LocalDateTime.now());
        request.setRemark("原始单据号：" + sourceCode);

        DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
        dmpSyncTaskDTO.setSourceId(sourceId);
        dmpSyncTaskDTO.setSourceCode(sourceCode);
        dmpSyncTaskDTO.setSourceType(SourceTypeEnum.OTHER_INSTOCK.getCode());
        dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_WANGDIAN_ERP_TOPIC);
        dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.WDT_OTHER_IN_STOCK_TAG.getName());
        dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(request));
        dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.WANGDIAN.getDesc());
        dmpSyncTaskDTO.setSyncOperate(operateEnum.getCode());
        dmpSyncTaskDTO.setThirdCode(outerCode);
        dmpSyncTaskDTO.setStatus(syncStatusEnum.getCode());
        return dmpMqFeign.saveTaskList(Collections.singletonList(dmpSyncTaskDTO));
    }
}