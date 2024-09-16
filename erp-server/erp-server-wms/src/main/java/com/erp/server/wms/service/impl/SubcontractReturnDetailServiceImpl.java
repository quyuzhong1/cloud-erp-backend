package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SubcontractIssueDetailEntity;
import com.erp.model.wms.entity.SubcontractReturnDetailEntity;
import com.erp.server.wms.mapper.SubcontractReturnDetailMapper;
import com.erp.server.wms.service.SubcontractReturnDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SubcontractReturnDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 委外退料明细单 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-09-15
 */
@Slf4j
@Service
public class SubcontractReturnDetailServiceImpl extends SuperServiceImpl<SubcontractReturnDetailMapper, SubcontractReturnDetailEntity> implements SubcontractReturnDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SubcontractReturnDetailDTO.AddDTO addDTO) {
        SubcontractReturnDetailEntity subcontractReturnDetailEntity = new SubcontractReturnDetailEntity();
        BeanMapperUtils.copy(addDTO, subcontractReturnDetailEntity);

        // 数据处理
        handleData(subcontractReturnDetailEntity);

        log.info("开始新增委外退料明细单");
        boolean save = super.save(subcontractReturnDetailEntity);
        if(!save) {
            throw new ServiceException("委外退料明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "委外退料明细单" , subcontractReturnDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, subcontractReturnDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(subcontractReturnDetailEntity.getId(), subcontractReturnDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SubcontractReturnDetailDTO.UpdateDTO updateDTO) {
        SubcontractReturnDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "委外退料明细单"));
        SubcontractReturnDetailEntity subcontractReturnDetailEntity =  BeanMapperUtils.map(SubcontractReturnDetailEntity.class, updateDTO);

        // 数据处理
        handleData(subcontractReturnDetailEntity);
        log.info("编辑 开始修改委外退料明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(subcontractReturnDetailEntity);
        if(!save) {
            throw new ServiceException("委外退料明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录委外退料明细单日志数据，id：【{}】", subcontractReturnDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), subcontractReturnDetailEntity.getId(), "委外退料明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, subcontractReturnDetailEntity, null, subcontractReturnDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SubcontractReturnDetailEntity> listBySubcontractOrderDetailIdList(List<String> subcontractOrderDetailIdList) {
        if (CollectionUtils.isEmpty(subcontractOrderDetailIdList)) {
            return Collections.EMPTY_LIST;
        }
        return baseMapper.listBySubcontractOrderDetailIdList(subcontractOrderDetailIdList);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SubcontractReturnDetailEntity subcontractReturnDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
