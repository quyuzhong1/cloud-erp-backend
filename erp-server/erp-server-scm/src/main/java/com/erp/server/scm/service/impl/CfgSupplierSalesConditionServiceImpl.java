package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.CfgSupplierSalesConditionEntity;
import com.erp.server.scm.mapper.CfgSupplierSalesConditionMapper;
import com.erp.server.scm.service.CfgSupplierSalesConditionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.scm.service.OperateLogService;
import com.erp.server.scm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.CfgSupplierSalesConditionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销量设置条件明细 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@Service
public class CfgSupplierSalesConditionServiceImpl extends SuperServiceImpl<CfgSupplierSalesConditionMapper, CfgSupplierSalesConditionEntity> implements CfgSupplierSalesConditionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgSupplierSalesConditionDTO.AddDTO addDTO) {
        CfgSupplierSalesConditionEntity cfgSupplierSalesConditionEntity = new CfgSupplierSalesConditionEntity();
        BeanMapperUtils.copy(addDTO, cfgSupplierSalesConditionEntity);

        // 数据处理
        handleData(cfgSupplierSalesConditionEntity);

        log.info("开始新增销量设置条件明细");
        boolean save = super.save(cfgSupplierSalesConditionEntity);
        if(!save) {
            throw new ServiceException("销量设置条件明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量设置条件明细" , cfgSupplierSalesConditionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgSupplierSalesConditionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgSupplierSalesConditionEntity.getId(), cfgSupplierSalesConditionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgSupplierSalesConditionDTO.UpdateDTO addOrUpdateDTO) {
        CfgSupplierSalesConditionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量设置条件明细"));
        CfgSupplierSalesConditionEntity cfgSupplierSalesConditionEntity =  BeanMapperUtils.map(CfgSupplierSalesConditionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(cfgSupplierSalesConditionEntity);
        log.info("编辑 开始修改销量设置条件明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgSupplierSalesConditionEntity);
        if(!save) {
            throw new ServiceException("销量设置条件明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量设置条件明细日志数据，id：【{}】", cfgSupplierSalesConditionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgSupplierSalesConditionEntity.getId(), "销量设置条件明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgSupplierSalesConditionEntity, null, cfgSupplierSalesConditionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CfgSupplierSalesConditionEntity cfgSupplierSalesConditionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
