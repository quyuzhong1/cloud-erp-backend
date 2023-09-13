package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.dmp.entity.BiTargetYearEntity;
import com.erp.server.dmp.mapper.BiTargetYearMapper;
import com.erp.server.dmp.service.BiTargetYearService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.erp.server.dmp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.BiTargetYearDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 年度目标表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetYearServiceImpl extends SuperServiceImpl<BiTargetYearMapper, BiTargetYearEntity> implements BiTargetYearService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetYearDTO.AddDTO addDTO) {
        BiTargetYearEntity biTargetYearEntity = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, biTargetYearEntity);

        // 数据处理
        handleData(biTargetYearEntity);

        log.info("开始新增年度目标单");
        boolean save = super.save(biTargetYearEntity);
        if(!save) {
            throw new ServiceException("年度目标单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "年度目标单" , biTargetYearEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, biTargetYearEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return biTargetYearEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetYearDTO.UpdateDTO updateDTO) {
        BiTargetYearEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "年度目标单"));
        BiTargetYearEntity biTargetYearEntity =  BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetYearEntity);
        log.info("编辑 开始修改年度目标单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetYearEntity);
        if(!save) {
            throw new ServiceException("年度目标单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录年度目标单日志数据，id：【{}】", biTargetYearEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), biTargetYearEntity.getId(), "年度目标单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, biTargetYearEntity, null, biTargetYearEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(BiTargetYearEntity biTargetYearEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
