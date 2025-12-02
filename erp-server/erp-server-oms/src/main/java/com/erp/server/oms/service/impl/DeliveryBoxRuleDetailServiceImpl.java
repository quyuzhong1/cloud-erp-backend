package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.erp.model.oms.entity.DeliveryBoxRuleEntity;
import com.erp.model.scm.enums.CreatePoTypeEnum;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.DeliveryBoxRuleDetailMapper;
import com.erp.server.oms.service.DeliveryBoxRuleDetailService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@Service
public class DeliveryBoxRuleDetailServiceImpl extends SuperServiceImpl<DeliveryBoxRuleDetailMapper, DeliveryBoxRuleDetailEntity> implements DeliveryBoxRuleDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DeliveryBoxRuleDetailDTO.AddDTO addDTO) {
        DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity = new DeliveryBoxRuleDetailEntity();
        BeanMapperUtils.copy(addDTO, deliveryBoxRuleDetailEntity);

        // 数据处理
        handleData(deliveryBoxRuleDetailEntity);

        log.info("开始新增");
        boolean save = super.save(deliveryBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , deliveryBoxRuleDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, deliveryBoxRuleDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(deliveryBoxRuleDetailEntity.getId(), deliveryBoxRuleDetailEntity.getId());
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DeliveryBoxRuleDetailDTO.UpdateDTO addOrUpdateDTO) {
        DeliveryBoxRuleDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity =  BeanMapperUtils.map(DeliveryBoxRuleDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(deliveryBoxRuleDetailEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(deliveryBoxRuleDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", deliveryBoxRuleDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), deliveryBoxRuleDetailEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, deliveryBoxRuleDetailEntity, null, deliveryBoxRuleDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean save(List<DeliveryBoxRuleDetailDTO.AddDTO> deliveryBoxRuleDetailDTOList, String deliveryBoxRuleId) {
        // 检查sku、单箱数量、优先级否重复
        checkAddDuplicate(deliveryBoxRuleDetailDTOList);

        List<DeliveryBoxRuleDetailEntity> deliveryBoxRuleDetailEntityList = BeanMapperUtils.copyList(DeliveryBoxRuleDetailEntity.class, deliveryBoxRuleDetailDTOList);
        for (DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity : deliveryBoxRuleDetailEntityList) {
            deliveryBoxRuleDetailEntity.setMainId(deliveryBoxRuleId);
            deliveryBoxRuleDetailEntity.setInvalidStatus(InvalidStatusEnum.NOT_VOIDED.getStatus());
        }
        return saveBatch(deliveryBoxRuleDetailEntityList);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(List<DeliveryBoxRuleDetailDTO.UpdateDTO> deliveryBoxRuleDetailDTOList, String deliveryBoxRuleId) {
        // 查询旧数据
        List<DeliveryBoxRuleDetailEntity> oldList = this.lambdaQuery()
                .eq(DeliveryBoxRuleDetailEntity::getMainId, deliveryBoxRuleId)
                .list();

        // 检查sku、单箱数量、优先级否重复
        checkUpdateDuplicate(deliveryBoxRuleDetailDTOList,oldList);

        Map<String, DeliveryBoxRuleDetailEntity> oldMap = oldList.stream()
                .collect(Collectors.toMap(DeliveryBoxRuleDetailEntity::getId, Function.identity()));

        // 新增（id为空）、修改（id存在且数据有变化）
        List<DeliveryBoxRuleDetailDTO.UpdateDTO> addList = new ArrayList<>();
        List<DeliveryBoxRuleDetailDTO.UpdateDTO> updateList = new ArrayList<>();

        for (DeliveryBoxRuleDetailDTO.UpdateDTO dto : deliveryBoxRuleDetailDTOList) {
            if (StringUtils.isBlank(dto.getId())) {
                // 新增
                addList.add(dto);
            } else {
                // 修改
                DeliveryBoxRuleDetailEntity oldEntity = oldMap.get(dto.getId());
                if (oldEntity != null && isDataChanged(oldEntity, dto)) {
                    updateList.add(dto);
                }
            }
        }

        Iterator<Map.Entry<String, DeliveryBoxRuleDetailEntity>> iterator = oldMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, DeliveryBoxRuleDetailEntity> entry = iterator.next();
            DeliveryBoxRuleDetailEntity oldEntity = entry.getValue();

            List<Pair<String, String>> updatePairs = updateList.stream()
                    .filter(newEntity -> newEntity.getId().equals(oldEntity.getId()))
                    .map(newEntity -> new Pair<>(
                            deliveryBoxRuleId,
                            String.format(
                                    "修改发货SKU从【%s】为【%s】，单箱数量从【%s】为【%s】，状态从【%s】为【%s",
                                    oldEntity.getDeliverySkuNo(),
                                    newEntity.getDeliverySkuNo(),
                                    oldEntity.getPerBoxQty(),
                                    newEntity.getPerBoxQty(),
                                    InvalidStatusEnum.getName(oldEntity.getInvalidStatus()),
                                    InvalidStatusEnum.getName(newEntity.getInvalidStatus())
                            )
                    ))
                    .collect(Collectors.toList());

            if (!updatePairs.isEmpty()) {
                operateLogService.batchAddModuleOperateLog(
                        "修改发货SKU从【%s】",
                        ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(),
                        updatePairs,
                        "编辑操作"
                );
            }
        }

        // 记录新增日志
        List<Pair<String, String>> addPairs = addList.stream()
                .map(obj -> new Pair<>(deliveryBoxRuleId,
                        obj.getDeliverySkuNo() + ",单箱数量【" + obj.getPerBoxQty() + "】"
                        ))
                .collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(
                "新增了发货SKU【%s",
                ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(),
                addPairs,
                "新增操作"
        );

        List<DeliveryBoxRuleDetailEntity> newList = BeanMapperUtils.copyList(DeliveryBoxRuleDetailEntity.class, deliveryBoxRuleDetailDTOList);
        for (DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity : newList) {
            deliveryBoxRuleDetailEntity.setMainId(deliveryBoxRuleId);
        }
        boolean success = this.saveOrUpdateBatch(newList);
        if (!success) {
            throw new ServiceException(ApiError.ERROR_BATCH_UPDATE_BOX_RULE);
        }
        return success;
    }

    private void checkAddDuplicate(List<DeliveryBoxRuleDetailDTO.AddDTO> dtoList){
        //发货sku是否有重复值
        Map<String, Long> skuCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.AddDTO::getDeliverySkuNo,
                        Collectors.counting()
                ));

        List<String> duplicateSkus = skuCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(duplicateSkus)) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_SKU,
                    duplicateSkus
            );
        }

        // 单箱数量是否有重复值
        Map<Integer, Long> qtyCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.AddDTO::getPerBoxQty,
                        Collectors.counting()
                ));
        List<Integer> duplicateQtys = qtyCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(duplicateQtys)) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_QTY,
                    duplicateQtys
            );
        }

        // 优先级是否有重复值
        Map<Integer, Long> sortCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.AddDTO::getSort,
                        Collectors.counting()
                ));

        List<Integer> duplicateSorts = sortCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(duplicateSorts)) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_SORT,
                    duplicateSorts
            );
        }
    }

    private void checkUpdateDuplicate(List<DeliveryBoxRuleDetailDTO.UpdateDTO> dtoList, List<DeliveryBoxRuleDetailEntity> oldList) {
        // 区分新增和更新记录
        List<DeliveryBoxRuleDetailDTO.UpdateDTO> newRecords = dtoList.stream()
                .filter(dto -> dto.getId() == null)
                .collect(Collectors.toList());

        List<DeliveryBoxRuleDetailDTO.UpdateDTO> updateRecords = dtoList.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toList());

        // 提取旧记录的字段值用于交叉检查
        Set<String> oldSkus = oldList.stream()
                .map(DeliveryBoxRuleDetailEntity::getDeliverySkuNo)
                .collect(Collectors.toSet());

        Set<Integer> oldQtys = oldList.stream()
                .map(DeliveryBoxRuleDetailEntity::getPerBoxQty)
                .collect(Collectors.toSet());

        Set<Integer> oldSorts = oldList.stream()
                .map(DeliveryBoxRuleDetailEntity::getSort)
                .collect(Collectors.toSet());

        // 检查新增记录内部的重复性和与旧记录的交叉重复性
        if (!newRecords.isEmpty()) {
            // 检查 deliverySkuNo 重复
            Map<String, Long> newSkuCountMap = newRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo,
                            Collectors.counting()
                    ));

            List<String> duplicateNewSkus = newSkuCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 检查新增记录与旧记录的 deliverySkuNo 重复
            List<String> crossDuplicateNewSkus = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo)
                    .filter(oldSkus::contains)
                    .distinct()
                    .collect(Collectors.toList());

            duplicateNewSkus.addAll(crossDuplicateNewSkus);

            if (!duplicateNewSkus.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SKU,
                        String.join("】, 【", duplicateNewSkus)
                );
            }

            // 检查 perBoxQty 重复
            Map<Integer, Long> newQtyCountMap = newRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty,
                            Collectors.counting()
                    ));

            List<Integer> duplicateNewQtys = newQtyCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 检查新增记录与旧记录的 perBoxQty 重复
            List<Integer> crossDuplicateNewQtys = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty)
                    .filter(oldQtys::contains)
                    .distinct()
                    .collect(Collectors.toList());

            duplicateNewQtys.addAll(crossDuplicateNewQtys);

            if (!duplicateNewQtys.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_QTY,
                        String.join("】, 【", duplicateNewQtys.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }

            // 检查 sort 重复
            Map<Integer, Long> newSortCountMap = newRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getSort,
                            Collectors.counting()
                    ));

            List<Integer> duplicateNewSorts = newSortCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            // 检查新增记录与旧记录的 sort 重复
            List<Integer> crossDuplicateNewSorts = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getSort)
                    .filter(oldSorts::contains)
                    .distinct()
                    .collect(Collectors.toList());

            duplicateNewSorts.addAll(crossDuplicateNewSorts);

            if (!duplicateNewSorts.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SORT,
                        String.join("】, 【", duplicateNewSorts.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }
        }

        // 检查更新记录内部的重复性（不需要检查与旧记录的重复，因为是在更新已有记录）
        if (!updateRecords.isEmpty()) {
            // 检查 deliverySkuNo 重复
            Map<String, Long> updateSkuCountMap = updateRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo,
                            Collectors.counting()
                    ));

            List<String> duplicateUpdateSkus = updateSkuCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicateUpdateSkus.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SKU,
                        String.join("】, 【", duplicateUpdateSkus)
                );
            }

            // 检查 perBoxQty 重复
            Map<Integer, Long> updateQtyCountMap = updateRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty,
                            Collectors.counting()
                    ));

            List<Integer> duplicateUpdateQtys = updateQtyCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicateUpdateQtys.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_QTY,
                        String.join("】, 【", duplicateUpdateQtys.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }

            // 检查 sort 重复
            Map<Integer, Long> updateSortCountMap = updateRecords.stream()
                    .collect(Collectors.groupingBy(
                            DeliveryBoxRuleDetailDTO.UpdateDTO::getSort,
                            Collectors.counting()
                    ));

            List<Integer> duplicateUpdateSorts = updateSortCountMap.entrySet().stream()
                    .filter(entry -> entry.getValue() > 1)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!duplicateUpdateSorts.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SORT,
                        String.join("】, 【", duplicateUpdateSorts.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }
        }

        // 最后检查新增记录和更新记录之间是否有重复
        if (!newRecords.isEmpty() && !updateRecords.isEmpty()) {
            // 检查 deliverySkuNo
            Set<String> updateSkus = updateRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo)
                    .collect(Collectors.toSet());

            List<String> crossNewUpdateSkus = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo)
                    .filter(updateSkus::contains)
                    .distinct()
                    .collect(Collectors.toList());

            if (!crossNewUpdateSkus.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SKU,
                        String.join("】, 【", crossNewUpdateSkus)
                );
            }

            // 检查 perBoxQty
            Set<Integer> updateQtys = updateRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty)
                    .collect(Collectors.toSet());

            List<Integer> crossNewUpdateQtys = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty)
                    .filter(updateQtys::contains)
                    .distinct()
                    .collect(Collectors.toList());

            if (!crossNewUpdateQtys.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_QTY,
                        String.join("】, 【", crossNewUpdateQtys.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }

            // 检查 sort
            Set<Integer> updateSorts = updateRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getSort)
                    .collect(Collectors.toSet());

            List<Integer> crossNewUpdateSorts = newRecords.stream()
                    .map(DeliveryBoxRuleDetailDTO.UpdateDTO::getSort)
                    .filter(updateSorts::contains)
                    .distinct()
                    .collect(Collectors.toList());

            if (!crossNewUpdateSorts.isEmpty()) {
                throw new ServiceException(
                        ApiError.ERROR_DUPLICATE_SORT,
                        String.join("】, 【", crossNewUpdateSorts.stream().map(Object::toString).collect(Collectors.toList()))
                );
            }
        }
    }

    private boolean isDataChanged(DeliveryBoxRuleDetailEntity oldEntity, DeliveryBoxRuleDetailDTO.UpdateDTO newDTO) {
        return !Objects.equals(oldEntity.getDeliverySkuNo(), newDTO.getDeliverySkuNo())
                || !Objects.equals(oldEntity.getPerBoxQty(), newDTO.getPerBoxQty())
                || !Objects.equals(oldEntity.getSort(), newDTO.getSort())
                || !Objects.equals(oldEntity.getInvalidStatus(), newDTO.getInvalidStatus());
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DeliveryBoxRuleDetailEntity deliveryBoxRuleDetailEntity) {

    }
}
