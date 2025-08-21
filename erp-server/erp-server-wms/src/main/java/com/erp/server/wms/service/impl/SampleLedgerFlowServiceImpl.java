package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleLedgerFlowEntity;
import com.erp.server.wms.mapper.SampleLedgerFlowMapper;
import com.erp.server.wms.service.SampleLedgerFlowService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SampleLedgerFlowDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品库存 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleLedgerFlowServiceImpl extends SuperServiceImpl<SampleLedgerFlowMapper, SampleLedgerFlowEntity> implements SampleLedgerFlowService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleLedgerFlowDTO.AddDTO addDTO) {
        SampleLedgerFlowEntity sampleLedgerFlowEntity = new SampleLedgerFlowEntity();
        BeanMapperUtils.copy(addDTO, sampleLedgerFlowEntity);

        // 数据处理
        handleData(sampleLedgerFlowEntity);

        log.info("开始新增样品库存");
        boolean save = super.save(sampleLedgerFlowEntity);
        if(!save) {
            throw new ServiceException("样品库存保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品库存" , sampleLedgerFlowEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleLedgerFlowEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleLedgerFlowEntity.getId(), sampleLedgerFlowEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleLedgerFlowDTO.UpdateDTO addOrUpdateDTO) {
        SampleLedgerFlowEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品库存"));
        SampleLedgerFlowEntity sampleLedgerFlowEntity =  BeanMapperUtils.map(SampleLedgerFlowEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleLedgerFlowEntity);
        log.info("编辑 开始修改样品库存数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleLedgerFlowEntity);
        if(!save) {
            throw new ServiceException("样品库存保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品库存日志数据，id：【{}】", sampleLedgerFlowEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleLedgerFlowEntity.getId(), "样品库存");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleLedgerFlowEntity, null, sampleLedgerFlowEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleLedgerFlowEntity sampleLedgerFlowEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
