package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.mapper.SoB2cDeclareProductMapper;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
/**
 * <p>
 * B2C销售订单申报产品信息表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2024-05-09
 */
@Slf4j
@Service
public class SoB2cDeclareProductServiceImpl extends SuperServiceImpl<SoB2cDeclareProductMapper, SoB2cDeclareProductEntity> implements SoB2cDeclareProductService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeclareProductDTO.AddDTO addDTO) {
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = new SoB2cDeclareProductEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeclareProductEntity);

        // 数据处理
        handleData(soB2cDeclareProductEntity);

        log.info("开始新增B2C销售订单申报产品信息单");
        boolean save = super.save(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "B2C销售订单申报产品信息单" , soB2cDeclareProductEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeclareProductEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeclareProductEntity.getId(), soB2cDeclareProductEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cDeclareProductDTO.UpdateDTO updateDTO) {
        SoB2cDeclareProductEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "B2C销售订单申报产品信息单"));
        SoB2cDeclareProductEntity soB2cDeclareProductEntity = B2cOrderConverter.INSTANCE.convertDeclareProductByDto(updateDTO);
        // 数据处理
        handleData(soB2cDeclareProductEntity);
        log.info("编辑 开始修改B2C销售订单申报产品信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }
        // 记录主单操作日志
            log.info("编辑 开始记录B2C销售订单申报产品信息单日志数据，id：【{}】", soB2cDeclareProductEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), soB2cDeclareProductEntity.getId(), "B2C销售订单申报产品信息单");
        // 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeclareProductEntity, ModuleTypeEnum.SO_B2C_DECLARE.getCode(), soB2cDeclareProductEntity.getId(), msg);
        return Boolean.TRUE;
    }
    /**
     * 根据销售订单id获取申报信息
     * @param id
     * @return
     */
    @Override
    public List<SoB2cDeclareProductEntity> listBySoId(String id) {
        if (StringUtils.isEmpty(id)){
            return Collections.emptyList();
        }
        return lambdaQuery().eq(SoB2cDeclareProductEntity::getSoId,id).list();
    }

    /**
     * 根据订单id删除申报信息
     * @param id
     */
    @Override
    public void removeBySoId(String id) {
        if (StringUtils.isNotEmpty(id)){
            lambdaUpdate().eq(SoB2cDeclareProductEntity::getSoId, id).remove();
        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeclareProductEntity soB2cDeclareProductEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
