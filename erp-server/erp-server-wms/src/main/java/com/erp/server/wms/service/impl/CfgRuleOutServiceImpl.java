package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.oms.entity.CfgRuleOrderHandleEntity;
import com.erp.model.wms.entity.CfgRuleOutEntity;
import com.erp.model.wms.enums.CfgRuleOutTypeEnum;
import com.erp.server.wms.mapper.CfgRuleOutMapper;
import com.erp.server.wms.service.CfgRuleOutService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 出库配置规则 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-06-28
 */
@Slf4j
@Service
public class CfgRuleOutServiceImpl extends SuperServiceImpl<CfgRuleOutMapper, CfgRuleOutEntity> implements CfgRuleOutService {

    @Resource
    private CfgRuleOutService service;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO addOrUpdate(CfgRuleOutDTO.CommonDTO commonDTO) {
        //处理规则详情
        CfgRuleOutEntity equipmentSortingPortEntity = new CfgRuleOutEntity();
        equipmentSortingPortEntity.setType(CfgRuleOutTypeEnum.EQUIPMENT_SORTING_PORT.getCode());
        Map<String, Object> equipmentSortingPortMap = BeanUtil.beanToMap(commonDTO.getEquipmentSortingPortDTO());
        equipmentSortingPortEntity.setRuleContent(equipmentSortingPortMap);
        CfgRuleOutEntity b2cAllowableDeviationsEntity = new CfgRuleOutEntity();
        b2cAllowableDeviationsEntity.setType(CfgRuleOutTypeEnum.B2C_ALLOWABLE_DEVIATIONS.getCode());
        Map<String, Object> b2cAllowableDeviationsMap = BeanUtil.beanToMap(commonDTO.getB2cAllowableDeviations());
        b2cAllowableDeviationsEntity.setRuleContent(b2cAllowableDeviationsMap);
        //删除数据后再保存
        service.remove(new QueryWrapper<>());
        service.saveBatch(Arrays.asList(equipmentSortingPortEntity, b2cAllowableDeviationsEntity));
        return new BaseResultDTO.AddDTO();
    }

    @Override
    public CfgRuleOutDTO.CommonDTO view() {
        List<CfgRuleOutEntity> cfgRuleOutEntities = this.list();
        CfgRuleOutEntity equipmentSortingPortEntity = cfgRuleOutEntities.stream().filter(entity -> entity.getType().equals(CfgRuleOutTypeEnum.EQUIPMENT_SORTING_PORT.getCode())).findFirst().orElse(new CfgRuleOutEntity());
        CfgRuleOutEntity b2cAllowableDeviationsEntity = cfgRuleOutEntities.stream().filter(entity -> entity.getType().equals(CfgRuleOutTypeEnum.B2C_ALLOWABLE_DEVIATIONS.getCode())).findFirst().orElse(new CfgRuleOutEntity()  );
        CfgRuleOutDTO.CommonDTO commonDTO = new CfgRuleOutDTO.CommonDTO();
        commonDTO.setEquipmentSortingPortDTO(BeanUtil.mapToBean(equipmentSortingPortEntity.getRuleContent(), CfgRuleOutDTO.EquipmentSortingPortDTO.class,true));;
        commonDTO.setB2cAllowableDeviations(BeanUtil.mapToBean(b2cAllowableDeviationsEntity.getRuleContent(), CfgRuleOutDTO.B2cAllowableDeviations.class,true));
        return commonDTO;
    }
}
