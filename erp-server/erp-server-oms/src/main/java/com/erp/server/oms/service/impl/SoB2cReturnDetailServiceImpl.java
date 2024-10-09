package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.server.oms.mapper.SoB2cReturnDetailMapper;
import com.erp.server.oms.service.SoB2cReturnDetailService;
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
import com.erp.model.oms.dto.SoB2cReturnDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c退货订单明细 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
@Slf4j
@Service
public class SoB2cReturnDetailServiceImpl extends SuperServiceImpl<SoB2cReturnDetailMapper, SoB2cReturnDetailEntity> implements SoB2cReturnDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cReturnDetailDTO.AddDTO addDTO) {
        SoB2cReturnDetailEntity soB2cReturnDetailEntity = new SoB2cReturnDetailEntity();
        BeanMapperUtils.copy(addDTO, soB2cReturnDetailEntity);

        // 数据处理
        handleData(soB2cReturnDetailEntity);

        log.info("开始新增b2c退货订单明细");
        boolean save = super.save(soB2cReturnDetailEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c退货订单明细" , soB2cReturnDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cReturnDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cReturnDetailEntity.getId(), soB2cReturnDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cReturnDetailDTO.UpdateDTO updateDTO) {
        SoB2cReturnDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c退货订单明细"));
        SoB2cReturnDetailEntity soB2cReturnDetailEntity =  BeanMapperUtils.map(SoB2cReturnDetailEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cReturnDetailEntity);
        log.info("编辑 开始修改b2c退货订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cReturnDetailEntity);
        if(!save) {
            throw new ServiceException("b2c退货订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c退货订单明细日志数据，id：【{}】", soB2cReturnDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cReturnDetailEntity.getId(), "b2c退货订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cReturnDetailEntity, null, soB2cReturnDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cReturnDetailEntity soB2cReturnDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
