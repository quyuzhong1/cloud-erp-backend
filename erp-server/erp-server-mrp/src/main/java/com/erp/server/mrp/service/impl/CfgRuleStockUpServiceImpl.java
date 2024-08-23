package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.entity.CfgRuleLogisticsEntity;
import com.erp.model.mrp.entity.CfgRuleStockUpEntity;
import com.erp.model.mrp.entity.CfgRuleStockingRatioEntity;
import com.erp.model.mrp.enums.CfgRuleStockingRatioTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.mrp.mapper.CfgRuleStockUpMapper;
import com.erp.server.mrp.service.CfgRuleLogisticsService;
import com.erp.server.mrp.service.CfgRuleStockUpService;
import com.erp.server.mrp.service.CfgRuleStockingRatioService;
import com.erp.server.mrp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 备货（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleStockUpServiceImpl extends SuperServiceImpl<CfgRuleStockUpMapper, CfgRuleStockUpEntity> implements CfgRuleStockUpService {

    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleLogisticsService cfgRuleLogisticsService;

    @Autowired
    private CfgRuleStockingRatioService cfgRuleStockingRatioService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleStockUpDTO.AddDTO addDTO) {
        CfgRuleStockUpEntity cfgRuleStockUpEntity = new CfgRuleStockUpEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleStockUpEntity);

        // 数据处理
        handleData(cfgRuleStockUpEntity);

        log.info("开始新增备货（规则设置）");
        boolean save = super.save(cfgRuleStockUpEntity);
        if(!save) {
            throw new ServiceException("备货（规则设置）保存失败");
        }

        //物流信息
        cfgRuleLogisticsService.add(addDTO.getCfgLogisticsList(),cfgRuleStockUpEntity.getId());

        //常规备货系数
        cfgRuleStockingRatioService.add(addDTO.getStockingRatioList(),cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
        //新品备货系数
        cfgRuleStockingRatioService.add(addDTO.getNewStockingRatioList(),cfgRuleStockUpEntity.getId(),CfgRuleStockingRatioTypeEnum.NEW.getCode());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "备货（规则设置）" , cfgRuleStockUpEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_COMMON.getCode(), cfgRuleStockUpEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgRuleStockUpEntity.getId(), cfgRuleStockUpEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleStockUpDTO.UpdateDTO updateDTO) {
        CfgRuleStockUpEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "备货（规则设置）"));
        CfgRuleStockUpEntity cfgRuleStockUpEntity =  BeanMapperUtils.map(CfgRuleStockUpEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleStockUpEntity);
        log.info("编辑 开始修改备货（规则设置）数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleStockUpEntity);
        if(!save) {
            throw new ServiceException("备货（规则设置）保存失败");
        }
        //物流信息
        cfgRuleLogisticsService.update(updateDTO.getCfgLogisticsList(),cfgRuleStockUpEntity.getId());

        //常规备货系数
        cfgRuleStockingRatioService.update(updateDTO.getStockingRatioList(),cfgRuleStockUpEntity.getId(), CfgRuleStockingRatioTypeEnum.CONVENTIONAL.getCode());
        //新品备货系数
        cfgRuleStockingRatioService.update(updateDTO.getNewStockingRatioList(),cfgRuleStockUpEntity.getId(),CfgRuleStockingRatioTypeEnum.NEW.getCode());

        // 记录主单操作日志
        log.info("编辑 开始记录备货（规则设置）日志数据，id：【{}】", cfgRuleStockUpEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleStockUpEntity.getId(), "备货（规则设置）");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleStockUpEntity, ModuleTypeEnum.CFG_RULE_COMMON.getCode(), cfgRuleStockUpEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public CfgRuleStockUpDTO.ViewDTO view(String id) {
        CfgRuleStockUpDTO.ViewDTO viewDTO = new CfgRuleStockUpDTO.ViewDTO();
        CfgRuleStockUpEntity oldEntity = this.getById(id);
        if (ObjectUtil.isEmpty(oldEntity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL,"备货设置");
        }
        BeanMapperUtils.copy(oldEntity,viewDTO);

        //物流配置信息
        List<CfgRuleLogisticsEntity> logisticsList = cfgRuleLogisticsService.listByStockUpIdList(Arrays.asList(id));
        if (CollectionUtils.isEmpty(logisticsList)) {
        }
        List<CfgRuleStockingRatioEntity> stockingRatioList = cfgRuleStockingRatioService.listByStockUpIdList(Arrays.asList(id));
        if (CollectionUtils.isEmpty(stockingRatioList)) {

        }
        return viewDTO;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleStockUpEntity cfgRuleStockUpEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
