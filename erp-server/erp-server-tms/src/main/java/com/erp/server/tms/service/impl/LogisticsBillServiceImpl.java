package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.server.tms.mapper.LogisticsBillMapper;
import com.erp.server.tms.service.LogisticsBillService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsBillDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 物流单 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsBillServiceImpl extends SuperServiceImpl<LogisticsBillMapper, LogisticsBillEntity> implements LogisticsBillService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsBillDTO.AddDTO addDTO) {
        LogisticsBillEntity logisticsBillEntity = new LogisticsBillEntity();
        BeanMapperUtils.copy(addDTO, logisticsBillEntity);

        // 数据处理
        handleData(logisticsBillEntity);

        log.info("开始新增物流单");
        boolean save = super.save(logisticsBillEntity);
        if(!save) {
            throw new ServiceException("物流单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "物流单" , logisticsBillEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, logisticsBillEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(logisticsBillEntity.getId(), logisticsBillEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsBillDTO.UpdateDTO updateDTO) {
        LogisticsBillEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "物流单"));
        LogisticsBillEntity logisticsBillEntity =  BeanMapperUtils.map(LogisticsBillEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsBillEntity);
        log.info("编辑 开始修改物流单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsBillEntity);
        if(!save) {
            throw new ServiceException("物流单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录物流单日志数据，id：【{}】", logisticsBillEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), logisticsBillEntity.getId(), "物流单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, logisticsBillEntity, null, logisticsBillEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsBillEntity logisticsBillEntity) {
    // TODO 验证数据 & 数据赋值
    }

    @Override
    public Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList) {
        List<LogisticsBillEntity> logisticsBillEntities = BeanMapper.copyList(addDTOList, LogisticsBillEntity.class);
        boolean flag = this.saveBatch(logisticsBillEntities);
        return flag;
    }
}
