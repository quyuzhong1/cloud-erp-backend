package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpTransferWarehouseInfoEntity;
import com.erp.server.dmp.mapper.DmpTransferWarehouseInfoMapper;
import com.erp.server.dmp.service.DmpTransferWarehouseInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpTransferWarehouseInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 第三方中转仓库 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-07
 */
@Slf4j
@Service
public class DmpTransferWarehouseInfoServiceImpl extends SuperServiceImpl<DmpTransferWarehouseInfoMapper, DmpTransferWarehouseInfoEntity> implements DmpTransferWarehouseInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpTransferWarehouseInfoDTO.AddDTO addDTO) {
        DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity = new DmpTransferWarehouseInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpTransferWarehouseInfoEntity);

        // 数据处理
        handleData(dmpTransferWarehouseInfoEntity);

        log.info("开始新增第三方中转仓库");
        boolean save = super.save(dmpTransferWarehouseInfoEntity);
        if(!save) {
            throw new ServiceException("第三方中转仓库保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "第三方中转仓库" , dmpTransferWarehouseInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpTransferWarehouseInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpTransferWarehouseInfoEntity.getId(), dmpTransferWarehouseInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpTransferWarehouseInfoDTO.UpdateDTO updateDTO) {
        DmpTransferWarehouseInfoEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "第三方中转仓库"));
        DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity =  BeanMapperUtils.map(DmpTransferWarehouseInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpTransferWarehouseInfoEntity);
        log.info("编辑 开始修改第三方中转仓库数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpTransferWarehouseInfoEntity);
        if(!save) {
            throw new ServiceException("第三方中转仓库保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录第三方中转仓库日志数据，id：【{}】", dmpTransferWarehouseInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpTransferWarehouseInfoEntity.getId(), "第三方中转仓库");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpTransferWarehouseInfoEntity, null, dmpTransferWarehouseInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpTransferWarehouseInfoEntity dmpTransferWarehouseInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
