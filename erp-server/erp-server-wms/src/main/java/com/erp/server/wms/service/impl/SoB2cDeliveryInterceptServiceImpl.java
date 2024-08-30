package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cAbnormalTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.rpc.wms.feign.OverseasProviderFeign;
import com.erp.rpc.wms.feign.ThirdWarehouseFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Resource
    private ThirdWarehouseFeign thirdWarehouseFeign;

    @Resource
    private TransferLogisticsFeign transferLogisticsFeign;
    @Autowired
    private SoOutstockDetailServiceImpl soOutstockDetailServiceImpl;
    @Resource
    private PickingListsService pickingListsService;
    @Lazy
    @Resource
    private AsyncService asyncService;

    @Resource
    @Lazy
    private WaveListDetailService waveListDetailService;

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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c发货拦截单" , soB2cDeliveryInterceptEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), soB2cDeliveryInterceptEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        soB2cDeliveryInterceptDetailService.add(addDTO, soB2cDeliveryInterceptEntity.getId());

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
        if(CancelStatusEnum.SUCCESS.getCode().equals(entity.getCancelStatus()) || InterceptStatusEnum.SUCCESS.getCode().equals(entity.getInterceptStatus())){
            throw new ServiceException("订单取消状态：取消成功或物流拦截状态：拦截成功，不支持再次发起物流拦截");
        }

        //已处理不可重复操作
        if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(entity.getHandleStatus()) || SoB2cDeliveryInterceptStatusEnum.CANCEL.getStatus().equals(entity.getHandleStatus())) {
            throw new ServiceException(ApiError.HANDLE_STATUS_IS_HANDLE_OR_CANCEL_NOT);
        }

        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(Collections.singletonList(entity.getSourceId()));
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "销售订单");
        }
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Arrays.asList(entity.getSoId()));
        if (CollectionUtils.isEmpty(soB2cLogisticsEntities)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_LOGISTICS_NOT_EXIST);
        }

        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthFeign.getAuthByChannelId(entity.getLogisticsChannelId());
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_EXIST);
        }

        SoB2cEntity soB2cEntity = soB2cEntityList.get(0);

        //校验中转状态
        if (TransferStatusEnum.SUCCESS.getCode().equals(soB2cEntity.getTransferStatus()) ) {
            List<TransferLogisticsChannelDTO.ListSelectDTO> transferList = transferLogisticsFeign.listByTransferChannelIds(Arrays.asList(soB2cLogisticsEntities.get(0).getTransferLogisticsChannelId()));
            String transferInfo = "";
            if(CollectionUtils.isNotEmpty(transferList)){
                transferInfo = transferList.get(0).getTransferLogisticSupplierName() + "-" +transferList.get(0).getName();
            }
            throw new ServiceException(StrUtil.format("订单{}已经预报给{}，请取消预报后操作",soB2cEntity.getCode(),transferInfo));
        }

        LogisticsBillDTO.CancelBillDTO dto = LogisticsBillDTO.CancelBillDTO.builder()
                .channelId(entity.getLogisticsChannelId())
                .transportNo(entity.getTransportNo())
                .referenceNumber(soB2cEntity.getCode())
                .platformCode(soB2cEntity.getPlatformCode())
                .reason("b2c发货拦截单自动拦截")
                .orderId(entity.getId())
                .shopId(soB2cEntity.getShopId())
                .build();
        //先取消订单，取消订单失败的再拦截订单
        ApiResult<CancelResponseVO> cancelResult = logisticsBillFeign.cancelBill(dto);
        boolean isSuccess = true;
        String msg = "拦截成功";
        if(cancelResult.isSuccess()){
            entity.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
            entity.setInterceptStatus("");

            //取消成功，清空订单运单号，删除物流单
            soB2cFeign.clearB2cLogisticsCode(Arrays.asList(entity.getSourceId()));
        }else{
            entity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());

            ApiResult<InterceptResponseVO> interceptResult = logisticsBillFeign.interceptBill(dto);
            if(interceptResult.isSuccess()){
                entity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
                //取消成功，清空订单运单号，删除物流单
                soB2cFeign.clearB2cLogisticsCode(Arrays.asList(entity.getSourceId()));
//                entity.setHandleResult(HandleResultEnum.SUCCESS.getCode());
            }else{
                String logisticsPlatform = auth.getLogisticsPlatform();
                entity.setInterceptStatus(InterceptStatusEnum.FAILURE.getCode());
                //记录异常原因：拦截失败
                soB2cFeign.updateAbnormalType(entity.getSourceId(), SoB2cAbnormalTypeEnum.INTERCEPT_FAILURE_REJECT.getCode());

                //判断是否不支持线上取消
                if(cancelResult.getCode().equals(-1) && interceptResult.getCode().equals(-1)){
                    msg = "该物流渠道不支持线上发起物流拦截，请线下与物流商沟通后，手动标记拦截结果";
                }else{
                    msg = StrUtil.format("取消订单失败原因：{}；拦截订单失败原因：{}", cancelResult.getMsg(),interceptResult.getMsg());
                }
                isSuccess = false;
            }
        }

        //更新拦截状态
        this.updateById(entity);

        // 操作日志
        String logMsg = StrUtil.format("用户【{}】发起物流拦截,单据【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截单", entity.getCode());
        operateLogService.addModuleOperateLog(logMsg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "处理操作");
        if(isSuccess){
            return BatchResultDTO.success(entity.getId(),entity.getCode(),msg);
        }else{
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),msg);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO interceptResultConfirm(SoB2cDeliveryInterceptDTO.InterceptResultConfirmDTO dto, String id) {
        SoB2cDeliveryInterceptEntity entity = this.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "发货拦截单");
        }
        //已处理不可重复操作
        if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(entity.getHandleStatus())) {
            throw new ServiceException(ApiError.STATUS_IS_HANDLE_NOT_OPERATE);
        }

        LoginUser userInfo = UserContext.getDefaultLoginUser();

        Boolean isSuccess= HandleResultEnum.SUCCESS.getCode().equals(dto.getHandleResult());
        if (isSuccess) {
            //已组包/已中转不可操作拦截成功
            //来源id 就是b2c销售订单id
            String sourceId = entity.getSourceId();
            if (StringUtils.isNotBlank(sourceId)) {
                SoB2cEntity soB2cEntity = soB2cFeign.getById(sourceId);
                if (Objects.nonNull(soB2cEntity)) {
                    //组包状态
                    String packageStatus = soB2cEntity.getPackageStatus();
                    //中转状态
                    String transferStatus = soB2cEntity.getTransferStatus();
                    //已组包
                    String alreadyPackage = PackageStatusEnum.ALREADY.getCode();
                    //已中转
                    String alreadyTransfer = TransferStatusEnum.SUCCESS.getCode();
                    if (alreadyPackage.equals(packageStatus) || alreadyTransfer.equals(transferStatus)) {
                        throw new ServiceException(ApiError.ALREADY_PACKAGE_TRANSFER_NOT_INTERCEPT, entity.getCode());
                    }
                }
            }
        }

        //修改状态
        lambdaUpdate()
                .set(SoB2cDeliveryInterceptEntity::getHandleResult, dto.getHandleResult())
                .set(SoB2cDeliveryInterceptEntity::getHandleRemark, dto.getResultRemark())
                .set(SoB2cDeliveryInterceptEntity::getHandleUserId, userInfo.getUid())
                .set(SoB2cDeliveryInterceptEntity::getHandleUserName, userInfo.getUserName())
                .set(SoB2cDeliveryInterceptEntity::getHandleTime, LocalDateTime.now())
                .set(SoB2cDeliveryInterceptEntity::getHandleStatus, SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus())
                .eq(SoB2cDeliveryInterceptEntity::getId, id)
                .update();


        // 拦截成功后，关联的发货单和销售出库单会作废，库存会自动退回到发货仓
        if (isSuccess) {
            //修改拦截状态，冻结状态
            updateInterceptStatus(entity);
            //反审核销售出库单，并作废
            List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySoIds(Arrays.asList(entity.getSourceId()));
            if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
                //查询已审核的出库单，进行反审核
                List<SoOutstockEntity> soOutstockEntityList = soOutstockEntities.stream().filter(req -> ApproveStatusEnum.APPROVE.equals(req.getApproveStatus())).collect(Collectors.toList());
//                BaseIdsDTO.IdsDTO approveIdDto = new BaseIdsDTO.IdsDTO();
//                approveIdDto.setIds(approveIds);
                if (CollectionUtils.isNotEmpty(soOutstockEntityList)) {
                    soOutstockEntityList.forEach(soOutstockEntity -> {
                        soOutstockService.disApprove(soOutstockEntity, Boolean.FALSE);
                    });
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
            SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryService.getById(entity.getDeliveryId());
            if (ObjectUtil.isNotEmpty(soB2cDelivery)) {
                if (SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(soB2cDelivery.getStatus()) ||
                        SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(soB2cDelivery.getStatus())) {
                    throw new ServiceException(ApiError.ERROR_99125);
                }

                List<WaveListDetailEntity> waveLists = waveListDetailService.listCancelByDeliveryIds(Collections.singletonList(soB2cDelivery.getId()));
                String cancelCodes = waveLists.stream().map(WaveListDetailEntity::getDeliveryCode).collect(Collectors.joining(","));
                if (ObjectUtil.isNotEmpty(cancelCodes)) {
                    throw new ServiceException(ApiError.ERROR_99123, cancelCodes);
                }
                //取消发货库存回滚
                soB2cDeliveryService.rollbackInventory(Collections.singletonList(soB2cDelivery.getId()));
                // 生成波次和拣货中回滚库存
                if (SoB2cDeliveryStatusEnum.GENERATE_WAVE.getCode().equals(soB2cDelivery.getStatus()) ||
                        SoB2cDeliveryStatusEnum.PICKING.getCode().equals(soB2cDelivery.getStatus()) ||
                        SoB2cDeliveryStatusEnum.SHIPPED.getCode().equals(soB2cDelivery.getStatus())) {
                    pickingListsService.deleteBySourceId(Collections.singletonList(soB2cDelivery.getId()));
                    // 移除波次
                    waveListDetailService.moveOut(soB2cDelivery.getId());
                }
            }
        } else {
            //拦截失败的订单正常自动出库流程（第三方仓除外）
            SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
            if(dto.isThirdWarehouse()){
                //修改拦截状态，冻结状态
                interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
                interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
                interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getSoId()));
                soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
            }else{
                SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryService.getById(entity.getDeliveryId());
                if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(soB2cDelivery.getStatus())
                        || SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(soB2cDelivery.getStatus())||
                        SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(soB2cDelivery.getStatus())){
                    throw new ServiceException(ApiError.ERROR_99124);
                }
                if(!SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(soB2cDelivery.getStatus())){
                    soB2cDelivery.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getStatus());
                    soB2cDeliveryService.updateById(soB2cDelivery);
                    interceptUpdateOrderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                    SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
