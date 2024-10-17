package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.erp.server.wms.mapper.ThirdWarehouseDeliveryMapper;
import com.erp.server.wms.service.ThirdWarehouseDeliveryService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
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
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 三方仓发货单 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
@Slf4j
@Service
public class ThirdWarehouseDeliveryServiceImpl extends SuperServiceImpl<ThirdWarehouseDeliveryMapper, ThirdWarehouseDeliveryEntity> implements ThirdWarehouseDeliveryService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ThirdWarehouseDeliveryDTO.AddDTO addDTO) {
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity = new ThirdWarehouseDeliveryEntity();
        BeanMapperUtils.copy(addDTO, thirdWarehouseDeliveryEntity);

        // 数据处理
        handleData(thirdWarehouseDeliveryEntity);

        log.info("开始新增三方仓发货单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        thirdWarehouseDeliveryEntity.setCode(code);
        boolean save = super.save(thirdWarehouseDeliveryEntity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "三方仓发货单" , thirdWarehouseDeliveryEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, thirdWarehouseDeliveryEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(thirdWarehouseDeliveryEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ThirdWarehouseDeliveryDTO.UpdateDTO updateDTO) {
        ThirdWarehouseDeliveryEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "三方仓发货单"));
        ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity =  BeanMapperUtils.map(ThirdWarehouseDeliveryEntity.class, updateDTO);

        // 数据处理
        handleData(thirdWarehouseDeliveryEntity);
        log.info("编辑 开始修改三方仓发货单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(thirdWarehouseDeliveryEntity);
        if(!save) {
            throw new ServiceException("三方仓发货单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录三方仓发货单日志数据，单号：【{}】", thirdWarehouseDeliveryEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), thirdWarehouseDeliveryEntity.getCode(), "三方仓发货单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, thirdWarehouseDeliveryEntity, null, thirdWarehouseDeliveryEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
