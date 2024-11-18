package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSoPrestockDetailEntity;
import com.erp.server.dmp.mapper.DmpSoPrestockDetailMapper;
import com.erp.server.dmp.service.DmpSoPrestockDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSoPrestockDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销售预入库明细表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
 */
@Slf4j
@Service
public class DmpSoPrestockDetailServiceImpl extends SuperServiceImpl<DmpSoPrestockDetailMapper, DmpSoPrestockDetailEntity> implements DmpSoPrestockDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoPrestockDetailDTO.AddDTO addDTO) {
        DmpSoPrestockDetailEntity dmpSoPrestockDetailEntity = new DmpSoPrestockDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpSoPrestockDetailEntity);

        // 数据处理
        handleData(dmpSoPrestockDetailEntity);

        log.info("开始新增销售预入库明细单");
        boolean save = super.save(dmpSoPrestockDetailEntity);
        if(!save) {
            throw new ServiceException("销售预入库明细单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售预入库明细单" , dmpSoPrestockDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoPrestockDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoPrestockDetailEntity.getId(), dmpSoPrestockDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoPrestockDetailDTO.UpdateDTO updateDTO) {
        DmpSoPrestockDetailEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售预入库明细单"));
        DmpSoPrestockDetailEntity dmpSoPrestockDetailEntity =  BeanMapperUtils.map(DmpSoPrestockDetailEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoPrestockDetailEntity);
        log.info("编辑 开始修改销售预入库明细单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoPrestockDetailEntity);
        if(!save) {
            throw new ServiceException("销售预入库明细单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销售预入库明细单日志数据，id：【{}】", dmpSoPrestockDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoPrestockDetailEntity.getId(), "销售预入库明细单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoPrestockDetailEntity, null, dmpSoPrestockDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoPrestockDetailEntity dmpSoPrestockDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
