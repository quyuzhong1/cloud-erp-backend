package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SampleReturnDetailEntity;
import com.erp.server.wms.mapper.SampleReturnDetailMapper;
import com.erp.server.wms.service.SampleReturnDetailService;
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
import com.erp.model.wms.dto.SampleReturnDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 样品归还单明细表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class SampleReturnDetailServiceImpl extends SuperServiceImpl<SampleReturnDetailMapper, SampleReturnDetailEntity> implements SampleReturnDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SampleReturnDetailDTO.AddDTO addDTO) {
        SampleReturnDetailEntity sampleReturnDetailEntity = new SampleReturnDetailEntity();
        BeanMapperUtils.copy(addDTO, sampleReturnDetailEntity);

        // 数据处理
        handleData(sampleReturnDetailEntity);

        log.info("开始新增样品归还单明细单");
        boolean save = super.save(sampleReturnDetailEntity);
        if(!save) {
            throw new ServiceException("样品归还单明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "样品归还单明细单" , sampleReturnDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, sampleReturnDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(sampleReturnDetailEntity.getId(), sampleReturnDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SampleReturnDetailDTO.UpdateDTO addOrUpdateDTO) {
        SampleReturnDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "样品归还单明细单"));
        SampleReturnDetailEntity sampleReturnDetailEntity =  BeanMapperUtils.map(SampleReturnDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(sampleReturnDetailEntity);
        log.info("编辑 开始修改样品归还单明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(sampleReturnDetailEntity);
        if(!save) {
            throw new ServiceException("样品归还单明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录样品归还单明细单日志数据，id：【{}】", sampleReturnDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), sampleReturnDetailEntity.getId(), "样品归还单明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, sampleReturnDetailEntity, null, sampleReturnDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SampleReturnDetailEntity sampleReturnDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
