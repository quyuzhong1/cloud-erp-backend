package com.erp.server.oms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.dto.CfgSettingDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.CfgSettingEntity;
import com.erp.model.oms.enums.CfgSettingEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.server.oms.mapper.CfgSettingMapper;
import com.erp.server.oms.service.CfgSettingService;
import com.erp.server.oms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {
    @Resource
    private DictBasicService dictBasicService;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSettingDTO.AddDTO addDTO) {
        // 数据处理
        List<CfgSettingEntity> cfgSettingList = handleData(addDTO);

        log.info("开始新增系统配置管理");
        boolean save = super.saveOrUpdateBatch(cfgSettingList);
        if(!save) {
            throw new ServiceException("系统配置管理保存失败");
        }
        return new BaseResultDTO.AddDTO("", "");
    }
    @Override
    public CfgSettingDTO.ViewDTO view() {
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            return viewDTO;
        }
        //查询已有配置信息
        List<CfgSettingEntity> list = listCfgSetting();
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        for (CfgSettingEntity cfgSetting : list) {
            handleViewEnum(cfgSetting,viewDTO);
        }
        return viewDTO;
    }

    @Override
    public CfgSettingDTO.ViewDTO getSetting(String key) {
        if (CharSequenceUtil.isBlank(key)){
            return null;
        }
        CfgSettingEntity cfgSetting = this.lambdaQuery().eq(CfgSettingEntity::getKey, key).one();
        if (Objects.isNull(cfgSetting)){
            return null;
        }
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        handleViewEnum(cfgSetting,viewDTO);
        return viewDTO;
    }

    @Override
    public CfgSettingEntity getSettingByKey(String key) {
        if (CharSequenceUtil.isBlank(key)){
            return null;
        }
        return this.lambdaQuery().eq(CfgSettingEntity::getKey, key).one();
    }

    @Override
    public List<CfgSettingEntity> listSettingByKey(String key) {
        if (CharSequenceUtil.isBlank(key)){
            return null;
        }
        return this.lambdaQuery().eq(CfgSettingEntity::getKey, key).eq(CfgSettingEntity::getDisabled,Boolean.FALSE).list();
    }

    private void handleViewEnum(CfgSettingEntity cfgSetting, CfgSettingDTO.ViewDTO viewDTO) {
        //获取枚举
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(cfgSetting.getKey());
        if(null == cfgSettingEnum){
            return;
        }
        switch (cfgSettingEnum) {
            case TIME_OUT_CONFIG:
                CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO = JSONUtil.toBean(cfgSetting.getValue(), CfgSettingDTO.TimeOutSettingDTO.class);
                viewDTO.setTimeOutSettingDTO(timeOutSettingDTO);
                break;
            default:
                break;
        }
    }


    /**
    * 新增修改处理数据
    */
    private List<CfgSettingEntity> handleData(CfgSettingDTO.AddDTO addDTO) {
        List<CfgSettingEntity> list = new ArrayList<>();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicTypeEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            throw new ServiceException(ApiError.ERROR_SELECTION_REQUIRED);
        }
        //查询已有配置信息
        List<CfgSettingEntity> cfgSettingList = listCfgSetting();

        for (DictBasicDTO.ViewDTO listDTO : dictList) {
            //添加数据
            CfgSettingEntity entity = handleAddEnum(listDTO, addDTO,cfgSettingList);
            if (Objects.nonNull(entity)){
                list.add(entity);
            }
        }
        return  list;
    }

    private CfgSettingEntity handleAddEnum(DictBasicDTO.ViewDTO listDTO, CfgSettingDTO.AddDTO addDTO, List<CfgSettingEntity> cfgSettingList) {
    CfgSettingEntity entity = new CfgSettingEntity();
        //系统配置json
        JSONObject jsonObject = new JSONObject();
        CfgSettingEnum cfgSettingEnum = CfgSettingEnum.getEnum(listDTO.getValue());
        if (Objects.isNull(cfgSettingEnum)){
            return null;
        }
        switch (cfgSettingEnum) {
            case TIME_OUT_CONFIG:
                jsonObject = JSONUtil.parseObj(addDTO.getTimeOutSettingDTO());
                break;
            default:
                break;
        }
        //查询是否是修改
        String id = cfgSettingList.stream().filter(obj -> CharSequenceUtil.equals(obj.getKey(),listDTO.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        entity.setId(id);
        entity.setIndex(listDTO.getSort());
        entity.setKey(listDTO.getValue());
        entity.setValue(jsonObject.toString());
        return entity;
    }

    private List<CfgSettingEntity> listCfgSetting() {
        return this.lambdaQuery().eq(CfgSettingEntity::getDisabled, false).orderByAsc(CfgSettingEntity::getIndex).list();
    }
}
