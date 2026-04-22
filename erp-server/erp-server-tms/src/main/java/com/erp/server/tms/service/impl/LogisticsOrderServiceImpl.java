package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.AfterSaleDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.dmp.feign.AfterSaleFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.mapper.LogisticsOrderMapper;
import com.erp.server.tms.service.*;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.*;
import com.sdk.tms.express.model.order.response.OrderResponse;
import com.sdk.tms.express.model.order.response.OrderUpdateResponse;
import com.sdk.tms.express.service.ExpressShipperService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * <p>
 * 物流下单表 服务实现类
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
 */
@Slf4j
@Service
public class LogisticsOrderServiceImpl extends SuperServiceImpl<LogisticsOrderMapper, LogisticsOrderEntity> implements LogisticsOrderService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private AfterSaleFeign afterSaleFeign;

    @Resource
    private LogisticsAuthService logisticsAuthService;

    @Resource
    private LogisticsAddressService logisticsAddressService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    @Qualifier("tmsLogisticsOrderPool")
    private ExecutorService tmsLogisticsOrderPool;

    @Resource
    private ExpressShipperService expressShipperService;

    @Resource
    private LogisticsSaleChannelService logisticsSaleChannelService;

    @Resource
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private AttachmentService attachmentService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsOrderDTO.AddDTO dto) {
        LogisticsOrderEntity logisticsOrderEntity = new LogisticsOrderEntity();
        BeanMapperUtils.copy(dto, logisticsOrderEntity);
        log.info("开始新增物流下单表");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WLD);
        logisticsOrderEntity.setCode(code);
        logisticsOrderEntity.setOrderId(code + LocalTime.now().format(DateTimeFormatter.ofPattern(DateUtil.FMT_HMS)));
        // 调用顺丰物流下单接口
        String channelId = dto.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        LogisticsSaleChannelEntity logisticsSaleChannelEntity = logisticsSaleChannelService.getById(logisticsChannel.getSyncSourceId());
        if (Objects.isNull(logisticsSaleChannelEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        // 收寄双方信息
        List<ContactInfo> contactInfoList = new ArrayList<>();
        // 寄件方信息
        ContactInfo sender = new ContactInfo();
        // 收货人地址信息
        List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, "all");
        LogisticsAddressTypeEnum finalDeliverType = LogisticsAddressTypeEnum.DELIVER;
        List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> finalDeliverType.equals(a.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY, logisticsChannel.getName(), finalDeliverType.getName());
        }
        // 寄件方信息
        LogisticsAddressEntity logisticsAddress = deliverList.get(0);
        sender.setContactType(1);
        sender.setContact(logisticsAddress.getContact());
        sender.setMobile(logisticsAddress.getTelNumber());
        sender.setCountry(logisticsAddress.getCountry());
        sender.setProvince(logisticsAddress.getProvinceName());
        sender.setCity(logisticsAddress.getCityName());
        sender.setAddress(logisticsAddress.getAddressFirst());
        contactInfoList.add(sender);
        // 到件方信息
        ContactInfo receiver = new ContactInfo();
        receiver.setContactType(2);
        receiver.setContact(dto.getReceiver());
        receiver.setMobile(dto.getContactNumber());
        receiver.setCountry(CountrySiteEnum.CHINA.getSite());
        receiver.setProvince(dto.getProvince());
        receiver.setCity(dto.getCity());
        receiver.setAddress(dto.getDetailedAddress());
        contactInfoList.add(receiver);
        OrderRequest orderRequest = OrderRequest.builder()
                .language("zh-CN")
                .orderId(logisticsOrderEntity.getOrderId())
                // 收寄双方信息
                .contactInfoList(contactInfoList)
                .payMethod(1)
                // 快件产品类别
                .expressTypeId(Integer.valueOf(logisticsSaleChannelEntity.getCode()))
                .parcelQty(1)
                // 是否返回路由标签： 默认1， 1：返回路由标签， 0：不返回；除部分特殊用户外，其余用户都默认返回
                .isReturnRoutelabel(1)
                .build();
        BaseResult baseResult;
        try {
            baseResult = expressShipperService.createOrder(authMap, orderRequest);
            // 转换实体
            if (baseResult.isSuccess()) {
                logisticsOrderEntity.setStatus(LogisticsStatusEnum.SUCCESS.getCode());
                OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                    if (Objects.nonNull(waybillNoInfo)) {
                        logisticsOrderEntity.setTrackNo(waybillNoInfo.getWaybillNo());
                    }
                }
            } else {
                logisticsOrderEntity.setStatus(LogisticsStatusEnum.FAILED.getCode());
                logisticsOrderEntity.setExceptionType(ExceptionTypeEnum.ORDER_EXCEPTION.getCode());
                logisticsOrderEntity.setExceptionReason(baseResult.getErrorMsg());
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        boolean save = super.save(logisticsOrderEntity);
        if (!save) {
            throw new ServiceException("物流下单表保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流下单表", logisticsOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), "新增操作");
        return new BaseResultDTO.AddDTO(logisticsOrderEntity.getId(), code);
    }

    /**
     * 修改
     */
    @DistributeLocker(keyName = "updateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsOrderDTO.UpdateDTO updateDTO) {
        LogisticsOrderEntity old = super.getById(updateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "物流下单表"));
        // 物流单据状态等于下单中或者下单成功，不允许编辑
        if (LogisticsStatusEnum.ORDERING.getCode().equals(old.getStatus()) || LogisticsStatusEnum.SUCCESS.getCode().equals(old.getStatus())) {
            throw new ServiceException(ApiError.LOGISTICS_ORDER_NOT_CANCEL);
        }
        // 有来源单号的物流单，不能再物流下单页面编辑
        if (StringUtils.isNotBlank(old.getSourceCode())) {
            throw new ServiceException(ApiError.LOGISTICS_ORDER_CANNOT_EDIT);
        }
        LogisticsOrderEntity logisticsOrderEntity = BeanMapperUtils.map(LogisticsOrderEntity.class, updateDTO);
        logisticsOrderEntity.setCode(old.getCode());
        logisticsOrderEntity.setOrderId(old.getCode() + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern(DateUtil.FMT_HMS)));
        log.info("编辑 开始修改物流下单表数据，单号：【{}】", logisticsOrderEntity.getCode());
        // 调用顺丰物流下单接口
        String channelId = updateDTO.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        LogisticsSaleChannelEntity logisticsSaleChannelEntity = logisticsSaleChannelService.getById(logisticsChannel.getSyncSourceId());
        if (Objects.isNull(logisticsSaleChannelEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        // 收寄双方信息
        List<ContactInfo> contactInfoList = new ArrayList<>();
        // 寄件方信息
        ContactInfo sender = new ContactInfo();
        // 收货人地址信息
        List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, "all");
        LogisticsAddressTypeEnum finalDeliverType = LogisticsAddressTypeEnum.DELIVER;
        List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> finalDeliverType.equals(a.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY, logisticsChannel.getName(), finalDeliverType.getName());
        }
        // 寄件方信息
        LogisticsAddressEntity logisticsAddress = deliverList.get(0);
        sender.setContactType(1);
        sender.setContact(logisticsAddress.getContact());
        sender.setMobile(logisticsAddress.getTelNumber());
        sender.setCountry(logisticsAddress.getCountry());
        sender.setProvince(logisticsAddress.getProvinceName());
        sender.setCity(logisticsAddress.getCityName());
        sender.setAddress(logisticsAddress.getAddressFirst());
        contactInfoList.add(sender);
        // 到件方信息
        ContactInfo receiver = new ContactInfo();
        receiver.setContactType(2);
        receiver.setContact(updateDTO.getReceiver());
        receiver.setMobile(updateDTO.getContactNumber());
        receiver.setCountry(CountrySiteEnum.CHINA.getSite());
        receiver.setProvince(updateDTO.getProvince());
        receiver.setCity(updateDTO.getCity());
        receiver.setAddress(updateDTO.getDetailedAddress());
        contactInfoList.add(receiver);
        OrderRequest orderRequest = OrderRequest.builder()
                .language("zh-CN")
                .orderId(logisticsOrderEntity.getOrderId())
                // 收寄双方信息
                .contactInfoList(contactInfoList)
                .payMethod(1)
                // 快件产品类别
                .expressTypeId(Integer.valueOf(logisticsSaleChannelEntity.getCode()))
                .parcelQty(1)
                // 是否返回路由标签： 默认1， 1：返回路由标签， 0：不返回；除部分特殊用户外，其余用户都默认返回
                .isReturnRoutelabel(1)
                .build();
        BaseResult baseResult;
        try {
            baseResult = expressShipperService.createOrder(authMap, orderRequest);
            // 转换实体
            if (baseResult.isSuccess()) {
                logisticsOrderEntity.setStatus(LogisticsStatusEnum.SUCCESS.getCode());
                OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                    if (Objects.nonNull(waybillNoInfo)) {
                        logisticsOrderEntity.setTrackNo(waybillNoInfo.getWaybillNo());
                    }
                }
            } else {
                logisticsOrderEntity.setStatus(LogisticsStatusEnum.FAILED.getCode());
                logisticsOrderEntity.setExceptionType(ExceptionTypeEnum.ORDER_EXCEPTION.getCode());
                logisticsOrderEntity.setExceptionReason(baseResult.getErrorMsg());
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        boolean save = super.updateById(logisticsOrderEntity);
        if (!save) {
            throw new ServiceException("物流下单表保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录物流下单表日志数据，单号：【{}】", logisticsOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsOrderEntity.getCode(), "物流下单表");
        operateLogService.addModuleOperateLogByObj(old, logisticsOrderEntity, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<LogisticsOrderDTO.ListDTO> paging(PagingDTO<LogisticsOrderDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<LogisticsOrderDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public List<LogisticsOrderDTO.TabListDTO> tabList(PermissionsDTO param) {
        LogisticsOrderDTO.PagingParamDTO searchParam = new LogisticsOrderDTO.PagingParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        List<LogisticsOrderDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        // 获取状态列表
        List<String> statusList = null;
        // 不存在的状态赋值为0
        List<String> existStatusList = list.stream().map(LogisticsOrderDTO.TabListDTO::getTabFlag).collect(Collectors.toList());
        statusList.parallelStream().forEach(status -> {
            if (!existStatusList.contains(status)) {
                list.add(new LogisticsOrderDTO.TabListDTO(status, 0));
            }
        });
        list.add(new LogisticsOrderDTO.TabListDTO("all", list.stream().mapToInt(LogisticsOrderDTO.TabListDTO::getCount).sum()));
        // 计算合计数量
        return list;
    }

    @Override
    public void exportList(LogisticsOrderDTO.ExportDTO param, HttpServletResponse response) {
        List<LogisticsOrderDTO.ListDTO> list = this.baseMapper.listExport(param);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 数据处理
        fillList(list);
        // 导出数据
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/logisticsOrder.xlsx";
        String name = "物流下单表导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date).append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (Exception e) {
            throw new ServiceException(ApiError.FILE_EXPORT_FAILED);
        }
    }

    @Override
    public LogisticsOrderDTO.ViewDTO view(String id) {
        LogisticsOrderEntity logisticsOrderEntity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到物流下单表数据"));
        LogisticsOrderDTO.ViewDTO data = BeanMapperUtils.map(LogisticsOrderDTO.ViewDTO.class, logisticsOrderEntity);
        // 数据填充处理
        data.setLogisticsPlatformName(LogisticsPlatformEnum.getDescByCode(data.getLogisticsPlatform()));
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(data.getLogisticsChannelId());
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        data.setLogisticsChannelName(logisticsChannel.getName());
        data.setStatusName(LogisticsStatusEnum.getName(data.getStatus()));
        data.setLabelStatusName(LogisticsLabelStatusEnum.getName(data.getLabelStatus()));
        data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        return data;
    }

    /**
     * 分页查询、导出 数据处理
     */
    private void fillList(List<LogisticsOrderDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 属性赋值
        for (LogisticsOrderDTO.ListDTO data : list) {
            data.setStatusName(LogisticsStatusEnum.getName(data.getStatus()));
            data.setLabelStatusName(LogisticsLabelStatusEnum.getName(data.getLabelStatus()));
            data.setSourceTypeName(SourceTypeEnum.getName(data.getSourceType()));
        }
    }

    @Override
    public List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(List<LogisticsOrderEntity> entityList) {
        // 筛选出所有来源单号
        List<String> sourceCodeList = entityList.stream().map(LogisticsOrderEntity::getSourceCode).filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
        List<LogisticsOrderEntity> logisticsOrderEntityList = this.baseMapper.selectList(new QueryWrapper<LogisticsOrderEntity>().lambda().in(LogisticsOrderEntity::getSourceCode, sourceCodeList));
        Map<String, LogisticsOrderEntity> logisticsOrderEntityMap = logisticsOrderEntityList.stream().collect(Collectors.toMap(LogisticsOrderEntity::getSourceCode, w -> w));
        // 筛选出所有不为空的寄修申请id
        List<String> afterSaleIdList = entityList.stream().map(LogisticsOrderEntity::getAfterSaleId).filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
        BaseIdsDTO.IdsDTO idsDTO = new BaseIdsDTO.IdsDTO();
        idsDTO.setIds(afterSaleIdList);
        // 查询寄修申请信息
        ApiResult<List<AfterSaleDTO.ViewDTO>> apiResult = afterSaleFeign.viewList(idsDTO);
        if (!apiResult.isSuccess()) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "寄修申请");
        }
        List<AfterSaleDTO.ViewDTO> viewDTOList = apiResult.getData();
        // 根据id和code分组
        Map<String, AfterSaleDTO.ViewDTO> viewDTOMap = viewDTOList.stream().collect(Collectors.toMap(AfterSaleDTO.ViewDTO::getId, w -> w));
        // 调用顺丰物流下单接口
        String channelId = entityList.get(0).getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        LogisticsChannelEntity logisticsChannel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(logisticsChannel)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        LogisticsSaleChannelEntity logisticsSaleChannelEntity = logisticsSaleChannelService.getById(logisticsChannel.getSyncSourceId());
        if (Objects.isNull(logisticsSaleChannelEntity)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
        // 组装请求参数
        List<OrderRequest> orderRequestList = new ArrayList<>();
        for (LogisticsOrderEntity logisticsOrderEntity : entityList) {
            LogisticsOrderEntity old = logisticsOrderEntityMap.get(logisticsOrderEntity.getSourceCode());
            logisticsOrderEntity.setOrderId(logisticsOrderEntity.getSourceCode() + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern(DateUtil.FMT_HMS)));
            if (old == null) {
                // 生成单号
                String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WLD);
                logisticsOrderEntity.setCode(code);
            } else {
                logisticsOrderEntity.setId(old.getId());
                logisticsOrderEntity.setCode(old.getCode());
            }
            AfterSaleDTO.ViewDTO viewDTO = viewDTOMap.get(logisticsOrderEntity.getAfterSaleId());
            // 托寄物信息
            List<CargoDetail> cargoDetails = new ArrayList<>();
            for (AfterSaleDetailEntity afterSaleDetailEntity : viewDTO.getDetailList()) {
                CargoDetail cargoDetail = CargoDetail.builder()
                        .name(afterSaleDetailEntity.getProductName())
                        .count(new BigDecimal(afterSaleDetailEntity.getSkuQty()))
                        .build();
                cargoDetails.add(cargoDetail);
            }
            // 收寄双方信息
            List<ContactInfo> contactInfoList = new ArrayList<>();
            // 寄件方信息
            ContactInfo sender = new ContactInfo();
            // 收货人地址信息
            List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, "all");
            LogisticsAddressTypeEnum finalDeliverType = LogisticsAddressTypeEnum.DELIVER;
            List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> finalDeliverType.equals(a.getType())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(deliverList)) {
                throw new ServiceException(ApiError.LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY, logisticsChannel.getName(), finalDeliverType.getName());
            }
            // 寄件方信息
            LogisticsAddressEntity logisticsAddress = deliverList.get(0);
            sender.setContactType(1);
            sender.setContact(logisticsAddress.getContact());
            sender.setMobile(logisticsAddress.getTelNumber());
            sender.setCountry(logisticsAddress.getCountry());
            sender.setProvince(logisticsAddress.getProvinceName());
            sender.setCity(logisticsAddress.getCityName());
            sender.setAddress(logisticsAddress.getAddressFirst());
            contactInfoList.add(sender);
            // 到件方信息
            ContactInfo receiver = new ContactInfo();
            receiver.setContactType(2);
            receiver.setContact(viewDTO.getThridUserName());
            receiver.setMobile(viewDTO.getPhoneNumber());
            receiver.setCountry(CountrySiteEnum.CHINA.getSite());
            receiver.setProvince(logisticsOrderEntity.getProvince());
            receiver.setCity(logisticsOrderEntity.getCity());
            receiver.setAddress(logisticsOrderEntity.getDetailedAddress());
            contactInfoList.add(receiver);
            OrderRequest orderRequest = OrderRequest.builder()
                    .language("zh-CN")
                    .orderId(logisticsOrderEntity.getOrderId())
                    // 托寄物信息
                    .cargoDetails(cargoDetails)
                    .cargoDesc(null)
                    .custReferenceNo(viewDTO.getPlatformCode())
                    // 收寄双方信息
                    .contactInfoList(contactInfoList)
                    .payMethod(1)
                    // 快件产品类别
                    .expressTypeId(Integer.valueOf(logisticsSaleChannelEntity.getCode()))
                    .parcelQty(1)
                    // 是否返回路由标签： 默认1， 1：返回路由标签， 0：不返回；除部分特殊用户外，其余用户都默认返回
                    .isReturnRoutelabel(1)
                    .build();
            orderRequestList.add(orderRequest);
        }
        // 为列表中的每个请求创建一个异步任务
        List<CompletableFuture<BaseResult>> futures = orderRequestList.stream()
                .map(orderRequest -> CompletableFuture.supplyAsync(() -> {
                    // 这里是调用外部接口的具体逻辑
                    try {
                        return expressShipperService.createOrder(authMap, orderRequest);
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }, tmsLogisticsOrderPool).exceptionally(ex -> {
                    // 处理单个任务的异常，防止一个失败导致整体失败
                    System.err.println("处理订单失败: " + orderRequest.getOrderId() + ", 原因: " + ex.getMessage());
                    return null; // 失败时返回 null 或一个默认对象
                })).collect(Collectors.toList());
        // 等待所有异步任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        // 在所有任务完成后，收集并返回结果
        List<BaseResult> baseResultList = allFutures.thenApply(v -> futures.stream()
                .map(CompletableFuture::join) // 获取每个任务的结果
                .filter(Objects::nonNull)     // 过滤掉处理失败的 null 结果
                .collect(Collectors.toList())
        ).join(); // 阻塞等待所有任务完成并获取最终结果列表
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultDTOList = new ArrayList<>();
        Map<String, String> map = entityList.stream().collect(Collectors.toMap(LogisticsOrderEntity::getSourceCode, LogisticsOrderEntity::getAfterSaleId));
        for (BaseResult baseResult : baseResultList) {
            AfterSaleDTO.LogisticsOrderResultDTO resultDTO = new AfterSaleDTO.LogisticsOrderResultDTO();
            // 转换实体
            if (baseResult.isSuccess()) {
                OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
                resultDTO.setAfterSaleId(map.get(StringUtils.substringBefore(orderResponse.getOrderId(), "_")));
                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                    if (Objects.nonNull(waybillNoInfo)) {
                        resultDTO.setTrackNo(waybillNoInfo.getWaybillNo());
                    }
                }
            } else {
                resultDTO.setErrorMsg(baseResult.getErrorMsg());
            }
            resultDTOList.add(resultDTO);
        }
        Map<String, AfterSaleDTO.LogisticsOrderResultDTO> resultDTOMap = resultDTOList.stream().collect(Collectors.toMap(AfterSaleDTO.LogisticsOrderResultDTO::getAfterSaleId, w -> w));
        entityList.forEach(e -> {
            if (resultDTOMap.get(e.getAfterSaleId()) != null) {
                e.setStatus(LogisticsStatusEnum.SUCCESS.getCode());
                e.setTrackNo(resultDTOMap.get(e.getAfterSaleId()).getTrackNo());
            } else {
                e.setStatus(LogisticsStatusEnum.FAILED.getCode());
                e.setExceptionType(ExceptionTypeEnum.ORDER_EXCEPTION.getCode());
                e.setExceptionReason(resultDTOMap.get(e.getAfterSaleId()).getErrorMsg());
            }
        });
        super.saveOrUpdateBatch(entityList);
        return resultDTOList;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        LogisticsOrderEntity entity = this.getById(id);
        // 物流单据状态等于下单中或者下单成功，不允许删除
        if (LogisticsStatusEnum.ORDERING.getCode().equals(entity.getStatus()) || LogisticsStatusEnum.SUCCESS.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "物流单据不是已取消或者下单失败状态，不能编辑");
        }
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.DELETE);
    }

    @Override
    public BatchResultDTO cancel(String id) {
        LogisticsOrderEntity entity = this.getById(id);
        // 物流单据状态不等于下单成功，不允许取消
        if (!LogisticsStatusEnum.SUCCESS.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "物流单据状态不是下单成功，不允许取消");
        }
        // 有来源单号的物流单，不允许在物流下单页面取消
        if (StringUtils.isNotBlank(entity.getSourceCode())) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "有来源单号的物流单，不允许在物流下单页面取消");
        }
        String channelId = entity.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "物流渠道不存在");
        }
        Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
        boolean success = false;
        BaseResult baseResult = null;
        BatchResultDTO resultDTO = null;
        // 支持单个取消
        OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                .orderId(entity.getOrderId())
                .dealType(2)
                .build();
        try {
            ValidatorUtil.validateEntity(orderUpdateRequest);
            baseResult = expressShipperService.updateOrder(authMap, orderUpdateRequest);
            // 转换实体
            if (baseResult.isSuccess()) {
                OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                if (orderUpdateResponse.getResStatus() == 2) {
                    success = true;
                } else {
                    resultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), baseResult.getErrorMsg());
                }
            } else {
                resultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), baseResult.getErrorMsg());
            }
        } catch (Exception e) {
            resultDTO = BatchResultDTO.success(entity.getId(), entity.getCode(), e.getMessage());
        }
        if (success) {
            entity.setStatus(LogisticsStatusEnum.CANCEL.getCode());
            this.updateById(entity);
            logisticsOperateService.pushOperateLog(entity.getCode(), entity.getTrackNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(),
                    LogisticsPlatformEnum.SF_EXPRESS.getCode(), RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(orderUpdateRequest),
                    JSONUtil.toJsonStr(baseResult), false);
            return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.CANCEL);
        } else {
            logisticsOperateService.pushOperateLog(entity.getCode(), entity.getTrackNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(),
                    LogisticsPlatformEnum.SF_EXPRESS.getCode(), RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(orderUpdateRequest),
                    JSONUtil.toJsonStr(baseResult), false);
            return resultDTO;
        }
    }

    @Override
    public List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)) {
            return Collections.emptyList();
        }
        List<LogisticsOrderEntity> entityList = this.list(new QueryWrapper<LogisticsOrderEntity>().lambda().in(LogisticsOrderEntity::getTrackNo, trackNoList));
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        List<LogisticsChannelEntity> channelList = logisticsChannelService.list(new QueryWrapper<LogisticsChannelEntity>().lambda()
                .in(LogisticsChannelEntity::getId, entityList.stream().map(LogisticsOrderEntity::getLogisticsChannelId).collect(Collectors.toList())));
        Map<String, String> channelMap = channelList.stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, LogisticsChannelEntity::getName));
        List<LogisticsOrderDTO.ListDTO> list = new ArrayList<>(entityList.size());
        for (LogisticsOrderEntity entity : entityList) {
            LogisticsOrderDTO.ListDTO dto = new LogisticsOrderDTO.ListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setLogisticsChannelName(channelMap.get(entity.getLogisticsChannelId()));
        }
        return list;
    }

    @Override
    public List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(List<String> codeList) {
        List<LogisticsOrderEntity> list = lambdaQuery().in(LogisticsOrderEntity::getSourceCode, codeList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultList = new ArrayList<>(list.size());
        for (LogisticsOrderEntity entity : list) {
            AfterSaleDTO.LogisticsOrderResultDTO result = new AfterSaleDTO.LogisticsOrderResultDTO();
            String channelId = entity.getLogisticsChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
                result.setAfterSaleId(entity.getAfterSaleId());
                result.setCode(entity.getSourceCode());
                result.setStatus(false);
                result.setErrorMsg("物流渠道不存在");
                resultList.add(result);
                continue;
            }
            Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
            boolean success = false;
            BaseResult baseResult = null;
            // 支持单个取消
            OrderUpdateRequest orderUpdateRequest = OrderUpdateRequest.builder()
                    .orderId(entity.getOrderId())
                    .dealType(2)
                    .build();
            try {
                ValidatorUtil.validateEntity(orderUpdateRequest);
                baseResult = expressShipperService.updateOrder(authMap, orderUpdateRequest);
                // 转换实体
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (orderUpdateResponse.getResStatus() == 2) {
                        success = true;
                        result.setAfterSaleId(entity.getAfterSaleId());
                        result.setCode(entity.getSourceCode());
                        result.setStatus(true);
                        resultList.add(result);
                    } else {
                        result.setAfterSaleId(entity.getAfterSaleId());
                        result.setCode(entity.getSourceCode());
                        result.setStatus(false);
                        result.setErrorMsg(baseResult.getErrorMsg());
                        resultList.add(result);
                    }
                } else {
                    result.setAfterSaleId(entity.getAfterSaleId());
                    result.setCode(entity.getSourceCode());
                    result.setStatus(false);
                    result.setErrorMsg(baseResult.getErrorMsg());
                    resultList.add(result);
                }
            } catch (Exception e) {
                result.setAfterSaleId(entity.getAfterSaleId());
                result.setCode(entity.getSourceCode());
                result.setStatus(false);
                result.setErrorMsg(e.getMessage());
                resultList.add(result);
            }
            if (success) {
                entity.setStatus(LogisticsStatusEnum.CANCEL.getCode());
                logisticsOperateService.pushOperateLog(entity.getCode(), entity.getTrackNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(),
                        LogisticsPlatformEnum.SF_EXPRESS.getCode(), RequestStatusEnums.SUCCESS.getCode(), JSONUtil.toJsonStr(orderUpdateRequest),
                        JSONUtil.toJsonStr(baseResult), false);
            } else {
                logisticsOperateService.pushOperateLog(entity.getCode(), entity.getTrackNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(),
                        LogisticsPlatformEnum.SF_EXPRESS.getCode(), RequestStatusEnums.FAILED.getCode(), JSONUtil.toJsonStr(orderUpdateRequest),
                        JSONUtil.toJsonStr(baseResult), false);
            }
            this.updateById(entity);
        }
        return resultList;
    }

    @Override
    public List<BatchResultDTO> printLogisticsWaybill(BaseIdsDTO.IdsDTO dto) {
        return Collections.emptyList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadLogisticLabel(LogisticsOrderDTO.UploadFileDTO dto) {
        LogisticsOrderEntity entity = getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException("物流单不存在");
        }
        MultipartFile multipartFile = dto.getFile();
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new ServiceException("文件不能为空");
        }
        // 获取文件的内容类型并检查是否为PDF
        if (!"application/pdf".equals(multipartFile.getContentType())) {
            throw new ServiceException("文件格式不正确，请上传PDF格式的文件");
        }
        String url = fileFeign.uploadFile(multipartFile);
        TmsAttachmentEntity attachmentEntity = new TmsAttachmentEntity();
        attachmentEntity.setAttachName(multipartFile.getOriginalFilename());
        attachmentEntity.setAttachUrl(url);
        attachmentEntity.setType("after_sale");
        attachmentEntity.setBusinessId(entity.getId());
        attachmentService.save(attachmentEntity);
        String msg = CharSequenceUtil.format("用户【{}】上传文件名为【{}】的物流面单 ", UserContext.getDefaultLoginUser().getUserName(), multipartFile.getOriginalFilename());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C.getCode(), dto.getId(), "上传面单");
        return "";
    }

}
