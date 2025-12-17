package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.dmp.mapper.DmpShopInfoMapper;
import com.erp.server.dmp.service.DmpShopInfoService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 中台店铺表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
 */
@Slf4j
@Service
public class DmpShopInfoServiceImpl extends SuperServiceImpl<DmpShopInfoMapper, DmpShopInfoEntity> implements DmpShopInfoService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(DmpShopInfoDTO.AddDTO addDTO) {
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        BeanMapperUtils.copy(addDTO, dmpShopInfoEntity);

        // 数据处理
        handleData(dmpShopInfoEntity);

        log.info("开始新增中台店铺单");
        boolean save = super.save(dmpShopInfoEntity);
        if(!save) {
            throw new ServiceException("中台店铺单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中台店铺单" , dmpShopInfoEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, dmpShopInfoEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(dmpShopInfoEntity.getId(), dmpShopInfoEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(DmpShopInfoDTO.UpdateDTO updateDTO) {
        DmpShopInfoEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中台店铺单"));
        DmpShopInfoEntity dmpShopInfoEntity =  BeanMapperUtils.map(DmpShopInfoEntity.class, updateDTO);

        // 数据处理
        handleData(dmpShopInfoEntity);
        log.info("编辑 开始修改中台店铺单数据，id：【{}】", old.getId());
        boolean save = super.updateById(dmpShopInfoEntity);
        if(!save) {
            throw new ServiceException("中台店铺单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录中台店铺单日志数据，id：【{}】", dmpShopInfoEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), dmpShopInfoEntity.getId(), "中台店铺单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, dmpShopInfoEntity, null, dmpShopInfoEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(DmpShopInfoEntity dmpShopInfoEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
