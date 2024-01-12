package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.OrderAcceptDTO;
import com.erp.model.srm.dto.ReturnConfirmDTO;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.srm.vo.ConfigVO;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.server.srm.convert.CfgSettingConfigConverter;
import com.erp.server.srm.mapper.CfgSettingMapper;
import com.erp.server.srm.service.CfgSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import com.erp.server.srm.service.UserService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Named;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.CfgSettingDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 系统配置管理 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-10
 */
@Slf4j
@Service
public class CfgSettingServiceImpl extends SuperServiceImpl<CfgSettingMapper, CfgSettingEntity> implements CfgSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Resource
    private UserService userService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CfgSettingDTO.AddDTO addDTO) {
        List<CfgSettingEntity> cfgSettingEntities = buildSettingData(addDTO.getOrderAcceptDTO(), addDTO.getReturnConfirmDTO());
        if (CollectionUtils.isNotEmpty(cfgSettingEntities)) {
            for (CfgSettingEntity cfgSettingEntity : cfgSettingEntities) {
                log.info("开始新增系统配置管理");
                boolean save = super.save(cfgSettingEntity);
                if (!save) {
                    throw new ServiceException("系统配置管理保存失败");
                }
                // 操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "系统配置管理", cfgSettingEntity.getId());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SRM_USER.getCode(), cfgSettingEntity.getId(), "新增操作");
            }
        }
    }

    private List<CfgSettingEntity> buildSettingData(OrderAcceptDTO orderAcceptDTO, ReturnConfirmDTO returnConfirmDTO) {
        List<CfgSettingEntity> cfgSettingEntities = new ArrayList<>();
        if (Objects.nonNull(orderAcceptDTO)) {
            CfgSettingEntity cfgSettingEntity = CfgSettingConfigConverter.INSTANCE.ConfigToOrderAcceptEntity(orderAcceptDTO);
            handleData(cfgSettingEntity);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("duration",orderAcceptDTO.getDuration());
            jsonObject.putOpt("unit",orderAcceptDTO.getUnit());
            jsonObject.putOpt("selectState",orderAcceptDTO.getSelectState());
            cfgSettingEntity.setDataJson(jsonObject);
            cfgSettingEntities.add(cfgSettingEntity);
        }
        if (Objects.nonNull(returnConfirmDTO)) {
            CfgSettingEntity cfgSettingEntity = CfgSettingConfigConverter.INSTANCE.ConfigToReturnConfigEntity(returnConfirmDTO);
            handleData(cfgSettingEntity);
            JSONObject jsonObject = new JSONObject();
            jsonObject.putOpt("duration",returnConfirmDTO.getDuration());
            jsonObject.putOpt("unit",returnConfirmDTO.getUnit());
            jsonObject.putOpt("selectState",returnConfirmDTO.getSelectState());
            cfgSettingEntity.setDataJson(jsonObject);
            cfgSettingEntities.add(cfgSettingEntity);
        }
        return cfgSettingEntities;
    }
    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSettingDTO.UpdateDTO updateDTO) {
        List<CfgSettingEntity> cfgSettingEntities = buildSettingData(updateDTO.getOrderAcceptDTO(), updateDTO.getReturnConfirmDTO());
        if (CollectionUtils.isNotEmpty(cfgSettingEntities)) {
            for (CfgSettingEntity cfgSettingEntity : cfgSettingEntities) {

                CfgSettingEntity old = super.getById(cfgSettingEntity.getId());
                Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "系统配置管理"));
                log.info("编辑 开始修改系统配置管理数据，id：【{}】", old.getId());
                boolean save = super.updateById(cfgSettingEntity);
                if (!save) {
                    throw new ServiceException("系统配置管理保存失败");
                }
                // 记录主单操作日志
                log.info("编辑 开始记录系统配置管理日志数据，id：【{}】", cfgSettingEntity.getId());
                String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgSettingEntity.getId(), "系统配置管理");
                operateLogService.addModuleOperateLogByObj(old, cfgSettingEntity, ModuleTypeEnum.SRM_USER.getCode(), cfgSettingEntity.getId(), msg);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public List<ConfigVO> getConfig() {
        List<ConfigVO> configVOList = new ArrayList<>();
        String supplierId = userService.getSupplierId();
        List<CfgSettingEntity> cfgSettingEntities = getListBySupplierId(supplierId);
        if (CollectionUtils.isNotEmpty(cfgSettingEntities)) {
            Map<String, CfgSettingEntity> collect = cfgSettingEntities.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, Function.identity()));
            configVOList.add(getSupplierConfig(supplierId, ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode(), collect.get(ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode())));
            configVOList.add(getSupplierConfig(supplierId, ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode(), collect.get(ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode())));
        } else {
            //获取默认配置
            configVOList.add(getSupplierConfig(supplierId, ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode(), null));
            configVOList.add(getSupplierConfig(supplierId, ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode(), null));
        }
        return configVOList;
    }

    @Override
    public List<SupplierConfigVO> getConfigList(List<String> supplierIds ) {
        if (CollectionUtils.isEmpty(supplierIds)) return Collections.emptyList();
        List<CfgSettingEntity> list = lambdaQuery().in(CfgSettingEntity::getSupplierId, supplierIds).eq(CfgSettingEntity::getIsDeleted, false).list();
        List<SupplierConfigVO> configVOList = new ArrayList<>(supplierIds.size());
        Map<String, CfgSettingEntity> settingEntityMap = null;
        if (CollectionUtils.isNotEmpty(list)){
            settingEntityMap = list.stream().collect(Collectors.toMap(e -> e.getSupplierId() + "_" + e.getKey(), Function.identity()));
        }
        for (String supplierId:supplierIds) {
            CfgSettingEntity orderCfgSettingEntity = null;
            CfgSettingEntity returnCfgSettingEntity = null;
            if (Objects.nonNull(settingEntityMap)){
                orderCfgSettingEntity = settingEntityMap.get(supplierId + "_" + ConfigKeyEnum.ORDER_AUTO_ACCEPT.getCode());
                returnCfgSettingEntity = settingEntityMap.get(supplierId + "_" + ConfigKeyEnum.RETURN_AUTO_CONFIRM.getCode());
            }
            SupplierConfigVO supplierConfigVO = SupplierConfigVO.builder()
                    .supplierId(supplierId)
                    .orderAcceptRule(getConfigOrderDesc(orderCfgSettingEntity))
                    .returnConfirmRule(getConfigReturnDesc(returnCfgSettingEntity))
                    .build();
            configVOList.add(supplierConfigVO);
        }
        return configVOList;
    }

    private String getConfigReturnDesc(CfgSettingEntity returnCfgSettingEntity) {
        StringBuffer stringBuffer = new StringBuffer();
        if (Objects.isNull(returnCfgSettingEntity)){
            stringBuffer.append("手工接受");
        }else {
            JSONObject jsonObject = returnCfgSettingEntity.getDataJson();
            Integer selectState = (Integer) jsonObject.getOrDefault("selectState",0);
            String duration = (String) jsonObject.getOrDefault("duration","48");
            String unit = (String) jsonObject.getOrDefault("unit","H");
            if (1 == selectState){
                //启用
                stringBuffer.append("[").append(duration).append(unit).append("]自动接受");
                return "["+duration+unit+"]自动接受";
            }else {
                stringBuffer.append("手工接受");
            }
        }
        return stringBuffer.toString();
    }

    private String getConfigOrderDesc(CfgSettingEntity orderCfgSettingEntity) {
        StringBuffer stringBuffer = new StringBuffer();
        if (Objects.isNull(orderCfgSettingEntity)){
            stringBuffer.append("手工确认");
        }else {
            JSONObject jsonObject = orderCfgSettingEntity.getDataJson();
            Integer selectState = (Integer) jsonObject.getOrDefault("selectState",0);
            String duration = (String) jsonObject.getOrDefault("duration","48");
            String unit = (String) jsonObject.getOrDefault("unit","H");
            if (1 == selectState){
                //启用
                stringBuffer.append("[").append(duration).append(unit).append("]自动接受");
                return "["+duration+unit+"]自动确认";
            }else {
                stringBuffer.append("手工确认");
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 获取用户配置
     *
     * @param code
     * @param entity
     * @return
     */
    private ConfigVO getSupplierConfig(String supplierId, String code, CfgSettingEntity entity) {
        if (Objects.nonNull(entity)) {
            ConfigVO configVO = JSONUtil.toBean(entity.getDataJson(), ConfigVO.class);
            configVO.setSupplierId(supplierId);
            configVO.setKey(code);
            return configVO;
        } else {
            return ConfigVO.builder()
                    .selectState(0)
                    .supplierId(supplierId)
                    .key(code)
                    .duration("48")
                    .unit("H")
                    .build();
        }
    }

    private List<CfgSettingEntity> getListBySupplierId(String supplierId) {
        if (StringUtils.isEmpty(supplierId)) return Collections.emptyList();
        return lambdaQuery()
                .eq(CfgSettingEntity::getSupplierId, supplierId)
                .eq(CfgSettingEntity::getIsDeleted, false)
                .orderByDesc(CfgSettingEntity::getIndex)
                .list();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgSettingEntity cfgSettingEntity) {
        //一个供应商只能存在一个配置
        int count = lambdaQuery()
                .eq(CfgSettingEntity::getKey, cfgSettingEntity.getKey())
                .eq(CfgSettingEntity::getIsDeleted, false)
                .eq(StringUtils.isNotEmpty(cfgSettingEntity.getId()), CfgSettingEntity::getId, cfgSettingEntity.getId())
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_96000);
        }
        cfgSettingEntity.setSupplierId(userService.getSupplierId());
        cfgSettingEntity.setDisabled(false);
    }
}
