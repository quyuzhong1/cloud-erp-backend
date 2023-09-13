package com.erp.server.plm.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.plm.entity.ProductRefLabelEntity;
import com.erp.server.plm.mapper.ProductRefLabelMapper;
import com.erp.server.plm.service.ProductRefLabelService;
import com.common.business.service.SuperServiceImpl;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.plm.dto.ProductRefLabelDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 产品便签关系表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class ProductRefLabelServiceImpl extends SuperServiceImpl<ProductRefLabelMapper, ProductRefLabelEntity> implements ProductRefLabelService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(ProductRefLabelDTO.AddDTO addDTO) {
        ProductRefLabelEntity productRefLabelEntity = new ProductRefLabelEntity();
        BeanMapperUtils.copy(addDTO, productRefLabelEntity);

        // 数据处理
        handleData(productRefLabelEntity);

        log.info("开始新增产品便签关系单");
        boolean save = super.save(productRefLabelEntity);
        if(!save) {
            throw new ServiceException("产品便签关系单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "产品便签关系单" , productRefLabelEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, productRefLabelEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）
        return productRefLabelEntity.getId();
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductRefLabelDTO.UpdateDTO updateDTO) {
        ProductRefLabelEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品便签关系单"));
        ProductRefLabelEntity productRefLabelEntity =  BeanMapperUtils.map(ProductRefLabelEntity.class, updateDTO);

        // 数据处理
        handleData(productRefLabelEntity);
        log.info("编辑 开始修改产品便签关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productRefLabelEntity);
        if(!save) {
            throw new ServiceException("产品便签关系单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录产品便签关系单日志数据，id：【{}】", productRefLabelEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), productRefLabelEntity.getId(), "产品便签关系单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, productRefLabelEntity, null, productRefLabelEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProductRefLabelEntity productRefLabelEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
