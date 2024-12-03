package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationEntity;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationMapper;
import com.erp.server.tms.service.TransferDeclareCostAllocationService;
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
import com.erp.model.tms.dto.TransferDeclareCostAllocationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转费用分摊 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@Service
public class TransferDeclareCostAllocationServiceImpl extends SuperServiceImpl<TransferDeclareCostAllocationMapper, TransferDeclareCostAllocationEntity> implements TransferDeclareCostAllocationService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDTO.AddDTO addDTO) {
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity = new TransferDeclareCostAllocationEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareCostAllocationEntity);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);

        log.info("开始新增中转费用分摊");
        boolean save = super.save(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转费用分摊" , transferDeclareCostAllocationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareCostAllocationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareCostAllocationEntity.getId(), transferDeclareCostAllocationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareCostAllocationDTO.UpdateDTO updateDTO) {
        TransferDeclareCostAllocationEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转费用分摊"));
        TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity =  BeanMapperUtils.map(TransferDeclareCostAllocationEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareCostAllocationEntity);
        log.info("编辑 开始修改中转费用分摊数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareCostAllocationEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转费用分摊日志数据，id：【{}】", transferDeclareCostAllocationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareCostAllocationEntity.getId(), "中转费用分摊");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareCostAllocationEntity, null, transferDeclareCostAllocationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareCostAllocationEntity transferDeclareCostAllocationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
