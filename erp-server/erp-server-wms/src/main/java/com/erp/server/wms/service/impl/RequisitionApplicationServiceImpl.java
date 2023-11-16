package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.server.wms.mapper.RequisitionApplicationMapper;
import com.erp.server.wms.service.RequisitionApplicationService;
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
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 要货申请单 服务实现类
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
 */
@Slf4j
@Service
public class RequisitionApplicationServiceImpl extends SuperServiceImpl<RequisitionApplicationMapper, RequisitionApplicationEntity> implements RequisitionApplicationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(RequisitionApplicationDTO.AddDTO addDTO) {
        RequisitionApplicationEntity requisitionApplicationEntity = new RequisitionApplicationEntity();
        BeanMapperUtils.copy(addDTO, requisitionApplicationEntity);

        // 数据处理
        handleData(requisitionApplicationEntity);

        log.info("开始新增要货申请单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        requisitionApplicationEntity.setCode(code);
        boolean save = super.save(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "要货申请单" , requisitionApplicationEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, requisitionApplicationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(requisitionApplicationEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(RequisitionApplicationDTO.UpdateDTO updateDTO) {
        RequisitionApplicationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "要货申请单"));
        RequisitionApplicationEntity requisitionApplicationEntity =  BeanMapperUtils.map(RequisitionApplicationEntity.class, updateDTO);

        // 数据处理
        handleData(requisitionApplicationEntity);
        log.info("编辑 开始修改要货申请单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(requisitionApplicationEntity);
        if(!save) {
            throw new ServiceException("要货申请单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录要货申请单日志数据，单号：【{}】", requisitionApplicationEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), requisitionApplicationEntity.getCode(), "要货申请单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, requisitionApplicationEntity, null, requisitionApplicationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(RequisitionApplicationEntity requisitionApplicationEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
