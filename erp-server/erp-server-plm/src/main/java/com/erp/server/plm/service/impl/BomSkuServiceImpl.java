package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BomInfoEntity;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.enums.BomStateEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.VirtualInventoryFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.plm.mapper.BomRefSkuMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * bom 与sku关系表(BomRefSku)表服务实现类
 *
 * @author yl
 * @since 2023-01-09 11:45:28
 */
@Service
@Slf4j
public class BomSkuServiceImpl extends ServiceImpl<BomRefSkuMapper, BomSkuEntity> implements BomSkuService {


    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;

    @Resource
    private InventoryFeign inventoryFeign;

    /**
     * 保存bom 与sku 关系
     *
     * @param bomId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-09 14:19
     */
    @Override
    public void saveBomSku(String bomId, List<BomSkuDTO> bomSkuList) {
        List<BomSkuEntity> saveBatchList = new LinkedList<>();



        for (BomSkuDTO item : bomSkuList) {
            List<BomChildrenSkuDTO> childrenList = item.getChildren();
            //SKU重复验证
            String childSkuNos = childrenList.stream().filter(obj -> obj.getSkuId().equals(item.getSkuId())).map(BomChildrenSkuDTO::getSkuNo).collect(Collectors.joining(","));
            if (StringUtils.isNotBlank(childSkuNos)) {
                throw new ServiceException(ApiError.ERROR_BOM_SKU_REPEAT,item.getSkuNo());
            }
            for (BomChildrenSkuDTO children : childrenList) {
                BomSkuEntity entity = new BomSkuEntity();
                entity.setParentSkuId(item.getSkuId());
                entity.setParentSkuNo(item.getSkuNo());
                entity.setSkuId(children.getSkuId());
                entity.setSkuNo(children.getSkuNo());
                entity.setQuantity(children.getQuantity());
                entity.setBomId(bomId);
                entity.setProductId(children.getProductId());
                saveBatchList.add(entity);
            }
        }
        if (CollectionUtils.isNotEmpty(saveBatchList)) {
            this.saveBatch(saveBatchList);
            //标记SKU
            List<String> skuIds = saveBatchList.stream().flatMap(obj -> Stream.of(obj.getSkuId(), obj.getParentSkuId())).distinct().collect(Collectors.toList());
            productDetailService.updateOccupyStatus(skuIds);
        }

    }


    /**
     * 根据bom id 查询出bom yu sku信息
     *
     * @param bomId
     * @return java.util.List<com.erp.model.plm.dto.BomSkuDTO>
     * @author yl
     * @date 2023-01-11 16:51
     */
    @Override
    public List<BomSkuDTO> getByBomId(String bomId) {
        List<BomSkuEntity> bomSkuEntityList = this.getBomSkuListByBomId(bomId);
        Map<String, List<BomSkuEntity>> map = bomSkuEntityList.stream().
                collect(Collectors.groupingBy(BomSkuEntity::getParentSkuNo));

        List<BomSkuDTO> resultList = new ArrayList<>(map.size());
        List<String> skuIdList = bomSkuEntityList.stream().map(BomSkuEntity::getParentSkuId).collect(Collectors.toList());
        List<SkuVO> skuVOList = productDetailService.getSkuBySkuIds(skuIdList);

        for (Map.Entry<String, List<BomSkuEntity>> item : map.entrySet()) {
            List<BomSkuEntity> bomSkuList = item.getValue();
            BomSkuEntity skuEntity = bomSkuList.get(0);
            BomSkuDTO bomSku = new BomSkuDTO();
            bomSku.setLevel(1);
            String skuId = skuEntity.getParentSkuId();
            bomSku.setSkuId(skuId);
            bomSku.setSkuNo(skuEntity.getParentSkuNo());
            SkuVO sku = skuVOList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().orElse(null);
            if (sku != null) {
                bomSku.setProductId(sku.getProductId());
            }
            List<BomChildrenSkuDTO> children = BeanMapper.copyList(bomSkuList, BomChildrenSkuDTO.class);
            for (BomChildrenSkuDTO skuDTO : children) {
                skuDTO.setLevel(2);
            }
            bomSku.setChildren(children);
            resultList.add(bomSku);
        }
        return resultList;
    }


