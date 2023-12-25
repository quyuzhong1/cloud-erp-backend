package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptDetailEntity;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptDetailMapper;
import com.erp.server.wms.service.SoB2cDeliveryInterceptDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货拦截单详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryInterceptDetailServiceImpl extends SuperServiceImpl<SoB2cDeliveryInterceptDetailMapper, SoB2cDeliveryInterceptDetailEntity> implements SoB2cDeliveryInterceptDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeliveryInterceptDTO.AddDTO addDTO, String mainId) {
        SoB2cDeliveryInterceptDetailEntity soB2cDeliveryInterceptDetailEntity = new SoB2cDeliveryInterceptDetailEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeliveryInterceptDetailEntity);

        // 数据处理
        handleData(soB2cDeliveryInterceptDetailEntity);

        log.info("开始新增b2c发货拦截单详情");
        boolean save = super.save(soB2cDeliveryInterceptDetailEntity);
        if(!save) {
            throw new ServiceException("b2c发货拦截单详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "b2c发货拦截单详情" , soB2cDeliveryInterceptDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeliveryInterceptDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeliveryInterceptDetailEntity.getId(), soB2cDeliveryInterceptDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cDeliveryInterceptDetailDTO.UpdateDTO updateDTO) {
        SoB2cDeliveryInterceptDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c发货拦截单详情"));
        SoB2cDeliveryInterceptDetailEntity soB2cDeliveryInterceptDetailEntity =  BeanMapperUtils.map(SoB2cDeliveryInterceptDetailEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cDeliveryInterceptDetailEntity);
        log.info("编辑 开始修改b2c发货拦截单详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cDeliveryInterceptDetailEntity);
        if(!save) {
            throw new ServiceException("b2c发货拦截单详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c发货拦截单详情日志数据，id：【{}】", soB2cDeliveryInterceptDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cDeliveryInterceptDetailEntity.getId(), "b2c发货拦截单详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeliveryInterceptDetailEntity, null, soB2cDeliveryInterceptDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryInterceptDetailEntity soB2cDeliveryInterceptDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
