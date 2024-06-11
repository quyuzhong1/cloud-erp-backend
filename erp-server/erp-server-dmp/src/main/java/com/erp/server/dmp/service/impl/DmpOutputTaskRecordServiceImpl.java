package com.erp.server.dmp.service.impl;


import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.server.dmp.mapper.DmpOutputTaskRecordMapper;
import com.erp.server.dmp.service.DmpOutputTaskRecordService;

import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
/**
 * <p>
 * 推送任务记录 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@Service
public class DmpOutputTaskRecordServiceImpl extends SuperServiceImpl<DmpOutputTaskRecordMapper, DmpOutputTaskRecordEntity> implements DmpOutputTaskRecordService {
    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpOutputTaskRecordDTO.AddDTO addDTO) {
        DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = new DmpOutputTaskRecordEntity();
        BeanMapperUtils.copy(addDTO, dmpOutputTaskRecordEntity);

        // 数据处理
        handleData(dmpOutputTaskRecordEntity);

        log.info("开始新增推送任务记录");
        boolean save = super.save(dmpOutputTaskRecordEntity);
        if(!save) {
            throw new ServiceException("推送任务记录保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "推送任务记录" , dmpOutputTaskRecordEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpOutputTaskRecordEntity.getId(), dmpOutputTaskRecordEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpOutputTaskRecordDTO.UpdateDTO updateDTO) {
        DmpOutputTaskRecordEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "推送任务记录"));
        DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity =  BeanMapperUtils.map(DmpOutputTaskRecordEntity.class, updateDTO);

        // 数据处理
        handleData(dmpOutputTaskRecordEntity);
        log.info("编辑 开始修改推送任务记录数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpOutputTaskRecordEntity);
        if(!save) {
            throw new ServiceException("推送任务记录保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录推送任务记录日志数据，id：【{}】", dmpOutputTaskRecordEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpOutputTaskRecordEntity.getId(), "推送任务记录");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
