package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpThirdInventoryEntity;
import com.erp.server.dmp.mapper.DmpThirdInventoryMapper;
import com.erp.server.dmp.service.DmpThirdInventoryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpThirdInventoryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 第三方仓库存 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
 */
@Slf4j
@Service
public class DmpThirdInventoryServiceImpl extends SuperServiceImpl<DmpThirdInventoryMapper, DmpThirdInventoryEntity> implements DmpThirdInventoryService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpThirdInventoryDTO.AddDTO addDTO) {
        DmpThirdInventoryEntity dmpThirdInventoryEntity = new DmpThirdInventoryEntity();
        BeanMapperUtils.copy(addDTO, dmpThirdInventoryEntity);

        // 数据处理
        handleData(dmpThirdInventoryEntity);

        log.info("开始新增第三方仓库存");
        boolean save = super.save(dmpThirdInventoryEntity);
        if(!save) {
            throw new ServiceException("第三方仓库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方仓库存" , dmpThirdInventoryEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpThirdInventoryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpThirdInventoryEntity.getId(), dmpThirdInventoryEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpThirdInventoryDTO.UpdateDTO updateDTO) {
        DmpThirdInventoryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方仓库存"));
        DmpThirdInventoryEntity dmpThirdInventoryEntity =  BeanMapperUtils.map(DmpThirdInventoryEntity.class, updateDTO);

        // 数据处理
        handleData(dmpThirdInventoryEntity);
        log.info("编辑 开始修改第三方仓库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpThirdInventoryEntity);
        if(!save) {
            throw new ServiceException("第三方仓库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方仓库存日志数据，id：【{}】", dmpThirdInventoryEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpThirdInventoryEntity.getId(), "第三方仓库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpThirdInventoryEntity, null, dmpThirdInventoryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpThirdInventoryEntity dmpThirdInventoryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
