package com.erp.server.plm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.entity.PlmCfgSettingEntity;
import com.erp.model.plm.enums.PlmCfgSettingEnum;
import com.erp.model.sys.dto.PlmCfgSettingDTO;
import com.erp.model.wms.dto.CfgRuleOutDTO;
import com.erp.server.plm.mapper.CfgSettingMapper;
import com.erp.server.plm.service.CfgSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-07-25
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, PlmCfgSettingEntity> implements CfgSettingService {

    @Resource
    private CfgSettingService service;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addOrUpdate(PlmCfgSettingDTO.CommonDTO commonDTO) {
        //金蝶物料属性控制
        PlmCfgSettingEntity cfgSettingEntity = new PlmCfgSettingEntity();
        cfgSettingEntity.setKey(PlmCfgSettingEnum.MATERIAL_ATTRIBUTE_CONTROL.getCode());
        Map<String, Object> materialAttributeControlMap = BeanUtil.beanToMap(commonDTO.getMaterialAttributeControl());
        this.checkMaterialAttributeControl(commonDTO.getMaterialAttributeControl());
        cfgSettingEntity.setDataJson(materialAttributeControlMap);

        List<PlmCfgSettingEntity> saveList = new ArrayList<>();
        saveList.add(cfgSettingEntity);

        //删除数据后再保存
        service.remove(new QueryWrapper<>());
        service.saveBatch(saveList);
        return true;
    }

    private void checkMaterialAttributeControl(PlmCfgSettingDTO.MaterialAttributeControl materialAttributeControl) {
        if(Objects.isNull(materialAttributeControl)){
            return;
        }
        List<PlmCfgSettingDTO.MaterialAttributeControlDetail> detailList = materialAttributeControl.getDetailList();
        if(CollectionUtil.isEmpty(detailList)){
            return;
        }
        List<String> valueList = detailList.stream().flatMap(v->v.getMaterialAttributeList().stream()).collect(Collectors.toList());
        Set<String> values = new HashSet<>();
        List<String> duplicates = valueList.stream()
                .filter(v -> !values.add(v))
                .collect(Collectors.toList());

        if (!duplicates.isEmpty()) {
            throw new ServiceException("相同物流属性不能有两条数据");
        }
    }

    @Override
    public PlmCfgSettingDTO.CommonDTO view() {
        PlmCfgSettingDTO.CommonDTO commonDTO = new PlmCfgSettingDTO.CommonDTO();
        List<PlmCfgSettingEntity> plmCfgSettingEntities = this.list();
        PlmCfgSettingEntity materialAttributeControl = plmCfgSettingEntities.stream().filter(entity -> entity.getKey().equals(PlmCfgSettingEnum.MATERIAL_ATTRIBUTE_CONTROL.getCode())).findFirst().orElse(new PlmCfgSettingEntity());
        if(Objects.nonNull(materialAttributeControl.getDataJson())){
            commonDTO.setMaterialAttributeControl(BeanUtil.toBeanIgnoreError(materialAttributeControl.getDataJson(), PlmCfgSettingDTO.MaterialAttributeControl.class));
        }
        return commonDTO;
    }
}
