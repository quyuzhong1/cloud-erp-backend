package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpProductInfoEntity;
import com.erp.server.dmp.mapper.DmpProductInfoMapper;
import com.erp.server.dmp.service.DmpProductInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpProductInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 产品spu信息 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-07-23
 */
@Slf4j
@Service
public class DmpProductInfoServiceImpl extends SuperServiceImpl<DmpProductInfoMapper, DmpProductInfoEntity> implements DmpProductInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpProductInfoDTO.AddDTO addDTO) {
        DmpProductInfoEntity dmpProductInfoEntity = new DmpProductInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpProductInfoEntity);

        // 数据处理
        handleData(dmpProductInfoEntity);

        log.info("开始新增产品spu信息");
        boolean save = super.save(dmpProductInfoEntity);
        if(!save) {
            throw new ServiceException("产品spu信息保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "产品spu信息" , dmpProductInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpProductInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpProductInfoEntity.getId(), dmpProductInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpProductInfoDTO.UpdateDTO updateDTO) {
        DmpProductInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品spu信息"));
        DmpProductInfoEntity dmpProductInfoEntity =  BeanMapperUtils.map(DmpProductInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpProductInfoEntity);
        log.info("编辑 开始修改产品spu信息数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpProductInfoEntity);
        if(!save) {
            throw new ServiceException("产品spu信息保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录产品spu信息日志数据，id：【{}】", dmpProductInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpProductInfoEntity.getId(), "产品spu信息");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpProductInfoEntity, null, dmpProductInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpProductInfoEntity dmpProductInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
