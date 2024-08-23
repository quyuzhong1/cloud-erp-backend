package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CfgRuleWarehouseDetailEntity;
import com.erp.server.mrp.mapper.CfgRuleWarehouseDetailMapper;
import com.erp.server.mrp.service.CfgRuleWarehouseDetailService;
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
import com.erp.model.mrp.dto.CfgRuleWarehouseDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 仓库（规则设置）明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleWarehouseDetailServiceImpl extends SuperServiceImpl<CfgRuleWarehouseDetailMapper, CfgRuleWarehouseDetailEntity> implements CfgRuleWarehouseDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWarehouseDetailDTO.AddDTO addDTO) {
        CfgRuleWarehouseDetailEntity cfgRuleWarehouseDetailEntity = new CfgRuleWarehouseDetailEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWarehouseDetailEntity);

        // 数据处理
        handleData(cfgRuleWarehouseDetailEntity);

        log.info("开始新增仓库（规则设置）明细");
        boolean save = super.save(cfgRuleWarehouseDetailEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "仓库（规则设置）明细" , cfgRuleWarehouseDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleWarehouseDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleWarehouseDetailEntity.getId(), cfgRuleWarehouseDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWarehouseDetailDTO.UpdateDTO updateDTO) {
        CfgRuleWarehouseDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "仓库（规则设置）明细"));
        CfgRuleWarehouseDetailEntity cfgRuleWarehouseDetailEntity =  BeanMapperUtils.map(CfgRuleWarehouseDetailEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleWarehouseDetailEntity);
        log.info("编辑 开始修改仓库（规则设置）明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWarehouseDetailEntity);
        if(!save) {
            throw new ServiceException("仓库（规则设置）明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录仓库（规则设置）明细日志数据，id：【{}】", cfgRuleWarehouseDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWarehouseDetailEntity.getId(), "仓库（规则设置）明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWarehouseDetailEntity, null, cfgRuleWarehouseDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleWarehouseDetailEntity cfgRuleWarehouseDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
