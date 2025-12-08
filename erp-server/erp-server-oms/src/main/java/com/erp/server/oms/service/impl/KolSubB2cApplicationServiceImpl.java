package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.KolSubB2cApplicationEntity;
import com.erp.server.oms.mapper.KolSubB2cApplicationMapper;
import com.erp.server.oms.service.KolSubB2cApplicationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolSubB2cApplicationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * B2C寄样申请单拆分单 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-12-04
 */
@Slf4j
@Service
public class KolSubB2cApplicationServiceImpl extends SuperServiceImpl<KolSubB2cApplicationMapper, KolSubB2cApplicationEntity> implements KolSubB2cApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolSubB2cApplicationDTO.AddDTO addDTO) {
        KolSubB2cApplicationEntity kolSubB2cApplicationEntity = new KolSubB2cApplicationEntity();
        BeanMapperUtils.copy(addDTO, kolSubB2cApplicationEntity);

        // 数据处理
        handleData(kolSubB2cApplicationEntity);

        log.info("开始新增B2C寄样申请单拆分单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        kolSubB2cApplicationEntity.setCode(code);
        boolean save = super.save(kolSubB2cApplicationEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单拆分单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C寄样申请单拆分单" , kolSubB2cApplicationEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, kolSubB2cApplicationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(kolSubB2cApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolSubB2cApplicationDTO.UpdateDTO addOrUpdateDTO) {
        KolSubB2cApplicationEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C寄样申请单拆分单"));
        KolSubB2cApplicationEntity kolSubB2cApplicationEntity =  BeanMapperUtils.map(KolSubB2cApplicationEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolSubB2cApplicationEntity);
        log.info("编辑 开始修改B2C寄样申请单拆分单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(kolSubB2cApplicationEntity);
        if(!save) {
            throw new ServiceException("B2C寄样申请单拆分单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C寄样申请单拆分单日志数据，单号：【{}】", kolSubB2cApplicationEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolSubB2cApplicationEntity.getCode(), "B2C寄样申请单拆分单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, kolSubB2cApplicationEntity, null, kolSubB2cApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(KolSubB2cApplicationEntity kolSubB2cApplicationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
