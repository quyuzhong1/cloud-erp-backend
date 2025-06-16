package com.erp.server.scm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.scm.entity.SalesSharingEntity;
import com.erp.server.scm.mapper.SalesSharingMapper;
import com.erp.server.scm.service.ModuleOperateLogService;
import com.erp.server.scm.service.SalesSharingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.scm.dto.SalesSharingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 销量共享表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-13
 */
@Slf4j
@Service
public class SalesSharingServiceImpl extends SuperServiceImpl<SalesSharingMapper, SalesSharingEntity> implements SalesSharingService {
    @Autowired
    private ModuleOperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SalesSharingDTO.AddDTO addDTO) {
        SalesSharingEntity salesSharingEntity = new SalesSharingEntity();
        BeanMapperUtils.copy(addDTO, salesSharingEntity);

        // 数据处理
        handleData(salesSharingEntity);

        log.info("开始新增销量共享单");
        boolean save = super.save(salesSharingEntity);
        if(!save) {
            throw new ServiceException("销量共享单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "销量共享单" , salesSharingEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, salesSharingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(salesSharingEntity.getId(), salesSharingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SalesSharingDTO.UpdateDTO addOrUpdateDTO) {
        SalesSharingEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "销量共享单"));
        SalesSharingEntity salesSharingEntity =  BeanMapperUtils.map(SalesSharingEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(salesSharingEntity);
        log.info("编辑 开始修改销量共享单数据，id：【{}】", old.getId());
        boolean save = super.updateById(salesSharingEntity);
        if(!save) {
            throw new ServiceException("销量共享单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录销量共享单日志数据，id：【{}】", salesSharingEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), salesSharingEntity.getId(), "销量共享单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, salesSharingEntity, null, salesSharingEntity.getId(),"", msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SalesSharingEntity salesSharingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
