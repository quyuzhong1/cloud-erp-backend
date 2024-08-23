package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaEntity;
import com.erp.server.mrp.mapper.CfgRuleSalesFormulaMapper;
import com.erp.server.mrp.service.CfgRuleSalesFormulaService;
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
import com.erp.model.mrp.dto.CfgRuleSalesFormulaDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销量公式（规则设置） 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-23
 */
@Slf4j
@Service
public class CfgRuleSalesFormulaServiceImpl extends SuperServiceImpl<CfgRuleSalesFormulaMapper, CfgRuleSalesFormulaEntity> implements CfgRuleSalesFormulaService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleSalesFormulaDTO.AddDTO addDTO) {
        CfgRuleSalesFormulaEntity cfgRuleSalesFormulaEntity = new CfgRuleSalesFormulaEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleSalesFormulaEntity);

        // 数据处理
        handleData(cfgRuleSalesFormulaEntity);

        log.info("开始新增销量公式（规则设置）");
        boolean save = super.save(cfgRuleSalesFormulaEntity);
        if(!save) {
            throw new ServiceException("销量公式（规则设置）保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量公式（规则设置）" , cfgRuleSalesFormulaEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgRuleSalesFormulaEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgRuleSalesFormulaEntity.getId(), cfgRuleSalesFormulaEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleSalesFormulaDTO.UpdateDTO updateDTO) {
        CfgRuleSalesFormulaEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量公式（规则设置）"));
        CfgRuleSalesFormulaEntity cfgRuleSalesFormulaEntity =  BeanMapperUtils.map(CfgRuleSalesFormulaEntity.class, updateDTO);

        // 数据处理
        handleData(cfgRuleSalesFormulaEntity);
        log.info("编辑 开始修改销量公式（规则设置）数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleSalesFormulaEntity);
        if(!save) {
            throw new ServiceException("销量公式（规则设置）保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量公式（规则设置）日志数据，id：【{}】", cfgRuleSalesFormulaEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleSalesFormulaEntity.getId(), "销量公式（规则设置）");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgRuleSalesFormulaEntity, null, cfgRuleSalesFormulaEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgRuleSalesFormulaEntity cfgRuleSalesFormulaEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
