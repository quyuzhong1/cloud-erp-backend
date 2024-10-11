package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
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
import com.erp.model.dmp.dto.ThirdMappingDTO;
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
import com.erp.model.wms.dto.*;
import com.erp.model.wms.dto.inventory.InventoryTransferDTO;
import com.erp.model.wms.dto.inventory.TransferDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpThirdMappingFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsBillFeign;
import com.erp.rpc.tms.feign.TransferLogisticsFeign;
import com.erp.server.wms.mapper.SoB2cDeliveryInterceptMapper;
import com.erp.server.wms.service.*;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.CreateOtherStockinRequest;
import com.sdk.wangdian.sdk.api.wms.stockout.dto.CreateOtherStockoutRequest;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Resource
    private OperateLogService operateLogService;
    @Resource
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
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private TransferLogisticsFeign transferLogisticsFeign;
    @Resource
    private PickingListsService pickingListsService;
    @Lazy
    @Resource
    private AsyncService asyncService;

    @Resource
    @Lazy
    private WaveListDetailService waveListDetailService;

    @Resource
    @Lazy
    private SoB2cDeliveryInterceptServiceImpl service;

    @Resource
    private CfgSettingService cfgSettingService;

    @Resource
    private DmpThirdMappingFeign dmpThirdMappingFeign;

    @Resource
    private InventoryTransCoreService inventoryTransCoreService;
    @Resource
    private AbstractWdtService abstractWdtService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private WarehouseService warehouseService;


    @Resource
    private WaveListService waveListService;

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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "b2c发货拦截单" , soB2cDeliveryInterceptEntity.getCode());
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
        List<SoB2cDeliveryInterceptDetailEntity> detailList = soB2cDeliveryInterceptDetailService.listByMainIds(Collections.singletonList(id));
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
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(Collections.singletonList(entity.getSoId()));
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
            List<TransferLogisticsChannelDTO.ListSelectDTO> transferList = transferLogisticsFeign.listByTransferChannelIds(Collections.singletonList(soB2cLogisticsEntities.get(0).getTransferLogisticsChannelId()));
            String transferInfo = "";
            if(CollectionUtils.isNotEmpty(transferList)){
                transferInfo = transferList.get(0).getTransferLogisticSupplierName() + "-" +transferList.get(0).getName();
            }
            throw new ServiceException(CharSequenceUtil.format("订单{}已经预报给{}，请取消预报后操作",soB2cEntity.getCode(),transferInfo));
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
            soB2cFeign.clearB2cLogisticsCode(Collections.singletonList(entity.getSourceId()));
        }else{
            entity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());

            ApiResult<InterceptResponseVO> interceptResult = logisticsBillFeign.interceptBill(dto);
            if(interceptResult.isSuccess()){
                entity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
                //取消成功，清空订单运单号，删除物流单
                soB2cFeign.clearB2cLogisticsCode(Collections.singletonList(entity.getSourceId()));
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
                    msg = CharSequenceUtil.format("取消订单失败原因：{}；拦截订单失败原因：{}", cancelResult.getMsg(),interceptResult.getMsg());
                }
                isSuccess = false;
            }
        }

        //更新拦截状态
        this.updateById(entity);

        // 操作日志
        String logMsg = CharSequenceUtil.format("用户【{}】发起物流拦截,单据【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截单", entity.getCode());
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
            if (CharSequenceUtil.isNotBlank(sourceId)) {
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
            List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySoIds(Collections.singletonList(entity.getSourceId()));
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
                    waveListDetailService.moveOut(soB2cDelivery.getId(), true);
                }
            }
        } else {
            //拦截失败的订单正常自动出库流程（第三方仓除外）
            SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
            if(dto.isThirdWarehouse()){
                //修改拦截状态，冻结状态
                interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
                interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
                interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSoId()));
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
                    interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSoId()));
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
                    interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSoId()));
                    interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_FAILURE_REJECT.getCode());
                    soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
                }
            }
        }

        // 操作日志
        String logMsg = CharSequenceUtil.format("用户【{}】物流拦截结果确认【{}】", UserContext.getDefaultLoginUser().getUserName(), "发货拦截单", entity.getCode());
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
        interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSoId()));
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
            if (CharSequenceUtil.isBlank(interceptEntity.getHandleResult())
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO handleSuccess(SoB2cDeliveryEntity entity, String interceptId, List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList, String remark) {
        SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity = this.getById(interceptId);
        SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSourceId());
        BaseIdsDTO.IdsDTO idDto = new BaseIdsDTO.IdsDTO();
        idDto.setIds(Collections.singletonList(soB2cEntity.getId()));

        LoginUser userInfo = UserContext.getDefaultLoginUser();

        //根据发货单状态处理
        if(SoB2cDeliveryStatusEnum.GENERATE_WAVE.getCode().equals(entity.getStatus())){
            //删除波次
            waveListDetailService.moveOut(entity.getId(), true);
        }
        if(!SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(entity.getStatus())
            && !(SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode().equals(entity.getStatus()) &&  AbnormalCauseEnum.GENERATION_WAVE.getCode().equals(entity.getAbnormalCause()))){
            //新增相反冻结库存,推送旺店通
            service.addReverseInventory(entity,soB2cDeliveryInterceptEntity,interceptInventoryDTOList);
            //删除拣货单
            pickingListsService.deleteBySourceId(Collections.singletonList(entity.getId()));

        }
        //释放虚拟库存
        soB2cDeliveryService.addUsableVirtualInventory(Collections.singletonList(entity));
        //更新发货单状态
        soB2cDeliveryService.updateStatus(Collections.singletonList(entity.getId()), SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getStatus());
        //更新拦截单状态
        soB2cDeliveryInterceptEntity.setHandleResult(HandleResultEnum.SUCCESS.getCode());
        soB2cDeliveryInterceptEntity.setHandleUserId(userInfo.getUid());
        soB2cDeliveryInterceptEntity.setHandleRemark(remark);
        soB2cDeliveryInterceptEntity.setHandleUserName(userInfo.getUserName());
        soB2cDeliveryInterceptEntity.setHandleTime(LocalDateTime.now());
        soB2cDeliveryInterceptEntity.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
        //取消保宏预报
        if (TransferStatusEnum.SUCCESS.getCode().equals(soB2cEntity.getTransferStatus())) {
            ApiResult<List<BatchResultDTO>> cancelOrderForecastResult = soB2cFeign.cancelOrderForecast(idDto);
            if(!cancelOrderForecastResult.isSuccess()){
                throw new ServiceException("订单取消保宏预报失败,无法处理拦截成功");
            }
        }
        //取消物流单，不管结果，继续向下执行
        if(!CancelStatusEnum.SUCCESS.getCode().equals(soB2cDeliveryInterceptEntity.getCancelStatus())
         && !InterceptStatusEnum.SUCCESS.getCode().equals(soB2cDeliveryInterceptEntity.getInterceptStatus())){
            ApiResult<List<BatchResultDTO>> cancelLogisticResult = soB2cFeign.cancelLogistic(idDto);
            String logisticLog;
            if(cancelLogisticResult.isSuccess()){
                soB2cDeliveryInterceptEntity.setCancelStatus(CancelStatusEnum.SUCCESS.getCode());
                soB2cDeliveryInterceptEntity.setInterceptStatus(InterceptStatusEnum.SUCCESS.getCode());
                logisticLog = "取消物流单-发起拦截，取消物流单成功";
            }else{
                soB2cDeliveryInterceptEntity.setCancelStatus(CancelStatusEnum.FAILURE.getCode());
                soB2cDeliveryInterceptEntity.setInterceptStatus(InterceptStatusEnum.FAILURE.getCode());
                logisticLog = "取消物流单-发起拦截，取消物流单失败";
            }
            // 操作日志
            operateLogService.addModuleOperateLog(logisticLog, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), interceptId, "取消物流单");

        }

        this.updateById(soB2cDeliveryInterceptEntity);
        operateLogService.addModuleOperateLog("操作拦截成功，备注："+remark, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), interceptId, "拦截成功");
        //更新销售订单
        SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
        interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
        interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
        interceptUpdateOrderDTO.setApproveStatus(ApproveStatusEnum.REJECT.getStatus());
        interceptUpdateOrderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_SUCCESS_REJECT.getCode());
        interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSourceId()));
        soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
        return BatchResultDTO.success(soB2cDeliveryInterceptEntity.getId(),soB2cDeliveryInterceptEntity.getCode(),"处理成功");
    }

    @Override
    public List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptSuccessView(List<String> ids) {
        //过滤掉不需要显示
        List<SoB2cDeliveryInterceptEntity> soB2cDeliveryInterceptEntityList = this.listByIds(ids);
        //已处理不可重复操作
        if (soB2cDeliveryInterceptEntityList.stream().anyMatch(v->v.getHandleStatus().equals(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus()))) {
            throw new ServiceException("已处理不可重复操作");
        }
        List<String> deliveryIds = soB2cDeliveryInterceptEntityList.stream().map(SoB2cDeliveryInterceptEntity::getDeliveryId).distinct().collect(Collectors.toList());
        List<SoB2cDeliveryEntity> deliveryEntityList = soB2cDeliveryService.listByIds(deliveryIds);
        deliveryEntityList = deliveryEntityList.stream().filter(v-> v.getStatus().equals(SoB2cDeliveryStatusEnum.PICKING.getCode())
                || (v.getStatus().equals(SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getCode()) && !v.getAbnormalCause().equals(AbnormalCauseEnum.GENERATION_WAVE.getCode()))).collect(Collectors.toList());
        List<String> filterDeliveryIds = deliveryEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        soB2cDeliveryInterceptEntityList = soB2cDeliveryInterceptEntityList.stream().filter(v->filterDeliveryIds.contains(v.getDeliveryId())).collect(Collectors.toList());
        List<String> queryIds = soB2cDeliveryInterceptEntityList.stream().map(v->v.getId()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(queryIds)){
            return new ArrayList<>();
        }
        return baseMapper.getInterceptInventoryDTOList(queryIds);
    }

    @Override
    public BatchResultDTO interceptSuccess(SoB2cDeliveryInterceptDTO.InterceptSuccessDTO dto, String id) {
        List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList = dto.getInterceptInventoryDTOList();
        interceptInventoryDTOList = interceptInventoryDTOList.stream().filter(v->v.getId().equals(id)).collect(Collectors.toList());
        SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity = this.getById(id);
        //已处理不可重复操作
        if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(soB2cDeliveryInterceptEntity.getHandleStatus())) {
            return BatchResultDTO.fail(soB2cDeliveryInterceptEntity.getId(),soB2cDeliveryInterceptEntity.getCode(),"已处理不可重复操作");
        }
        SoB2cDeliveryEntity soB2cDeliveryEntity = soB2cDeliveryService.getNotCancelBySoId(soB2cDeliveryInterceptEntity.getSoId());
        if(Objects.isNull(soB2cDeliveryEntity)){
            return BatchResultDTO.fail(soB2cDeliveryInterceptEntity.getId(),soB2cDeliveryInterceptEntity.getCode(),"查询不到发货单");
        }
        return service.handleSuccess(soB2cDeliveryEntity,id,interceptInventoryDTOList, dto.getResultRemark());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO interceptFailure(String id, Boolean isAutoOut, String remark) {
        SoB2cDeliveryInterceptEntity entity = Optional.ofNullable(this.getById(id)).orElseThrow(()->new ServiceException("拦截单为空"));
        //已处理不可重复操作
        if (SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus().equals(entity.getHandleStatus())) {
            return BatchResultDTO.fail(entity.getId(),entity.getCode(),"已处理不可重复操作");
        }
        LoginUser userInfo = UserContext.getDefaultLoginUser();

        SoB2cDeliveryEntity soB2cDelivery = soB2cDeliveryService.getById(entity.getDeliveryId());
        if (SoB2cDeliveryStatusEnum.WAIT_HANDLE.getCode().equals(soB2cDelivery.getStatus())
                || SoB2cDeliveryStatusEnum.CANCEL_DELIVERY.getCode().equals(soB2cDelivery.getStatus())){
            throw new ServiceException(ApiError.ERROR_99124);
        }
        //更新拦截单状态
        entity.setHandleResult(HandleResultEnum.FAILURE.getCode());
        entity.setHandleUserId(userInfo.getUid());
        entity.setHandleUserName(userInfo.getUserName());
        entity.setHandleRemark(remark);
        entity.setHandleTime(LocalDateTime.now());
        entity.setHandleStatus(SoB2cDeliveryInterceptStatusEnum.HANDLE.getStatus());
        this.updateById(entity);
        SoB2cDTO.InterceptUpdateOrderDTO interceptUpdateOrderDTO = new SoB2cDTO.InterceptUpdateOrderDTO();
        interceptUpdateOrderDTO.setIsIntercept(Boolean.FALSE);
        interceptUpdateOrderDTO.setIsFrozen(Boolean.FALSE);
        interceptUpdateOrderDTO.setIds(Collections.singletonList(entity.getSoId()));
        interceptUpdateOrderDTO.setAbnormalType(SoB2cAbnormalTypeEnum.INTERCEPT_FAILURE_REJECT.getCode());
        if(!SoB2cDeliveryStatusEnum.SHIPPED.getStatus().equals(soB2cDelivery.getStatus()) && isAutoOut){
            soB2cDelivery.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getStatus());
            soB2cDeliveryService.updateById(soB2cDelivery);
            //波次列表波次状态自动变更
            waveListService.waveListStatusAutoChange(soB2cDelivery.getId());

            //更新销售订单,在这里修改拦截状态，冻结状态，因为下面生成销售出库单依赖这个状态
            interceptUpdateOrderDTO.setBillStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
            soB2cFeign.updateIntercept(interceptUpdateOrderDTO);

            SoB2cEntity soB2cEntity = soB2cFeign.getById(entity.getSoId());
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
                        id,
                        businessDesc, false);
            } else {
                log.warn("【{}】未达到条件:忽略标记平台发货", soB2cEntity.getCode());
            }
        }else{
            soB2cFeign.updateIntercept(interceptUpdateOrderDTO);
        }

        operateLogService.addModuleOperateLog("操作拦截失败，备注："+remark, ModuleTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode(), entity.getId(), "拦截失败");
        return BatchResultDTO.success(entity.getId(),entity.getCode(),"成功");
    }

    /**
     * 拦截单新增相反冻结库存
     * @param entity
     * @param soB2cDeliveryInterceptEntity
     * @param interceptInventoryDTOList
     */
    public void addReverseInventory(SoB2cDeliveryEntity entity, SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity, List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList) {

        List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> originList = this.baseMapper.getInterceptInventoryDTOList(Collections.singletonList(soB2cDeliveryInterceptEntity.getId()));
        //为空取拣货单信息
        if(CollectionUtils.isEmpty(interceptInventoryDTOList)){
            interceptInventoryDTOList = BeanUtil.copyToList(originList,SoB2cDeliveryInterceptDTO.InterceptInventoryDTO.class);
        }
        if(CollectionUtils.isEmpty(interceptInventoryDTOList)){
            return;
        }
        List<String> warehouseIds = interceptInventoryDTOList.stream().map(SoB2cDeliveryInterceptDTO.InterceptInventoryDTO::getWarehouseId).collect(Collectors.toList());
        warehouseIds.addAll(originList.stream().map(SoB2cDeliveryInterceptDTO.InterceptInventoryDTO::getWarehouseId).collect(Collectors.toList()));
        List<WarehouseEntity> warehouseEntityList = warehouseService.listByIds(warehouseIds);
        //从原仓冻结到可用，如果原仓与用户选择的仓库不一致，新增调拨
        List<TransferInfoDTO.AddDTO> transferInfoList = new ArrayList<>();
        List<TransferDTO> transferDTOList = new ArrayList<>();
        for (SoB2cDeliveryInterceptDTO.InterceptInventoryDTO interceptInventoryDTO : interceptInventoryDTOList) {
            SoB2cDeliveryInterceptDTO.InterceptInventoryDTO origin = originList.stream().filter(v->v.getPickDetailId().equals(interceptInventoryDTO.getPickDetailId())).findFirst().orElse(null);
            if(Objects.isNull(origin)){
                continue;
            }
            TransferDTO transferDTO = new TransferDTO();
            transferDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY_INTERCEPT);
            transferDTO.setSourceId(soB2cDeliveryInterceptEntity.getId());
            transferDTO.setSourceCode(soB2cDeliveryInterceptEntity.getCode());
            transferDTO.setBillDate(LocalDate.now());
            transferDTO.setCurWarehouseId(origin.getWarehouseId());
            transferDTO.setCurWarehouseLocation(origin.getWarehouseLocation());
            transferDTO.setTargetWarehouseId(origin.getWarehouseId());
            if(origin.getWarehouseId().equals(interceptInventoryDTO.getWarehouseId())){
                transferDTO.setTargetWarehouseLocation(interceptInventoryDTO.getWarehouseLocation());
            }else{
                transferDTO.setTargetWarehouseLocation(origin.getWarehouseLocation());
            }
            transferDTO.setQty(origin.getQty());
            transferDTO.setSkuId(origin.getSkuId());
            transferDTO.setSkuNo(origin.getSkuNo());
            transferDTO.setWarehouseId(origin.getWarehouseId());
            transferDTO.setInventoryStatus(InventoryStatusEnum.USABLE);
            transferDTOList.add(transferDTO);

            if(!origin.getWarehouseId().equals(interceptInventoryDTO.getWarehouseId())){
                TransferInfoDetailDTO.AddDTO transferDetailAddDTO = new TransferInfoDetailDTO.AddDTO();
                transferDetailAddDTO.setSkuId(interceptInventoryDTO.getSkuId());
                transferDetailAddDTO.setSkuNo(interceptInventoryDTO.getSkuNo());
                transferDetailAddDTO.setQty(interceptInventoryDTO.getQty());
                transferDetailAddDTO.setSourceDetailId(interceptInventoryDTO.getInterceptDetailId());
                transferDetailAddDTO.setInWarehouseId(interceptInventoryDTO.getWarehouseId());
                transferDetailAddDTO.setInWarehouseLocation(interceptInventoryDTO.getWarehouseLocation());
                transferDetailAddDTO.setOutWarehouseId(origin.getWarehouseId());
                transferDetailAddDTO.setOutWarehouseLocation(origin.getWarehouseLocation());
                WarehouseEntity inWarehouseEntity = warehouseEntityList.stream().filter(v->v.getId().equals(interceptInventoryDTO.getWarehouseId())).findFirst().orElseThrow(()->new ServiceException("调入仓库实体为空"));
                WarehouseEntity outWarehouseEntity = warehouseEntityList.stream().filter(v->v.getId().equals(origin.getWarehouseId())).findFirst().orElseThrow(()->new ServiceException("调出仓库实体为空"));

                TransferInfoDTO.AddDTO addDTO = transferInfoList.stream().filter(v->v.getSourceId().equals(interceptInventoryDTO.getId()) && v.getInOrgId().equals(inWarehouseEntity.getOrgId())).findFirst().orElse(null);
                if(Objects.isNull(addDTO)){
                    addDTO = new TransferInfoDTO.AddDTO();
                    addDTO.setSourceType(SourceTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode());
                    addDTO.setBillDate(LocalDate.now());
                    addDTO.setTransferDirection(TransferDirectionEnum.ORDINARY.getCode());
                    addDTO.setInOrgId(inWarehouseEntity.getOrgId());
                    addDTO.setOutOrgId(outWarehouseEntity.getOrgId());
                    if (inWarehouseEntity.getOrgId().equals(outWarehouseEntity.getOrgId()))  {
                        addDTO.setType(TransferTypeEnum.IN_ORG.getCode());
                    } else {
                        addDTO.setType(TransferTypeEnum.CROSS_ORG.getCode());
                    }
                    addDTO.setSourceId(interceptInventoryDTO.getId());
                    addDTO.setSourceCode(interceptInventoryDTO.getInterceptCode());
                    addDTO.setRemark(String.format("发货拦截单【%s】拦截成功自动创建", interceptInventoryDTO.getInterceptCode()));
                    List<TransferInfoDetailDTO.AddDTO> detailList = new ArrayList<>();
                    detailList.add(transferDetailAddDTO);
                    addDTO.setDetailList(detailList);
                    transferInfoList.add(addDTO);
                }else{
                    List<TransferInfoDetailDTO.AddDTO> detailList = addDTO.getDetailList();
                    detailList.add(transferDetailAddDTO);
                    addDTO.setDetailList(detailList);
                }
            }
        }
        if(CollectionUtils.isNotEmpty(transferDTOList)){
            InventoryTransferDTO inventoryTransferDTO = new InventoryTransferDTO();
            inventoryTransferDTO.setParamList(transferDTOList);
            inventoryTransferDTO.setBusinessType(InventoryBusinessTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode());
            inventoryTransCoreService.approveByType(inventoryTransferDTO);
            //发送旺店通
            syncInfoToWdt(soB2cDeliveryInterceptEntity,entity,interceptInventoryDTOList,originList);
        }
        if(CollectionUtils.isNotEmpty(transferInfoList)){
            transferInfoList.forEach(v->{
                transferInfoService.addAndApprove(v);
            });
        }
    }

    private void syncInfoToWdt(SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity, SoB2cDeliveryEntity entity, List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> interceptInventoryDTOList, List<SoB2cDeliveryInterceptDTO.InterceptInventoryDTO> originList) {
        // 如果存在黑名单则跳过推送旺店通
        CfgSettingEntity blackListEntity = cfgSettingService.getByKey(CfgSettingEnum.WAREHOUSE_LOCATION_MOVE_BLACKLIST.getCode());
        List<String> blackList = ListUtil.empty();
        if (ObjectUtil.isNotEmpty(blackListEntity) && ObjectUtil.isNotEmpty(blackListEntity.getDataJson())) {
            JSONObject dataJson = blackListEntity.getDataJson();
            blackList = new ArrayList<>(dataJson.getBeanList("blackList", String.class));
        }
        if (blackList.contains(soB2cDeliveryInterceptEntity.getCode())) {
            log.warn("单号【{}】的发货拦截弹，被加入黑名单，不推送旺店通", entity.getCode());
            return;
        }

        Set<String> warehouseIdSet = interceptInventoryDTOList.stream().map(v->v.getWarehouseId()).collect(Collectors.toSet());
        warehouseIdSet.addAll(originList.stream().map(v->v.getWarehouseId()).collect(Collectors.toSet()));
        //查询三方仓库映射
        List<ThirdMappingDTO.WarehouseMappingDTO> mappingList = dmpThirdMappingFeign.listMappingBySysIds(new ArrayList<>(warehouseIdSet), "wdt");
        if(mappingList.isEmpty()){
            return;
        }
        //同步旺店通 审核{调入仓位做其他入库单，调出仓位做其他出库单}，反审核{调入仓位做其他出库单，调出仓位做其他入库单}
        List<CreateOtherStockoutRequest.GoodsList> outgoodsList = new ArrayList<>();
        List<CreateOtherStockinRequest.GoodsList> ingoodsList = new ArrayList<>();
        for (SoB2cDeliveryInterceptDTO.InterceptInventoryDTO interceptInventoryDTO : interceptInventoryDTOList) {
            SoB2cDeliveryInterceptDTO.InterceptInventoryDTO origin = originList.stream().filter(v->v.getInterceptDetailId().equals(interceptInventoryDTO.getInterceptDetailId())).findFirst().orElse(null);
            if(Objects.isNull(origin)){
                continue;
            }
            CreateOtherStockoutRequest.GoodsList outGoods = new CreateOtherStockoutRequest.GoodsList();
            outGoods.setSpecNo(origin.getSkuNo());
            outGoods.setNum(BigDecimal.valueOf(origin.getQty()));
            outGoods.setPositionNo(origin.getWarehouseLocation());
            outGoods.setWarehouseId(origin.getWarehouseId());
            outgoodsList.add(outGoods);

            CreateOtherStockinRequest.GoodsList inGoods = new CreateOtherStockinRequest.GoodsList();
            inGoods.setSpecNo(interceptInventoryDTO.getSkuNo());
            inGoods.setNum(BigDecimal.valueOf(interceptInventoryDTO.getQty()));
            inGoods.setPositionNo(interceptInventoryDTO.getWarehouseLocation());
            inGoods.setWarehouseId(interceptInventoryDTO.getWarehouseId());
            ingoodsList.add(inGoods);
        }
        //转换为同一个其他出库单
        abstractWdtService.transfer(SyncOperateEnum.OPERATE_APPROVE, soB2cDeliveryInterceptEntity.getId(), soB2cDeliveryInterceptEntity.getCode(), outgoodsList, SourceTypeEnum.OTHER_OUTSTOCK);

        //转换为同一个其他入库单
        abstractWdtService.transfer(SyncOperateEnum.OPERATE_APPROVE, soB2cDeliveryInterceptEntity.getId(), soB2cDeliveryInterceptEntity.getCode(), ingoodsList, SourceTypeEnum.OTHER_INSTOCK);
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cDeliveryInterceptEntity soB2cDeliveryInterceptEntity) {
        //查询关联的出库单匹配单号
        List<SoOutstockEntity> soOutstockEntities = soOutstockService.listBySoIds(Collections.singletonList(soB2cDeliveryInterceptEntity.getSourceId()));
        if (CollectionUtils.isNotEmpty(soOutstockEntities)) {
            soB2cDeliveryInterceptEntity.setSoOutstockCode(soOutstockEntities.get(MathUtil.ZERO).getCode());
        }
        //查询管理的发货单匹配单号
        List<SoB2cDeliveryEntity> soB2cDeliveryEntities = soB2cDeliveryService.listBySourceIds(Collections.singletonList(soB2cDeliveryInterceptEntity.getSourceId()));
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
