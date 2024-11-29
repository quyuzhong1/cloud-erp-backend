package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.RemotePostcodeDetailEntity;
import com.erp.server.tms.mapper.RemotePostcodeDetailMapper;
import com.erp.server.tms.service.RemotePostcodeDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.RemotePostcodeDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 偏远邮编明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2024-11-29
 */
@Slf4j
@Service
public class RemotePostcodeDetailServiceImpl extends SuperServiceImpl<RemotePostcodeDetailMapper, RemotePostcodeDetailEntity> implements RemotePostcodeDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RemotePostcodeDetailDTO.AddDTO addDTO) {
        RemotePostcodeDetailEntity remotePostcodeDetailEntity = new RemotePostcodeDetailEntity();
        BeanMapperUtils.copy(addDTO, remotePostcodeDetailEntity);

        // 数据处理
        handleData(remotePostcodeDetailEntity);

        log.info("开始新增偏远邮编明细单");
        boolean save = super.save(remotePostcodeDetailEntity);
        if(!save) {
            throw new ServiceException("偏远邮编明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "偏远邮编明细单" , remotePostcodeDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, remotePostcodeDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(remotePostcodeDetailEntity.getId(), remotePostcodeDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RemotePostcodeDetailDTO.UpdateDTO updateDTO) {
        RemotePostcodeDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "偏远邮编明细单"));
        RemotePostcodeDetailEntity remotePostcodeDetailEntity =  BeanMapperUtils.map(RemotePostcodeDetailEntity.class, updateDTO);

        // 数据处理
        handleData(remotePostcodeDetailEntity);
        log.info("编辑 开始修改偏远邮编明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(remotePostcodeDetailEntity);
        if(!save) {
            throw new ServiceException("偏远邮编明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录偏远邮编明细单日志数据，id：【{}】", remotePostcodeDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), remotePostcodeDetailEntity.getId(), "偏远邮编明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, remotePostcodeDetailEntity, null, remotePostcodeDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RemotePostcodeDetailEntity remotePostcodeDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
