package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.enums.CancelStatusEnum;
import com.erp.model.wms.enums.HandleResultEnum;
import com.erp.model.wms.enums.InterceptStatusEnum;
import com.erp.model.wms.enums.SoB2cDeliveryInterceptStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
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

import javax.annotation.Resource;

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

    @Resource
    private LogisticsBillFeign logisticsBillFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

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
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHLJ);
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
        SoB2cDeliveryInterceptEntity entity = this.getById(id);
        if(Objects.isNull(entity)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货拦截单");
        }
        if(HandleResultEnum.SUCCESS.getCode().equals(entity.getHandleResult())){
            throw new ServiceException("发货单已成功拦截，无法重复操作");
        }
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(Collections.singletonList(entity.getSourceId()));
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单");
        }
        SoB2cEntity soB2cEntity = soB2cEntityList.get(0);
        LogisticsBillDTO.CancelBillDTO dto = LogisticsBillDTO.CancelBillDTO.builder()
                .channelId(entity.getLogisticsChannelId())
                .transportNo(entity.getTransportNo())
                .referenceNumber(soB2cEntity.getId())
                .reason("b2c发货拦截单自动拦截")
                .build();
        //先取消订单，取消订单失败的再拦截订单
        ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(dto);
        boolean isSuccess = true;
        String msg = "拦截成功";
        if(cancelResult.isSuccess()){
            entity.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            entity.setHandleResult(HandleResultEnum.SUCCESS.getCode());
        }else{
            entity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());
            ApiResult<InterceptResponseVO> interceptResult = logisticsBillFeign.interceptBill(dto);
            if(interceptResult.isSuccess()){
                entity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
                entity.setHandleResult(HandleResultEnum.SUCCESS.getCode());
            }else{
                entity.setInterceptStatus(InterceptStatusEnum.FAILURE.getCode());
                entity.setHandleResult(HandleResultEnum.FAILURE.getCode());
                //判断是否不支持线上取消
                if(cancelResult.getCode().equals(-1) && interceptResult.getCode().equals(-1)){
                    msg = "该物流渠道不支持线上发起物流拦截，请线下与物流商沟通后，手动标记拦截结果";
                }else{
                    msg = StrUtil.format("取消订单失败原因：{}；拦截订单失败原因：{}", cancelResult.getMsg(),interceptResult.getMsg());
                }
                isSuccess = false;
            }
        }
        entity.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
        this.updateById(entity);
        if(isSuccess){
            return BatchResultDTO.success(entity.getId(),entity.getCode(),msg);
        }else{
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),msg);
        }
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
