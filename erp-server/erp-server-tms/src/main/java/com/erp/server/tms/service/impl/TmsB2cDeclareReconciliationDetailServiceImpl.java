package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TmsB2cDeclareReconciliationDetailEntity;
import com.erp.server.tms.mapper.TmsB2cDeclareReconciliationDetailMapper;
import com.erp.server.tms.service.TmsB2cDeclareReconciliationDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TmsB2cDeclareReconciliationDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c报关对账单明细 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@Service
public class TmsB2cDeclareReconciliationDetailServiceImpl extends SuperServiceImpl<TmsB2cDeclareReconciliationDetailMapper, TmsB2cDeclareReconciliationDetailEntity> implements TmsB2cDeclareReconciliationDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsB2cDeclareReconciliationDetailDTO.AddDTO addDTO) {
        TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity = new TmsB2cDeclareReconciliationDetailEntity();
        BeanMapperUtils.copy(addDTO, tmsB2cDeclareReconciliationDetailEntity);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationDetailEntity);

        log.info("开始新增b2c报关对账单明细");
        boolean save = super.save(tmsB2cDeclareReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("b2c报关对账单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "b2c报关对账单明细" , tmsB2cDeclareReconciliationDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, tmsB2cDeclareReconciliationDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(tmsB2cDeclareReconciliationDetailEntity.getId(), tmsB2cDeclareReconciliationDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsB2cDeclareReconciliationDetailDTO.UpdateDTO updateDTO) {
        TmsB2cDeclareReconciliationDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c报关对账单明细"));
        TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity =  BeanMapperUtils.map(TmsB2cDeclareReconciliationDetailEntity.class, updateDTO);

        // 数据处理
        handleData(tmsB2cDeclareReconciliationDetailEntity);
        log.info("编辑 开始修改b2c报关对账单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsB2cDeclareReconciliationDetailEntity);
        if(!save) {
            throw new ServiceException("b2c报关对账单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c报关对账单明细日志数据，id：【{}】", tmsB2cDeclareReconciliationDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), tmsB2cDeclareReconciliationDetailEntity.getId(), "b2c报关对账单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, tmsB2cDeclareReconciliationDetailEntity, null, tmsB2cDeclareReconciliationDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TmsB2cDeclareReconciliationDetailEntity tmsB2cDeclareReconciliationDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
