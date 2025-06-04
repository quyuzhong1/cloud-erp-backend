package com.erp.server.sys.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.sys.entity.DictNoticeRoleOptionEntity;
import com.erp.server.sys.mapper.DictNoticeRoleOptionMapper;
import com.erp.server.sys.service.DictNoticeRoleOptionService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.sys.service.OperateLogService;
import com.erp.server.sys.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.sys.dto.DictNoticeRoleOptionDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@Service
public class DictNoticeRoleOptionServiceImpl extends SuperServiceImpl<DictNoticeRoleOptionMapper, DictNoticeRoleOptionEntity> implements DictNoticeRoleOptionService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DictNoticeRoleOptionDTO.AddDTO addDTO) {
        DictNoticeRoleOptionEntity dictNoticeRoleOptionEntity = new DictNoticeRoleOptionEntity();
        BeanMapperUtils.copy(addDTO, dictNoticeRoleOptionEntity);

        // 数据处理
        handleData(dictNoticeRoleOptionEntity);

        log.info("开始新增");
        boolean save = super.save(dictNoticeRoleOptionEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , dictNoticeRoleOptionEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dictNoticeRoleOptionEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dictNoticeRoleOptionEntity.getId(), dictNoticeRoleOptionEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DictNoticeRoleOptionDTO.UpdateDTO addOrUpdateDTO) {
        DictNoticeRoleOptionEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DictNoticeRoleOptionEntity dictNoticeRoleOptionEntity =  BeanMapperUtils.map(DictNoticeRoleOptionEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dictNoticeRoleOptionEntity);
        log.info("编辑 开始修改数据，id：【{}】", old.getId());
        boolean save = super.updateById(dictNoticeRoleOptionEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，id：【{}】", dictNoticeRoleOptionEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dictNoticeRoleOptionEntity.getId(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dictNoticeRoleOptionEntity, null, dictNoticeRoleOptionEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DictNoticeRoleOptionEntity dictNoticeRoleOptionEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
