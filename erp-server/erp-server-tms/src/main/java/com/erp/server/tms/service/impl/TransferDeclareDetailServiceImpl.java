package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.server.tms.mapper.TransferDeclareDetailMapper;
import com.erp.server.tms.service.TransferDeclareDetailService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中转报关详情 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferDeclareDetailServiceImpl extends SuperServiceImpl<TransferDeclareDetailMapper, TransferDeclareDetailEntity> implements TransferDeclareDetailService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareDetailDTO.AddDTO addDTO) {
        TransferDeclareDetailEntity transferDeclareDetailEntity = new TransferDeclareDetailEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareDetailEntity);

        // 数据处理
        handleData(transferDeclareDetailEntity);

        log.info("开始新增中转报关详情");
        boolean save = super.save(transferDeclareDetailEntity);
        if(!save) {
            throw new ServiceException("中转报关详情保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "中转报关详情" , transferDeclareDetailEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, transferDeclareDetailEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(transferDeclareDetailEntity.getId(), transferDeclareDetailEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareDetailDTO.UpdateDTO updateDTO) {
        TransferDeclareDetailEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关详情"));
        TransferDeclareDetailEntity transferDeclareDetailEntity =  BeanMapperUtils.map(TransferDeclareDetailEntity.class, updateDTO);

        // 数据处理
        handleData(transferDeclareDetailEntity);
        log.info("编辑 开始修改中转报关详情数据，id：【{}】", old.getId());
        boolean save = super.updateById(transferDeclareDetailEntity);
        if(!save) {
            throw new ServiceException("中转报关详情保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中转报关详情日志数据，id：【{}】", transferDeclareDetailEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareDetailEntity.getId(), "中转报关详情");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, transferDeclareDetailEntity, null, transferDeclareDetailEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(TransferDeclareDetailEntity transferDeclareDetailEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
