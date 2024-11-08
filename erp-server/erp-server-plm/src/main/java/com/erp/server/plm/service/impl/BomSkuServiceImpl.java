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
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.plm.mapper.BomRefSkuMapper;
import com.erp.server.plm.service.BomSkuService;
import com.erp.server.plm.service.ProductBomHistoryService;
import com.erp.server.plm.service.ProductDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class BomSkuServiceImpl extends ServiceImpl<BomRefSkuMapper, BomSkuEntity> implements BomSkuService {


    @Resource
    private ProductDetailService productDetailService;

    @Resource
    private ProductBomHistoryService productBomHistoryService;

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
        deleteBomSku(bomId);
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
     * 根据Bomid 删除 bom sku 信息
     *
     * @param bomId
     * @return void
     * @author yl
     * @date 2023-01-12 17:29
     */
    private void deleteBomSku(String bomId) {
        LambdaQueryWrapper<BomSkuEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BomSkuEntity::getBomId, bomId);
        this.remove(queryWrapper);

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
    public List<ProductBomInfoDTO.skuBomVersion> listBomVersionBySkuNos(List<String> skuNos) {
        if (CollectionUtils.isEmpty(skuNos)) {
            return Collections.emptyList();
        }
        List<BomSkuEntity> list = lambdaQuery().in(BomSkuEntity::getParentSkuNo, skuNos).list();
        List<String> bomIds = list.stream().map(req -> req.getBomId()).distinct().collect(Collectors.toList());
        List<ProductBomHistoryEntity> productBomHistoryEntities = productBomHistoryService.listByBomIds(bomIds);
        List<ProductBomInfoDTO.skuBomVersion> skuBomVersionList = new ArrayList<>();
        Map<String, List<BomSkuEntity>> map = list.stream().collect(Collectors.groupingBy(BomSkuEntity::getParentSkuNo));
        for (Map.Entry<String, List<BomSkuEntity>> stringListEntry : map.entrySet()) {
            ProductBomInfoDTO.skuBomVersion bomVersion = new ProductBomInfoDTO.skuBomVersion();
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
        return baseMapper.listAllBomByChildSkuIdList(childSkuIdList);
    }

    @Override
    public List<BomChildrenSkuDTO> checkExistAndListCombinationSku(List<String> parentSkuNos) {
        if (CollectionUtils.isEmpty(parentSkuNos)) {
            return Collections.emptyList();
        }
        return baseMapper.checkExistAndListCombinationSku(parentSkuNos);
    }
}
