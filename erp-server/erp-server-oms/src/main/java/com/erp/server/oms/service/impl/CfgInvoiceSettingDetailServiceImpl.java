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
        ArrayList<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOS = new ArrayList<>();
        //查询详情列表
        List<CfgInvoiceSettingDetailEntity> entityList = baseMapper.selectList(new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>().eq(CfgInvoiceSettingDetailEntity::getMainId, dto.getId())
                .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false).orderByDesc(CfgInvoiceSettingDetailEntity::getDictPlatform));
        // 列表为空，第一次点击，返回平台情况即可
        if (ObjectUtil.isEmpty(entityList)) {
            return viewDTOS;
        }
        //按平台分组
        Map<String, List<CfgInvoiceSettingDetailEntity>> groupedMap = entityList.stream()
                .collect(Collectors.groupingBy(CfgInvoiceSettingDetailEntity::getDictPlatform));
        //查询平台value对应
        List<DictBasicDTO.ViewDTO> keyList = dictBasicService.getByKey(dto.getKey());
        if (ObjectUtil.isNotEmpty(dto.getNames())) {
            keyList = keyList.stream().filter(item -> ObjectUtil.isNotEmpty(dto.getNames()) && dto.getNames().contains(item.getValue()))
                    .collect(Collectors.toList());
        }
        // 平台value => 平台name 映射
        Map<String, String> valueNameMap = keyList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));
        // 遍历分组 map，封装返回结构
        for (Map.Entry<String, List<CfgInvoiceSettingDetailEntity>> entry : groupedMap.entrySet()) {
            String dictPlatform = entry.getKey();
            List<CfgInvoiceSettingDetailEntity> details = entry.getValue();
            CfgInvoiceSettingDetailDTO.ViewDTO viewDTO = new CfgInvoiceSettingDetailDTO.ViewDTO();
            viewDTO.setPlatformValue(dictPlatform);
            // 没查到name就用value兜底
            viewDTO.setPlatformName(valueNameMap.getOrDefault(dictPlatform, dictPlatform));
            List<CfgInvoiceSettingDetailDTO.DetailDTO> detailDTOS = BeanUtil.copyToList(details, CfgInvoiceSettingDetailDTO.DetailDTO.class);
            viewDTO.setDetailDTOList(detailDTOS);
            viewDTOS.add(viewDTO);
        }
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
        handleDeletedDetails(dtoList);
        //新增 & 更新
        handleCreateDetails(dtoList);
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
    public List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop() {
        return baseMapper.selectDetailShop();
    }

    @Override
    public List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList) {
        return baseMapper.listByShopIdList(shopIdList);
    }

    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewShopDTO> listShopSelect(String dictplatform) {
        List<ShopInfoEntity> shoplist = shopInfoService.list(new LambdaQueryWrapper<ShopInfoEntity>().eq(ShopInfoEntity::getDictPlatform, dictplatform));
        //记录已绑定的店铺id
        Set<String> usedShopidList = this.list(new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>().eq(CfgInvoiceSettingDetailEntity::getDictPlatform, dictplatform)
                        .eq(CfgInvoiceSettingDetailEntity::getIsDeleted, false).select(CfgInvoiceSettingDetailEntity::getShopId)).stream()
                .map(CfgInvoiceSettingDetailEntity::getShopId).collect(Collectors.toSet());
        //过滤
        Set<ShopInfoEntity> availableShopList = shoplist.stream()
                .filter(shop -> !usedShopidList.contains(shop.getId()))
                .collect(Collectors.toSet());
        //转换元素
        List<CfgInvoiceSettingDetailDTO.ViewShopDTO> viewShopDTOList = availableShopList.stream()
                .map(item -> {
                    CfgInvoiceSettingDetailDTO.ViewShopDTO dto = new CfgInvoiceSettingDetailDTO.ViewShopDTO();
                    dto.setId(item.getId());
                    dto.setName(item.getDictPlatform());
                    dto.setValue(item.getName());
                    dto.setDisabled(item.getDisabled());
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
    private void handleDeletedDetails(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> dtoList) {
        String mainId = dtoList.get(0).getDetailDTOList().get(0).getMainId();
        //查询已存在的发票设置明细
        Set<String> existingIds = this.list(
                        new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>()
                                .eq(CfgInvoiceSettingDetailEntity::getMainId, mainId)
                ).stream()
                .map(CfgInvoiceSettingDetailEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        //传入的id
        Set<String> incomingIds = dtoList.stream()
                .flatMap(dto -> dto.getDetailDTOList().stream()) // 扁平化处理 detailDTOList
                .map(CfgInvoiceSettingDetailDTO.CommonDTO::getId) // 提取 id
                .filter(ObjectUtil::isNotEmpty) // 过滤空值
                .collect(Collectors.toSet());
        //过滤差集（需要删除的ids）
        existingIds.removeAll(incomingIds);
        if (CollUtil.isNotEmpty(existingIds)) {
            log.info("开始删除以下发票设置明细: {}", existingIds);
            boolean removed = this.lambdaUpdate()
                    .in(CfgInvoiceSettingDetailEntity::getId, existingIds)
                    .remove();
            if (!removed) {
                throw new ServiceException("删除发票设置明细失败");
            }
        }
    }

    /**
     * @description:新增逻辑封装
     * @author: hcg
     * @date: 2025/4/15 12:10
     * @param: createList
     * @return: null
     **/
    private void handleCreateDetails(List<CfgInvoiceSettingDetailDTO.AddOrUpdateDTO> createList) {
        //处理数据
        List<CfgInvoiceSettingDetailEntity> saveList = createList.stream()
                .flatMap(item -> item.getDetailDTOList().stream()
                        .filter(detail -> ObjectUtil.isEmpty(detail.getId()))
                        .map(detail -> {
                            CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(detail, CfgInvoiceSettingDetailEntity.class);
                            entity.setDictPlatform(item.getPlatformValue());
                            return entity;
                        }))
                .collect(Collectors.toList());
        log.info("开始新增发票设置明细");
        if (!super.saveBatch(saveList)) {
            throw new ServiceException("新增发票设置明细失败");
        }
        return;
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
                            entity.setDictPlatform(item.getPlatformValue());
                            return entity;
                        }))
                .collect(Collectors.toList());
        log.info("开始更新发票设置明细");
        if (!super.updateBatchById(saveList)) {
            throw new ServiceException("更新发票设置明细失败");
        }
        return saveList;
    }
}
