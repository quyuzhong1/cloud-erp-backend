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
        List<CfgInvoiceSettingDetailEntity> entityList = baseMapper.selectList(new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>().eq(CfgInvoiceSettingDetailEntity::getMainId, dto.getId()).orderByDesc(CfgInvoiceSettingDetailEntity::getDictPlatform));
        List<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOList = BeanUtil.copyToList(entityList, CfgInvoiceSettingDetailDTO.ViewDTO.class);
        //查询平台value对应
        List<DictBasicDTO.ViewDTO> keyList = dictBasicService.getByKey(dto.getKey());
        if (ObjectUtil.isNotEmpty(dto.getNames())) {
            keyList = keyList.stream().filter(item -> ObjectUtil.isNotEmpty(dto.getNames()) && dto.getNames().contains(item.getValue())).collect(Collectors.toList());
        }
        // 列表为空，第一次点击，返回平台情况即可
        if (ObjectUtil.isEmpty(viewDTOList)) {
            keyList.forEach(item -> {
                CfgInvoiceSettingDetailDTO.ViewDTO viewDTO = new CfgInvoiceSettingDetailDTO.ViewDTO();
                viewDTO.setMainId(dto.getId());
                viewDTO.setPlatformName(item.getName());
                viewDTO.setPlatformValue(item.getValue());
                viewDTOS.add(viewDTO);
            });
            return viewDTOS;
        }
        // 平台value对应名称
        Map<String, String> valueNameMap = keyList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));
        viewDTOList.forEach(item -> {
            item.setPlatformName(valueNameMap.get(item.getDictPlatform()));
            item.setPlatformValue(item.getDictPlatform());
        });
        return viewDTOList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(List<CfgInvoiceSettingDetailDTO.AddDTO> dtoList) {
        if (CollUtil.isEmpty(dtoList)) {
            throw new ServiceException("参数不能为空");
        }
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = validateInvoiceSetting(dtoList.get(0).getMainId());
        // 处理删除逻辑
        handleDeletedDetails(dtoList);
        // 拆分新增 & 更新
        List<CfgInvoiceSettingDetailDTO.AddDTO> createList = dtoList.stream()
                .filter(item -> ObjectUtil.isEmpty(item.getId()))
                .collect(Collectors.toList());
        List<CfgInvoiceSettingDetailDTO.AddDTO> updateList = dtoList.stream()
                .filter(item -> ObjectUtil.isNotEmpty(item.getId()))
                .collect(Collectors.toList());
        // 处理新增
        if (CollUtil.isNotEmpty(createList)) {
            handleCreateDetails(createList, cfgInvoiceSettingEntity.getId());
        }
        // 处理更新
        if (CollUtil.isNotEmpty(updateList)) {
            handleUpdateDetails(updateList);
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】",
                UserContext.getDefaultLoginUser().getUserName(), "发票设置明细", dtoList.get(0).getId());
        operateLogService.addModuleOperateLog(msg,
                ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(),
                dtoList.get(0).getId(), "新增操作");
        return new BaseResultDTO.AddDTO(dtoList.get(0).getId(), dtoList.get(0).getId());
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
        ArrayList<CfgInvoiceSettingDetailDTO.ViewShopDTO> viewShopDTOS = new ArrayList<>();
        shoplist.forEach(item -> {
            CfgInvoiceSettingDetailDTO.ViewShopDTO viewShopDTO = new CfgInvoiceSettingDetailDTO.ViewShopDTO();
            viewShopDTO.setId(item.getId());
            viewShopDTO.setName(item.getDictPlatform());
            viewShopDTO.setValue(item.getName());
            viewShopDTO.setDisabled(item.getDisabled());
            viewShopDTOS.add(viewShopDTO);
        });
        return viewShopDTOS;
    }

    @Override
    public CfgInvoiceSettingDetailEntity getInvoiceSettingDetail(String dictPlatform, String shopId) {
        return baseMapper.getInvoiceSettingDetail(dictPlatform, shopId);
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
    private void handleDeletedDetails(List<CfgInvoiceSettingDetailDTO.AddDTO> dtoList) {
        String mainId = dtoList.get(0).getMainId();
        Set<String> existingIds = this.list(
                        new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>()
                                .eq(CfgInvoiceSettingDetailEntity::getMainId, mainId)
                ).stream()
                .map(CfgInvoiceSettingDetailEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> incomingIds = dtoList.stream()
                .map(CfgInvoiceSettingDetailDTO.AddDTO::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
      * @param: createList，mainId
      * @return: null
      **/
    private void handleCreateDetails(List<CfgInvoiceSettingDetailDTO.AddDTO> createList, String mainId) {
        List<String> incomingShopIds = createList.stream()
                .map(CfgInvoiceSettingDetailDTO.AddDTO::getShopId)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        if (CollUtil.isNotEmpty(incomingShopIds)) {
            long count = super.count(
                    new LambdaQueryWrapper<CfgInvoiceSettingDetailEntity>()
                            .in(CfgInvoiceSettingDetailEntity::getShopId, incomingShopIds)
                            .eq(CfgInvoiceSettingDetailEntity::getMainId, mainId)
            );
            if (count > 0) {
                throw new ServiceException("店铺已存在!请不要重复添加");
            }
        }
        List<CfgInvoiceSettingDetailEntity> entityCreateList = createList.stream()
                .map(item -> {
                    CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(item, CfgInvoiceSettingDetailEntity.class);
                    entity.setDictPlatform(item.getPlatformValue());
                    return entity;
                })
                .collect(Collectors.toList());
        log.info("开始新增发票设置明细");
        if (!super.saveBatch(entityCreateList)) {
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
    private void handleUpdateDetails(List<CfgInvoiceSettingDetailDTO.AddDTO> updateList) {
        List<CfgInvoiceSettingDetailEntity> entityUpdateList = updateList.stream()
                .map(item -> {
                    CfgInvoiceSettingDetailEntity entity = BeanUtil.copyProperties(item, CfgInvoiceSettingDetailEntity.class);
                    entity.setDictPlatform(item.getPlatformValue());
                    return entity;
                })
                .collect(Collectors.toList());
        log.info("开始更新发票设置明细");
        if (!super.updateBatchById(entityUpdateList)) {
            throw new ServiceException("更新发票设置明细失败");
        }
    }
}
