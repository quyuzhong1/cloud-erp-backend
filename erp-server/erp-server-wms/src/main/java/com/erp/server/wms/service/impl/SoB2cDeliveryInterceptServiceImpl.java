package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;
import javax.json.JsonObject;

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

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

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

        //触发物流拦截
        this.logisticsIntercept(soB2cDeliveryInterceptEntity.getId());

        return new BaseResultDTO.AddDTO(soB2cDeliveryInterceptEntity.getId(), code);
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
            //单据类型
            record.setBillTypeName(OrderTypeEnum.getName(record.getBillType()));
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
        SoB2cDeliveryInterceptEntity interceptEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到B2C发货拦截单数据"));
        SoB2cDeliveryInterceptDTO.ViewDTO data = BeanMapperUtils.map(SoB2cDeliveryInterceptDTO.ViewDTO.class, interceptEntity);
        //发货单详情
        List<SoB2cDeliveryInterceptDetailEntity> detailList = soB2cDeliveryInterceptDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, detailList);
        return data;
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
                .referenceNumber(soB2cEntity.getCode())
                .reason("b2c发货拦截单自动拦截")
                .build();
        //先取消订单，取消订单失败的再拦截订单
        ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(dto);
        boolean isSuccess = true;
        String msg = "拦截成功";
        if(cancelResult.isSuccess()){
            entity.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            entity.setInterceptStatus("");
        }else{
            entity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());
            ApiResult<InterceptResponseVO> interceptResult = logisticsBillFeign.interceptBill(dto);
            if(interceptResult.isSuccess()){
                entity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
//                entity.setHandleResult(HandleResultEnum.SUCCESS.getCode());
            }else{
                LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(entity.getLogisticsChannelId());
                String logisticsPlatform = auth.getLogisticsPlatform();
                //顺丰没有拦截，不更新拦截状态
                if (!LogisticsPlatformEnum.SF_EXPRESS.getCode().equals(logisticsPlatform)) {
                    entity.setInterceptStatus(InterceptStatusEnum.FAILURE.getCode());
                }
//                entity.setHandleResult(HandleResultEnum.FAILURE.getCode());
                //异常订单
                SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
                addError.setType(InterceptStatusEnum.FAILURE.getCode());
                addError.setParamJson(JSONObject.toJSONString(dto));
                addError.setReturnJson("");
                addError.setMainId(entity.getSourceId());
                addError.setMessage(InterceptStatusEnum.FAILURE.getName());
                soB2cFeign.addSoB2cError(addError);

                //判断是否不支持线上取消
                if(cancelResult.getCode().equals(-1) && interceptResult.getCode().equals(-1)){
                    msg = "该物流渠道不支持线上发起物流拦截，请线下与物流商沟通后，手动标记拦截结果";
                }else{
                    msg = StrUtil.format("取消订单失败原因：{}；拦截订单失败原因：{}", cancelResult.getMsg(),interceptResult.getMsg());
                }
                isSuccess = false;
            }
        }
        LoginUser userInfo = commonService.getUserInfo();
        entity.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.WAIT_HANDLE.getStatus());
        entity.setHandleUserId(userInfo.getUid());
        entity.setHandleUserName(userInfo.getUserName());
        entity.setHandleTime(LocalDateTime.now());
        this.updateById(entity);

        // 操作日志
        String logMsg = StrUtil.format("用户【{}】发起物流拦截,单据【{}】", commonService.getUserInfo().getUserName(), "发货拦截单", entity.getCode());
        operateLogService.addModuleOperateLog(logMsg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "处理操作");
        if(isSuccess){
            return BatchResultDTO.success(entity.getId(),entity.getCode(),msg);
        }else{
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),msg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO interceptResultConfirm(SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto, String id) {
        SoB2cDeliveryInterceptEntity entity = this.getById(id);
        List<SoB2cDeliveryInterceptDetailEntity> detailEntityList = soB2cDeliveryInterceptDetailService.listByMainIds(Arrays.asList(id));
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货拦截单");
        }

        //修改状态
        lambdaUpdate()
                .set(SoB2cDeliveryInterceptEntity::getHandleResult, dto.getHandleResult())
                .set(SoB2cDeliveryInterceptEntity::getHandleRemark, dto.getResultRemark())
                .set(SoB2cDeliveryInterceptEntity::getHandleTime, LocalDateTime.now())
                .set(SoB2cDeliveryInterceptEntity::getHandleStatus, SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus())
                .eq(SoB2cDeliveryInterceptEntity::getId, id)
                .update();

        String handleResult = HandleResultEnum.FAILURE.getName();
        String soB2cErrorType = SoB2cErrorTypeEnum.INTERCEPT_FAIL.getCode();
        // 拦截成功后，关联的发货单和销售出库单会作废，库存会自动退回到发货仓
        if (HandleResultEnum.SUCCESS.getCode().equals(dto.getHandleResult())) {

            //反审核销售出库单，并作废
            List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySoIds(Arrays.asList(entity.getSourceId()));
            if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
                //查询已审核的出库单，进行反审核
                List<String> approveIds = soOutstockEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
                BaseIdsDTO.IdsDTO approveIdDto = new BaseIdsDTO.IdsDTO();
                approveIdDto.setIds(approveIds);
                if (CollectionUtils.isNotEmpty(approveIds)) {
                    soOutstockService.disApprove(approveIdDto, Boolean.TRUE);
                }

                //查询已提交的出库单，进行撤销
                List<String> approveIng = soOutstockEntities.stream().filter(req -> ApproveStatusEnum.APPROVE_ING.equals(req.getApproveStatus())).map(req -> req.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(approveIng)) {
                    soOutstockService.cancelProcess(approveIng);
                }

                //反审核后删除
                List<String> ids = soOutstockEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
                soOutstockService.delete(ids);
            }

            //回滚冻结库存
            List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryService.listBySourceIds(Arrays.asList(entity.getSourceId()));
            if (CollectionUtils.isNotEmpty(soB2cDeliveryEntities)) {
                List<String> ids = soB2cDeliveryEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
                soB2cDeliveryService.rollbackInventory(ids);
                soB2cDeliveryService.updateStatus(ids, SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
            }
            handleResult = HandleResultEnum.SUCCESS.getName();
            soB2cErrorType = SoB2cErrorTypeEnum.INTERCEPT_SUCCESS.getCode();

            //修改订单状态
            soB2cFeign.updateSoB2cStatus(Arrays.asList(entity.getSourceId()), ApproveStatusEnum.WAIT_SUBMIT.getStatus());

        } else {
            handleResult = HandleResultEnum.FAILURE.getName();
            soB2cErrorType = SoB2cErrorTypeEnum.INTERCEPT_FAIL.getCode();
        }


        //异常订单
        SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO();
        addError.setType(soB2cErrorType);
        addError.setParamJson(JSONObject.toJSONString(dto));
        addError.setReturnJson("");
        addError.setMainId(entity.getSourceId());
        addError.setMessage(handleResult);
        soB2cFeign.addSoB2cError(addError);

        // 操作日志
        String logMsg = StrUtil.format("用户【{}】物流拦截结果确认【{}】", commonService.getUserInfo().getUserName(), "发货拦截单", entity.getCode());
        operateLogService.addModuleOperateLog(logMsg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "处理操作");

        return BatchResultDTO.success(entity.getId(),entity.getCode(), "拦截结果确认");
    }

    @Override
    public List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> listIsIntercept(List<String> sourceIdList) {
        if (CollectionUtils.isEmpty(sourceIdList)) {
            return Collections.emptyList();
        }
        List<SoB2cDeliveryInterceptDTO.IsInterceptDTO> dtoList = new ArrayList<>();
        List<SoB2cDeliveryInterceptEntity> list = lambdaQuery()
                .in(SoB2cDeliveryInterceptEntity::getSourceId, sourceIdList)
                .ne(SoB2cDeliveryInterceptEntity::getHandleStatus, SoB2cDeliveryInterceptStatusEnum.CANCEL.getCode())
                .list();
        for (SoB2cDeliveryInterceptEntity interceptEntity : list) {
            SoB2cDeliveryInterceptDTO.IsInterceptDTO isInterceptDTO = new SoB2cDeliveryInterceptDTO.IsInterceptDTO();
            isInterceptDTO.setId(interceptEntity.getSoId());
            isInterceptDTO.setHandleResult(interceptEntity.getHandleResult());
            //如果结果确认是拦截成功,返回拦截标识
            if (HandleResultEnum.SUCCESS.getCode().equals(interceptEntity.getHandleResult())) {
                isInterceptDTO.setIsIntercept(Boolean.TRUE);
                dtoList.add(isInterceptDTO);
                continue;
            }
            //如果结果确认是拦截失败,取消拦截标识
            if (HandleResultEnum.FAILURE.getCode().equals(interceptEntity.getHandleResult())) {
                isInterceptDTO.setIsIntercept(Boolean.FALSE);
            }
            //如果还未手动确认拦截结果，按平台处理结果
            if (StringUtils.isBlank(interceptEntity.getHandleResult())
                    && !CancelStatusEnum.FAILURE.getCode().equals(interceptEntity.getCancelStatus())
                    && !InterceptStatusEnum.FAILURE.getCode().equals(interceptEntity.getInterceptStatus())
            ) {
                isInterceptDTO.setIsIntercept(Boolean.TRUE);
                dtoList.add(isInterceptDTO);
                continue;
            }
        }
        return dtoList;
    }

    @Override
    public List<SoB2cDeliveryInterceptEntity> listBySourceIds(List<String> sourceIds) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cDeliveryInterceptEntity::getSourceId, sourceIds).list();
    }

    @Override
    public Boolean updateHandleStatus(List<String> sourceIds, String status) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }
        return lambdaUpdate()
                .in(SoB2cDeliveryInterceptEntity::getSourceId, sourceIds)
                .set(SoB2cDeliveryInterceptEntity::getHandleStatus, status)
                .update();
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

    /**
     * 详情字段映射
     * @Author Luo_WG
     * @Date 2023/12/25 17:00
     * @param data
     * @param detailList
     * @return void
     **/
    private void fillOne(SoB2cDeliveryInterceptDTO.ViewDTO data, List<SoB2cDeliveryInterceptDetailEntity> detailList) {
        //查询产品信息
        List<String> skuIdList = detailList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> productDetailEntityList = plmTaskFeign.getByIdList(skuIdList);
        //处理状态名称
        data.setHandleStatusName(SoB2cDeliveryInterceptStatusEnum.getName(data.getHandleStatus()));
        //详情字段设置
        List<SoB2cDeliveryInterceptDetailDTO.ViewDTO> viewDetailList = BeanMapper.copyList(detailList, SoB2cDeliveryInterceptDetailDTO.ViewDTO.class);
        for (SoB2cDeliveryInterceptDetailDTO.ViewDTO viewDTO : viewDetailList) {
            //产品信息
            ProductDetailEntity entity = productDetailEntityList.stream().filter(req -> req.getId().equals(viewDTO.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(entity)) {
                viewDTO.setProductName(entity.getName());
                viewDTO.setWarehouseLocation(entity.getWarehouseLocation());
            }
        }
        data.setDetailList(viewDetailList);
    }
}
