package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.ExhibitionOrderDetailEntity;
import com.erp.server.oms.mapper.ExhibitionOrderDetailMapper;
import com.erp.server.oms.service.ExhibitionOrderDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.ExhibitionOrderDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 展会订单详情 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-08-20
 */
@Slf4j
@Service
public class ExhibitionOrderDetailServiceImpl extends SuperServiceImpl<ExhibitionOrderDetailMapper, ExhibitionOrderDetailEntity> implements ExhibitionOrderDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ExhibitionOrderDetailDTO.AddDTO addDTO) {
        ExhibitionOrderDetailEntity exhibitionOrderDetailEntity = new ExhibitionOrderDetailEntity();
        BeanMapperUtils.copy(addDTO, exhibitionOrderDetailEntity);

        // 数据处理
        handleData(exhibitionOrderDetailEntity);

        log.info("开始新增展会订单详情");
        boolean save = super.save(exhibitionOrderDetailEntity);
        if(!save) {
            throw new ServiceException("展会订单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "展会订单详情" , exhibitionOrderDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, exhibitionOrderDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(exhibitionOrderDetailEntity.getId(), exhibitionOrderDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ExhibitionOrderDetailDTO.UpdateDTO addOrUpdateDTO) {
        ExhibitionOrderDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "展会订单详情"));
        ExhibitionOrderDetailEntity exhibitionOrderDetailEntity =  BeanMapperUtils.map(ExhibitionOrderDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(exhibitionOrderDetailEntity);
        log.info("编辑 开始修改展会订单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(exhibitionOrderDetailEntity);
        if(!save) {
            throw new ServiceException("展会订单详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录展会订单详情日志数据，id：【{}】", exhibitionOrderDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), exhibitionOrderDetailEntity.getId(), "展会订单详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, exhibitionOrderDetailEntity, null, exhibitionOrderDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ExhibitionOrderDetailEntity exhibitionOrderDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
