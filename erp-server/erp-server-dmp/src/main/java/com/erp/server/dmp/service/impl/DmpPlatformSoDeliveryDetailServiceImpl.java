package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.DmpPlatformSoDeliveryDetailDTO;
import com.erp.model.dmp.entity.DmpPlatformSoDeliveryDetailEntity;
import com.erp.server.dmp.mapper.DmpPlatformSoDeliveryDetailMapper;
import com.erp.server.dmp.service.DmpPlatformSoDeliveryDetailService;
import com.erp.server.dmp.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-08-29
 */
@Slf4j
@Service
public class DmpPlatformSoDeliveryDetailServiceImpl extends SuperServiceImpl<DmpPlatformSoDeliveryDetailMapper, DmpPlatformSoDeliveryDetailEntity> implements DmpPlatformSoDeliveryDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpPlatformSoDeliveryDetailDTO.AddDTO addDTO) {
        DmpPlatformSoDeliveryDetailEntity dmpPlatformSoDeliveryDetailEntity = new DmpPlatformSoDeliveryDetailEntity();
        BeanMapperUtils.copy(addDTO, dmpPlatformSoDeliveryDetailEntity);

        // 数据处理
        handleData(dmpPlatformSoDeliveryDetailEntity);

        log.info("开始新增");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        dmpPlatformSoDeliveryDetailEntity.setCode(code);
        boolean save = super.save(dmpPlatformSoDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "" , dmpPlatformSoDeliveryDetailEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpPlatformSoDeliveryDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpPlatformSoDeliveryDetailEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpPlatformSoDeliveryDetailDTO.UpdateDTO addOrUpdateDTO) {
        DmpPlatformSoDeliveryDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, ""));
        DmpPlatformSoDeliveryDetailEntity dmpPlatformSoDeliveryDetailEntity =  BeanMapperUtils.map(DmpPlatformSoDeliveryDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(dmpPlatformSoDeliveryDetailEntity);
        log.info("编辑 开始修改数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(dmpPlatformSoDeliveryDetailEntity);
        if(!save) {
            throw new ServiceException("保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录日志数据，单号：【{}】", dmpPlatformSoDeliveryDetailEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpPlatformSoDeliveryDetailEntity.getCode(), "");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpPlatformSoDeliveryDetailEntity, null, dmpPlatformSoDeliveryDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpPlatformSoDeliveryDetailEntity dmpPlatformSoDeliveryDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
