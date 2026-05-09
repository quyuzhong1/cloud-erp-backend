package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductRefSkuDTO;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductRefSkuEntity;
import com.erp.model.plm.enums.ProductDetailStatusEnum;
import com.erp.server.plm.mapper.ProductRefSkuMapper;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.ProductDetailService;
import com.erp.server.plm.service.ProductRefSkuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 产品关联sku信息表 服务实现类
 *
 * @author codex
 * @since 2026-04-10
 */
@Slf4j
@Service
public class ProductRefSkuServiceImpl extends SuperServiceImpl<ProductRefSkuMapper, ProductRefSkuEntity> implements ProductRefSkuService {

    private static final String CLASSPATH = String.valueOf(ProductRefSkuEntity.class);

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ProductDetailService productDetailService;

    @Override
    public List<ProductRefSkuDTO> listByProductId(String productId) {
        if (StrUtil.isBlank(productId)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(baseMapper.listByProductId(productId)).orElse(Collections.emptyList());
    }

    @Override
    public List<ProductRefSkuDTO> listBySkuId(String skuId) {
        if (StrUtil.isBlank(skuId)) {
            return Collections.emptyList();
        }
        return Optional.ofNullable(baseMapper.listBySkuId(skuId)).orElse(Collections.emptyList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceNoSpec(String productId, String skuId, List<ProductRefSkuDTO> dtoList) {
        if (StrUtil.isBlank(productId) || StrUtil.isBlank(skuId)) {
            throw new ServiceException("产品或SKU不能为空");
        }
        List<ProductDetailEntity> currentSkuList = productDetailService.listByIds(Collections.singletonList(skuId));
        List<ProductRefSkuDTO> normalizedList = sanitizeInput(Optional.ofNullable(dtoList).orElse(Collections.emptyList()), true, skuId);
        replaceInternal(productId, currentSkuList, normalizedList, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replaceManySpec(String productId, List<ProductDetailEntity> currentSkuList, List<ProductRefSkuDTO> dtoList) {
        if (StrUtil.isBlank(productId)) {
            throw new ServiceException("产品不能为空");
        }
        List<ProductRefSkuDTO> normalizedList = sanitizeInput(Optional.ofNullable(dtoList).orElse(Collections.emptyList()), false, null);
        replaceInternal(productId, currentSkuList, normalizedList, false);
    }

    @Override
    public void removeBySkuIds(List<String> skuIds) {
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        remove(new LambdaQueryWrapper<ProductRefSkuEntity>()
                .and(wrapper -> wrapper.in(ProductRefSkuEntity::getSkuId, skuIds)
                        .or()
                        .in(ProductRefSkuEntity::getRefSkuId, skuIds)));
    }

    @Override
    public void updateProductIdBySkuIds(List<String> skuIds, String productId) {
        if (CollUtil.isEmpty(skuIds) || StrUtil.isBlank(productId)) {
            return;
        }
        update(new LambdaUpdateWrapper<ProductRefSkuEntity>()
                .in(ProductRefSkuEntity::getSkuId, skuIds)
                .set(ProductRefSkuEntity::getProductId, productId));
    }

    private void replaceInternal(String productId, List<ProductDetailEntity> currentSkuList, List<ProductRefSkuDTO> dtoList, boolean noSpec) {
        if (CollectionUtils.isEmpty(currentSkuList)) {
            if (CollectionUtils.isEmpty(dtoList)) {
                remove(new LambdaQueryWrapper<ProductRefSkuEntity>().eq(ProductRefSkuEntity::getProductId, productId));
                return;
            }
            throw new ServiceException("当前产品不存在可关联的SKU");
        }
        Map<String, ProductDetailEntity> ownerSkuMap = buildOwnerSkuMap(productId, currentSkuList);

        List<ProductRefSkuEntity> dbList = lambdaQuery()
                .eq(ProductRefSkuEntity::getProductId, productId)
                .list();
        Map<String, ProductRefSkuEntity> dbMap = dbList.stream()
                .collect(Collectors.toMap(ProductRefSkuEntity::getId, Function.identity(), (v1, v2) -> v2));

        validateRows(productId, dtoList, ownerSkuMap, dbMap, noSpec);

        Set<String> refSkuIdSet = dtoList.stream()
                .map(ProductRefSkuDTO::getRefSkuId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        Map<String, ProductDetailEntity> refSkuMap = loadRefSkuMap(refSkuIdSet);

        Set<String> keepIdSet = dtoList.stream()
                .map(ProductRefSkuDTO::getId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        List<ProductRefSkuEntity> deleteList = dbList.stream()
                .filter(item -> !keepIdSet.contains(item.getId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteList)) {
            removeByIds(deleteList.stream().map(ProductRefSkuEntity::getId).collect(Collectors.toList()));
        }

        List<ProductRefSkuEntity> saveList = new ArrayList<>(dtoList.size());
        for (int i = 0; i < dtoList.size(); i++) {
            ProductRefSkuDTO dto = dtoList.get(i);
            ProductRefSkuEntity entity = StringUtils.isNotBlank(dto.getId()) ? dbMap.get(dto.getId()) : new ProductRefSkuEntity();
            if (entity == null) {
                throw new ServiceException(String.format("第%s行关联SKU数据不存在或已变更", i + 1));
            }
            entity.setProductId(productId);
            entity.setSkuId(dto.getSkuId());
            entity.setRefSkuId(dto.getRefSkuId());
            entity.setRemark(StringUtils.defaultString(dto.getRemark()));
            entity.setSort(Objects.nonNull(dto.getSort()) ? dto.getSort() : i + 1);
            saveList.add(entity);
        }
        if (CollectionUtils.isNotEmpty(saveList)) {
            saveOrUpdateBatch(saveList);
        }

        writeOperateLogs(productId, ownerSkuMap, refSkuMap, dbMap, deleteList, saveList);
    }

    private Map<String, ProductDetailEntity> buildOwnerSkuMap(String productId, List<ProductDetailEntity> currentSkuList) {
        if (CollectionUtils.isEmpty(currentSkuList)) {
            throw new ServiceException("当前产品不存在可关联的SKU");
        }
        Map<String, ProductDetailEntity> ownerSkuMap = currentSkuList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNotBlank(item.getId()))
                .collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity(), (v1, v2) -> v2));
        if (ownerSkuMap.isEmpty()) {
            throw new ServiceException("当前产品不存在可关联的SKU");
        }
        long invalidCount = ownerSkuMap.values().stream()
                .filter(item -> !StringUtils.equals(productId, item.getProductId()))
                .count();
        if (invalidCount > 0) {
            throw new ServiceException("当前SKU不属于当前产品");
        }
        return ownerSkuMap;
    }

    /**
     * 只保留关联关系真正需要的字段，展示字段均忽略。
     */
    private List<ProductRefSkuDTO> sanitizeInput(List<ProductRefSkuDTO> dtoList, boolean noSpec, String defaultSkuId) {
        if (CollectionUtils.isEmpty(dtoList)) {
            return dtoList;
        }
        List<ProductRefSkuDTO> result = new ArrayList<>(dtoList.size());
        for (ProductRefSkuDTO item : dtoList) {
            ProductRefSkuDTO dto = new ProductRefSkuDTO();
            dto.setId(item.getId());
            dto.setSkuId(noSpec ? defaultSkuId : item.getSkuId());
            dto.setRefSkuId(item.getRefSkuId());
            dto.setRemark(item.getRemark());
            dto.setSort(item.getSort());
            result.add(dto);
        }
        return result;
    }

    private void validateRows(String productId, List<ProductRefSkuDTO> dtoList, Map<String, ProductDetailEntity> ownerSkuMap,
                              Map<String, ProductRefSkuEntity> dbMap, boolean noSpec) {
        Set<String> uniqueKeySet = new HashSet<>();
        Set<String> refSkuIdSet = new HashSet<>();
        for (int i = 0; i < dtoList.size(); i++) {
            ProductRefSkuDTO dto = dtoList.get(i);
            int rowNum = i + 1;
            if (StringUtils.isBlank(dto.getRefSkuId())) {
                throw new ServiceException(String.format("第%s行关联SKU不能为空", rowNum));
            }
            if (StringUtils.length(dto.getRemark()) > 200) {
                throw new ServiceException(String.format("第%s行备注最大200字符", rowNum));
            }
            if (StringUtils.isBlank(dto.getSkuId())) {
                throw new ServiceException(String.format("第%s行SKU不能为空", rowNum));
            }
            ProductDetailEntity ownerSku = ownerSkuMap.get(dto.getSkuId());
            if (ownerSku == null) {
                throw new ServiceException(noSpec ? String.format("第%s行当前SKU与页面SKU不一致", rowNum) : String.format("第%s行SKU不属于当前产品", rowNum));
            }
            if (!StringUtils.equals(productId, ownerSku.getProductId())) {
                throw new ServiceException(String.format("第%s行SKU不属于当前产品", rowNum));
            }
            if (StringUtils.equals(dto.getSkuId(), dto.getRefSkuId())) {
                throw new ServiceException(String.format("第%s行当前SKU不能关联自己", rowNum));
            }
            String uniqueKey = dto.getSkuId() + "_" + dto.getRefSkuId();
            if (!uniqueKeySet.add(uniqueKey)) {
                throw new ServiceException(String.format("第%s行关联SKU重复", rowNum));
            }
            if (StringUtils.isNotBlank(dto.getId()) && !dbMap.containsKey(dto.getId())) {
                throw new ServiceException(String.format("第%s行关联SKU数据不存在或已变更", rowNum));
            }
            refSkuIdSet.add(dto.getRefSkuId());
        }

        Map<String, ProductDetailEntity> refSkuMap = loadRefSkuMap(refSkuIdSet);
        for (int i = 0; i < dtoList.size(); i++) {
            ProductRefSkuDTO dto = dtoList.get(i);
            ProductDetailEntity refSku = refSkuMap.get(dto.getRefSkuId());
            if (refSku == null) {
                throw new ServiceException(String.format("第%s行关联SKU不存在", i + 1));
            }
            if (!Objects.equals(refSku.getStatus(), ProductDetailStatusEnum.APPROVAL_PASS.getCode())) {
                throw new ServiceException(String.format("第%s行关联SKU未审核通过", i + 1));
            }
        }
    }

    private Map<String, ProductDetailEntity> loadRefSkuMap(Set<String> refSkuIdSet) {
        if (CollectionUtils.isEmpty(refSkuIdSet)) {
            return Collections.emptyMap();
        }
        List<ProductDetailEntity> refSkuList = productDetailService.listByIds(new ArrayList<>(refSkuIdSet));
        return refSkuList.stream().collect(Collectors.toMap(ProductDetailEntity::getId, Function.identity(), (v1, v2) -> v2));
    }

    private void writeOperateLogs(String productId, Map<String, ProductDetailEntity> ownerSkuMap, Map<String, ProductDetailEntity> refSkuMap,
                                  Map<String, ProductRefSkuEntity> dbMap, List<ProductRefSkuEntity> deleteList, List<ProductRefSkuEntity> saveList) {
        List<String> oldRefSkuIds = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deleteList)) {
            oldRefSkuIds.addAll(deleteList.stream().map(ProductRefSkuEntity::getRefSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        }
        oldRefSkuIds.addAll(dbMap.values().stream().map(ProductRefSkuEntity::getRefSkuId).filter(StringUtils::isNotBlank).collect(Collectors.toList()));
        Map<String, ProductDetailEntity> oldRefSkuMap = loadRefSkuMap(new HashSet<>(oldRefSkuIds));

        List<OperateLogEntity> logs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(deleteList)) {
            for (ProductRefSkuEntity item : deleteList) {
                ProductDetailEntity ownerSku = ownerSkuMap.get(item.getSkuId());
                ProductDetailEntity refSku = oldRefSkuMap.get(item.getRefSkuId());
                logs.add(buildLog(item.getSkuId(), productId,
                        String.format("删除关联SKU，当前SKU【%s】删除关联SKU【%s】",
                                ownerSku == null ? "" : ownerSku.getSkuNo(),
                                refSku == null ? "" : refSku.getSkuNo()),
                        "删除操作"));
            }
        }

        if (CollectionUtils.isNotEmpty(saveList)) {
            for (ProductRefSkuEntity item : saveList) {
                ProductRefSkuEntity oldEntity = StringUtils.isNotBlank(item.getId()) ? dbMap.get(item.getId()) : null;
                ProductDetailEntity ownerSku = ownerSkuMap.get(item.getSkuId());
                ProductDetailEntity refSku = refSkuMap.get(item.getRefSkuId());
                if (oldEntity == null) {
                    logs.add(buildLog(item.getSkuId(), productId,
                            String.format("新增关联SKU，当前SKU【%s】关联SKU【%s】",
                                    ownerSku == null ? "" : ownerSku.getSkuNo(),
                                    refSku == null ? "" : refSku.getSkuNo()),
                            "新增信息"));
                    continue;
                }
                if (!StringUtils.equals(oldEntity.getSkuId(), item.getSkuId())
                        || !StringUtils.equals(oldEntity.getRefSkuId(), item.getRefSkuId())
                        || !StringUtils.equals(StringUtils.defaultString(oldEntity.getRemark()), StringUtils.defaultString(item.getRemark()))
                        || !Objects.equals(oldEntity.getSort(), item.getSort())) {
                    ProductDetailEntity oldOwnerSku = ownerSkuMap.get(oldEntity.getSkuId());
                    ProductDetailEntity oldRefSku = oldRefSkuMap.get(oldEntity.getRefSkuId());
                    logs.add(buildLog(item.getSkuId(), productId,
                            String.format("编辑关联SKU，当前SKU【%s】由关联SKU【%s】变更为【%s】",
                                    oldOwnerSku == null ? "" : oldOwnerSku.getSkuNo(),
                                    oldRefSku == null ? "" : oldRefSku.getSkuNo(),
                                    refSku == null ? "" : refSku.getSkuNo()),
                            "编辑操作"));
                }
            }
        }
        if (CollectionUtils.isNotEmpty(logs)) {
            operateLogService.addSysLogByBatchSave(logs);
        }
    }

    private OperateLogEntity buildLog(String skuId, String productId, String content, String operation) {
        return new OperateLogEntity()
                .setClassPath(CLASSPATH)
                .setBusinessId(skuId)
                .setPid(productId)
                .setContent(content)
                .setOperation(operation);
    }
}
