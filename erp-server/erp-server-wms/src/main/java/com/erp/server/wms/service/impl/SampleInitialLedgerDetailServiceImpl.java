package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleInitialLedgerDetailEntity;
import com.erp.server.wms.mapper.SampleInitialLedgerDetailMapper;
import com.erp.server.wms.service.SampleInitialLedgerDetailService;
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
import com.erp.model.wms.dto.SampleInitialLedgerDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品期初台账详情 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
@Slf4j
@Service
public class SampleInitialLedgerDetailServiceImpl extends SuperServiceImpl<SampleInitialLedgerDetailMapper, SampleInitialLedgerDetailEntity> implements SampleInitialLedgerDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleInitialLedgerDetailDTO.AddDTO addDTO) {
        SampleInitialLedgerDetailEntity sampleInitialLedgerDetailEntity = new SampleInitialLedgerDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleInitialLedgerDetailEntity);

        // 数据处理
        handleData(sampleInitialLedgerDetailEntity);

        log.info("开始新增样品期初台账详情");
        boolean save = super.save(sampleInitialLedgerDetailEntity);
        if(!save) {
            throw new ServiceException("样品期初台账详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品期初台账详情" , sampleInitialLedgerDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleInitialLedgerDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleInitialLedgerDetailEntity.getId(), sampleInitialLedgerDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleInitialLedgerDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleInitialLedgerDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品期初台账详情"));
        SampleInitialLedgerDetailEntity sampleInitialLedgerDetailEntity =  BeanMapperUtils.map(SampleInitialLedgerDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleInitialLedgerDetailEntity);
        log.info("编辑 开始修改样品期初台账详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleInitialLedgerDetailEntity);
        if(!save) {
            throw new ServiceException("样品期初台账详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品期初台账详情日志数据，id：【{}】", sampleInitialLedgerDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleInitialLedgerDetailEntity.getId(), "样品期初台账详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleInitialLedgerDetailEntity, null, sampleInitialLedgerDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleInitialLedgerDetailEntity sampleInitialLedgerDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