//                if(!soB2cEntity.getIsCancel() && !SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())){
//                    //调用第三方平台SDK发货
//                    PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
//                    platformShipOrderDTO.setSoB2cId(soB2cEntity.getId());
//                    platformShipOrderDTO.setDictPlatform(soB2cEntity.getDictPlatform());
//                    PlatformSaveHandler.shipOrder(platformShipOrderDTO);
//                }
                    //在这里修改拦截状态，冻结状态，因为下面生成销售出库单依赖这个状态
                    interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
                    interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
                    interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getSoId()));
                    interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_FAILURE_REJECT.getCode());
                    soB2cFeign.updateIntercept(interceptUpdateOrderDTO);

                    //扣减冻结库存
                    soB2cDeliveryService.outFreezeVirtualInventory(soB2cDelivery);
                    //生成直接调拨单
                    Boolean isPush = soB2cDeliveryService.pushTransferInfo(soB2cDelivery);
                    if (isPush) {
                        asyncService.asyncGenerateB2cSoOutstock(soB2cEntity.getId());
                    }
                    //更新备注
                    soOutstockService.updateRemarkBySoId(soB2cEntity.getId(),"发货拦截失败");

                    if (!soB2cEntity.getIsCancel() && soB2cFeign.checkPlatformShipOrder(soB2cEntity.getId())) {
                        // 调用第三方平台SDK标记发货(独立事务)
                        String businessDesc = "称重出库";
                        asyncService.asyncShipOrder(soB2cEntity.getId(),
                                soB2cEntity.getCode(),
                                soB2cEntity.getDictPlatform(),
                                soB2cEntity.convertSubmitPlatformUniqueKey(),
                                JSONUtil.toJsonStr(dto),
                                businessDesc, false);
                    } else {
                        log.warn("【{}】未达到条件:忽略标记平台发货", soB2cEntity.getCode());
                    }
                }else{
                    //修改拦截状态，冻结状态
                    interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
                    interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
                    interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getSoId()));
                    interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_FAILURE_REJECT.getCode());
                    soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
                }
            }
        }

        // 操作日志
        String logMsg = StrUtil.format("用户【{}】物流拦截结果确认【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截单", entity.getCode());
        operateLogService.addModuleOperateLog(logMsg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), entity.getId(), "处理操作");

        return BatchResultDTO.success(entity.getId(),entity.getCode(), "拦截结果确认");
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateInterceptStatus(SoB2cDeliveryInterceptEntity entity) {
        SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
        interceptUpdateOrderDTO.setIsIntercept(Boolean.TRUE);
        interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
        interceptUpdateOrderDTO.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT.getStatus());
        interceptUpdateOrderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_SUCCESS_REJECT.getCode());
        interceptUpdateOrderDTO.setIds(Arrays.asList(entity.getSoId()));
        soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
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
    public List<SoB2cDeliveryInterceptEntity> listByDeliveryIds(List<String> deliveryIds) {
        if (CollectionUtils.isEmpty(deliveryIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(SoB2cDeliveryInterceptEntity::getDeliveryId, deliveryIds).list();
    }

    @Override
    public Boolean updateHandleStatus(List<String> sourceIds, String status) {
        if (CollectionUtils.isEmpty(sourceIds)) {
            return Boolean.FALSE;
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        return lambdaUpdate()
                .in(SoB2cDeliveryInterceptEntity::getSourceId, sourceIds)
                .set(SoB2cDeliveryInterceptEntity::getHandleStatus, status)
                .set(SoB2cDeliveryInterceptEntity::getHandleUserId, userInfo.getUid())
                .set(SoB2cDeliveryInterceptEntity::getHandleUserName, userInfo.getUserName())
                .set(SoB2cDeliveryInterceptEntity::getHandleTime, LocalDateTime.now())
                .update();
    }

    @Override
    public List<SoB2cDeliveryInterceptEntity> listByStatus(String code) {
        return lambdaQuery().eq(SoB2cDeliveryInterceptEntity::getHandleStatus,code).list();
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

        SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryService.getNotCancelBySoId(soB2cDeliveryInterceptEntity.getSourceId());
        if(Objects.nonNull(soB2cDelivery)){
            soB2cDeliveryInterceptEntity.setDeliveryId(soB2cDelivery.getId());
            soB2cDeliveryInterceptEntity.setSoDeliveryCode(soB2cDelivery.getCode());
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
