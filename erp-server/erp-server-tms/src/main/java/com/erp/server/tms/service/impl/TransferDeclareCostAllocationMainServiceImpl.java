package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationMainEntity;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationMainMapper;
import com.erp.server.tms.service.TransferDeclareCostAllocationMainService;
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
import com.erp.model.tms.dto.TransferDeclareCostAllocationMainDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转费用分摊主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
 */
@Slf4j
@Service
public class TransferDeclareCostAllocationMainServiceImpl extends SuperServiceImpl<TransferDeclareCostAllocationMainMapper, TransferDeclareCostAllocationMainEntity> implements TransferDeclareCostAllocationMainService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareCostAllocationMainDTO.AddDTO addDTO) {
        TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity = new TransferDeclareCostAllocationMainEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareCostAllocationMainEntity);

        // 数据处理
        handleData(transferDeclareCostAllocationMainEntity);

        log.info("开始新增中转费用分摊主单");
        boolean save = super.save(transferDeclareCostAllocationMainEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转费用分摊主单" , transferDeclareCostAllocationMainEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareCostAllocationMainEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareCostAllocationMainEntity.getId(), transferDeclareCostAllocationMainEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareCostAllocationMainDTO.UpdateDTO updateDTO) {
        TransferDeclareCostAllocationMainEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转费用分摊主单"));
        TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity =  BeanMapperUtils.map(TransferDeclareCostAllocationMainEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareCostAllocationMainEntity);
        log.info("编辑 开始修改中转费用分摊主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareCostAllocationMainEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转费用分摊主单日志数据，id：【{}】", transferDeclareCostAllocationMainEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareCostAllocationMainEntity.getId(), "中转费用分摊主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareCostAllocationMainEntity, null, transferDeclareCostAllocationMainEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareCostAllocationMainEntity transferDeclareCostAllocationMainEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
