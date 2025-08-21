package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleBorrowDetailEntity;
import com.erp.server.wms.mapper.SampleBorrowDetailMapper;
import com.erp.server.wms.service.SampleBorrowDetailService;
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
import com.erp.model.wms.dto.SampleBorrowDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 借用变更单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleBorrowDetailServiceImpl extends SuperServiceImpl<SampleBorrowDetailMapper, SampleBorrowDetailEntity> implements SampleBorrowDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleBorrowDetailDTO.AddDTO addDTO) {
        SampleBorrowDetailEntity sampleBorrowDetailEntity = new SampleBorrowDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleBorrowDetailEntity);

        // 数据处理
        handleData(sampleBorrowDetailEntity);

        log.info("开始新增借用变更单明细单");
        boolean save = super.save(sampleBorrowDetailEntity);
        if(!save) {
            throw new ServiceException("借用变更单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "借用变更单明细单" , sampleBorrowDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleBorrowDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleBorrowDetailEntity.getId(), sampleBorrowDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleBorrowDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleBorrowDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "借用变更单明细单"));
        SampleBorrowDetailEntity sampleBorrowDetailEntity =  BeanMapperUtils.map(SampleBorrowDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleBorrowDetailEntity);
        log.info("编辑 开始修改借用变更单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleBorrowDetailEntity);
        if(!save) {
            throw new ServiceException("借用变更单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录借用变更单明细单日志数据，id：【{}】", sampleBorrowDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleBorrowDetailEntity.getId(), "借用变更单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleBorrowDetailEntity, null, sampleBorrowDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleBorrowDetailEntity sampleBorrowDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
