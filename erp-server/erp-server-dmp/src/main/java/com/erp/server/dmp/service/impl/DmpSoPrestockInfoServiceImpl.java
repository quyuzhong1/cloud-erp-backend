package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSoPrestockInfoEntity;
import com.erp.server.dmp.mapper.DmpSoPrestockInfoMapper;
import com.erp.server.dmp.service.DmpSoPrestockInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSoPrestockInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销售预入库主表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-08-09
 */
@Slf4j
@Service
public class DmpSoPrestockInfoServiceImpl extends SuperServiceImpl<DmpSoPrestockInfoMapper, DmpSoPrestockInfoEntity> implements DmpSoPrestockInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoPrestockInfoDTO.AddDTO addDTO) {
        DmpSoPrestockInfoEntity dmpSoPrestockInfoEntity = new DmpSoPrestockInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoPrestockInfoEntity);

        // 数据处理
        handleData(dmpSoPrestockInfoEntity);

        log.info("开始新增销售预入库主单");
        boolean save = super.save(dmpSoPrestockInfoEntity);
        if(!save) {
            throw new ServiceException("销售预入库主单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销售预入库主单" , dmpSoPrestockInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoPrestockInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoPrestockInfoEntity.getId(), dmpSoPrestockInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoPrestockInfoDTO.UpdateDTO updateDTO) {
        DmpSoPrestockInfoEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销售预入库主单"));
        DmpSoPrestockInfoEntity dmpSoPrestockInfoEntity =  BeanMapperUtils.map(DmpSoPrestockInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoPrestockInfoEntity);
        log.info("编辑 开始修改销售预入库主单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoPrestockInfoEntity);
        if(!save) {
            throw new ServiceException("销售预入库主单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销售预入库主单日志数据，id：【{}】", dmpSoPrestockInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoPrestockInfoEntity.getId(), "销售预入库主单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoPrestockInfoEntity, null, dmpSoPrestockInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoPrestockInfoEntity dmpSoPrestockInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
