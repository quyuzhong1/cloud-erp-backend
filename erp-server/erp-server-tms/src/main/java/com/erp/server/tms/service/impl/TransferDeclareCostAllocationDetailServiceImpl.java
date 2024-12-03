package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TransferDeclareCostAllocationDetailEntity;
import com.erp.server.tms.mapper.TransferDeclareCostAllocationDetailMapper;
import com.erp.server.tms.service.TransferDeclareCostAllocationDetailService;
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
import com.erp.model.tms.dto.TransferDeclareCostAllocationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转费用分摊明细 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
 */
@Slf4j
@Service
public class TransferDeclareCostAllocationDetailServiceImpl extends SuperServiceImpl<TransferDeclareCostAllocationDetailMapper, TransferDeclareCostAllocationDetailEntity> implements TransferDeclareCostAllocationDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareCostAllocationDetailDTO.AddDTO addDTO) {
        TransferDeclareCostAllocationDetailEntity transferDeclareCostAllocationDetailEntity = new TransferDeclareCostAllocationDetailEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareCostAllocationDetailEntity);

        // 数据处理
        handleData(transferDeclareCostAllocationDetailEntity);

        log.info("开始新增中转费用分摊明细");
        boolean save = super.save(transferDeclareCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转费用分摊明细" , transferDeclareCostAllocationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareCostAllocationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareCostAllocationDetailEntity.getId(), transferDeclareCostAllocationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareCostAllocationDetailDTO.UpdateDTO updateDTO) {
        TransferDeclareCostAllocationDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转费用分摊明细"));
        TransferDeclareCostAllocationDetailEntity transferDeclareCostAllocationDetailEntity =  BeanMapperUtils.map(TransferDeclareCostAllocationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareCostAllocationDetailEntity);
        log.info("编辑 开始修改中转费用分摊明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareCostAllocationDetailEntity);
        if(!save) {
            throw new ServiceException("中转费用分摊明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转费用分摊明细日志数据，id：【{}】", transferDeclareCostAllocationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareCostAllocationDetailEntity.getId(), "中转费用分摊明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareCostAllocationDetailEntity, null, transferDeclareCostAllocationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareCostAllocationDetailEntity transferDeclareCostAllocationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
