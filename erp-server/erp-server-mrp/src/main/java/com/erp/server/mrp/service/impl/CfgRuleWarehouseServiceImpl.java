package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleWarehouseDTO;
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseEntity;
import com.erp.model.mrp.enums.CfgRuleWarehouseTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleWarehouseMapper;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
import com.erp.server.mrp.service.CfgRuleWarehouseService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 仓库（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-24
 */
@Slf4j
@Service
public class CfgRuleWarehouseServiceImpl extends SuperServiceImpl<CfgRuleWarehouseMapper, CfgRuleWarehouseEntity> implements CfgRuleWarehouseService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleWarehouseDetailService cfgRuleWarehouseDetailService;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWarehouseDTO.UpdateDTO updateDTO) {
        CfgRuleWarehouseEntity cfgRuleWarehouseEntity =  BeanMapperUtils.map(CfgRuleWarehouseEntity.class, updateDTO);
        //旧数据
        CfgRuleWarehouseEntity old = super.getById(updateDTO.getId());
        if (ObjectUtil.isEmpty(old)) {
            cfgRuleWarehouseEntity.setId(old.getId());
        }

        // 数据处理
        handleData(cfgRuleWarehouseEntity);
        log.info("编辑 开始修改仓库（规则设置）数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWarehouseEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）保存失败");
        }

        //添加本地仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgLocalWarehouseList(),cfgRuleWarehouseEntity.getId(), CfgRuleWarehouseTypeEnum.LOCAL.getCode());
        //添加海外仓设置
        cfgRuleWarehouseDetailService.update(updateDTO.getCfgOverseasWarehouseList(),cfgRuleWarehouseEntity.getId(),CfgRuleWarehouseTypeEnum.OVERSEAS.getCode());

        // 记录主单操作日志
        log.info("编辑 开始记录仓库（规则设置）日志数据，id：【{}】", cfgRuleWarehouseEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWarehouseEntity.getId(), "仓库（规则设置）");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWarehouseEntity, ModuleTypeEnum.CFG_RULE_COMMON.getCode(), cfgRuleWarehouseEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleWarehouseDTO.ViewDTO view(String platformType) {
        CfgRuleWarehouseDTO.ViewDTO viewDTO = new CfgRuleWarehouseDTO.ViewDTO();
        CfgRuleWarehouseEntity oldEntity = this.getByPlatformType(platformType);
        if (ObjectUtil.isEmpty(oldEntity)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);
        //仓库设置明细
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgRuleWarehouseDetailList = cfgRuleWarehouseDetailService.listViewByMainIdList(Arrays.asList(oldEntity.getId()));
        //本地仓设置
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgLocalWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.LOCAL.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfgLocalWarehouseList)) {
            viewDTO.setCfgLocalWarehouseList(cfgLocalWarehouseList);
        }
        //海外仓设置
        List<CfgRuleWarehouseDetailDTO.ViewDTO> cfgOverseasWarehouseList = cfgRuleWarehouseDetailList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseType(), CfgRuleWarehouseTypeEnum.OVERSEAS.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(cfgOverseasWarehouseList)) {
            viewDTO.setCfgOverseasWarehouseList(cfgOverseasWarehouseList);
        }
        handleCfgRuleWarehouseView(viewDTO);
        return viewDTO;
    }


    /**
     * 根据平台类型查询
     * @author will
     * @date 2024/8/24 15:50
     * @param platformType
     * @return CfgRuleWarehouseEntity
     */
    @Override
    public CfgRuleWarehouseEntity getByPlatformType (String platformType) {
        return lambdaQuery().eq(CfgRuleWarehouseEntity::getPlatformType,platformType).last("limit 1").one();
    }


    /**
     * 处理仓库配置查看数据
     * @author will
     * @date 2024/8/24 16:26
     * @param viewDTO
     */
    private void handleCfgRuleWarehouseView (CfgRuleWarehouseDTO.ViewDTO viewDTO) {

    }

    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWarehouseEntity cfgRuleWarehouseEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
