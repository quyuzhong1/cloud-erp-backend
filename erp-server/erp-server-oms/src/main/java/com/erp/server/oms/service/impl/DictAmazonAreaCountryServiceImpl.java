package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.entity.DictAmazonAreaCountryEntity;
import com.erp.server.oms.mapper.DictAmazonAreaCountryMapper;
import com.erp.server.oms.service.DictAmazonAreaCountryService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.DictAmazonAreaCountryDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
 */
@Slf4j
@Service
public class DictAmazonAreaCountryServiceImpl extends SuperServiceImpl<DictAmazonAreaCountryMapper, DictAmazonAreaCountryEntity> implements DictAmazonAreaCountryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(DictAmazonAreaCountryDTO.AddDTO addDTO) {
        DictAmazonAreaCountryEntity dictAmazonAreaCountryEntity = new DictAmazonAreaCountryEntity();
        BeanMapperUtils.copy(addDTO, dictAmazonAreaCountryEntity);

        // 数据处理
        handleData(dictAmazonAreaCountryEntity);

        log.info("开始新增");
        boolean save = super.save(dictAmazonAreaCountryEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "", dictAmazonAreaCountryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictAmazonAreaCountryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return dictAmazonAreaCountryEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictAmazonAreaCountryDTO.UpdateDTO updateDTO) {
        DictAmazonAreaCountryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DictAmazonAreaCountryEntity dictAmazonAreaCountryEntity = BeanMapperUtils.map(DictAmazonAreaCountryEntity.class, updateDTO);

        // 数据处理
        handleData(dictAmazonAreaCountryEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictAmazonAreaCountryEntity);
        if (!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录日志数据，id：【{}】", dictAmazonAreaCountryEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), dictAmazonAreaCountryEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictAmazonAreaCountryEntity, null, dictAmazonAreaCountryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 获取地区
     *
     * @param
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-08-30 9:40
     */
    @Override
    public List<BaseDropDownDTO.CommonDTO> areaList() {
        return baseMapper.areaList();
    }


    /**
     * 根据区域获取国家
     *
     * @param areaList
     * @return java.util.List<com.common.business.dto.base.BaseDropDownDTO.CommonDTO>
     * @author yl
     * @date 2023-08-30 10:02
     */
    @Override
    public List<BaseDropDownDTO.CommonDTO> listCountryByArea(List<String> areaList) {
        if (CollectionUtils.isEmpty(areaList)) {
            return Collections.emptyList();
        }
        return baseMapper.listCountryByArea(areaList);
    }

    /**
     * 根据国家code
     *
     * @param countryCodeList
     * @return java.util.List<com.erp.model.oms.entity.DictAmazonAreaCountryEntity>
     * @author yl
     * @date 2023-08-30 10:21
     */
    @Override
    public List<DictAmazonAreaCountryEntity> listByCountryCodes(List<String> countryCodeList) {
        if (CollectionUtils.isEmpty(countryCodeList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictAmazonAreaCountryEntity::getCountryCode,countryCodeList).list();
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(DictAmazonAreaCountryEntity dictAmazonAreaCountryEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
