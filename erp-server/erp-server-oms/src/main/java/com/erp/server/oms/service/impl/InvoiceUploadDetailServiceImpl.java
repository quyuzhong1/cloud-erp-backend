package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.InvoiceUploadDetailEntity;
import com.erp.server.oms.mapper.InvoiceUploadDetailMapper;
import com.erp.server.oms.service.InvoiceUploadDetailService;
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
import com.erp.model.oms.dto.InvoiceUploadDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
public class InvoiceUploadDetailServiceImpl extends SuperServiceImpl<InvoiceUploadDetailMapper, InvoiceUploadDetailEntity> implements InvoiceUploadDetailService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(InvoiceUploadDetailDTO.AddDTO addDTO) {
        InvoiceUploadDetailEntity invoiceUploadDetailEntity = new InvoiceUploadDetailEntity();
        BeanMapperUtils.copy(addDTO, invoiceUploadDetailEntity);

        // 数据处理
        handleData(invoiceUploadDetailEntity);

        log.info("开始新增上传记录订单明细");
        boolean save = super.save(invoiceUploadDetailEntity);
        if(!save) {
            throw new ServiceException("上传记录订单明细保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "上传记录订单明细" , invoiceUploadDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, invoiceUploadDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(invoiceUploadDetailEntity.getId(), invoiceUploadDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(InvoiceUploadDetailDTO.UpdateDTO addOrUpdateDTO) {
        InvoiceUploadDetailEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "上传记录订单明细"));
        InvoiceUploadDetailEntity invoiceUploadDetailEntity =  BeanMapperUtils.map(InvoiceUploadDetailEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(invoiceUploadDetailEntity);
        log.info("编辑 开始修改上传记录订单明细数据，id：【{}】", old.getId());
        boolean save = super.updateById(invoiceUploadDetailEntity);
        if(!save) {
            throw new ServiceException("上传记录订单明细保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录上传记录订单明细日志数据，id：【{}】", invoiceUploadDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), invoiceUploadDetailEntity.getId(), "上传记录订单明细");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, invoiceUploadDetailEntity, null, invoiceUploadDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public void batchAdd(String id, List<InvoiceUploadDetailDTO.AddDTO> detailList) {

    }

    @Override
    public void batchUpdate(String id, List<InvoiceUploadDetailDTO.UpdateDTO> detailList) {

    }


    /**
    * 新增修改处理数据
    */
    private void handleData(InvoiceUploadDetailEntity invoiceUploadDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
