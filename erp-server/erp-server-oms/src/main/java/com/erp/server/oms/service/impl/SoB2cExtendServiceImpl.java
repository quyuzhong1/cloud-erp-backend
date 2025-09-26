package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformOrderExtendDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.dto.CfgSettingDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cExtendEntity;
import com.erp.model.oms.enums.CfgSettingEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.sys.feign.SysPartitionFeign;
import com.erp.server.oms.mapper.SoB2cExtendMapper;
import com.erp.server.oms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cExtendDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 销售订单-全托管属性表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
 */
@Slf4j
@Service
public class SoB2cExtendServiceImpl extends SuperServiceImpl<SoB2cExtendMapper, SoB2cExtendEntity> implements SoB2cExtendService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private SysPartitionFeign sysPartitionFeign;
    @Resource
    private DictBasicService dictBasicService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cExtendDTO.AddDTO addDTO, SoB2cEntity soB2cEntity) {
        SoB2cExtendEntity soB2cExtendEntity = new SoB2cExtendEntity();
        BeanMapperUtils.copy(addDTO, soB2cExtendEntity);
        // 数据处理
        handleData(soB2cExtendEntity,soB2cEntity);

        log.info("开始新增销售订单-全托管属性单");
        boolean save = super.save(soB2cExtendEntity);
        if(!save) {
            throw new ServiceException("销售订单-全托管属性单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售订单-全托管属性单" , soB2cExtendEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_EXTEND.getCode(), soB2cExtendEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(soB2cExtendEntity.getId(), soB2cExtendEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cExtendDTO.UpdateDTO addOrUpdateDTO,SoB2cEntity soB2cEntity) {
        SoB2cExtendEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单-全托管属性单"));
        SoB2cExtendEntity soB2cExtendEntity =  BeanMapperUtils.map(SoB2cExtendEntity.class, addOrUpdateDTO);
        // 数据处理
        handleData(soB2cExtendEntity, soB2cEntity);
        log.info("编辑 开始修改销售订单-全托管属性单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cExtendEntity);
        if(!save) {
            throw new ServiceException("销售订单-全托管属性单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录销售订单-全托管属性单日志数据，id：【{}】", soB2cExtendEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cExtendEntity.getId(), "销售订单-全托管属性单");
        operateLogService.addModuleOperateLogByObj(old, soB2cExtendEntity, ModuleTypeEnum.SO_B2C_EXTEND.getCode(), soB2cExtendEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public IPage<SoB2cExtendEntity> pagePartitionIsNull(Page query) {
        return baseMapper.pagePartitionIsNull(query);
    }

    @Override
    public SoB2cExtendEntity getByMainId(String id) {
        if (CharSequenceUtil.isNotBlank(id)){
            return lambdaQuery().eq(SoB2cExtendEntity::getMainId,id).one();
        }
        return null;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cExtendEntity soB2cExtendEntity, SoB2cEntity soB2cEntity) {
        soB2cExtendEntity.setMainId(soB2cEntity.getId());
        //要求发货时间不为空时，获取预警时间配置
        if (Objects.nonNull(soB2cExtendEntity.getRequiredDeliveryTime())){
            CfgSettingDTO.ViewDTO setting = cfgSettingService.getSetting(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
            if (Objects.nonNull(setting) && Objects.nonNull(setting.getTimeOutSettingDTO()) && Objects.nonNull(setting.getTimeOutSettingDTO().getWarningTime())){
                BigDecimal warningTime = setting.getTimeOutSettingDTO().getWarningTime();
                soB2cExtendEntity.setDeliveryWarningTime(soB2cExtendEntity.getRequiredDeliveryTime().plusMinutes(-warningTime.multiply(new BigDecimal(60)).longValue()));
            }
        }
        if (Objects.isNull(soB2cExtendEntity.getWarningCount())){
            soB2cExtendEntity.setWarningCount(MathUtil.ZERO);
        }
        String shopId = soB2cEntity.getShopId();
        String partitionId = "";
        if (CharSequenceUtil.isNotBlank(shopId)){
            ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
            if (Objects.nonNull(shopInfo) && CharSequenceUtil.isNotBlank(shopInfo.getDictCountryCode())){
                String partitionByCountry = sysPartitionFeign.getPartitionByCountry(shopInfo.getDictCountryCode());
                partitionId = Objects.nonNull(partitionByCountry) ? partitionByCountry : "";
            }
        }
        soB2cExtendEntity.setPartitionId(partitionId);
    }

    @Override
    public List<FullyManagedDTO.WarningDTO> fullyManagedOrderMsgWarning(Integer offsetMinutes) {
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        if (CollUtil.isEmpty(dtoList)){
            return Collections.emptyList();
        }
        return baseMapper.fullyManagedOrderMsgWarning(offsetMinutes,dtoList.stream().map(DictBasicDTO.ViewDTO::getValue).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity) {
        PlatformOrderExtendDTO platformOrderExtendDTO = dto.getExtend();
        if (Objects.isNull(platformOrderExtendDTO)){
            return;
        }
        SoB2cExtendEntity soB2cExtendEntity = getByMainId(mainEntity.getId());
        if (Objects.isNull(soB2cExtendEntity)) {
            soB2cExtendEntity = new SoB2cExtendEntity();
            soB2cExtendEntity.setMainId(mainEntity.getId());
            BeanMapperUtils.copy(platformOrderExtendDTO, soB2cExtendEntity);
            handleData(soB2cExtendEntity, mainEntity);
            super.save(soB2cExtendEntity);
        }else{
            BeanMapperUtils.copy(platformOrderExtendDTO, soB2cExtendEntity);
            handleData(soB2cExtendEntity, mainEntity);
            super.updateById(soB2cExtendEntity);
        }
    }
}
