package com.erp.server.oms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
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
import java.util.Collections;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.enums.ApiError;


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
        List<CfgInvoiceSettingDetailDTO.ViewDTO> viewDTOList = baseMapper.selectDetailDict(dto.getId());
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
        return viewDTOList;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(CfgInvoiceSettingDetailDTO.AddDTO addDTO) {
        CfgInvoiceSettingEntity cfgInvoiceSettingEntity = cfgInvoiceSettingService.getById(addDTO.getMainId());
        if (ObjectUtil.isEmpty(cfgInvoiceSettingEntity)){
            throw new ServiceException("发票设置不存在");
        }
        CfgInvoiceSettingDetailEntity cfgInvoiceSettingDetailEntity = new CfgInvoiceSettingDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgInvoiceSettingDetailEntity);
        cfgInvoiceSettingDetailEntity.setDictPlatform(addDTO.getPlatformValue());
        //保存平台value
        cfgInvoiceSettingDetailEntity.setDictPlatform(addDTO.getPlatformValue());
        log.info("开始新增发票设置明细");
        boolean save = super.saveOrUpdate(cfgInvoiceSettingDetailEntity);
        if (!save) {
            throw new ServiceException("发票设置明细保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票设置明细", cfgInvoiceSettingDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(), cfgInvoiceSettingDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgInvoiceSettingDetailEntity.getId(), cfgInvoiceSettingDetailEntity.getId());
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
            viewShopDTOS.add(viewShopDTO);
        });
        return viewShopDTOS;
    }
}
