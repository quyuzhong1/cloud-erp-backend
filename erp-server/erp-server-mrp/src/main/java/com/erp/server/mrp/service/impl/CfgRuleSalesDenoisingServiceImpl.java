package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesDenoisingMapper;
import com.erp.server.mrp.service.CfgRuleSalesDenoisingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.CfgRuleSalesDenoisingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销量去噪信息 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesDenoisingServiceImpl extends SuperServiceImpl<CfgRuleSalesDenoisingMapper, CfgRuleSalesDenoisingEntity> implements CfgRuleSalesDenoisingService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleSalesDenoisingDTO.AddDTO addDTO) {
        CfgRuleSalesDenoisingEntity cfgRuleSalesDenoisingEntity = new CfgRuleSalesDenoisingEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleSalesDenoisingEntity);

        // 数据处理
        handleData(cfgRuleSalesDenoisingEntity);

        log.info("开始新增销量去噪信息");
        boolean save = super.save(cfgRuleSalesDenoisingEntity);
        if(!save) {
            throw new ServiceException("销量去噪信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量去噪信息" , cfgRuleSalesDenoisingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleSalesDenoisingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleSalesDenoisingEntity.getId(), cfgRuleSalesDenoisingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleSalesDenoisingDTO.UpdateDTO updateDTO) {
        CfgRuleSalesDenoisingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量去噪信息"));
        CfgRuleSalesDenoisingEntity cfgRuleSalesDenoisingEntity =  BeanMapperUtils.map(CfgRuleSalesDenoisingEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleSalesDenoisingEntity);
        log.info("编辑 开始修改销量去噪信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleSalesDenoisingEntity);
        if(!save) {
            throw new ServiceException("销量去噪信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量去噪信息日志数据，id：【{}】", cfgRuleSalesDenoisingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleSalesDenoisingEntity.getId(), "销量去噪信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleSalesDenoisingEntity, null, cfgRuleSalesDenoisingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleSalesDenoisingEntity cfgRuleSalesDenoisingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
