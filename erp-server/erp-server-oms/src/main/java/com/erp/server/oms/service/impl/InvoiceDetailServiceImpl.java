package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.InvoiceDetailDTO;
import com.erp.model.oms.entity.InvoiceDetailEntity;
import com.erp.server.oms.mapper.InvoiceDetailMapper;
import com.erp.server.oms.service.InvoiceDetailService;
import com.erp.server.oms.service.OperateLogService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * 上传记录订单明细 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Slf4j
@Service
public class InvoiceDetailServiceImpl extends SuperServiceImpl<InvoiceDetailMapper, InvoiceDetailEntity> implements InvoiceDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceDetailDTO.AddDTO addDTO) {
        InvoiceDetailEntity invoiceDetailEntity = new InvoiceDetailEntity();
        BeanMapperUtils.copy(addDTO, invoiceDetailEntity);

        // 数据处理
        handleData(invoiceDetailEntity);

        log.info("开始新增上传记录订单明细");
        boolean save = super.save(invoiceDetailEntity);
        if(!save) {
            throw new ServiceException("上传记录订单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "上传记录订单明细" , invoiceDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, invoiceDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(invoiceDetailEntity.getId(), invoiceDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceDetailDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录订单明细"));
        InvoiceDetailEntity invoiceDetailEntity =  BeanMapperUtils.map(InvoiceDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceDetailEntity);
        log.info("编辑 开始修改上传记录订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(invoiceDetailEntity);
        if(!save) {
            throw new ServiceException("上传记录订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录上传记录订单明细日志数据，id：【{}】", invoiceDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceDetailEntity.getId(), "上传记录订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceDetailEntity, null, invoiceDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void batchAdd(String id, List<InvoiceDetailDTO.AddDTO> detailList) {

    }

    @Override
    public void batchUpdate(String id, List<InvoiceDetailDTO.UpdateDTO> detailList) {

    }

    @Override
    public List<InvoiceDetailEntity> listByMainIdList(List<String> mainIdList) {
        if (CollUtil.isEmpty(mainIdList)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(InvoiceDetailEntity::getMainId,mainIdList).list();
    }

    @Override
    public void removeByMainId(String mainId) {
        lambdaUpdate().eq(InvoiceDetailEntity::getMainId,mainId).remove();
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceDetailEntity invoiceDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
