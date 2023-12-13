package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.server.wms.mapper.SoB2cDeliveryMapper;
import com.erp.server.wms.service.SoB2cDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryServiceImpl extends SuperServiceImpl<SoB2cDeliveryMapper, SoB2cDeliveryEntity> implements SoB2cDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeliveryDTO.AddDTO addDTO) {
        SoB2cDeliveryEntity soB2cDeliveryEntity = new SoB2cDeliveryEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeliveryEntity);

        // 数据处理
        handleData(soB2cDeliveryEntity);

        log.info("开始新增b2c发货单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        soB2cDeliveryEntity.setCode(code);
        boolean save = super.save(soB2cDeliveryEntity);
        if(!save) {
            throw new ServiceException("b2c发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货单" , soB2cDeliveryEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeliveryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cDeliveryDTO.UpdateDTO updateDTO) {
        SoB2cDeliveryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c发货单"));
        SoB2cDeliveryEntity soB2cDeliveryEntity =  BeanMapperUtils.map(SoB2cDeliveryEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cDeliveryEntity);
        log.info("编辑 开始修改b2c发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soB2cDeliveryEntity);
        if(!save) {
            throw new ServiceException("b2c发货单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c发货单日志数据，单号：【{}】", soB2cDeliveryEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cDeliveryEntity.getCode(), "b2c发货单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeliveryEntity, null, soB2cDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryEntity soB2cDeliveryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
