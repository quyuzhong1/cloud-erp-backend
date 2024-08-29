package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.mrp.entity.PurchaseSuggestEntity;
import com.erp.server.mrp.mapper.PurchaseSuggestMapper;
import com.erp.server.mrp.service.PurchaseSuggestService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.mrp.service.OperateLogService;
import com.erp.server.mrp.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.mrp.dto.PurchaseSuggestDTO;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * 建议采购 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class PurchaseSuggestServiceImpl extends SuperServiceImpl<PurchaseSuggestMapper, PurchaseSuggestEntity> implements PurchaseSuggestService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PurchaseSuggestDTO.AddDTO addDTO) {
        PurchaseSuggestEntity purchaseSuggestEntity = new PurchaseSuggestEntity();
        BeanMapperUtils.copy(addDTO, purchaseSuggestEntity);

        // 数据处理
        handleData(purchaseSuggestEntity);

        log.info("开始新增建议采购");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        purchaseSuggestEntity.setCode(code);
        boolean save = super.save(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("建议采购保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "建议采购" , purchaseSuggestEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, purchaseSuggestEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(purchaseSuggestEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PurchaseSuggestDTO.UpdateDTO updateDTO) {
        PurchaseSuggestEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "建议采购"));
        PurchaseSuggestEntity purchaseSuggestEntity =  BeanMapperUtils.map(PurchaseSuggestEntity.class, updateDTO);

        // 数据处理
        handleData(purchaseSuggestEntity);
        log.info("编辑 开始修改建议采购数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(purchaseSuggestEntity);
        if(!save) {
            throw new ServiceException("建议采购保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录建议采购日志数据，单号：【{}】", purchaseSuggestEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), purchaseSuggestEntity.getCode(), "建议采购");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, purchaseSuggestEntity, null, purchaseSuggestEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(PurchaseSuggestEntity purchaseSuggestEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
