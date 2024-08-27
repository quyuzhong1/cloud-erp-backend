package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.HistoryImportRecordEntity;
import com.erp.server.mrp.mapper.HistoryImportRecordMapper;
import com.erp.server.mrp.service.HistoryImportRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.HistoryImportRecordDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 历史导入记录 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@Service
public class HistoryImportRecordServiceImpl extends SuperServiceImpl<HistoryImportRecordMapper, HistoryImportRecordEntity> implements HistoryImportRecordService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(HistoryImportRecordDTO.AddDTO addDTO) {
        HistoryImportRecordEntity historyImportRecordEntity = new HistoryImportRecordEntity();
        BeanMapperUtils.copy(addDTO, historyImportRecordEntity);

        // 数据处理
        handleData(historyImportRecordEntity);

        log.info("开始新增历史导入记录");
        boolean save = super.save(historyImportRecordEntity);
        if(!save) {
            throw new ServiceException("历史导入记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "历史导入记录" , historyImportRecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, historyImportRecordEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(historyImportRecordEntity.getId(), historyImportRecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(HistoryImportRecordDTO.UpdateDTO updateDTO) {
        HistoryImportRecordEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "历史导入记录"));
        HistoryImportRecordEntity historyImportRecordEntity =  BeanMapperUtils.map(HistoryImportRecordEntity.class, updateDTO);

        // 数据处理
        handleData(historyImportRecordEntity);
        log.info("编辑 开始修改历史导入记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(historyImportRecordEntity);
        if(!save) {
            throw new ServiceException("历史导入记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录历史导入记录日志数据，id：【{}】", historyImportRecordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), historyImportRecordEntity.getId(), "历史导入记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, historyImportRecordEntity, null, historyImportRecordEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(HistoryImportRecordEntity historyImportRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
