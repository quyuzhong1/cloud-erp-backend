package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.CfgLogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.CfgLogisticsAuthFieldMapper;
import com.erp.server.tms.service.CfgLogisticsAuthFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.CfgLogisticsAuthFieldDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流商授权字段配置表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class CfgLogisticsAuthFieldServiceImpl extends SuperServiceImpl<CfgLogisticsAuthFieldMapper, CfgLogisticsAuthFieldEntity> implements CfgLogisticsAuthFieldService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgLogisticsAuthFieldDTO.AddDTO addDTO) {
        CfgLogisticsAuthFieldEntity cfgLogisticsAuthFieldEntity = new CfgLogisticsAuthFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgLogisticsAuthFieldEntity);

        // 数据处理
        handleData(cfgLogisticsAuthFieldEntity);

        log.info("开始新增物流商授权字段配置单");
        boolean save = super.save(cfgLogisticsAuthFieldEntity);
        if(!save) {
            throw new ServiceException("物流商授权字段配置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流商授权字段配置单" , cfgLogisticsAuthFieldEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgLogisticsAuthFieldEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgLogisticsAuthFieldEntity.getId(), cfgLogisticsAuthFieldEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgLogisticsAuthFieldDTO.UpdateDTO updateDTO) {
        CfgLogisticsAuthFieldEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流商授权字段配置单"));
        CfgLogisticsAuthFieldEntity cfgLogisticsAuthFieldEntity =  BeanMapperUtils.map(CfgLogisticsAuthFieldEntity.class, updateDTO);

        // 数据处理
        handleData(cfgLogisticsAuthFieldEntity);
        log.info("编辑 开始修改物流商授权字段配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgLogisticsAuthFieldEntity);
        if(!save) {
            throw new ServiceException("物流商授权字段配置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流商授权字段配置单日志数据，id：【{}】", cfgLogisticsAuthFieldEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), cfgLogisticsAuthFieldEntity.getId(), "物流商授权字段配置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgLogisticsAuthFieldEntity, null, cfgLogisticsAuthFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgLogisticsAuthFieldEntity cfgLogisticsAuthFieldEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