    /**
     * 更改bom sku信息
     *
     * @param bomId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-11 18:27
     */
    @Override
    @Transactional
    public void updateBomSku(String bomId, List<BomSkuDTO> bomSkuList) {
        //先删除
        deleteByBomId(bomId);
        saveBomSku(bomId, bomSkuList);

    }

    @Override
    public void deleteByBomId(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        this.remove(queryWrapper);
    }

    @Override
    public List<BomSkuEntity> getByParentSkuId(String parentSkuId) {
        List<BomSkuEntity> list = lambdaQuery().eq(BomSkuEntity::getParentSkuId, parentSkuId).list();
        return list;
    }

    @Override
    public List<BomSkuEntity> listByParentSkuNos(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(BomSkuEntity::getParentSkuNo,parentSkuNos)
                .list();
    }
    @Override
    public List<BomSkuEntity> listByParentSkuIds(List<String> parentSkuIds) {
        if (CollectionUtils.isEmpty(parentSkuIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(BomSkuEntity::getParentSkuId,parentSkuIds)
                .list();
    }
    @Override
    public List<BomSkuEntity> listByChildSkuIds(List<String> childSkuIds) {
        if (CollectionUtils.isEmpty(childSkuIds)) {
            return Collections.EMPTY_LIST;
        }
        return lambdaQuery().in(BomSkuEntity::getSkuId,childSkuIds)
                .list();
    }

    @Override
    public List<BomChildrenSkuDTO> listAllBomChildBySkuIds(List<String> parentSkuIds) {
        if (CollectionUtils.isEmpty(parentSkuIds)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listAllBomChildBySkuIds(parentSkuIds);
    }

    @Override
    public List<BomChildrenSkuDTO> listBomChildBySkuIds(List<String> parentSkuIds) {
        if (CollectionUtils.isEmpty(parentSkuIds)) {
            return Collections.emptyList();
        }
        return baseMapper.listBomChildBySkuIds(parentSkuIds);
    }

    @Override
    public List<BomChildrenSkuDTO> listBomChildBySkuNos(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBomChildBySkuNos(parentSkuNos);
    }

    @Override
    public List<BomInfoEntity> listBomByParentSkuIds(List<String> parentSkuIds) {
        if (CollectionUtils.isEmpty(parentSkuIds)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBomByParentSkuIds(parentSkuIds);
    }

    @Override
    public List<BomInfoEntity> listBomByParentSkuNos(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBomByParentSkuNos(parentSkuNos);
    }


    @Override
    public List<BomInfoEntity> listAllBomByParentSkuNos(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listAllBomByParentSkuNos(parentSkuNos);
    }

    @Override
    public List<BomDTO.BomSku> listAllBomByChildSkuIdList(List<String> childSkuIdList) {
        if (CollectionUtils.isEmpty(childSkuIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listAllBomByChildSkuIdList(childSkuIdList);
    }

    @Override
    public BomSkuPageDTO.ListAllSkuDTO listAllLevelSku(BomSkuPageDTO.AllSkuParamDTO params) {
        BomSkuPageDTO.ListAllSkuDTO listAllSkuDTO = new BomSkuPageDTO.ListAllSkuDTO();
        //父级SKU
        List<BomSkuPageDTO.ListSkuLevelDTO> parentSkuList = baseMapper.listAllParentSku(params);
        //父级SKUId集合
        List<String> parentSkuNoList = parentSkuList.stream().map(BomSkuPageDTO.ListSkuLevelDTO::getParentSkuNo).distinct().collect(Collectors.toList());

        List<BomSkuPageDTO.ListSkuLevelDTO> childSkuList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(parentSkuNoList)) {

            //根据父级sku递归查询子级SKU
            params.setSkuNoList(null);
            params.setSkuNoList(parentSkuNoList);
            childSkuList = baseMapper.listAllChildSku(params);
        }
        listAllSkuDTO.setParentList(parentSkuList);
        listAllSkuDTO.setChildList(childSkuList);
        return listAllSkuDTO;
    }

    /**
     * 根据bom id  获取列表
     *
     * @param bomId
     * @return java.util.List<com.erp.model.plm.entity.BomSkuEntity>
     * @author yl
     * @date 2023-01-11 17:03
     */
    private List<BomSkuEntity> getBomSkuListByBomId(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        queryWrapper.orderByDesc(BomSkuEntity::getCreateTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<BomChildrenSkuDTO> listBomBySkuIds(List<String> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBomBySkuIds(skuIds);
    }

    @Override
    public List<BomSkuEntity> listBomSkuByBomId(String bomId) {
        return this.lambdaQuery().eq(BomSkuEntity::getBomId, bomId).list();
    }


    @Override
    public List<BomDTO.BomSku> listBySkuIds(List<String> skuIdList) {
        if (CollectionUtils.isEmpty(skuIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listBySkuIds(skuIdList);
    }


    @Override
    public List<ProductBomInfoDTO.SkuBomVersion> listBomVersionBySkuNos(List<String> skuNos) {
        if (CollectionUtils.isEmpty(skuNos)) {
            return Collections.emptyList();
        }
        List<BomSkuEntity> list = lambdaQuery().in(BomSkuEntity::getParentSkuNo, skuNos).list();
        List<String> bomIds = list.stream().map(req -> req.getBomId()).distinct().collect(Collectors.toList());
        List<ProductBomHistoryEntity> productBomHistoryEntities = productBomHistoryService.listByBomIds(bomIds);
        List<ProductBomInfoDTO.SkuBomVersion> skuBomVersionList = new ArrayList<>();
        Map<String, List<BomSkuEntity>> map = list.stream().collect(Collectors.groupingBy(BomSkuEntity::getParentSkuNo));
        for (Map.Entry<String, List<BomSkuEntity>> stringListEntry : map.entrySet()) {
            ProductBomInfoDTO.SkuBomVersion bomVersion = new ProductBomInfoDTO.SkuBomVersion();
            bomVersion.setSkuNo(stringListEntry.getKey());
            List<String> bomVersionList = productBomHistoryEntities.stream().filter(req -> req.getBomId().equals(stringListEntry.getValue().get(0).getBomId())).map(req -> req.getBomVersion()).distinct().collect(Collectors.toList());
            Collections.reverse(bomVersionList);
            bomVersion.setBomVersionList(bomVersionList);
            skuBomVersionList.add(bomVersion);
        }
        return skuBomVersionList;
    }

    @Override
    public List<BomDTO.BomSku> listAllBom(List<String> childSkuIdList) {
        List<BomDTO.BomSku> list = baseMapper.listAllBomByChildSkuIdList(childSkuIdList);
        list = list.stream().filter(v-> BomStateEnum.AUDIT_PASS.getState().equals(v.getState())).collect(Collectors.toList());
        return list;
    }

    @Override
    public List<BomChildrenSkuDTO> checkExistAndListCombinationSku(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.emptyList();
        }
        return baseMapper.checkExistAndListCombinationSku(parentSkuNos);
    }

    /**
     * 获取单品BOM信息
     * @param skuIdList SKU ID列表，用于查询BOM信息
     * @return 包含BomSku对象的列表，每个对象代表一个SKU的BOM信息如果输入列表为空，则返回空列表
     */
    @Override
    public List<BomDTO.BomSku> getSingleBomInfo(List<String> skuIdList) {
        if(CollectionUtils.isEmpty(skuIdList)){
            return Collections.emptyList();
        }
        return baseMapper.getSingleBomInfo(skuIdList);
    }

    @Override
    public List<BomChildrenSkuDTO> listBomChildBySoB2cDetailId(ProductBomInfoDTO.SkuIdParams params) {
        if (CollectionUtils.isEmpty(params.getSkuIds()) || StringUtils.isBlank(params.getSoB2cDetailId())) {
            return Collections.emptyList();
        }

        // 1. 根据SoB2cDetailEntity的ID获取明细信息
        SoB2cDetailEntity soB2cDetail = null;
        try {
            List<SoB2cDetailEntity> detailList = soB2cFeign.listDetailByIds(Collections.singletonList(params.getSoB2cDetailId()));
            if (CollectionUtils.isNotEmpty(detailList)) {
                soB2cDetail = detailList.get(0);
            }
        } catch (Exception e) {
            log.warn("获取SoB2cDetailEntity失败", e);
            return Collections.emptyList();
        }

        if (soB2cDetail == null) {
            return Collections.emptyList();
        }

        // 2. 获取BOM子件信息
        List<BomChildrenSkuDTO> result = baseMapper.listBomChildBySkuIds(params.getSkuIds());
        
        // 3. 为每个子件添加库存信息
        if (CollectionUtils.isNotEmpty(result)) {
            // 获取所有子件的SKU ID
            List<String> childSkuIds = result.stream()
                    .map(BomChildrenSkuDTO::getSkuId)
                    .distinct()
                    .collect(Collectors.toList());

            // 获取虚拟仓库存 - 使用Feign调用
            Map<String, Integer> virtualInventoryMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(childSkuIds) && StringUtils.isNotBlank(soB2cDetail.getVirtualWarehouseId())) {
                try {
                    VirtualInventoryDTO.VirtualInventoryParamDTO virtualParam = new VirtualInventoryDTO.VirtualInventoryParamDTO();
                    virtualParam.setSkuIdList(childSkuIds);
                    virtualParam.setVirtualWarehouseIdList(Collections.singletonList(soB2cDetail.getVirtualWarehouseId()));
                    // warehouseIdList 是必填的，必须设置
                    if (StringUtils.isNotBlank(soB2cDetail.getWarehouseId())) {
                        virtualParam.setWarehouseIdList(Collections.singletonList(soB2cDetail.getWarehouseId()));
                    } else {
                        // 如果没有实体仓ID，使用一个默认值或者跳过虚拟仓查询
                        log.warn("SoB2cDetailEntity缺少warehouseId，跳过虚拟仓库存查询");
                    }
                    virtualParam.setDictInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
                    
                    List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryList = virtualInventoryFeign.listInventoryQty(virtualParam);
                    if (CollectionUtils.isNotEmpty(virtualInventoryList)) {
                        for (VirtualInventoryDTO.VirtualInventoryQtyDTO virtualQty : virtualInventoryList) {
                            String key = virtualQty.getSkuId();
                            Integer currentQty = virtualInventoryMap.getOrDefault(key, 0);
                            virtualInventoryMap.put(key, currentQty + virtualQty.getInventoryQty());
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取虚拟仓库存失败", e);
                }
            }

            // 获取实体仓库存 - 使用Feign调用
            Map<String, Integer> realInventoryMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(childSkuIds) && StringUtils.isNotBlank(soB2cDetail.getWarehouseId())) {
                try {
                    InventoryQtyDTO.SkuInventoryStatusParamDTO realParam = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
                    realParam.setSkuIdList(childSkuIds);
                    realParam.setWarehouseIdList(Collections.singletonList(soB2cDetail.getWarehouseId()));
                    realParam.setInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
                    
                    List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> realInventoryList = inventoryFeign.listSkuInventoryStatusByParam(realParam);
                    if (CollectionUtils.isNotEmpty(realInventoryList)) {
                        for (InventoryQtyDTO.SkuInventoryStatusTotalDTO realQty : realInventoryList) {
                            String key = realQty.getSkuId();
                            Integer currentQty = realInventoryMap.getOrDefault(key, 0);
                            realInventoryMap.put(key, currentQty + realQty.getInventoryTotal());
                        }
                    }
                } catch (Exception e) {
                    log.warn("获取实体仓库存失败", e);
                }
            }

            // 设置库存信息到结果中
            for (BomChildrenSkuDTO bomChildrenSkuDTO : result) {
                String skuId = bomChildrenSkuDTO.getSkuId();
                
                // 设置虚拟仓库存
                Integer virtualQty = virtualInventoryMap.get(skuId);
                bomChildrenSkuDTO.setVirtualUsableQty(virtualQty != null ? virtualQty : 0);
                
                // 设置实体仓库存
                Integer realQty = realInventoryMap.get(skuId);
                bomChildrenSkuDTO.setWarehouseUsableQty(realQty != null ? realQty : 0);
            }
        }
        
        return result;
    }
}
