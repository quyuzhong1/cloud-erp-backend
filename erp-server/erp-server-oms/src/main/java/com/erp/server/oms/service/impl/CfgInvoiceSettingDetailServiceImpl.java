package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgInvoiceSettingDetailMapper;
import com.erp.server.oms.service.*;

import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 发票设置明细 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-04-09
 */
@Slf4j
@Service
public class CfgInvoiceSettingDetailServiceImpl extends SuperServiceImpl<CfgInvoiceSettingDetailMapper, CfgInvoiceSettingDetailEntity> implements CfgInvoiceSettingDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    DictBasicService dictBasicService;

    @Resource
    CfgInvoiceSettingService cfgInvoiceSettingService;

    @Resource
    ShopInfoService shopInfoService;

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDTO> view(CfgInvoiceSettingDetailDTO.ViewParamsDTO dto) {
        List<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOS = baseMapper.selectDetailsByMainIdGroupByPlatformWithRatioAdjusted(
                dto.getId(),
                dto.getKey(),
                dto.getNames()
        );
        return viewDTOS;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> dtoList) {
        // 参数校验
        if (CollUtil.isEmpty(dtoList)) {
            throw new ServiceException("参数不能为空");
        }
        // 校验主表是否存在
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = validateInvoiceSetting(dtoList.get(0).getDetailDTOList().get(0).getMainId());
        // 处理删除逻辑
        Set<String> idsWithChangedShopIdSet = handleDeletedDetails(dtoList);
        //新增 & 更新
        handleCreateDetails(dtoList,idsWithChangedShopIdSet);
        List<CfgInvoiceSettingDetailEntity> updateList = handleUpdateDetails(dtoList);
        // 操作日志
        if (ObjectUtil.isNotEmpty(updateList)) {
            List<Pair<String, String>> pairList = updateList.stream().map(obj -> new Pair<>(obj.getId(), "")).collect(Collectors.toList());
            String msg = StrUtil.format("用户【{}】修改【{}】绑定店铺id为【{}】",
                    UserContext.getDefaultLoginUser().getUserName(), "发票设置明细", updateList.stream().map(CfgInvoiceSettingDetailEntity::getId).collect(Collectors.toList()));
            operateLogService.batchAddModuleOperateLog(msg,
                    ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(), pairList, "新增操作");
        }
        return new BaseResultDTO.AddDTO(dtoList.get(0).getDetailDTOList().get(0).getMainId(), dtoList.get(0).getDetailDTOList().get(0).getMainId());
    }

    @Override
    public List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList) {
        return baseMapper.listByShopIdList(shopIdList);
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewShopDTO> listShopSelect(String dictplatform) {
        List<ShopInfoEntity> shoplist = shopInfoService.list(new LambdaQueryWrapper<ShopInfoEntity>().eq(ShopInfoEntity::getDictPlatform, dictplatform)
                .eq(ShopInfoEntity::getIsDeleted, false).select(ShopInfoEntity::getId, ShopInfoEntity::getName,ShopInfoEntity::getDictPlatform, ShopInfoEntity::getDisabled));
        //记录已绑定的店铺id
        Set<String> usedShopidList = this.list(new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>().eq(CfgInvoiceSettingDetailEntity::getDictPlatform, dictplatform)
                        .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false).select(CfgInvoiceSettingDetailEntity::getShopId)).stream()
                .map(CfgInvoiceSettingDetailEntity::getShopId).collect(Collectors.toSet());
        // 转换为 ViewShopDTO，并设置 disabled 字段
        List<CfgInvoiceSettingDetailDTO.ViewShopDTO> viewShopDTOList = shoplist.stream()
                .map(shop -> {
                    CfgInvoiceSettingDetailDTO.ViewShopDTO dto = new CfgInvoiceSettingDetailDTO.ViewShopDTO();
                    dto.setId(shop.getId());
                    dto.setName(shop.getDictPlatform());
                    dto.setValue(shop.getName());
                    //场景
                    dto.setDisabled(Boolean.TRUE.equals(shop.getDisabled()) || usedShopidList.contains(shop.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
        return viewShopDTOList;
    }

    @Override
    public CfgInvoiceSettingDetailEntity getInvoiceSettingDetail(String dictPlatform, String shopId) {
        return baseMapper.getInvoiceSettingDetail(dictPlatform, shopId);
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO> listDictSelect(CfgInvoiceSettingDetailDTO.ParamsDictPlatformDTO dto) {
        List<DictBasicDTO.ViewDTO> platformList = dictBasicService.getByKey(dto.getKey());
        List<DictBasicDTO.ViewDTO> filtrationDictList = platformList.stream()
                .filter(p -> dto.getNames().contains(p.getValue()))
                .collect(Collectors.toList());
        List<CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO> viewDictPlatformDTOList = BeanUtil.copyToList(filtrationDictList, CfgInvoiceSettingDetailDTO.ViewDictPlatformDTO.class);
        return viewDictPlatformDTOList;
    }

    @Override
    public void delateByMainIds(List<String> ids, Boolean aTrue) {
        boolean remove = this.lambdaUpdate().in(CfgInvoiceSettingDetailEntity::getMainId, ids).remove();
        return;
    }

    /**
     * @description: 校验发票设置是否存在
     * @author: hcg
     * @date: 2025/4/15 12:08
     * @param: mainId
     * @return: null
     **/
    private CfgInvoiceSettingEntity validateInvoiceSetting(String mainId) {
        CfgInvoiceSettingEntity entity = cfgInvoiceSettingService.getById(mainId);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException("发票设置不存在");
        }
        return entity;
    }

    /**
     * @description:删除逻辑封装
     * @author: hcg
     * @date: 2025/4/15 12:09
     * @param: dtoList
     * @return: null
     **/
    private Set<String> handleDeletedDetails(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> dtoList) {
        String mainId = dtoList.get(0).getDetailDTOList().get(0).getMainId();
        List<CfgInvoiceSettingDetailEntity> detailEntityList = this.list(
                new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>()
                        .eq(CfgInvoiceSettingDetailEntity::getMainId, mainId).eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false)
        );
        // 构建现有记录的 Map<id, entity>
        Map<String, CfgInvoiceSettingDetailEntity> existingMap = detailEntityList.stream()
                .filter(item -> ObjectUtil.isNotEmpty(item.getId())) // 排除 id 为空
                .collect(Collectors.toMap(
                item -> item.getId(),
                item -> item,
                (a, b) -> a // 保留第一个
        ));
        // 已存在的 id 列表
        Set<String> existingIds = existingMap.keySet();
        //传入的id
        Set<String> incomingIds = dtoList.stream()
                .flatMap(dto -> dto.getDetailDTOList().stream()) // 扁平化处理 detailDTOList
                .map(CfgInvoiceSettingDetailDTO.CommonDTO::getId) // 提取 id
                .filter(ObjectUtil::isNotEmpty) // 过滤空值
                .collect(Collectors.toSet());
        // 第一种：差集（原有但传入中没有的）
        Set<String> idsToDelete = new HashSet<>(existingIds);
        idsToDelete.removeAll(incomingIds);
        // 第二种：传入中有，但 shopId 与原记录不一致的
        Set<String> idsWithChangedShopIdSet = dtoList.stream()
                .flatMap(dto -> dto.getDetailDTOList().stream())
                .filter(detailDTO -> StrUtil.isNotEmpty(detailDTO.getId()))
                .filter(detailDTO -> {
                    CfgInvoiceSettingDetailEntity existing = existingMap.get(detailDTO.getId());
                    return existing != null && !Objects.equals(existing.getShopId(), detailDTO.getShopId());
                })
                .map(CfgInvoiceSettingDetailDTO.CommonDTO::getId)
                .collect(Collectors.toSet());
        // 将两种情况合并
        idsToDelete.addAll(idsWithChangedShopIdSet);
        if (CollUtil.isNotEmpty(idsToDelete)) {
            log.info("开始删除以下发票设置明细: {}", idsToDelete);
            boolean removed = this.lambdaUpdate()
                    .in(CfgInvoiceSettingDetailEntity::getId, idsToDelete)
                    .remove();
            if (!removed) {
                throw new ServiceException("删除发票设置明细失败");
            }
        }
        return idsWithChangedShopIdSet;
    }

    /**
     * @description:新增逻辑封装
     * @author: hcg
     * @date: 2025/4/15 12:10
     * @param: createList
     * @return: null
     **/
    private void handleCreateDetails(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> createList,
                                     Set<String> idsWithChangedShopIdSet) {
        // 处理数据
        List<CfgInvoiceSettingDetailEntity> saveList = createList.stream()
                .flatMap(item -> item.getDetailDTOList().stream()
                        .filter(detail -> ObjectUtil.isEmpty(detail.getId()) || (ObjectUtil.isNotEmpty(idsWithChangedShopIdSet) && idsWithChangedShopIdSet.contains(detail.getId())))
                        .map(detail -> {
                            // 如果 id 在变更集合中，则置空
                            if (ObjectUtil.isNotEmpty(idsWithChangedShopIdSet) && idsWithChangedShopIdSet.contains(detail.getId())) {
                                detail.setId(null);
                            }
                            CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(detail, CfgInvoiceSettingDetailEntity.class);
                            entity.setRatio((entity.getRatio() == null ? BigDecimal.ZERO : entity.getRatio()).divide(new BigDecimal("100")));
                            entity.setDictPlatform(item.getPlatformValue());
                            return entity;
                        }))
                .collect(Collectors.toList());
        log.info("开始新增发票设置明细，共 {} 条", saveList.size());
        if (CollUtil.isNotEmpty(saveList) && !super.saveBatch(saveList)) {
            throw new ServiceException("新增发票设置明细失败");
        }
    }


    /**
     * @description:更新逻辑封装
     * @author: hcg
     * @date: 2025/4/15 12:10
     * @param: updateList
     * @return: null
     **/
    private List<CfgInvoiceSettingDetailEntity> handleUpdateDetails(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> updateList) {
        //处理数据
        List<CfgInvoiceSettingDetailEntity> saveList = updateList.stream()
                .flatMap(item -> item.getDetailDTOList().stream()
                        .filter(detail -> ObjectUtil.isNotEmpty(detail.getId()))
                        .map(detail -> {
                            CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(detail, CfgInvoiceSettingDetailEntity.class);
                            entity.setRatio((entity.getRatio() == null ? BigDecimal.ZERO : entity.getRatio()).divide(new BigDecimal("100")));
                            entity.setDictPlatform(item.getPlatformValue());
                            return entity;
                        }))
                .collect(Collectors.toList());
        log.info("开始更新发票设置明细");
        if (ObjectUtil.isNotEmpty(saveList) &&!super.updateBatchById(saveList)) {
            throw new ServiceException("更新发票设置明细失败");
        }
        return saveList;
    }
}
