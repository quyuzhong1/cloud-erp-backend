package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.server.wms.mapper.FbaShipmentMapper;
import com.erp.server.wms.service.FbaShipmentService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.FbaShipmentDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * FBI货件表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@Service
public class FbaShipmentServiceImpl extends SuperServiceImpl<FbaShipmentMapper, FbaShipmentEntity> implements FbaShipmentService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(FbaShipmentDTO.AddDTO addDTO) {
        FbaShipmentEntity fbaShipmentEntity = new FbaShipmentEntity();
        BeanMapperUtils.copy(addDTO, fbaShipmentEntity);

        // 数据处理
        handleData(fbaShipmentEntity);

        log.info("开始新增FBI货件单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        fbaShipmentEntity.setCode(code);
        boolean save = super.save(fbaShipmentEntity);
        if(!save) {
            throw new ServiceException("FBI货件单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "FBI货件单" , fbaShipmentEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, fbaShipmentEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        BaseResultDTO.AddDTO resuletAdd = new BaseResultDTO.AddDTO();
        resuletAdd.setCode(code);
        resuletAdd.setId(fbaShipmentEntity.getId());
        return resuletAdd;
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(FbaShipmentDTO.UpdateDTO updateDTO) {
        FbaShipmentEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "FBI货件单"));
        FbaShipmentEntity fbaShipmentEntity =  BeanMapperUtils.map(FbaShipmentEntity.class, updateDTO);

        // 数据处理
        handleData(fbaShipmentEntity);
        log.info("编辑 开始修改FBI货件单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(fbaShipmentEntity);
        if(!save) {
            throw new ServiceException("FBI货件单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录FBI货件单日志数据，单号：【{}】", fbaShipmentEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), fbaShipmentEntity.getCode(), "FBI货件单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, fbaShipmentEntity, null, fbaShipmentEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(FbaShipmentEntity fbaShipmentEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    public Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto) {
        return null;
    }
}
