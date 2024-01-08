package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.server.wms.mapper.SubcontractIssueDetailMapper;
import com.erp.server.wms.service.SubcontractIssueDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 委外发料明细单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@Service
public class SubcontractIssueDetailServiceImpl extends SuperServiceImpl<SubcontractIssueDetailMapper, SubcontractIssueDetailEntity> implements SubcontractIssueDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractIssueDetailDTO.AddDTO addDTO) {
        SubcontractIssueDetailEntity subcontractIssueDetailEntity = new SubcontractIssueDetailEntity();
        BeanMapperUtils.copy(addDTO, subcontractIssueDetailEntity);

        // 数据处理
        handleData(subcontractIssueDetailEntity);

        log.info("开始新增委外发料明细单");
        boolean save = super.save(subcontractIssueDetailEntity);
        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "委外发料明细单" , subcontractIssueDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, subcontractIssueDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(subcontractIssueDetailEntity.getId(), subcontractIssueDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractIssueDetailDTO.UpdateDTO updateDTO) {
        SubcontractIssueDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外发料明细单"));
        SubcontractIssueDetailEntity subcontractIssueDetailEntity =  BeanMapperUtils.map(SubcontractIssueDetailEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractIssueDetailEntity);
        log.info("编辑 开始修改委外发料明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(subcontractIssueDetailEntity);
        if(!save) {
            throw new ServiceException("委外发料明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录委外发料明细单日志数据，id：【{}】", subcontractIssueDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), subcontractIssueDetailEntity.getId(), "委外发料明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, subcontractIssueDetailEntity, null, subcontractIssueDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractIssueDetailEntity subcontractIssueDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
