package com.erp.server.wms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.core.exception.ServiceException;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.PackingInspectionDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.PackingInspectionOperationEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.convert.PackingInspectConverter;
import com.erp.server.wms.service.PackingInspectionService;
import com.erp.server.wms.service.SoB2cDeliveryDetailService;
import com.erp.server.wms.service.SoB2cDeliveryService;
import io.seata.common.util.CollectionUtils;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liuruipeng
 * @date 2023年12月13日 20:03
 */
@Slf4j
@Service
public class PackingInspectionServiceImpl implements PackingInspectionService {

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PackingInspectionDTO.ViewDTO scan(PackingInspectionDTO.ScanDTO dto) {
        if(PackingInspectionOperationEnum.BY_ORDER.getCode().equals(dto.getOperationType())){
            throw new ServiceException("不支持该操作类型");
        }
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getByBusinessCode(dto.getBusinessCode());
        if(Objects.isNull(entity)){
            throw new ServiceException("查询不到发货单，请确认扫描单号");
        }
        //因为明细只保存父级SKU，所以如果有组合品没办法直接更新明细，将明细sku拆分放到redis，扫描时操作redis的值，在最后全部扫描完成统一更新数据库
        PackingInspectionDTO.ViewDTO viewDTO = this.getViewDTO(entity.getId());
        //redis没有值，说明可能是开始扫描，或者过期
        if(Objects.isNull(viewDTO)){
            List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            if(CollectionUtils.isEmpty(detailEntityList)){
                throw new ServiceException("发货单详情为空");
            }
            //组合产品，按最新BOM拆分为子产品和销售数量显示
            //根据SKU查询BOM判断是否是组合SKU
            List<String> skuIdList = detailEntityList.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList());
//            List<BomChildrenSkuDTO> bomChildrenList = plmTaskFeign.listBomChildBySkuIds(skuIdList);
//            //可能有多条，取最新bom版本
//            Map<String, List<BomChildrenSkuDTO>> bomChildrenSkuDTOMap = bomChildrenList.stream()
//                    .collect(Collectors.toMap(
//                            BomChildrenSkuDTO::getParentSkuId,
//                            // 如果有相同的parentSkuId，合并数据
//                            Collections::singletonList,
//                            // 合并函数，选择bomVersion最大的数据
//                            (list1, list2) -> {
//                                int maxVersion = Math.max(
//                                        Integer.parseInt(list1.get(0).getBomVersion()),
//                                        Integer.parseInt(list2.get(0).getBomVersion())
//                                );
//                                return Stream.of(list1, list2)
//                                        .flatMap(Collection::stream)
//                                        .filter(v -> Integer.parseInt(v.getBomVersion()) == maxVersion)
//                                        .collect(Collectors.toList());
//                            },
//                            // 使用LinkedHashMap保持顺序
//                            LinkedHashMap::new
//                    ));

//            List<BomChildrenSkuDTO> distinctBomChildrenSkuList = bomChildrenSkuDTOMap.values().stream().flatMap(List::stream).collect(Collectors.toList());
//            //去掉组合SKU
//            skuIdList = skuIdList.stream().filter(v->!bomChildrenSkuDTOMap.containsKey(v)).collect(Collectors.toList());
            //增加子件SKU 现在skuIdList 里面是单品SKU+组合SKU的子件
//            skuIdList.addAll(distinctBomChildrenSkuList.stream().map(BomChildrenSkuDTO::getSkuId).distinct().collect(Collectors.toList()));
            //查询sku基础信息
            List<SkuVO> skuVOList = plmTaskFeign.getSkuInfoByIds(skuIdList);
            Map<String,SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId,Function.identity()));
            //组合的SKU明细
//            Map<String,SoB2cDeliveryDetailEntity> combineSkuDetailMap = detailEntityList.stream().filter(v->bomChildrenSkuDTOMap.containsKey(v.getSkuId())).collect(Collectors.toMap(SoB2cDeliveryDetailEntity::getSkuId,Function.identity()));
//            //单品SKU明细
//            List<SoB2cDeliveryDetailEntity> singleSkuDetailList = detailEntityList.stream().filter(v->!bomChildrenSkuDTOMap.containsKey(v.getSkuId())).collect(Collectors.toList());
            PackingInspectionDTO.ViewDTO addViewDTO;
            //新增主记录和单品SKU view
//            addViewDTO = PackingInspectConverter.INSTANCE.convertViewDTO(entity,singleSkuDetailList);
            addViewDTO = PackingInspectConverter.INSTANCE.convertViewDTO(entity,detailEntityList);
            addViewDTO.setScannedSkuList(new ArrayList<>());
            //添加组合SKU的子件
