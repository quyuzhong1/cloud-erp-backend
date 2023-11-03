package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.entity.ShippingTemplateCostSettingEntity;
import com.erp.server.tms.mapper.ShippingTemplateCostSettingMapper;
import com.erp.server.tms.service.ShippingTemplateCostSettingService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.ShippingTemplateCostSettingDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 运费模板其他费用选值表 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateCostSettingServiceImpl extends SuperServiceImpl<ShippingTemplateCostSettingMapper, ShippingTemplateCostSettingEntity> implements ShippingTemplateCostSettingService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateCostSettingDTO.AddDTO addDTO) {
        ShippingTemplateCostSettingEntity shippingTemplateCostSettingEntity = new ShippingTemplateCostSettingEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateCostSettingEntity);

        // 数据处理
        handleData(shippingTemplateCostSettingEntity);

        log.info("开始新增运费模板其他费用选值单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        shippingTemplateCostSettingEntity.setCode(code);
        boolean save = super.save(shippingTemplateCostSettingEntity);
        if(!save) {
            throw new ServiceException("运费模板其他费用选值单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "运费模板其他费用选值单" , shippingTemplateCostSettingEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, shippingTemplateCostSettingEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(shippingTemplateCostSettingEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateCostSettingDTO.UpdateDTO updateDTO) {
        ShippingTemplateCostSettingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板其他费用选值单"));
        ShippingTemplateCostSettingEntity shippingTemplateCostSettingEntity =  BeanMapperUtils.map(ShippingTemplateCostSettingEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateCostSettingEntity);
        log.info("编辑 开始修改运费模板其他费用选值单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(shippingTemplateCostSettingEntity);
        if(!save) {
            throw new ServiceException("运费模板其他费用选值单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录运费模板其他费用选值单日志数据，单号：【{}】", shippingTemplateCostSettingEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), shippingTemplateCostSettingEntity.getCode(), "运费模板其他费用选值单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, shippingTemplateCostSettingEntity, null, shippingTemplateCostSettingEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateCostSettingEntity shippingTemplateCostSettingEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
