package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.enums.CancelStatusEnum;
import com.erp.model.wms.enums.HandleResultEnum;
import com.erp.model.wms.enums.InterceptStatusEnum;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptMapper;
import com.erp.server.wms.service.SoB2cDeliveryInterceptService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import com.common.business.config.DocNoGenHelper;
import com.common.core.controller.vo.ApiResult;
import cn.hutool.core.util.ObjectUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
/**
 * <p>
 * b2c发货拦截单 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
@Slf4j
@Service
public class SoB2cDeliveryInterceptServiceImpl extends SuperServiceImpl<SoB2cDeliveryInterceptMapper, SoB2cDeliveryInterceptEntity> implements SoB2cDeliveryInterceptService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SoB2cDeliveryInterceptDTO.AddDTO addDTO) {
        SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity = new SoB2cDeliveryInterceptEntity();
        BeanMapperUtils.copy(addDTO, soB2cDeliveryInterceptEntity);

        // 数据处理
        handleData(soB2cDeliveryInterceptEntity);

        log.info("开始新增b2c发货拦截单");
        // 生成单号
        // TODO 此处的null需填写生成单号类型，type查看BusinessNoTypeEnum枚举类 注意需要填写prefix 为单号前缀
        String code = docNoGenHelper.generateCode(null);
        soB2cDeliveryInterceptEntity.setCode(code);
        boolean save = super.save(soB2cDeliveryInterceptEntity);
        if(!save) {
            throw new ServiceException("b2c发货拦截单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货拦截单" , soB2cDeliveryInterceptEntity.getCode());
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLog(msg, null, soB2cDeliveryInterceptEntity.getId(), "新增操作");
        // TODO 新增明细（如果有明细的话）

        return new BaseResultDTO.AddDTO(soB2cDeliveryInterceptEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SoB2cDeliveryInterceptDTO.UpdateDTO updateDTO) {
        SoB2cDeliveryInterceptEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "b2c发货拦截单"));
        SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity =  BeanMapperUtils.map(SoB2cDeliveryInterceptEntity.class, updateDTO);

        // 数据处理
        handleData(soB2cDeliveryInterceptEntity);
        log.info("编辑 开始修改b2c发货拦截单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(soB2cDeliveryInterceptEntity);
        if(!save) {
            throw new ServiceException("b2c发货拦截单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        // 记录主单操作日志
            log.info("编辑 开始记录b2c发货拦截单日志数据，单号：【{}】", soB2cDeliveryInterceptEntity.getCode());
            String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), soB2cDeliveryInterceptEntity.getCode(), "b2c发货拦截单");
        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
        operateLogService.addModuleOperateLogByObj(old, soB2cDeliveryInterceptEntity, null, soB2cDeliveryInterceptEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDeliveryInterceptDTO.TabListDTO> tabList(PermissionsDTO dto) {
        return null;
    }

    @Override
    public PagingVO<SoB2cDeliveryInterceptDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryInterceptDTO.PagingParamDTO> dto) {
        return null;
    }

    @Override
    public SoB2cDeliveryInterceptDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    public BatchResultDTO logisticsIntercept(String id) {
        return null;
    }

    @Override
    public BatchResultDTO interceptResultConfirm(String id) {
        return null;
    }

    @Override
    public Boolean getIsIntercept(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Boolean.FALSE;
        }
        List<SoB2cDeliveryInterceptEntity> list = lambdaQuery().in(SoB2cDeliveryInterceptEntity::getSourceId, sourceIdList).list();

        //如果结果确认是拦截成功,返回拦截标识
        List<SoB2cDeliveryInterceptEntity> interceptSuccess = list.stream().filter(req -> HandleResultEnum.SUCCESS.getCode().equals(req.getHandleResult())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(interceptSuccess)) {
            return Boolean.TRUE;
        }
        //如果结果确认是拦截失败,返回拦截标识
        List<SoB2cDeliveryInterceptEntity> interceptFailure = list.stream().filter(req -> HandleResultEnum.FAILURE.getCode().equals(req.getHandleResult())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(interceptFailure)) {
            return Boolean.FALSE;
        }
        //如果还未手动确认拦截结果，按平台处理结果
        List<SoB2cDeliveryInterceptEntity> intercept = list.stream()
                .filter(req -> StringUtils.isBlank(req.getHandleResult())
                        && !CancelStatusEnum.FAILURE.getCode().equals(req.getCancelStatus())
                        && !InterceptStatusEnum.FAILURE.getCode().equals(req.getInterceptStatus())
                ).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(intercept)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity) {
    // TODO 验证数据 & 数据赋值
    }
}
