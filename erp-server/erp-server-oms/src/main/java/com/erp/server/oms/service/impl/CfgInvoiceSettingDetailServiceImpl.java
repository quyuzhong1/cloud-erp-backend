package com.erp.server.oms.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.CfgInvoiceSettingDetailDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.CfgInvoiceSettingDetailEntity;
import com.erp.model.oms.entity.CfgInvoiceSettingEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.mapper.CfgInvoiceSettingDetailMapper;
import com.erp.server.oms.service.CfgInvoiceSettingDetailService;
import com.erp.server.oms.service.CfgInvoiceSettingService;
import com.erp.server.oms.service.DictBasicService;
import com.erp.server.oms.service.OperateLogService;

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
    public BaseResultDTO.AddDTO add(CfgInvoiceSettingDetailDTO.AddDTO addDTO) {
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
        boolean save = super.save(cfgInvoiceSettingDetailEntity);
        if (!save) {
            throw new ServiceException("发票设置明细保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "发票设置明细", cfgInvoiceSettingDetailEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(), cfgInvoiceSettingDetailEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(cfgInvoiceSettingDetailEntity.getId(), cfgInvoiceSettingDetailEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgInvoiceSettingDetailDTO.UpdateDTO updateDTO) {
        CfgInvoiceSettingDetailEntity old = super.getById(updateDTO.getId());
        if (ObjectUtil.isEmpty(old)){
            throw new ServiceException("发票设置明细不存在");
        }
        if (!old.getMainId().equals(updateDTO.getMainId())){
            throw new ServiceException("不允许修改对应的设置id");
        }

        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "发票设置明细"));
        CfgInvoiceSettingDetailEntity cfgInvoiceSettingDetailEntity = BeanMapperUtils.map(CfgInvoiceSettingDetailEntity.class, updateDTO);
        log.info("编辑 开始修改发票设置明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgInvoiceSettingDetailEntity);
        if (!save) {
            throw new ServiceException("发票设置明细修改失败");
        }
        log.info("编辑 开始记录发票设置明细日志数据，id：【{}】", cfgInvoiceSettingDetailEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgInvoiceSettingDetailEntity.getId(), "发票设置明细");
        operateLogService.addModuleOperateLogByObj(old, cfgInvoiceSettingDetailEntity, ModuleTypeEnum.INVOICE_SETTING_DETAIL.getCode(), cfgInvoiceSettingDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public List<CfgInvoiceSettingDetailDTO.ViewDetailShop> getDetailShop() {
        return baseMapper.selectDetailShop();
    }

    @Override
    public List<CfgInvoiceSettingDetailEntity> listByShopIdList(List<String> shopIdList) {
        return baseMapper.listByShopIdList(shopIdList);
    }
}
