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
        checkUpdateDuplicate(deliveryBoxRuleDetailDTOList);

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
                                    "【%s】为【%s】，单箱数量从【%s】为【%s】，状态从【%s】为【%s】",
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
                        "修改发货SKU从%s",
                        ModuleTypeEnum.DELIVERY_BOX_RULE.getCode(),
                        updatePairs,
                        "编辑操作"
                );
            }
        }

        // 记录新增日志
        List<Pair<String, String>> addPairs = addList.stream()
                .map(obj -> new Pair<>(deliveryBoxRuleId,
                        "【" + obj.getDeliverySkuNo() + "】,单箱数量【" + obj.getPerBoxQty() + "】"
                        ))
                .collect(Collectors.toList());
        operateLogService.batchAddModuleOperateLog(
                "新增了发货SKU %s",
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

    private void checkUpdateDuplicate(List<DeliveryBoxRuleDetailDTO.UpdateDTO> dtoList) {
        // 检查发货sku重复
        Map<String, Long> deliveryCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.UpdateDTO::getDeliverySkuNo,
                        Collectors.counting()
                ));
        List<String> duplicateDeliveries = deliveryCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 检查单箱数量重复
        Map<Integer, Long> perBoxQtyCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.UpdateDTO::getPerBoxQty,
                        Collectors.counting()
                ));
        List<Integer> duplicatePerBoxQtys = perBoxQtyCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 检查优先级重复
        Map<Integer, Long> sortCountMap = dtoList.stream()
                .collect(Collectors.groupingBy(
                        DeliveryBoxRuleDetailDTO.UpdateDTO::getSort,
                        Collectors.counting()
                ));
        List<Integer> duplicateSorts = sortCountMap.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (!duplicateDeliveries.isEmpty()) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_SKU,
                    String.join("】, 【", duplicateDeliveries.toString())
            );
        }
        if (!duplicatePerBoxQtys.isEmpty()) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_QTY,
                    String.join("】, 【", duplicatePerBoxQtys.toString())
            );
        }
        if (!duplicateSorts.isEmpty()) {
            throw new ServiceException(
                    ApiError.ERROR_DUPLICATE_SORT,
                    String.join("】, 【", duplicateSorts.toString())
            );
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
