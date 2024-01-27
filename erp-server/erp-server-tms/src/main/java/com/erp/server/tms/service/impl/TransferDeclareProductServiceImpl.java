package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.TransferDeclareProductEntity;
import com.erp.server.tms.mapper.TransferDeclareProductMapper;
import com.erp.server.tms.service.TransferDeclareProductService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareProductDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转报关产品 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
@Slf4j
@Service
public class TransferDeclareProductServiceImpl extends SuperServiceImpl<TransferDeclareProductMapper, TransferDeclareProductEntity> implements TransferDeclareProductService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareProductDTO.AddDTO addDTO) {
        TransferDeclareProductEntity transferDeclareProductEntity = new TransferDeclareProductEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareProductEntity);

        // 数据处理
        handleData(transferDeclareProductEntity);

        log.info("开始新增中转报关产品");
        boolean save = super.save(transferDeclareProductEntity);
        if(!save) {
            throw new ServiceException("中转报关产品保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "中转报关产品" , transferDeclareProductEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareProductEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareProductEntity.getId(), transferDeclareProductEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareProductDTO.UpdateDTO updateDTO) {
        TransferDeclareProductEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关产品"));
        TransferDeclareProductEntity transferDeclareProductEntity =  BeanMapperUtils.map(TransferDeclareProductEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareProductEntity);
        log.info("编辑 开始修改中转报关产品数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareProductEntity);
        if(!save) {
            throw new ServiceException("中转报关产品保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转报关产品日志数据，id：【{}】", transferDeclareProductEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareProductEntity.getId(), "中转报关产品");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareProductEntity, null, transferDeclareProductEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareProductEntity transferDeclareProductEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