//            PackingInspectionDTO.ViewDTO finalAddViewDTO = addViewDTO;
//            combineSkuDetailMap.forEach((key, value)->{
//                List<BomChildrenSkuDTO> bomChildrenSkuDTOS = bomChildrenSkuDTOMap.get(key);
//                for(BomChildrenSkuDTO bomChildrenSkuDTO : bomChildrenSkuDTOS){
//                    PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = PackingInspectionDTO.ViewDTO.ScanSkuInfo.builder()
//                            .skuId(bomChildrenSkuDTO.getSkuId())
//                            .skuNo(bomChildrenSkuDTO.getSkuNo())
//                            .warehouseLocation(value.getWarehouseLocation())
//                            .waitScanQty(value.getWaitScanQty() * bomChildrenSkuDTO.getQuantity())
//                            .scannedQty((value.getDeliveryQty() - value.getWaitScanQty())* bomChildrenSkuDTO.getQuantity())
//                            .saleQty(value.getDeliveryQty()* bomChildrenSkuDTO.getQuantity())
//                            .build();
//                    finalAddViewDTO.getWaitScanSkuList().add(scanSkuInfo);
//                }
//            });
            //设置sku信息
            for (PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo: addViewDTO.getWaitScanSkuList()){
                SkuVO skuVO = skuVOMap.get(scanSkuInfo.getSkuId());
                if(Objects.nonNull(skuVO)){
                    scanSkuInfo.setProductName(skuVO.getSkuName());
                    scanSkuInfo.setSkuImageUrl(skuVO.getSkuImagesUrl());
                    scanSkuInfo.setSkuNo(skuVO.getSkuNo());
                }
            }
            addViewDTO.setSkuSpeciesQty(addViewDTO.getWaitScanSkuList().size()+addViewDTO.getScannedSkuList().size());
            addViewDTO.setSkuTotalQty(addViewDTO.getWaitScanSkuList().stream().mapToInt(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSaleQty).sum()+addViewDTO.getScannedSkuList().stream().mapToInt(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSaleQty).sum());
            Iterator<PackingInspectionDTO.ViewDTO.ScanSkuInfo> it = addViewDTO.getWaitScanSkuList().iterator();
            while (it.hasNext()) {
                PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = it.next();
                if(scanSkuInfo.getScannedQty().equals(scanSkuInfo.getSaleQty())){
                    addViewDTO.getScannedSkuList().add(scanSkuInfo);
                    it.remove();
                }
            }
            viewDTO = addViewDTO;
        }
        if(StringUtils.isNotBlank(dto.getSkuNo())){
            //修改对应SKU扫描数量
            Iterator<PackingInspectionDTO.ViewDTO.ScanSkuInfo> it = viewDTO.getWaitScanSkuList().iterator();
            List<String> scannedSkuNoList = viewDTO.getScannedSkuList().stream().map(PackingInspectionDTO.ViewDTO.ScanSkuInfo::getSkuNo).collect(Collectors.toList());
            boolean matchFlag = false;
            while (it.hasNext()) {
                PackingInspectionDTO.ViewDTO.ScanSkuInfo scanSkuInfo = it.next();
                if(dto.getSkuNo().equals(scanSkuInfo.getSkuNo())){
                    matchFlag = true;
                    if(scanSkuInfo.getWaitScanQty() <= 0){
                        throw new ServiceException("SKU已验货完成，无需再次验货");
                    }
                    scanSkuInfo.setWaitScanQty(scanSkuInfo.getWaitScanQty()-1);
                    scanSkuInfo.setScannedQty(scanSkuInfo.getSaleQty() - scanSkuInfo.getWaitScanQty());
                    //如果已扫描数等于销售数，放到已扫描队列
                    if(scanSkuInfo.getScannedQty().equals(scanSkuInfo.getSaleQty())){
                        viewDTO.getScannedSkuList().add(scanSkuInfo);
                        it.remove();
                    }
                    break;
                }
            }
            if(!matchFlag){
                if(scannedSkuNoList.contains(dto.getSkuNo())){
                    throw new ServiceException("SKU已验货完成，无需再次验货");
                }
                throw new ServiceException("SKU不匹配，验货失败");
            }
        }else{
            if(entity.getIsInspection()){
                throw new ServiceException("订单已验货，无法重复验货");
            }
        }
        //判断是否全部扫描完成
        if(CollectionUtils.isEmpty(viewDTO.getWaitScanSkuList())){
            //更新数据
            List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
            detailEntityList.forEach(v-> v.setWaitScanQty(0));
            entity.setIsInspection(true);
            viewDTO.setStatus(true);
            if(!soB2cDeliveryService.updateById(entity)){
                throw new ServiceException("发货单更新失败");
            }
            if(!soB2cDeliveryDetailService.updateBatchById(detailEntityList)){
                throw new ServiceException("发货单明细更新失败");
            }
        }
        if(dto.getIsAutoDelivery() && entity.getIsInspection()){
            //将发货状态更新为已发货
            entity.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            if (!soB2cDeliveryService.updateById(entity)) {
                throw new ServiceException("发货单更新失败");
            }
        }
        this.saveViewDTO(entity.getId(),viewDTO);
        return viewDTO;
    }

    @Override
    public void reset(String id) {
        SoB2cDeliveryEntity entity = soB2cDeliveryService.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException("查询的发货单为空");
        }
        List<SoB2cDeliveryDetailEntity> detailEntityList = soB2cDeliveryDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        detailEntityList.forEach(v-> v.setWaitScanQty(v.getDeliveryQty()));
        entity.setIsInspection(false);
        if(!soB2cDeliveryService.updateById(entity)){
            throw new ServiceException("发货单更新失败");
        }
        if(!soB2cDeliveryDetailService.updateBatchById(detailEntityList)){
            throw new ServiceException("发货单明细更新失败");
        }
        this.deleteViewDTO(id);
    }

    private void deleteViewDTO(String id) {
        redisTemplate.delete(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id));
    }


    private void saveViewDTO(String id, PackingInspectionDTO.ViewDTO viewDTO) {
        String json = JSONObject.toJSONString(viewDTO);
        redisTemplate.opsForValue().set(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id), json,1, TimeUnit.DAYS);
    }

    private PackingInspectionDTO.ViewDTO getViewDTO(String id) {
        String json = redisTemplate.opsForValue().get(StrUtil.format(RedisKeyConstant.WMS_PACKING_INSPECTION, id));
        return JSONObject.parseObject(json,new TypeReference<PackingInspectionDTO.ViewDTO>() {}.getType());
    }
}
