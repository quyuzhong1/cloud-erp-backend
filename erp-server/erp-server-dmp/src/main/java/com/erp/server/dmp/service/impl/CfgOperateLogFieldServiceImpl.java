package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.threadlocal.UserContext;
import com.erp.model.dmp.entity.CfgOperateLogFieldEntity;
import com.erp.server.dmp.mapper.CfgOperateLogFieldMapper;
import com.erp.server.dmp.service.CfgOperateLogFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.dmp.service.OperateLogService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.dmp.dto.CfgOperateLogFieldDTO;

import java.util.*;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 日志字段配置表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-05-22
 */
@Slf4j
@Service
public class CfgOperateLogFieldServiceImpl extends SuperServiceImpl<CfgOperateLogFieldMapper, CfgOperateLogFieldEntity> implements CfgOperateLogFieldService {
    @Autowired
    private OperateLogService operateLogService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgOperateLogFieldDTO.AddDTO addDTO) {
        CfgOperateLogFieldEntity cfgOperateLogFieldEntity = new CfgOperateLogFieldEntity();
        BeanMapperUtils.copy(addDTO, cfgOperateLogFieldEntity);

        // 数据处理
        handleData(cfgOperateLogFieldEntity);

        log.info("开始新增日志字段配置单");
        boolean save = super.save(cfgOperateLogFieldEntity);
        if (!save) {
            throw new ServiceException("日志字段配置单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "日志字段配置单", cfgOperateLogFieldEntity.getId());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, cfgOperateLogFieldEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(cfgOperateLogFieldEntity.getId(), cfgOperateLogFieldEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgOperateLogFieldDTO.UpdateDTO updateDTO) {
        CfgOperateLogFieldEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "日志字段配置单"));
        CfgOperateLogFieldEntity cfgOperateLogFieldEntity = BeanMapperUtils.map(CfgOperateLogFieldEntity.class, updateDTO);

        // 数据处理
        handleData(cfgOperateLogFieldEntity);
        log.info("编辑 开始修改日志字段配置单数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgOperateLogFieldEntity);
        if (!save) {
            throw new ServiceException("日志字段配置单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
        log.info("编辑 开始记录日志字段配置单日志数据，id：【{}】", cfgOperateLogFieldEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgOperateLogFieldEntity.getId(), "日志字段配置单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, cfgOperateLogFieldEntity, null, cfgOperateLogFieldEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(CfgOperateLogFieldEntity cfgOperateLogFieldEntity) {
        // TODO 验证数据 & 数据赋值
    }

    @Override
    public List<CfgOperateLogFieldEntity> listByClassPaths(List<String> classPaths) {
        LambdaQueryWrapper<CfgOperateLogFieldEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgOperateLogFieldEntity::getClassPath, classPaths);
        return this.list(queryWrapper);
    }

    @Override
    public Boolean saveBatchSysLogField() {
//        //用于手动添加字段对应信息，后续可添加界面添加,classPath为比较DTO路径
//        String  classPath = String.valueOf(DeliveryOrderDetailEntity.class);
//        List<CfgOperateLogFieldEntity> logFields =  Arrays.asList(
//                new CfgOperateLogFieldEntity().setField("deliveryQty").setFieldName("送货数量").setClassPath(classPath).setType(0) .setEnumClass(""),
//                new CfgOperateLogFieldEntity().setField("giftQty").setFieldName("赠品数量").setClassPath(classPath).setType(0) .setEnumClass(""),
//                new CfgOperateLogFieldEntity().setField("remark").setFieldName("备注").setClassPath(classPath).setType(0) .setEnumClass("")
////                new CfgOperateLogFieldEntity().setField("categoryName").setFieldName("分类名").setClassPath(classPath).setType(0) .setEnumClass(""),
////
////                new CfgOperateLogFieldEntity().setField("gradeId").setFieldName("等级").setClassPath(classPath).setType(3) .setEnumClass(""),
////
////                new CfgOperateLogFieldEntity().setField("purchaseUserName").setFieldName("采购员").setClassPath(classPath).setType(0) .setEnumClass(""),
////
////                new CfgOperateLogFieldEntity().setField("companyWebsite").setFieldName("公司网址").setClassPath(classPath).setType(0) .setEnumClass(""),
////                new CfgOperateLogFieldEntity().setField("disabled").setFieldName("禁用状态").setClassPath(classPath).setType(1) .setEnumClass(""),
////                new CfgOperateLogFieldEntity().setField("payMethodId").setFieldName("付款方式").setClassPath(classPath).setType(3) .setEnumClass(""),
////                new CfgOperateLogFieldEntity().setField("payCurrency").setFieldName("付款币种").setClassPath(classPath).setType(0) .setEnumClass(""),
////
////                new CfgOperateLogFieldEntity().setField("companyAddress").setFieldName("公司地址").setClassPath(classPath).setType(0) .setEnumClass("")
//
//
//        );
//        return this.saveBatch(logFields);
        return null;
    }
}
