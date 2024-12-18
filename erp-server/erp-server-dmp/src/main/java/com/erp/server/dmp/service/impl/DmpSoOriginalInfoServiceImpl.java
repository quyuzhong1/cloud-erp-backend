package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpSoOriginalInfoEntity;
import com.erp.server.dmp.mapper.DmpSoOriginalInfoMapper;
import com.erp.server.dmp.service.DmpSoOriginalInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpSoOriginalInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中台原始销售订单表 服务实现类
 * </p>
 *
 * @author shukai
 * @since 2024-11-25
 */
@Slf4j
@Service
public class DmpSoOriginalInfoServiceImpl extends SuperServiceImpl<DmpSoOriginalInfoMapper, DmpSoOriginalInfoEntity> implements DmpSoOriginalInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpSoOriginalInfoDTO.AddDTO addDTO) {
        DmpSoOriginalInfoEntity dmpSoOriginalInfoEntity = new DmpSoOriginalInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpSoOriginalInfoEntity);

        // 数据处理
        handleData(dmpSoOriginalInfoEntity);

        log.info("开始新增中台原始销售订单表");
        boolean save = super.save(dmpSoOriginalInfoEntity);
        if(!save) {
            throw new ServiceException("中台原始销售订单表保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台原始销售订单表" , dmpSoOriginalInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpSoOriginalInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpSoOriginalInfoEntity.getId(), dmpSoOriginalInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpSoOriginalInfoDTO.UpdateDTO updateDTO) {
        DmpSoOriginalInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台原始销售订单表"));
        DmpSoOriginalInfoEntity dmpSoOriginalInfoEntity =  BeanMapperUtils.map(DmpSoOriginalInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpSoOriginalInfoEntity);
        log.info("编辑 开始修改中台原始销售订单表数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpSoOriginalInfoEntity);
        if(!save) {
            throw new ServiceException("中台原始销售订单表保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台原始销售订单表日志数据，id：【{}】", dmpSoOriginalInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpSoOriginalInfoEntity.getId(), "中台原始销售订单表");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpSoOriginalInfoEntity, null, dmpSoOriginalInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpSoOriginalInfoEntity dmpSoOriginalInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
