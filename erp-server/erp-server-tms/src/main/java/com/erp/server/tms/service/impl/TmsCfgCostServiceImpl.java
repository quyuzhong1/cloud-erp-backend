package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsCfgCostEntity;
import com.erp.server.tms.mapper.TmsCfgCostMapper;
import com.erp.server.tms.service.TmsCfgCostService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsCfgCostDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 费用管理配置表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@Service
public class TmsCfgCostServiceImpl extends SuperServiceImpl<TmsCfgCostMapper, TmsCfgCostEntity> implements TmsCfgCostService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsCfgCostDTO.AddDTO addDTO) {
        TmsCfgCostEntity tmsCfgCostEntity = new TmsCfgCostEntity();
        BeanMapperUtils.copy(addDTO, tmsCfgCostEntity);

        // 数据处理
        handleData(tmsCfgCostEntity);

        log.info("开始新增费用管理配置单");
        boolean save = super.save(tmsCfgCostEntity);
        if(!save) {
            throw new ServiceException("费用管理配置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "费用管理配置单" , tmsCfgCostEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsCfgCostEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsCfgCostEntity.getId(), tmsCfgCostEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsCfgCostDTO.UpdateDTO updateDTO) {
        TmsCfgCostEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "费用管理配置单"));
        TmsCfgCostEntity tmsCfgCostEntity =  BeanMapperUtils.map(TmsCfgCostEntity.class, updateDTO);

        // 数据处理
        handleData(tmsCfgCostEntity);
        log.info("编辑 开始修改费用管理配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsCfgCostEntity);
        if(!save) {
            throw new ServiceException("费用管理配置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录费用管理配置单日志数据，id：【{}】", tmsCfgCostEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsCfgCostEntity.getId(), "费用管理配置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsCfgCostEntity, null, tmsCfgCostEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsCfgCostEntity tmsCfgCostEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
