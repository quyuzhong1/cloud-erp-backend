package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.oms.entity.SoB2cDeclareProductEntity;
import com.erp.server.oms.mapper.SoB2cDeclareProductMapper;
import com.erp.server.oms.service.SoB2cDeclareProductService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.SoB2cDeclareProductDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
    @Autowired
    private CommonService commonService;

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
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", commonService.getUserInfo().getUserName(), "B2C销售订单申报产品信息单" , soB2cDeclareProductEntity.getId());
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
        SoB2cDeclareProductEntity soB2cDeclareProductEntity =  BeanMapperUtils.map(SoB2cDeclareProductEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cDeclareProductEntity);
        log.info("编辑 开始修改B2C销售订单申报产品信息单数据，id：【{}】", old.getId());
        boolean save = super.updateById(soB2cDeclareProductEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单申报产品信息单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录B2C销售订单申报产品信息单日志数据，id：【{}】", soB2cDeclareProductEntity.getId());
            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cDeclareProductEntity.getId(), "B2C销售订单申报产品信息单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeclareProductEntity, null, soB2cDeclareProductEntity.getId(), msg);
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
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeclareProductEntity soB2cDeclareProductEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
