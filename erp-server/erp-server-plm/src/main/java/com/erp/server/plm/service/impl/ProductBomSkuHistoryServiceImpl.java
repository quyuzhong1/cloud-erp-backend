package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.utils.RedisUtil;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.dto.BomSkuDTO;
import com.erp.model.plm.entity.BomSkuEntity;
import com.erp.model.plm.entity.ProductBomSkuHistoryEntity;
import com.erp.server.plm.mapper.ProductBomSkuHistoryMapper;
import com.erp.server.plm.service.ProductBomSkuHistoryService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)表服务实现类
 *
 * @author yl
 * @since 2023-01-11 14:04:52
 */
@Service
public class ProductBomSkuHistoryServiceImpl extends ServiceImpl<ProductBomSkuHistoryMapper, ProductBomSkuHistoryEntity> implements ProductBomSkuHistoryService {

    @Resource
    private RedisUtil redisUtil;

    /**
     * 方法说明
     *
     * @param bomHistoryId
     * @param bomSkuList
     * @return void
     * @author yl
     * @date 2023-01-12 18:58
     */
    @Override
    public void saveBomSku(String bomHistoryId, List<BomSkuDTO> bomSkuList) {
        List<ProductBomSkuHistoryEntity> saveBatchList = new LinkedList<>();
        for (BomSkuDTO item : bomSkuList) {
            List<BomChildrenSkuDTO> childrenList = item.getChildren();
            for (BomChildrenSkuDTO children : childrenList) {
                ProductBomSkuHistoryEntity entity = new ProductBomSkuHistoryEntity();
                entity.setParentSkuId(item.getSkuId());
                entity.setParentSkuNo(item.getSkuNo());
                entity.setSkuId(children.getSkuId());
                entity.setSkuNo(children.getSkuNo());
                entity.setQuantity(children.getQuantity());
                entity.setBomHistoryId(bomHistoryId);
                entity.setProductId(children.getProductId());
                saveBatchList.add(entity);
            }
        }
        if (CollUtil.isEmpty(saveBatchList)) {
            return;
        }
        this.saveBatch(saveBatchList);

        //删除redis
        List<String> parentSkuIdList = saveBatchList.stream().map(ProductBomSkuHistoryEntity::getParentSkuId).distinct().collect(Collectors.toList());
        //删除redis缓存
        for (String parentSkuId : parentSkuIdList) {
            redisUtil.del(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY, parentSkuId));
        }
    }

    @Override
    public List<ProductBomSkuHistoryEntity> getSkuByHistoryIds(List<String> bomHistoryIds) {
        if (CollectionUtils.isNotEmpty(bomHistoryIds)) {
            LambdaQueryWrapper<ProductBomSkuHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(ProductBomSkuHistoryEntity::getBomHistoryId, bomHistoryIds);
            return this.list(queryWrapper);
        }
        return new ArrayList<>();
    }

    @Override
    public void removeByBomSku(BomSkuEntity bomSkuEntity) {
        LambdaQueryWrapper<ProductBomSkuHistoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductBomSkuHistoryEntity::getParentSkuId,bomSkuEntity.getParentSkuId());
        queryWrapper.eq(ProductBomSkuHistoryEntity::getSkuId,bomSkuEntity.getSkuId());
        queryWrapper.eq(ProductBomSkuHistoryEntity::getProductId,bomSkuEntity.getProductId());
        this.remove(queryWrapper);
        //删除redis缓存
        redisUtil.del(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY, bomSkuEntity.getParentSkuId()));
    }

    @Override
    public List<BomChildrenSkuDTO> listHistoryBomChildBySkuIds(List<String> skuIds) {
        List<BomChildrenSkuDTO> resultList = new ArrayList<>();
        if (CollUtil.isEmpty(skuIds)) {
            return Collections.emptyList();
        }
        //未添加缓存的sku
        List<String>unSkuIdList = new ArrayList<>();
        
        for (String skuId : skuIds) {
            //查询redis,没有则添加缓存，有则直接查询
            if (redisUtil.hasKey(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY,skuId))) {
                Object json = redisUtil.get(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY,skuId));
                List<BomChildrenSkuDTO> bomChildrenSkuList = JSON.parseArray((String) json, BomChildrenSkuDTO.class);
                if (CollUtil.isEmpty(bomChildrenSkuList)) {
                    continue;
                }
                resultList.addAll(bomChildrenSkuList);
                continue;
            }
            unSkuIdList.add(skuId);
        }
        if (CollUtil.isNotEmpty(unSkuIdList)) {
            List<BomChildrenSkuDTO> list = baseMapper.listHistoryBomChildBySkuIds(unSkuIdList);
            if (CollUtil.isEmpty(list)) {
                return resultList;
            }
            Map<String, List<BomChildrenSkuDTO>> unSkuMap = list.stream().collect(Collectors.groupingBy(BomChildrenSkuDTO::getParentSkuId));

            for (String unSkuId : unSkuIdList) {
                List<BomChildrenSkuDTO> bomChildrenSkuList = unSkuMap.get(unSkuId);
                redisUtil.set(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY,unSkuId), JSON.toJSONString(bomChildrenSkuList), 0);
                resultList.addAll(bomChildrenSkuList);
            }
        }
        return resultList;
    }

    @Override
    public void removeByHistoryBomIdList(List<String> historyBomIdList) {
        if (CollectionUtils.isEmpty(historyBomIdList)) {
            return;
        }
        List<ProductBomSkuHistoryEntity> bomSkuHistoryList = this.getSkuByHistoryIds(historyBomIdList);
        if (CollUtil.isEmpty(bomSkuHistoryList)) {
            return;
        }
        lambdaUpdate()
                .in(ProductBomSkuHistoryEntity::getBomHistoryId,historyBomIdList)
                .remove();
        //删除redis缓存
        List<String> parentSkuIdList = bomSkuHistoryList.stream().map(ProductBomSkuHistoryEntity::getParentSkuId).distinct().collect(Collectors.toList());
        for (String parentSkuId : parentSkuIdList) {
            redisUtil.del(CharSequenceUtil.format(RedisKeyConstant.CACHE_BOM_SKU_HISTORY, parentSkuId));
        }
    }



}
