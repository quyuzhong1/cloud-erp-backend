package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDimEntity;
import com.erp.server.mrp.mapper.CalcSalesInfoDimMapper;
import com.erp.server.mrp.service.CalcSalesInfoDimService;
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
import com.erp.model.mrp.dto.CalcSalesInfoDimDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销量试算表 服务实现类
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Slf4j
@Service
public class CalcSalesInfoDimServiceImpl extends SuperServiceImpl<CalcSalesInfoDimMapper, CalcSalesInfoDimEntity> implements CalcSalesInfoDimService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CalcSalesInfoDimDTO.AddDTO addDTO) {
        CalcSalesInfoDimEntity calcSalesInfoDimEntity = new CalcSalesInfoDimEntity();
        BeanMapperUtils.copy(addDTO, calcSalesInfoDimEntity);

        // 数据处理
        handleData(calcSalesInfoDimEntity);

        log.info("开始新增销量试算单");
        boolean save = super.save(calcSalesInfoDimEntity);
        if(!save) {
            throw new ServiceException("销量试算单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量试算单" , calcSalesInfoDimEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, calcSalesInfoDimEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(calcSalesInfoDimEntity.getId(), calcSalesInfoDimEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CalcSalesInfoDimDTO.UpdateDTO updateDTO) {
        CalcSalesInfoDimEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量试算单"));
        CalcSalesInfoDimEntity calcSalesInfoDimEntity =  BeanMapperUtils.map(CalcSalesInfoDimEntity.class, updateDTO);

        // 数据处理
        handleData(calcSalesInfoDimEntity);
        log.info("编辑 开始修改销量试算单数据，id：【{}】", old.getId());
        boolean save = super.updateById(calcSalesInfoDimEntity);
        if(!save) {
            throw new ServiceException("销量试算单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量试算单日志数据，id：【{}】", calcSalesInfoDimEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), calcSalesInfoDimEntity.getId(), "销量试算单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, calcSalesInfoDimEntity, null, calcSalesInfoDimEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(CalcSalesInfoDimEntity calcSalesInfoDimEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
