package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.ProductRegistrationStatusEnum;
import com.erp.server.tms.mapper.ProductRegistrationMapper;
import com.erp.server.tms.service.ProductRegistrationService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ProductRegistrationDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 产品备案表 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2024-03-14
 */
@Slf4j
@Service
public class ProductRegistrationServiceImpl extends SuperServiceImpl<ProductRegistrationMapper, ProductRegistrationEntity> implements ProductRegistrationService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ProductRegistrationDTO.AddDTO addDTO) {
        ProductRegistrationEntity productRegistrationEntity = new ProductRegistrationEntity();
        BeanMapperUtils.copy(addDTO, productRegistrationEntity);

        // 数据处理
        handleData(productRegistrationEntity);

        log.info("开始新增产品备案单");
        boolean save = super.save(productRegistrationEntity);
        if(!save) {
            throw new ServiceException("产品备案单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "产品备案单" , productRegistrationEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, productRegistrationEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(productRegistrationEntity.getId(), productRegistrationEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ProductRegistrationDTO.UpdateDTO updateDTO) {
        ProductRegistrationEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "产品备案单"));
        ProductRegistrationEntity productRegistrationEntity =  BeanMapperUtils.map(ProductRegistrationEntity.class, updateDTO);

        // 数据处理
        handleData(productRegistrationEntity);
        log.info("编辑 开始修改产品备案单数据，id：【{}】", old.getId());
        boolean save = super.updateById(productRegistrationEntity);
        if(!save) {
            throw new ServiceException("产品备案单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录产品备案单日志数据，id：【{}】", productRegistrationEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), productRegistrationEntity.getId(), "产品备案单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, productRegistrationEntity, null, productRegistrationEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ProductRegistrationEntity productRegistrationEntity) {
    // TODO 验证数据 & 数据赋值
    }


    @Override
    public List<ProductRegistrationEntity> listBySkuNoList(List<String> skuNoList) {
        if (CollectionUtils.isEmpty(skuNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(ProductRegistrationEntity::getSkuNo, skuNoList).list();
    }


    /**
     * 查询是否备案 获取未备案的skuNo
     *
     * @param dto
     * @return
     */
    @Override
    public List<String> listNotRegistrationByParam(SettingForecastDTO.CheckRegistrationDTO dto) {
        String declarePlatform = dto.getDeclarePlatform();
        String registered = ProductRegistrationStatusEnum.REGISTERED.getCode();
        List<String> skuNoList = dto.getSkuNoList();
        List<ProductRegistrationEntity> dbList=this.lambdaQuery().
                eq(ProductRegistrationEntity::getDeclarePlatform,declarePlatform).
                eq(ProductRegistrationEntity::getStatus,registered).
                in(ProductRegistrationEntity::getSkuNo,skuNoList).list();
        //这个是查询到的
        List<String> dbSkuNoList = dbList.stream().map(ProductRegistrationEntity::getSkuNo).collect(Collectors.toList());

        return skuNoList.stream().filter(s->!dbSkuNoList.contains(s)).collect(Collectors.toList());
    }

}
