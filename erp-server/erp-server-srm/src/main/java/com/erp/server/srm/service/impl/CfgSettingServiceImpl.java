package com.erp.server.srm.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.srm.dto.*;
import com.erp.model.srm.entity.CfgSettingEntity;
import com.erp.model.srm.enums.ConfigKeyEnum;
import com.erp.model.srm.enums.DictBasicEnum;
import com.erp.model.srm.vo.ConfigVO;
import com.erp.model.srm.vo.SupplierConfigVO;
import com.erp.server.srm.convert.CfgSettingConfigConverter;
import com.erp.server.srm.mapper.CfgSettingMapper;
import com.erp.server.srm.service.CfgSettingService;
import com.erp.server.srm.service.DictBasicService;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private UserService userService;
    @Resource
    private DictBasicService dictBasicService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(CfgSettingDTO.AddDTO addDTO) {
        List<CfgSettingEntity> cfgSettingEntities = buildSettingData(addDTO.getOrderAcceptDTO(), addDTO.getReturnConfirmDTO());
        if (CollectionUtils.isEmpty(cfgSettingEntities)) {
            return;
        }
        for (CfgSettingEntity cfgSettingEntity : cfgSettingEntities) {
            log.info("开始新增系统配置管理");
            boolean save = super.save(cfgSettingEntity);
            if (!save) {
                throw new ServiceException("系统配置管理保存失败");
            }
            // 操作日志
            String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), ApiError.CONTENT_96018.msg, cfgSettingEntity.getId());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SRM_USER.getCode(), cfgSettingEntity.getId(), "新增操作");
        }

    }

    private List<CfgSettingEntity> buildSettingData(OrderAcceptDTO orderAcceptDTO, ReturnConfirmDTO returnConfirmDTO) {
        List<CfgSettingEntity> cfgSettingEntities = new ArrayList<>();
        if (Objects.nonNull(orderAcceptDTO)) {
            CfgSettingEntity cfgSettingEntity = CfgSettingConfigConverter.INSTANCE.configToOrderAcceptEntity(orderAcceptDTO);
            handleData(cfgSettingEntity);
            cfgSettingEntity.setDataJson(JSONUtil.parseObj(orderAcceptDTO, true));
            cfgSettingEntities.add(cfgSettingEntity);
        }
        if (Objects.nonNull(returnConfirmDTO)) {
            CfgSettingEntity cfgSettingEntity = CfgSettingConfigConverter.INSTANCE.configToReturnConfigEntity(returnConfirmDTO);
            handleData(cfgSettingEntity);
            cfgSettingEntity.setDataJson(JSONUtil.parseObj(returnConfirmDTO, true));
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
                if(null == old){
                    throw  new ServiceException(ApiError.NOT_EXIST_BILL, ApiError.CONTENT_96018.msg);
                }
                log.info("编辑 开始修改系统配置管理数据，id：【{}】", old.getId());
                boolean save = super.updateById(cfgSettingEntity);
                if (!save) {
                    throw new ServiceException("系统配置管理保存失败");
                }
                // 记录主单操作日志
                log.info("编辑 开始记录系统配置管理日志数据，id：【{}】", cfgSettingEntity.getId());
                String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgSettingEntity.getId(), ApiError.CONTENT_96018.msg);
                operateLogService.addModuleOperateLogByObj(old, cfgSettingEntity, ModuleTypeEnum.SRM_USER.getCode(), cfgSettingEntity.getId(), msg);
            }
        }

        return Boolean.TRUE;
    }

    @Override
    public List<ConfigVO> getConfig() {
        List<ConfigVO> configVOList = new ArrayList<>();
        String supplierId = userService.getSupplierId();
        List<DictBasicDTO.ViewDTO> dicts = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
        List<CfgSettingEntity> cfgSettingEntities = getListBySupplierId(supplierId);
        if (CollectionUtils.isEmpty(cfgSettingEntities)) return configVOList;
        Map<String, CfgSettingEntity> collect = cfgSettingEntities.stream().collect(Collectors.toMap(CfgSettingEntity::getKey, Function.identity()));
        if (CollectionUtils.isNotEmpty(dicts)) {
            dicts.forEach(viewDTO -> {
                ConfigVO supplierConfig = getSupplierConfig(supplierId, viewDTO.getCode(), collect.get(viewDTO.getCode()));
                if (Objects.nonNull(supplierConfig)) {
                    configVOList.add(supplierConfig);
                }

            });
        }

        return configVOList;
    }

    @Override
    public List<SupplierConfigVO> getConfigList(List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return Collections.emptyList();
        }
        List<CfgSettingEntity> list = lambdaQuery().in(CfgSettingEntity::getSupplierId, supplierIds).eq(CfgSettingEntity::getDisabled,Boolean.FALSE).list();
        List<SupplierConfigVO> configVOList = new ArrayList<>(supplierIds.size());
        Map<String, CfgSettingEntity> settingEntityMap = null;
        if (CollectionUtils.isNotEmpty(list)) {
            settingEntityMap = list.stream().collect(Collectors.toMap(e -> e.getSupplierId() + "_" + e.getKey(), Function.identity()));
        }
        for (String supplierId : supplierIds) {
            CfgSettingEntity orderCfgSettingEntity = null;
            CfgSettingEntity returnCfgSettingEntity = null;
            if (Objects.nonNull(settingEntityMap)) {
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

    @Override
    public List<CfgSettingEntity> listByKeyAndSupplier(String key, List<String> supplierIds) {
        if (CollectionUtils.isEmpty(supplierIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().eq(CfgSettingEntity::getKey, key).in(CfgSettingEntity::getSupplierId, supplierIds).list();
    }

    @Override
    public CfgSettingDTO.ViewDTO view() {
        CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(DictBasicEnum.CFG_SETTING.getType());
        if (CollectionUtils.isEmpty(dictList)) {
            return viewDTO;
        }
        //查询已有配置信息
        List<CfgSettingEntity> list = getListBySupplierId(userService.getSupplierId());
        if (CollectionUtils.isEmpty(list)) {
            return viewDTO;
        }
        for (CfgSettingEntity cfgSetting : list) {
            handleViewEnum(cfgSetting, viewDTO);
        }
        return viewDTO;
    }

    @Override
    public List<CfgSettingDTO.ViewDTO> listByKey(String key) {
        List<CfgSettingEntity> list = lambdaQuery().eq(CfgSettingEntity::getKey, key).eq(CfgSettingEntity::getDisabled,Boolean.FALSE).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        ConfigKeyEnum keyEnum = ConfigKeyEnum.getEnum(key);
        List<CfgSettingDTO.ViewDTO> configList = new ArrayList<>(list.size());
        for (CfgSettingEntity cfgSettingEntity : list) {
            CfgSettingDTO.ViewDTO viewDTO = new CfgSettingDTO.ViewDTO();
            viewDTO.setSupplierId(cfgSettingEntity.getSupplierId());
            switch (keyEnum) {
                case ORDER_AUTO_ACCEPT :
                    OrderAcceptDTO orderAcceptDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), OrderAcceptDTO.class);
                    viewDTO.setOrderAcceptDTO(orderAcceptDTO);
                    break;
                case RETURN_AUTO_CONFIRM:
                    ReturnConfirmDTO returnConfirmDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), ReturnConfirmDTO.class);
                    viewDTO.setReturnConfirmDTO(returnConfirmDTO);
                    break;
                case PO_RECONCILIATION:
                    PoReconciliationDetailDTO.AddSettingDTO addSettingDTO = BeanUtil.toBean(cfgSettingEntity.getDataJson(), PoReconciliationDetailDTO.AddSettingDTO.class);
                    viewDTO.setSetDTO(addSettingDTO);
                    break;
                default:
                    throw new ServiceException(ApiError.ERROR_CFG_SETTING_KEY,key);
            }
            configList.add(viewDTO);
        }
        return configList;
    }

    @Override
    public CfgSettingEntity getByKey(String key) {
        return lambdaQuery().eq(CfgSettingEntity::getKey,key).last("limit 1").one();
    }

    private void handleViewEnum(CfgSettingEntity cfgSetting, CfgSettingDTO.ViewDTO viewDTO) {

        ConfigKeyEnum configKeyEnum = ConfigKeyEnum.getEnum(cfgSetting.getKey());
        switch (configKeyEnum) {
            case ORDER_AUTO_ACCEPT:
                OrderAcceptDTO orderAcceptDTO = BeanUtil.toBean(cfgSetting.getDataJson(), OrderAcceptDTO.class);
                orderAcceptDTO.setId(cfgSetting.getId());
                orderAcceptDTO.setKey(cfgSetting.getKey());
                orderAcceptDTO.setSupplierId(cfgSetting.getSupplierId());
                orderAcceptDTO.setDisabled(cfgSetting.getDisabled());
                viewDTO.setOrderAcceptDTO(orderAcceptDTO);
                break;
            case RETURN_AUTO_CONFIRM:
                ReturnConfirmDTO returnConfirmDTO = BeanUtil.toBean(cfgSetting.getDataJson(), ReturnConfirmDTO.class);
                returnConfirmDTO.setId(cfgSetting.getId());
                returnConfirmDTO.setKey(cfgSetting.getKey());
                returnConfirmDTO.setSupplierId(cfgSetting.getSupplierId());
                returnConfirmDTO.setDisabled(cfgSetting.getDisabled());
                viewDTO.setReturnConfirmDTO(returnConfirmDTO);
                break;
            default:
                break;
        }
    }

    private String getConfigOrderDesc(CfgSettingEntity orderCfgSettingEntity) {
        StringBuilder sb = new StringBuilder();
        if (Objects.isNull(orderCfgSettingEntity)) {
            sb.append("手工接受");
        } else {
            OrderAcceptDTO orderAcceptDTO = JSONUtil.toBean(orderCfgSettingEntity.getDataJson(), OrderAcceptDTO.class);
            Boolean enable = orderAcceptDTO.getEnable();
            Integer duration = orderAcceptDTO.getDuration();
            String unit = orderAcceptDTO.getUnit();
            if (Objects.nonNull(enable) && Boolean.TRUE.equals(enable)) {
                //启用
                sb.append("[").append(duration).append(unit).append("]自动接受");
                return sb.toString();
            } else {
                sb.append("手工接受");
            }
        }
        return sb.toString();
    }

    private String getConfigReturnDesc(CfgSettingEntity returnCfgSettingEntity) {
        StringBuilder sb = new StringBuilder();
        if (Objects.isNull(returnCfgSettingEntity)) {
            sb.append("手工确认");
        } else {
            ReturnConfirmDTO returnConfirmDTO = JSONUtil.toBean(returnCfgSettingEntity.getDataJson(), ReturnConfirmDTO.class);
            Boolean enable = returnConfirmDTO.getEnable();
            Integer duration = returnConfirmDTO.getDuration();
            String unit = returnConfirmDTO.getUnit();
            if (Objects.nonNull(enable) && Boolean.TRUE.equals(enable)) {
                //启用
                sb.append("[").append(duration).append(unit).append("]自动接受");
                return "[" + duration + unit + "]自动确认";
            } else {
                sb.append("手工确认");
            }
        }
        return sb.toString();
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
            configVO.setId(entity.getId());
            return configVO;
        } else {
            return null;
        }
    }

    private List<CfgSettingEntity> getListBySupplierId(String supplierId) {
        if (StringUtils.isEmpty(supplierId)) {
            return Collections.emptyList();
        }
        return baseMapper.getListBySupplierId(supplierId);
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(CfgSettingEntity cfgSettingEntity) {
        //一个供应商只能存在一个配置
        int count = lambdaQuery()
                .eq(CfgSettingEntity::getKey, cfgSettingEntity.getKey())
                .eq(CfgSettingEntity::getSupplierId, cfgSettingEntity.getSupplierId())
                .ne(StringUtils.isNotEmpty(cfgSettingEntity.getId()), CfgSettingEntity::getId, cfgSettingEntity.getId())
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_96000);
        }
        cfgSettingEntity.setSupplierId(userService.getSupplierId());
        cfgSettingEntity.setDisabled(false);
    }
}
