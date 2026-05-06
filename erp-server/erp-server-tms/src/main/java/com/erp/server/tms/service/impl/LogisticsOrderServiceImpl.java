package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.constant.RedisCacheConstants;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.RedisUtil;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.CountrySiteEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.entity.AfterSaleDetailEntity;
import com.erp.model.file.dto.FileDTO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.dmp.feign.AfterSaleFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.mapper.LogisticsOrderMapper;
import com.erp.server.tms.service.*;
import com.sdk.tms.express.model.base.BaseResult;
import com.sdk.tms.express.model.order.request.*;
import com.sdk.tms.express.model.order.response.LabelResponse;
import com.sdk.tms.express.model.order.response.OrderResponse;
import com.sdk.tms.express.model.order.response.OrderUpdateResponse;
import com.sdk.tms.express.model.order.response.PrintFile;
import com.sdk.tms.express.service.ExpressShipperService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_ORDER;

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
    private LogisticsOperateService logisticsOperateService;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private AttachmentService attachmentService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private MQProducerService mqProducerService;

    @Resource
    private DictBasicService dictBasicService;

    @Value("${tms.sf-express.templateCode}")
    private String templateCode;

    @Value("${tms.sf-express.version}")
    private String version;

    @Value("${tms.sf-express.fileType}")
    private String fileType;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsOrderDTO.AddDTO dto) {
        LogisticsOrderEntity logisticsOrderEntity = new LogisticsOrderEntity();
        BeanMapperUtils.copy(dto, logisticsOrderEntity);
        log.info("开始新增物流下单表");
        // 调用顺丰物流下单接口
        String channelId = dto.getLogisticsChannelId();
        ChannelAuthInfo channelAuth = getChannelAuthInfo(channelId);
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_WLD);
        logisticsOrderEntity.setCode(code);
        logisticsOrderEntity.setOrderId(code + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern(DateUtil.FMT_HMS)));
        // 收寄双方信息
        List<ContactInfo> contactInfoList = new ArrayList<>();
        contactInfoList.add(buildSenderContactInfo(channelId, channelAuth.channel.getName()));
        contactInfoList.add(buildReceiverContactInfo(dto.getReceiver(), dto.getContactNumber(), dto.getProvince(), dto.getCity(), dto.getDetailedAddress()));
        // 组装请求顺丰下单接口参数
        OrderRequest orderRequest = getOrderRequest(logisticsOrderEntity, contactInfoList, channelAuth);
        BaseResult baseResult;
        try {
            baseResult = expressShipperService.createOrder(channelAuth.authMap, orderRequest);
            handleOrderResult(baseResult, logisticsOrderEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        boolean save = super.save(logisticsOrderEntity);
        if (!save) {
            throw new ServiceException("物流下单表保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流下单", logisticsOrderEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), "新增操作");
        // 下单成功发送异步请求保存面单
        asyncSendLabelRequest(logisticsOrderEntity);
        return new BaseResultDTO.AddDTO(logisticsOrderEntity.getId(), code);
    }

    private LogisticsOrderDTO.LogisticsLabelDTO getLogisticsOrderLabel(LogisticsOrderEntity logisticsOrderEntity) {
        LogisticsOrderDTO.LogisticsLabelDTO labelDTO = new LogisticsOrderDTO.LogisticsLabelDTO();
        labelDTO.setId(logisticsOrderEntity.getId());
        labelDTO.setCode(logisticsOrderEntity.getCode());
        labelDTO.setTrackNo(logisticsOrderEntity.getTrackNo());
        labelDTO.setLogisticsChannelId(logisticsOrderEntity.getLogisticsChannelId());
        labelDTO.setLogisticsPlatform(logisticsOrderEntity.getLogisticsPlatform());
        labelDTO.setAfterSaleId(logisticsOrderEntity.getAfterSaleId());
        labelDTO.setSourceCode(logisticsOrderEntity.getSourceCode());
        return labelDTO;
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
        logisticsOrderEntity.setId(old.getId());
        logisticsOrderEntity.setCode(old.getCode());
        logisticsOrderEntity.setOrderId(old.getCode() + "_" + LocalTime.now().format(DateTimeFormatter.ofPattern(DateUtil.FMT_HMS)));
        log.info("编辑 开始修改物流下单表数据，单号：【{}】", logisticsOrderEntity.getCode());
        // 调用顺丰物流下单接口
        String channelId = updateDTO.getLogisticsChannelId();
        ChannelAuthInfo channelAuth = getChannelAuthInfo(channelId);
        // 收寄双方信息
        List<ContactInfo> contactInfoList = new ArrayList<>();
        contactInfoList.add(buildSenderContactInfo(channelId, channelAuth.channel.getName()));
        contactInfoList.add(buildReceiverContactInfo(updateDTO.getReceiver(), updateDTO.getContactNumber(), updateDTO.getProvince(), updateDTO.getCity(), updateDTO.getDetailedAddress()));
        // 组装请求顺丰下单接口参数
        OrderRequest orderRequest = getOrderRequest(logisticsOrderEntity, contactInfoList, channelAuth);
        BaseResult baseResult;
        try {
            baseResult = expressShipperService.createOrder(channelAuth.authMap, orderRequest);
            handleOrderResult(baseResult, logisticsOrderEntity);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        boolean save = super.updateById(logisticsOrderEntity);
        if (!save) {
            throw new ServiceException("物流下单表保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录物流下单表日志数据，单号：【{}】", logisticsOrderEntity.getCode());
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), logisticsOrderEntity.getCode(), "物流下单");
        operateLogService.addModuleOperateLogByObj(old, logisticsOrderEntity, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), msg);
        // 下单成功发送异步请求保存面单
        asyncSendLabelRequest(logisticsOrderEntity);
        return Boolean.TRUE;
    }

    private static OrderRequest getOrderRequest(LogisticsOrderEntity logisticsOrderEntity, List<ContactInfo> contactInfoList, ChannelAuthInfo channelAuth) {
        return OrderRequest.builder()
                .language("zh-CN")
                .orderId(logisticsOrderEntity.getOrderId())
                .contactInfoList(contactInfoList)
                .payMethod(1)
                .expressTypeId(Integer.valueOf(channelAuth.channel.getCode()))
                .parcelQty(1)
                .isReturnRoutelabel(1)
                .build();
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
        downloadTaskFeign.saveDownloadTask("物流下单导出", EXPORT_TMS_LOGISTICS_ORDER.getCode(), param);
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(List<LogisticsOrderEntity> entityList) {
        // 筛选出所有来源单号
        List<String> sourceCodeList = entityList.stream().map(LogisticsOrderEntity::getSourceCode).filter(ObjectUtil::isNotEmpty).collect(Collectors.toList());
        log.info("批量新增物流下单开始，来源单号：{}", StringUtils.join(sourceCodeList, ","));
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
        ChannelAuthInfo channelAuth = getChannelAuthInfo(channelId);
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
            contactInfoList.add(buildSenderContactInfo(channelId, channelAuth.channel.getName()));
            contactInfoList.add(buildReceiverContactInfo(viewDTO.getThridUserName(), viewDTO.getPhoneNumber(), logisticsOrderEntity.getProvince(), logisticsOrderEntity.getCity(), logisticsOrderEntity.getDetailedAddress()));
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
                    .expressTypeId(Integer.valueOf(channelAuth.channel.getCode()))
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
                        return expressShipperService.createOrder(channelAuth.authMap, orderRequest);
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
                resultDTO.setStatus(true);
                List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
                if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                    WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                    if (Objects.nonNull(waybillNoInfo)) {
                        resultDTO.setTrackNo(waybillNoInfo.getWaybillNo());
                    }
                }
            } else {
                resultDTO.setErrorMsg(baseResult.getErrorMsg());
                resultDTO.setStatus(false);
            }
            resultDTOList.add(resultDTO);
        }
        Map<String, AfterSaleDTO.LogisticsOrderResultDTO> resultDTOMap = resultDTOList.stream().collect(Collectors.toMap(AfterSaleDTO.LogisticsOrderResultDTO::getAfterSaleId, w -> w));
        List<LogisticsOrderDTO.LogisticsLabelDTO> successLabelList = new ArrayList<>();
        entityList.forEach(e -> {
            if (resultDTOMap.get(e.getAfterSaleId()) != null) {
                e.setStatus(LogisticsStatusEnum.SUCCESS.getCode());
                e.setTrackNo(resultDTOMap.get(e.getAfterSaleId()).getTrackNo());
                // 操作日志
                String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流下单", e.getCode());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), e.getId(), "新增操作");
                // 下单成功发送异步请求保存面单
                // 设置redis
                String labelRedisKey = StrUtil.format(RedisCacheConstants.TMS_LOGISTIC_LABEL, e.getId(), e.getTrackNo());
                redisUtil.set(labelRedisKey, true, 86400);
                LogisticsOrderDTO.LogisticsLabelDTO labelDTO = getLogisticsOrderLabel(e);
                successLabelList.add(labelDTO);
            } else {
                e.setStatus(LogisticsStatusEnum.FAILED.getCode());
                e.setExceptionType(ExceptionTypeEnum.ORDER_EXCEPTION.getCode());
                e.setExceptionReason(resultDTOMap.get(e.getAfterSaleId()).getErrorMsg());
            }
        });
        if (CollUtil.isNotEmpty(successLabelList)) {
            mqProducerService.asyncClassMsg(RocketMqTopic.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TOPIC,
                    RocketMqTagEnum.DMP_ASYNC_GET_LOGISTICS_ORDER_LABEL_TAG.getName(), successLabelList, IdUtil.simpleUUID());
        }
        super.saveOrUpdateBatch(entityList);
        return resultDTOList;
    }

    @DistributeLocker(keyName = "id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO delete(String id) {
        LogisticsOrderEntity entity = this.getById(id);
        log.info("开始执行删除操作，单号：【{}】", entity.getCode());
        // 物流单据状态等于下单中或者下单成功，不允许删除
        if (LogisticsStatusEnum.ORDERING.getCode().equals(entity.getStatus()) || LogisticsStatusEnum.SUCCESS.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "物流单据不是已取消或者下单失败状态，不能编辑");
        }
        this.removeById(id);
        // 记录主单操作日志
        log.info("删除 开始记录物流下单表日志数据，单号：【{}】", entity.getCode());
        String msg = StrUtil.format("用户【{}】删除单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "物流下单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), entity.getId(), "删除物流单");
        return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.DELETE);
    }

    @DistributeLocker(keyName = "id")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO cancel(String id) {
        LogisticsOrderEntity entity = this.getById(id);
        log.info("开始执行取消操作，单号：【{}】", entity.getCode());
        // 物流单据状态不等于下单成功，不允许取消
        if (!LogisticsStatusEnum.SUCCESS.getCode().equals(entity.getStatus())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "物流单据状态不是下单成功，不允许取消");
        }
        // 有来源单号的物流单，不允许在物流下单页面取消
        if (StringUtils.isNotBlank(entity.getSourceCode())) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "有来源单号的物流单，不允许在物流下单页面取消");
        }
        String channelId = entity.getLogisticsChannelId();
        LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(auth)) {
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "物流渠道不存在");
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
                    resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), baseResult.getErrorMsg());
                }
            } else {
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), baseResult.getErrorMsg());
            }
        } catch (Exception e) {
            resultDTO = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
        }
        // 记录主单操作日志
        log.info("取消 开始记录物流下单表日志数据，单号：【{}】", entity.getCode());
        String msg = StrUtil.format("用户【{}】取消单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "物流下单");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), entity.getId(), "取消物流单");
        if (success) {
            // 取消成功清空物流跟踪号
            entity.setStatus(LogisticsStatusEnum.CANCEL.getCode());
            entity.setTrackNo("");
            entity.setLabelStatus(LogisticsLabelStatusEnum.NOT_OBTAINED.getCode());
            this.updateById(entity);
            // 取消成功删除面单信息
            TmsAttachmentEntity tmsAttachmentEntity = attachmentService.getOne(new QueryWrapper<TmsAttachmentEntity>().lambda()
                    .eq(TmsAttachmentEntity::getBusinessId, entity.getId())
                    .eq(TmsAttachmentEntity::getType, "logistics_label"));
            if (tmsAttachmentEntity != null) {
                attachmentService.removeById(tmsAttachmentEntity.getId());
            }
            pushCancelOperateLog(entity, RequestStatusEnums.SUCCESS.getCode(), orderUpdateRequest, baseResult);
            return BatchResultDTO.success(entity.getId(), entity.getTrackNo(), OperationTypeEnum.CANCEL);
        } else {
            pushCancelOperateLog(entity, RequestStatusEnums.FAILED.getCode(), orderUpdateRequest, baseResult);
            return resultDTO;
        }
    }

    @Override
    public List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)) {
            return Collections.emptyList();
        }
        log.info("开始查询物流单号：【{}】的物流单信息", StringUtils.join(trackNoList, ","));
        List<LogisticsOrderEntity> entityList = this.list(new QueryWrapper<LogisticsOrderEntity>().lambda()
                .in(LogisticsOrderEntity::getTrackNo, trackNoList)
                .eq(LogisticsOrderEntity::getStatus, LogisticsStatusEnum.SUCCESS.getCode()));
        if (CollectionUtils.isEmpty(entityList)) {
            return Collections.emptyList();
        }
        // 查询物流渠道信息
        List<LogisticsChannelEntity> channelList = logisticsChannelService.list(new QueryWrapper<LogisticsChannelEntity>().lambda()
                .in(LogisticsChannelEntity::getId, entityList.stream().map(LogisticsOrderEntity::getLogisticsChannelId).collect(Collectors.toList())));
        Map<String, String> channelMap = channelList.stream().collect(Collectors.toMap(LogisticsChannelEntity::getId, LogisticsChannelEntity::getName));
        // 查询物流平台商信息
        List<DictBasicEntity> platformList = dictBasicService.list(new QueryWrapper<DictBasicEntity>().lambda()
                .eq(DictBasicEntity::getType, "logisticsPlatform")
                .in(DictBasicEntity::getCode, entityList.stream().map(LogisticsOrderEntity::getLogisticsPlatform).collect(Collectors.toList())));
        Map<String, String> platformMap = platformList.stream().collect(Collectors.toMap(DictBasicEntity::getCode, DictBasicEntity::getName));
        List<LogisticsOrderDTO.ListDTO> list = new ArrayList<>(entityList.size());
        for (LogisticsOrderEntity entity : entityList) {
            LogisticsOrderDTO.ListDTO dto = new LogisticsOrderDTO.ListDTO();
            BeanUtils.copyProperties(entity, dto);
            dto.setLogisticsChannelName(channelMap.get(entity.getLogisticsChannelId()));
            dto.setLogisticsPlatformName(platformMap.get(entity.getLogisticsPlatform()));
            dto.setLabelStatusName(LogisticsLabelStatusEnum.getName(entity.getLabelStatus()));
            list.add(dto);
        }
        return list;
    }

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(List<String> codeList) {
        List<LogisticsOrderEntity> list = lambdaQuery().in(LogisticsOrderEntity::getSourceCode, codeList).list();
        if (CollectionUtils.isEmpty(list)) {
            return Collections.emptyList();
        }
        log.info("开始批量取消物流单号：【{}】的物流单信息", StringUtils.join(codeList, ","));
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultList = new ArrayList<>(list.size());
        for (LogisticsOrderEntity entity : list) {
            AfterSaleDTO.LogisticsOrderResultDTO result = new AfterSaleDTO.LogisticsOrderResultDTO();
            result.setAfterSaleId(entity.getAfterSaleId());
            result.setCode(entity.getSourceCode());
            String channelId = entity.getLogisticsChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
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
                if (baseResult.isSuccess()) {
                    OrderUpdateResponse orderUpdateResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderUpdateResponse.class);
                    if (orderUpdateResponse.getResStatus() == 2) {
                        success = true;
                        result.setStatus(true);
                    } else {
                        result.setStatus(false);
                        result.setErrorMsg(baseResult.getErrorMsg());
                    }
                } else {
                    result.setStatus(false);
                    result.setErrorMsg(baseResult.getErrorMsg());
                }
            } catch (Exception e) {
                result.setStatus(false);
                result.setErrorMsg(e.getMessage());
            }
            resultList.add(result);
            if (success) {
                entity.setStatus(LogisticsStatusEnum.CANCEL.getCode());
                entity.setTrackNo("");
                pushCancelOperateLog(entity, RequestStatusEnums.SUCCESS.getCode(), orderUpdateRequest, baseResult);
            } else {
                pushCancelOperateLog(entity, RequestStatusEnums.FAILED.getCode(), orderUpdateRequest, baseResult);
            }
            this.updateById(entity);
            // 记录主单操作日志
            log.info("取消 开始记录物流下单表日志数据，单号：【{}】", entity.getCode());
            String msg = StrUtil.format("用户【{}】取消单号为【{}】的【{}】单据", UserContext.getDefaultLoginUser().getUserName(), entity.getCode(), "物流下单");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), entity.getId(), "取消物流单");
        }
        return resultList;
    }

    @Override
    public LogisticsOrderDTO.LogisticsLabelPreviewDTO printLogisticsLabelPreview(BaseIdsDTO.IdsDTO dto) {
        LogisticsOrderDTO.LogisticsLabelPreviewDTO result = new LogisticsOrderDTO.LogisticsLabelPreviewDTO();
        List<LogisticsOrderDTO.LogisticsLabelPreviewListDTO> labelPreviewListDTOS = this.baseMapper.printLogisticsLabelPreview(dto.getIds());
        // 有运单号数量
        Integer trackNoCount = Math.toIntExact(labelPreviewListDTOS.stream().filter(req -> CharSequenceUtil.isNotBlank(req.getTrackNo())).count());
        // 无运单号数量
        Integer notTrackNoCount = Math.toIntExact(labelPreviewListDTOS.stream().filter(req -> CharSequenceUtil.isBlank(req.getTrackNo())).count());
        // 查询面单信息
        List<TmsAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<TmsAttachmentEntity>().lambda()
                .in(TmsAttachmentEntity::getBusinessId, dto.getIds())
                .eq(TmsAttachmentEntity::getType, "logistics_label"));
        List<String> bussinessIdList = attachmentList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getAttachUrl())).map(TmsAttachmentEntity::getBusinessId).distinct().collect(Collectors.toList());
        List<String> notPrintCodes = labelPreviewListDTOS.stream().filter(e -> !bussinessIdList.contains(e.getId())).map(LogisticsOrderDTO.LogisticsLabelPreviewListDTO::getCode).distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(notPrintCodes)) {
            throw new ServiceException(ApiError.SO_LOGISTICS_WAYBILL_NOT_OBTAINED, CharSequenceUtil.join(",", notPrintCodes));
        }
        Map<String, TmsAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(TmsAttachmentEntity::getBusinessId, v -> v));
        Map<String, String> notPrintReasonMap = null;
        for (LogisticsOrderDTO.LogisticsLabelPreviewListDTO labelPreviewListDTO : labelPreviewListDTOS) {
            TmsAttachmentEntity att = attachmentMap.get(labelPreviewListDTO.getId());
            if (att == null) {
                notPrintReasonMap = new HashMap<>();
                if (StringUtils.isBlank(labelPreviewListDTO.getExceptionType())) {
                    notPrintReasonMap.put(labelPreviewListDTO.getLogisticsChannelName(), "未获取面单");
                } else if (ExceptionTypeEnum.LABEL_EXCEPTION.getCode().equals(labelPreviewListDTO.getExceptionType())) {
                    notPrintReasonMap.put(labelPreviewListDTO.getLogisticsChannelName(), labelPreviewListDTO.getExceptionReason());
                }
            } else {
                labelPreviewListDTO.setAttachName(att.getAttachName());
                labelPreviewListDTO.setAttachUrl(att.getAttachUrl());
            }
        }
        result.setLabelPreviewListDTOS(labelPreviewListDTOS);
        result.setTrackNoCount(trackNoCount);
        result.setNotTrackNoCount(notTrackNoCount);
        result.setNotPrintCount(dto.getIds().size() - attachmentList.size());
        result.setNotPrintReasonMap(notPrintReasonMap);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String uploadLogisticLabel(LogisticsOrderDTO.UploadFileDTO dto) {
        LogisticsOrderEntity entity = getById(dto.getId());
        if (Objects.isNull(entity)) {
            throw new ServiceException("物流单不存在");
        }
        TmsAttachmentEntity tmsAttachmentEntity = attachmentService.getOne(new QueryWrapper<TmsAttachmentEntity>().lambda()
                .eq(TmsAttachmentEntity::getBusinessId, entity.getId())
                .eq(TmsAttachmentEntity::getType, "logistics_label"));
        if (tmsAttachmentEntity != null) {
            attachmentService.removeById(tmsAttachmentEntity.getId());
        }
        TmsAttachmentEntity attachmentEntity = new TmsAttachmentEntity();
        attachmentEntity.setAttachName(dto.getAttachName());
        attachmentEntity.setAttachUrl(dto.getAttachUrl());
        attachmentEntity.setType("logistics_label");
        attachmentEntity.setBusinessId(entity.getId());
        attachmentService.save(attachmentEntity);
        entity.setLabelStatus(LogisticsLabelStatusEnum.OBTAINED.getCode());
        this.updateById(entity);
        String msg = CharSequenceUtil.format("用户【{}】上传文件名为【{}】的物流面单 ", UserContext.getDefaultLoginUser().getUserName(), dto.getAttachName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), dto.getId(), "上传面单");
        return "";
    }

    @Override
    public String printLogisticsLabelConfirm(BaseIdsDTO.IdsDTO dto) {
        // 查询面单信息
        List<TmsAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<TmsAttachmentEntity>().lambda()
                .in(TmsAttachmentEntity::getBusinessId, dto.getIds())
                .eq(TmsAttachmentEntity::getType, "logistics_label"));
        if (CollectionUtils.isEmpty(attachmentList)) {
            throw new ServiceException("无可打印的物流面单");
        }
        Map<String, String> baseMap = attachmentList.stream().collect(Collectors.toMap(TmsAttachmentEntity::getBusinessId, TmsAttachmentEntity::getAttachUrl));
        List<String> urlList = dto.getIds().stream().map(e -> baseMap.getOrDefault(e, null)).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        try {
            return fileFeign.mergeFiles(urlList);
        } catch (Exception e) {
            log.error("合并文件失败", e);
            throw new ServiceException(ApiError.LOGISTICS_PDF_SO_MERGE_ERROR);
        }
    }

    @Override
    public List<BatchResultDTO> getLogisticsOrderLabel(List<LogisticsOrderDTO.LogisticsLabelDTO> dtoList) {
        List<TmsAttachmentEntity> attachmentList = attachmentService.list(new QueryWrapper<TmsAttachmentEntity>().lambda()
                .in(TmsAttachmentEntity::getBusinessId, dtoList.stream().map(LogisticsOrderDTO.LogisticsLabelDTO::getId).collect(Collectors.toList()))
                .eq(TmsAttachmentEntity::getType, "logistics_label"));
        Map<String, TmsAttachmentEntity> attachmentMap = attachmentList.stream().collect(Collectors.toMap(TmsAttachmentEntity::getBusinessId, v -> v));
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dtoList.size());
        for (LogisticsOrderDTO.LogisticsLabelDTO logisticsLabelDTO : dtoList) {
            BatchResultDTO resultDTO = new BatchResultDTO();
            LogisticsOrderEntity logisticsOrderEntity = getById(logisticsLabelDTO.getId());
            if (logisticsOrderEntity == null || LogisticsStatusEnum.CANCEL.getCode().equals(logisticsOrderEntity.getStatus())) {
                resultDTO = BatchResultDTO.fail(logisticsLabelDTO.getId(), logisticsLabelDTO.getCode(), "单据不存在或者状态发生变更");
                resultDTOS.add(resultDTO);
                continue;
            } else if (StringUtils.isNotBlank(logisticsOrderEntity.getSourceCode())) {
                resultDTO = BatchResultDTO.fail(logisticsOrderEntity.getId(), logisticsOrderEntity.getCode(), "有来源单号的单据不能在该页面获取面单");
                resultDTOS.add(resultDTO);
                continue;
            }
            String channelId = logisticsLabelDTO.getLogisticsChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
                throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
            }
            Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
            OrderLabelRequest orderLabelRequest = buildLabelRequest(authMap, logisticsLabelDTO.getTrackNo());
            boolean success = false;
            BaseResult baseResult = null;
            TmsAttachmentEntity attachmentEntity = new TmsAttachmentEntity();
            try {
                ValidatorUtil.validateEntity(orderLabelRequest);
                baseResult = expressShipperService.getLabel(authMap, orderLabelRequest);
                // 转换实体
                if (baseResult.isSuccess()) {
                    LabelResponse labelResponse = JSONUtil.toBean(JSONUtil.toJsonStr(baseResult.getObj()), LabelResponse.class);
                    // 根据文件列表 调用文件中心的接口获得ERP的文件url
                    List<PrintFile> files = labelResponse.getFiles();
                    if (1 == files.size()) {
                        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                                .url(files.get(0).getUrl())
                                .token(files.get(0).getToken())
                                .fileName(logisticsLabelDTO.getTrackNo() + ".pdf")
                                .build();
                        String url = fileFeign.uploadFileByUrl(uploadBase64);
                        logisticsOrderEntity.setLabelStatus(LogisticsLabelStatusEnum.OBTAINED.getCode());
                        attachmentEntity.setAttachName(uploadBase64.getFileName());
                        attachmentEntity.setAttachUrl(url);
                        attachmentEntity.setType("logistics_label");
                        attachmentEntity.setBusinessId(logisticsLabelDTO.getId());
                    }
                    success = true;
                } else {
                    logisticsOrderEntity.setExceptionReason(baseResult.getErrorMessage());
                    resultDTO = BatchResultDTO.fail(logisticsOrderEntity.getId(), logisticsOrderEntity.getCode(), baseResult.getErrorMessage());
                }
            } catch (Exception e) {
                logisticsOrderEntity.setExceptionReason(e.getMessage());
                resultDTO = BatchResultDTO.fail(logisticsOrderEntity.getId(), logisticsOrderEntity.getCode(), e.getMessage());
            }
            if (success) {
                if (attachmentMap.get(logisticsOrderEntity.getId()) != null) {
                    attachmentService.removeById(attachmentMap.get(logisticsOrderEntity.getId()).getId());
                }
                attachmentService.save(attachmentEntity);
                pushLabelOperateLog(logisticsLabelDTO.getId(), logisticsLabelDTO.getTrackNo(), RequestStatusEnums.SUCCESS.getCode(), logisticsLabelDTO, baseResult);
                resultDTO = BatchResultDTO.success(logisticsOrderEntity.getId(), logisticsOrderEntity.getCode(), "获取面单成功");
            } else {
                logisticsOrderEntity.setLabelStatus(LogisticsLabelStatusEnum.NOT_OBTAINED.getCode());
                logisticsOrderEntity.setExceptionType(ExceptionTypeEnum.LABEL_EXCEPTION.getCode());
                pushLabelOperateLog(logisticsLabelDTO.getId(), logisticsLabelDTO.getTrackNo(), RequestStatusEnums.FAILED.getCode(), logisticsLabelDTO, baseResult);
            }
            this.updateById(logisticsOrderEntity);
            // 记录主单操作日志
            log.info("获取面单 开始记录物流下单表日志数据，单号：【{}】", logisticsOrderEntity.getCode());
            String msg = StrUtil.format("获取物流面单{}", success ? "成功" : "失败");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), "获取物流面单");
            resultDTOS.add(resultDTO);
        }
        return resultDTOS;
    }

    @Override
    public List<BatchResultDTO> batchLogisticsLabel(BaseIdsDTO.IdsDTO dto) {
        List<LogisticsOrderEntity> entityList = this.listByIds(dto.getIds());
        List<LogisticsOrderDTO.LogisticsLabelDTO> labelDTOList = entityList.stream().map(this::getLogisticsOrderLabel).collect(Collectors.toList());
        return this.getLogisticsOrderLabel(labelDTOList);
    }

    @Override
    public List<AfterSaleDTO.LogisticsOrderResultDTO> batchGetLabel(List<LogisticsOrderDTO.LogisticsLabelDTO> logisticsLabelDTOS) {
        log.info("批量获取物流面单开始：{}", JSON.toJSONString(logisticsLabelDTOS));
        List<AfterSaleDTO.LogisticsOrderResultDTO> resultDTOList = new ArrayList<>();
        for (LogisticsOrderDTO.LogisticsLabelDTO logisticsLabelDTO : logisticsLabelDTOS) {
            AfterSaleDTO.LogisticsOrderResultDTO resultDTO = new AfterSaleDTO.LogisticsOrderResultDTO();
            LogisticsOrderEntity logisticsOrderEntity = this.lambdaQuery().eq(LogisticsOrderEntity::getAfterSaleId, logisticsLabelDTO.getAfterSaleId()).one();
            if (logisticsOrderEntity == null || LogisticsStatusEnum.CANCEL.getCode().equals(logisticsOrderEntity.getStatus())) {
                log.warn("未找到物流单：{}", logisticsLabelDTO.getAfterSaleId());
                resultDTO.setStatus(false);
                resultDTO.setAfterSaleId(logisticsLabelDTO.getAfterSaleId());
                resultDTO.setErrorMsg("单据不存在或者状态发生变更");
                resultDTOList.add(resultDTO);
                continue;
            }
            String channelId = logisticsOrderEntity.getLogisticsChannelId();
            LogisticsSupplierDTO.AuthDTO auth = logisticsAuthService.getAuthByChannelId(channelId);
            if (Objects.isNull(auth)) {
                log.warn("未找到物流渠道：{}", channelId);
                throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
            }
            Map<String, String> authMap = logisticsAuthService.getLogisticsAuthConfig(auth.getAuthId(), null, auth.getLogisticsPlatform());
            OrderLabelRequest orderLabelRequest = buildLabelRequest(authMap, logisticsOrderEntity.getTrackNo());
            boolean success = false;
            BaseResult baseResult = null;
            String url = "";
            try {
                ValidatorUtil.validateEntity(orderLabelRequest);
                log.info("获取物流面单开始：{}", JSON.toJSONString(orderLabelRequest));
                baseResult = expressShipperService.getLabel(authMap, orderLabelRequest);
                log.info("获取物流面单结束：{}", JSON.toJSONString(baseResult));
                // 转换实体
                if (baseResult.isSuccess()) {
                    LabelResponse labelResponse = JSONUtil.toBean(JSONUtil.toJsonStr(baseResult.getObj()), LabelResponse.class);
                    // 根据文件列表 调用文件中心的接口获得ERP的文件url
                    List<PrintFile> files = labelResponse.getFiles();
                    if (1 == files.size()) {
                        FileDTO.UploadBase64 uploadBase64 = FileDTO.UploadBase64.builder()
                                .url(files.get(0).getUrl())
                                .token(files.get(0).getToken())
                                .fileName(logisticsOrderEntity.getTrackNo() + ".pdf")
                                .build();
                        url = fileFeign.uploadFileByUrl(uploadBase64);
                    }
                    success = true;
                } else {
                    resultDTO.setErrorMsg(baseResult.getErrorMessage());
                    logisticsOrderEntity.setExceptionReason(baseResult.getErrorMessage());
                }
            } catch (Exception e) {
                resultDTO.setErrorMsg(e.getMessage());
                logisticsOrderEntity.setExceptionReason(e.getMessage());
            }
            if (success) {
                logisticsOrderEntity.setLabelStatus(LogisticsLabelStatusEnum.OBTAINED.getCode());
                resultDTO.setStatus(true);
                resultDTO.setAfterSaleId(logisticsOrderEntity.getAfterSaleId());
                resultDTO.setTrackNo(logisticsOrderEntity.getTrackNo());
                resultDTO.setUrl(url);
                resultDTOList.add(resultDTO);
                pushLabelOperateLog(logisticsOrderEntity.getId(), logisticsOrderEntity.getTrackNo(), RequestStatusEnums.SUCCESS.getCode(), logisticsLabelDTO, baseResult);
            } else {
                resultDTO.setStatus(false);
                resultDTO.setAfterSaleId(logisticsOrderEntity.getAfterSaleId());
                resultDTOList.add(resultDTO);
                logisticsOrderEntity.setLabelStatus(LogisticsLabelStatusEnum.NOT_OBTAINED.getCode());
                logisticsOrderEntity.setExceptionType(ExceptionTypeEnum.LABEL_EXCEPTION.getCode());
                pushLabelOperateLog(logisticsOrderEntity.getId(), logisticsOrderEntity.getTrackNo(), RequestStatusEnums.FAILED.getCode(), logisticsLabelDTO, baseResult);
            }
            this.updateById(logisticsOrderEntity);
            // 记录主单操作日志
            log.info("寄修申请下单的数据 开始记录物流下单表日志数据，单号：【{}】", logisticsOrderEntity.getCode());
            String msg = StrUtil.format("获取物流面单{}", success ? "成功" : "失败");
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_ORDER.getCode(), logisticsOrderEntity.getId(), "获取物流面单");
        }
        return resultDTOList;
    }

    @Override
    public void updateLogisticsOrder(String afterSaleId) {
        super.lambdaUpdate().eq(LogisticsOrderEntity::getAfterSaleId, afterSaleId).set(LogisticsOrderEntity::getLabelStatus, LogisticsLabelStatusEnum.OBTAINED.getCode()).update();
    }

    /**
     * 渠道授权信息载体
     */
    private static class ChannelAuthInfo {
        LogisticsSupplierDTO.AuthDTO auth;
        Map<String, String> authMap;
        LogisticsChannelEntity channel;
    }

    /**
     * 获取渠道授权、渠道、销售渠道信息（add/update/addBatch 共用）
     */
    private ChannelAuthInfo getChannelAuthInfo(String channelId) {
        ChannelAuthInfo info = new ChannelAuthInfo();
        info.auth = logisticsAuthService.getAuthByChannelId(channelId);
        if (Objects.isNull(info.auth)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        info.authMap = logisticsAuthService.getLogisticsAuthConfig(info.auth.getAuthId(), null, info.auth.getLogisticsPlatform());
        info.channel = logisticsChannelService.getById(channelId);
        if (Objects.isNull(info.channel)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_NOT_FOUND);
        }
        // 渠道编码不能为空，或者必须是数字
        if (StringUtils.isBlank(info.channel.getCode()) || !StringUtils.isNumeric(info.channel.getCode())) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_CODE_EMPTY);
        }
        return info;
    }

    /**
     * 构建寄件方ContactInfo（add/update/addBatch 共用）
     */
    private ContactInfo buildSenderContactInfo(String channelId, String channelName) {
        List<LogisticsAddressEntity> addressList = logisticsAddressService.listByChannelIdAndShopId(channelId, "all");
        LogisticsAddressTypeEnum finalDeliverType = LogisticsAddressTypeEnum.DELIVER;
        List<LogisticsAddressEntity> deliverList = addressList.stream().filter(a -> finalDeliverType.equals(a.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(deliverList)) {
            throw new ServiceException(ApiError.LOGISTICS_CHANNEL_ADDRESS_TYPE_EMPTY, channelName, finalDeliverType.getName());
        }
        LogisticsAddressEntity logisticsAddress = deliverList.get(0);
        ContactInfo sender = new ContactInfo();
        sender.setContactType(1);
        sender.setContact(logisticsAddress.getContact());
        sender.setMobile(logisticsAddress.getTelNumber());
        sender.setCountry(logisticsAddress.getCountry());
        sender.setProvince(logisticsAddress.getProvinceName());
        sender.setCity(logisticsAddress.getCityName());
        sender.setAddress(logisticsAddress.getAddressFirst());
        return sender;
    }

    /**
     * 构建收件方ContactInfo（add/update/addBatch 共用）
     */
    private ContactInfo buildReceiverContactInfo(String contact, String mobile, String province, String city, String address) {
        ContactInfo receiver = new ContactInfo();
        receiver.setContactType(2);
        receiver.setContact(contact);
        receiver.setMobile(mobile);
        receiver.setCountry(CountrySiteEnum.CHINA.getSite());
        receiver.setProvince(province);
        receiver.setCity(city);
        receiver.setAddress(address);
        return receiver;
    }

    /**
     * 处理下单结果，设置status/trackNo/exceptionType/exceptionReason（add/update 共用）
     */
    private void handleOrderResult(BaseResult baseResult, LogisticsOrderEntity entity) {
        if (baseResult.isSuccess()) {
            entity.setStatus(LogisticsStatusEnum.SUCCESS.getCode());
            OrderResponse orderResponse = JSONUtil.toBean(baseResult.getMsgData(), OrderResponse.class);
            List<WaybillNoInfo> waybillNoInfoList = orderResponse.getWaybillNoInfoList();
            if (CollectionUtils.isNotEmpty(waybillNoInfoList)) {
                WaybillNoInfo waybillNoInfo = waybillNoInfoList.stream().filter(e -> e.getWaybillType() == 1).findFirst().orElse(null);
                if (Objects.nonNull(waybillNoInfo)) {
                    entity.setTrackNo(waybillNoInfo.getWaybillNo());
                }
            }
        } else {
            entity.setStatus(LogisticsStatusEnum.FAILED.getCode());
            entity.setExceptionType(ExceptionTypeEnum.ORDER_EXCEPTION.getCode());
            entity.setExceptionReason(baseResult.getErrorMsg());
        }
    }

    /**
     * 下单成功后异步发送获取面单的MQ消息（add/update 共用）
     */
    private void asyncSendLabelRequest(LogisticsOrderEntity entity) {
        if (StringUtils.isNotBlank(entity.getTrackNo())) {
            String labelRedisKey = StrUtil.format(RedisCacheConstants.TMS_LOGISTIC_LABEL, entity.getId(), entity.getTrackNo());
            redisUtil.set(labelRedisKey, true, 86400);
            LogisticsOrderDTO.LogisticsLabelDTO labelDTO = getLogisticsOrderLabel(entity);
            mqProducerService.asyncClassMsg(RocketMqTopic.TMS_ASYNC_GET_LOGISTICS_ORDER_LABEL_TOPIC, RocketMqTagEnum.TMS_ASYNC_GET_LOGISTICS_ORDER_LABEL_TAG.getName(), labelDTO, IdUtil.simpleUUID());
        }
    }

    /**
     * 推送取消订单操作日志（cancel/batchCancel 共用）
     */
    private void pushCancelOperateLog(LogisticsOrderEntity entity, String requestStatus, OrderUpdateRequest request, BaseResult result) {
        logisticsOperateService.pushOperateLog(entity.getCode(), entity.getTrackNo(), BusinessTypeEnum.CANCEL_ORDER.getCode(),
                LogisticsPlatformEnum.SF_EXPRESS.getCode(), requestStatus, JSONUtil.toJsonStr(request),
                JSONUtil.toJsonStr(result), false);
    }

    /**
     * 构建面单请求（getLogisticsOrderLabel/batchGetLabel 共用）
     */
    private OrderLabelRequest buildLabelRequest(Map<String, String> authMap, String trackNo) {
        return OrderLabelRequest.builder()
                .templateCode(templateCode + authMap.get("clientId"))
                .documents(Collections.singletonList(Document.builder().masterWaybillNo(trackNo).build()))
                .version(version)
                .fileType(fileType)
                .sync(true)
                .build();
    }

    /**
     * 推送面单操作日志（getLogisticsOrderLabel/batchGetLabel 共用）
     */
    private void pushLabelOperateLog(String id, String trackNo, String status, Object request, Object result) {
        logisticsOperateService.pullOperateLog(id, trackNo, BusinessTypeEnum.GET_LABEL.getCode(),
                LogisticsPlatformEnum.SF_EXPRESS.getCode(), status, JSONUtil.toJsonStr(request), JSONUtil.toJsonStr(result));
    }

}
