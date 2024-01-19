package com.erp.server.srm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.srm.entity.PoReconciliationEntity;
import com.erp.server.srm.mapper.PoReconciliationMapper;
import com.erp.server.srm.service.PoReconciliationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.srm.service.OperateLogService;
import com.erp.server.srm.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.srm.dto.PoReconciliationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 采购对账单 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@Service
public class PoReconciliationServiceImpl extends SuperServiceImpl<PoReconciliationMapper, PoReconciliationEntity> implements PoReconciliationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PoReconciliationDTO.AddDTO addDTO) {
        PoReconciliationEntity poReconciliationEntity = new PoReconciliationEntity();
        BeanMapperUtils.copy(addDTO, poReconciliationEntity);

        // 数据处理
        handleData(poReconciliationEntity);

        log.info("开始新增采购对账单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        poReconciliationEntity.setCode(code);
        boolean save = super.save(poReconciliationEntity);
        if(!save) {
            throw new ServiceException("采购对账单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "采购对账单" , poReconciliationEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, poReconciliationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(poReconciliationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PoReconciliationDTO.UpdateDTO updateDTO) {
        PoReconciliationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "采购对账单"));
        PoReconciliationEntity poReconciliationEntity =  BeanMapperUtils.map(PoReconciliationEntity.class, updateDTO);

        // 数据处理
        handleData(poReconciliationEntity);
        log.info("编辑 开始修改采购对账单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(poReconciliationEntity);
        if(!save) {
            throw new ServiceException("采购对账单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录采购对账单日志数据，单号：【{}】", poReconciliationEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), poReconciliationEntity.getCode(), "采购对账单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, poReconciliationEntity, null, poReconciliationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PoReconciliationEntity poReconciliationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
