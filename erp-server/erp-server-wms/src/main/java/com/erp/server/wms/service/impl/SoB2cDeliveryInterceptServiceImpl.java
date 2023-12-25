package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryInterceptEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptMapper;
import com.erp.server.wms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
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

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cDeliveryInterceptDetailService soB2cDeliveryInterceptDetailService;

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
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_FHLJ);
        soB2cDeliveryInterceptEntity.setCode(code);
        boolean save = super.save(soB2cDeliveryInterceptEntity);
        if(!save) {
            throw new ServiceException("b2c发货拦截单保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "b2c发货拦截单" , soB2cDeliveryInterceptEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), soB2cDeliveryInterceptEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        soB2cDeliveryInterceptDetailService.add(addDTO, soB2cDeliveryInterceptEntity.getId());
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
        operateLogService.addModuleOperateLogByObj(old, soB2cDeliveryInterceptEntity, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), soB2cDeliveryInterceptEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SoB2cDeliveryInterceptDTO.TabListDTO> tabList(PermissionsDTO param) {
        SoB2cDeliveryInterceptDTO.PagingParamDTO searchParam = new SoB2cDeliveryInterceptDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<SoB2cDeliveryInterceptDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = SoB2cDeliveryInterceptStatusEnum.getStatusList();
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(SoB2cDeliveryInterceptDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new SoB2cDeliveryInterceptDTO.TabListDTO(status, 0));
            }
        });
        list.add(new SoB2cDeliveryInterceptDTO.TabListDTO("all", list.stream().mapToInt(SoB2cDeliveryInterceptDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public PagingVO<SoB2cDeliveryInterceptDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryInterceptDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<SoB2cDeliveryInterceptDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    private void fillList(List<SoB2cDeliveryInterceptDTO.ListDTO> records) {
        List<String> skuIdList = records.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> detailEntityList = plmTaskFeign.getByIdList(skuIdList);
        for (SoB2cDeliveryInterceptDTO.ListDTO record : records) {
            //取消状态名称
            record.setCancelStatusName(CancelStatusEnum.getName(record.getCancelStatus()));
            //处理结果中文
            record.setHandleResultName(HandleResultEnum.getName(record.getHandleResult()));
            //处理状态名称
            record.setHandleStatusName(SoB2cDeliveryInterceptStatusEnum.getName(record.getHandleStatus()));
            //拦截状态名称
            record.setInterceptStatusName(InterceptStatusEnum.getName(record.getInterceptStatus()));
            //产品信息
            ProductDetailEntity productDetailEntity = detailEntityList.stream().filter(req -> req.getId().equals(record.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(productDetailEntity)) {
                record.setSkuNo(productDetailEntity.getSkuNo());
                record.setProductName(productDetailEntity.getName());
            }

        }
    }

    @Override
    public SoB2cDeliveryInterceptDTO.ViewDTO view(String id) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
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
        //查询关联的出库单匹配单号
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySoIds(Arrays.asList(soB2cDeliveryInterceptEntity.getSourceId()));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            soB2cDeliveryInterceptEntity.setSoOutstockCode(soOutstockEntities.get(MathUtil.ZERO).getCode());
        }
        //查询管理的发货单匹配单号
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryService.listBySourceIds(Arrays.asList(soB2cDeliveryInterceptEntity.getSourceId()));
        if (CollectionUtils.isNotEmpty(soB2cDeliveryEntities)) {
            soB2cDeliveryInterceptEntity.setSoDeliveryCode(soB2cDeliveryEntities.get(MathUtil.ZERO).getCode());
        }

    }
}
