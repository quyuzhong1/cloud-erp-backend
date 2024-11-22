package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.constant.SqlConstants;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SplitSkuDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateInboundReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.PackageForecastFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.tms.convert.TransferDeclareConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.TransferDeclareMapper;
import com.erp.server.tms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.internal.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_TRANSFER_DECLARE;

/**
 * <p>
 * 中转报关表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferDeclareServiceImpl extends SuperServiceImpl<TransferDeclareMapper, TransferDeclareEntity> implements TransferDeclareService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private DocNoGenHelper docNoGenHelper;
    @Resource
    private TransferDeclareDetailService transferDeclareDetailService;
    @Resource
    private MultipleOptionService multipleOptionService;
    @Resource
    private TransferDeclareGenerationSettingService transferDeclareGenerationSettingService;
    @Resource
    private TransferDeclareDeadlineSettingService transferDeclareDeadlineSettingService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private LogisticsAuthService logisticsAuthService;
    @Resource
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Resource
    private TransferLogisticsChannelService transferLogisticsChannelService;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private TransferLogisticsRegistry transferLogisticsRegistry;
    @Resource
    private TransferLogisticsAuthService transferLogisticsAuthService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private PackageForecastFeign packageForecastFeign;
    @Resource
    private TransferDeclareProductService transferDeclareProductService;
    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Resource
    private TmsB2cDeclareReconciliationDetailService tmsB2cDeclareReconciliationDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TransferDeclareDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        //数据处理
        fillList(pageData.getRecords(),pagingParamDTO.getParams());
        return new PagingVO(pageData);
    }

    @Override
    public List<TransferDeclareDTO.TabListDTO> tabList(PermissionsDTO param) {
        List<TransferDeclareDTO.TabListDTO> result = new ArrayList<>();
        TransferDeclareTabFlagEnum[] values = TransferDeclareTabFlagEnum.values();
        for (TransferDeclareTabFlagEnum item : values) {
            TransferDeclareDTO.PagingParamDTO pagingParamDTO = new TransferDeclareDTO.PagingParamDTO();
            pagingParamDTO.setPermissionSql(param.getPermissionSql());
            //搜索类型
            pagingParamDTO.setTabFlag(item.getCode());
            //tab页条件匹配状态
            handleTableParam(pagingParamDTO);

            Integer count = MathUtil.ZERO;
            if (TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(item.getCode())) {
                count = this.baseMapper.listUploadStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(item.getCode())) {
                count = this.baseMapper.listUploadStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.LOGISTICS_UN_OUTSTOCK.getCode().equals(item.getCode())) {
                count = this.baseMapper.listTransferStatusCount(pagingParamDTO);
            }
            if (TransferDeclareTabFlagEnum.LOGISTICS_OUTSTOCK.getCode().equals(item.getCode())) {
                count = this.baseMapper.listTransferStatusCount(pagingParamDTO);
            }

            TransferDeclareDTO.TabListDTO resultDTO = new TransferDeclareDTO.TabListDTO();
            resultDTO.setTabFlag(item.getCode());
            resultDTO.setCount(count);
            result.add(resultDTO);
        }
        return result;
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TransferDeclareDTO.AddDTO addDTO) {
        TransferDeclareEntity transferDeclareEntity = new TransferDeclareEntity();
        BeanMapperUtils.copy(addDTO, transferDeclareEntity);

        //包裹总重量
        BigDecimal packageTotalWeight = addDTO.getDetailList().stream().map(req -> req.getPackageWeight()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        transferDeclareEntity.setPackageTotalWeight(packageTotalWeight);
        //包裹总数量
        transferDeclareEntity.setPackageTotalQty(addDTO.getDetailList().size());

        //设置预计中转日期
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> view = transferDeclareDeadlineSettingService.view();
        TransferDeclareDeadlineSettingDTO.ViewDTO viewDTO = view.stream().filter(req -> req.getTransferLogisticsSupplierIdList().contains(transferDeclareEntity.getTransferLogisticsSupplierId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
            if (viewDTO.getDeadlineTime().isAfter(addDTO.getGenerateTime())) {
                transferDeclareEntity.setPlanTransferDate(LocalDate.now());
            } else {
                transferDeclareEntity.setPlanTransferDate(LocalDate.now().plusDays(1));
            }
        }

        if (StringUtils.isBlank(transferDeclareEntity.getTransferLogisticsSupplierId())) {
            throw new ServiceException("未找到订单的中转物流商，请检查是否无需中转，无需中转不需要入库预报");
        }

        // 数据处理
        handleData(transferDeclareEntity);

        log.info("开始新增中转报关单");
        // 生成单号
        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZZBG);
        transferDeclareEntity.setCode(code);
        boolean save = super.save(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }

        //新增明细
        transferDeclareDetailService.add(addDTO, transferDeclareEntity.getId());

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "中转报关单" , transferDeclareEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.TRANSFER_DECLARE.getCode(), transferDeclareEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(transferDeclareEntity.getId(), code);
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TransferDeclareDTO.UpdateDTO updateDTO) {
        TransferDeclareEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "中转报关单"));
        TransferDeclareEntity transferDeclareEntity =  BeanMapperUtils.map(TransferDeclareEntity.class, updateDTO);

        //包裹总重量
        BigDecimal packageTotalWeight = updateDTO.getDetailList().stream().map(req -> req.getPackageWeight()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        transferDeclareEntity.setPackageTotalWeight(packageTotalWeight);
        //包裹总数量
        transferDeclareEntity.setPackageTotalQty(updateDTO.getDetailList().size());

        // 数据处理
        handleData(transferDeclareEntity);
        log.info("编辑 开始修改中转报关单数据，单号：【{}】", old.getCode());
        boolean save = super.updateById(transferDeclareEntity);
        if(!save) {
            throw new ServiceException("中转报关单保存失败");
        }
        // 修改明细数据（包含增删改）
        transferDeclareDetailService.update(updateDTO, transferDeclareEntity.getId());

        //如果明细全部上传成功，修改主单据上传状态为上传成功
        List<TransferDeclareDetailEntity> detailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(transferDeclareEntity.getId()));
        long count = detailEntities.stream().filter(req -> !TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getOrderUploadStatus())).count();
        if (count == 0) {
            this.updateUploadStatus(transferDeclareEntity.getId(), TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode());
        }

        // 记录主单操作日志
        log.info("编辑 开始记录中转报关单日志数据，单号：【{}】", transferDeclareEntity.getCode());
        String msg = CharSequenceUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), transferDeclareEntity.getCode(), "中转报关单");
        operateLogService.addModuleOperateLogByObj(old, transferDeclareEntity, ModuleTypeEnum.TRANSFER_DECLARE.getCode(), transferDeclareEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public TransferDeclareEntity checkExistByChannelIds(List<String> ids) {
        return lambdaQuery().in(TransferDeclareEntity::getTransferChannelId, ids).last(SqlConstants.LIMIT_1).one();
    }

    @Override
    public TransferDeclareDTO.ViewDTO view(String id) {
        //报关单主信息
        TransferDeclareEntity transferDeclareEntity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到报关单数据"));
        TransferDeclareDTO.ViewDTO data = BeanMapperUtils.map(TransferDeclareDTO.ViewDTO.class, transferDeclareEntity);

        //报关单详情
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(id));
        // 数据填充处理
        fillOne(data, transferDeclareDetailEntities);
        return data;
    }

    @Override
    public List<TransferDeclareDetailDTO.ViewDTO> viewDetailList(TransferDeclareDTO.ViewDetailParamDTO dto) {
        List<TransferDeclareDetailDTO.ViewDTO> viewDTOS = transferDeclareDetailService.viewDetailList(dto);
        for (TransferDeclareDetailDTO.ViewDTO viewDTO : viewDTOS) {
            //出库状态中文
            viewDTO.setOutstockStatusName(TransferOutstockStatusEnum.getName(viewDTO.getOutstockStatus()));
            //中转状态中文
            viewDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(viewDTO.getTransferStatus()));
            //上传状态（订单）中文
            viewDTO.setOrderUploadStatusName(TransferDeclareUploadStatusEnum.getName(viewDTO.getOrderUploadStatus()));
        }
        return viewDTOS;
    }

    @Override
    public Boolean forcastSetting(List<TransferDeclareGenerationSettingDTO.AddDTO> dtoList) {
        transferDeclareGenerationSettingService.save(dtoList);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView() {
        return transferDeclareGenerationSettingService.forcastSettingView();
    }

    @Override
    public Boolean deadlineSetting(List<TransferDeclareDeadlineSettingDTO.AddDTO> dto) {
        transferDeclareDeadlineSettingService.add(dto);
        return Boolean.TRUE;
    }

    @Override
    public List<TransferDeclareDeadlineSettingDTO.ViewDTO> deadlineSettingView() {
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> view = transferDeclareDeadlineSettingService.view();
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //上传成功不能删除
        List<TransferDeclareEntity> transferDeclareEntities = this.listByIds(ids);
        long count = transferDeclareEntities.stream()
                .filter(req -> InstockForecastStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getInstockForecastStatus()))
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.UPLOAD_SUCCESS_NOT_DELETE);
        }

        //删除详情
        transferDeclareDetailService.deleteByMainIds(ids);

        return this.removeByIds(ids);
    }

    @Override
    public Boolean exportExcel(TransferDeclareDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("中转报关单导出", EXPORT_TMS_TRANSFER_DECLARE.getCode(), dto);
        return Boolean.TRUE;
    }

    private void fileExportList(List<TransferDeclareDTO.ExportListDTO> list) {
        if (CollectionUtil.isEmpty(list)){
            return;
        }
        List<String> soIdList = list.stream().map(TransferDeclareDTO.ExportListDTO::getSoId).distinct().collect(Collectors.toList());
        List<PackageForecastDetailEntity> packageForecastDetailEntityList = CollectionUtils.isNotEmpty(soIdList)?FeignQuery.create(PackageForecastDetailEntity.class).in(PackageForecastDetailEntity::getSoId,soIdList).list():new ArrayList<>();
        List<String> packageForecastIds = packageForecastDetailEntityList.stream().map(PackageForecastDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<PackageForecastEntity> packageForecastEntityList = CollectionUtils.isNotEmpty(packageForecastIds)?FeignQuery.create(PackageForecastEntity.class).in(PackageForecastEntity::getId,packageForecastIds).list():new ArrayList<>();
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIdList);
        list.forEach(exportListDTO -> {
            PackageForecastDetailEntity mainPackageForecastDetailEntity = packageForecastDetailEntityList.stream().filter(v -> v.getSoCode().equals(exportListDTO.getSoCode())).findFirst().orElse(new PackageForecastDetailEntity());
            PackageForecastEntity mainPackageForecastEntity = packageForecastEntityList.stream().filter(v -> v.getId().equals(mainPackageForecastDetailEntity.getMainId())).findFirst().orElse(new PackageForecastEntity());
            exportListDTO.setPackageForecastCode(mainPackageForecastEntity.getCode());
            exportListDTO.setUploadBatchStatusName(TransferDeclareUploadStatusEnum.getName(exportListDTO.getUploadBatchStatus()));
            exportListDTO.setInstockForecastStatusName(InstockForecastStatusEnum.getName(exportListDTO.getInstockForecastStatus()));
            exportListDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.getName(exportListDTO.getUploadOrderStatus()));
//            exportListDTO.setOutstockStatusName(TransferOutstockStatusEnum.getName(exportListDTO.getOutstockStatus()));
            exportListDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(exportListDTO.getTransferStatus()));
            SoB2cEntity detailSoB2cEntity = soB2cEntityList.stream()
                    .filter(req -> req.getId().equals(exportListDTO.getSoId())
                            && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(detailSoB2cEntity)) {
                exportListDTO.setOutstockStatusName(TransferOutstockStatusEnum.OUTSTOCK.getName());
            } else {
                exportListDTO.setOutstockStatusName(TransferOutstockStatusEnum.UN_OUTSTOCK.getName());
            }
        });
    }

    @Override
    public List<BatchResultDTO> instockForecast(BaseDTO.QtyDTO qtyDTO) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        TransferDeclareEntity transferDeclareEntity = this.getById(qtyDTO.getId());

        //仅支持【订单预报(批次)】上传成功时且入库预报为【待上传/上传失败】，可操作【入库预报】
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(qtyDTO.getId()));
        List<TransferDeclareDetailEntity> transferDeclareDetailList = transferDeclareDetailEntities.stream()
                .filter(req -> TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getOrderUploadStatus()))
                .collect(Collectors.toList());

        //新增校验推送订单是否全部出库，未成功提示：{销售订单号}未完成出库无法执行入库预报
        List<TransferDeclareDetailEntity> detailEntityList = transferDeclareDetailService.listByMainIds(Arrays.asList(qtyDTO.getId()));
        List<String> soIds = detailEntityList.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        for (TransferDeclareDetailEntity transferDeclareDetailEntity : detailEntityList) {
            List<SoB2cEntity> soB2cEntities = soB2cEntityList.stream().filter(req -> transferDeclareDetailEntity.getSoId().equals(req.getId())
                    && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus())
            ).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(soB2cEntities)) {
                resultDTOList.add(BatchResultDTO.fail(transferDeclareEntity.getId(), transferDeclareEntity.getCode(), "【"+transferDeclareDetailEntity.getSoCode()+"】未完成出库无法执行入库预报"));
                return resultDTOList;
            }
        }
        List<TransferLogisticsCreateInboundReq.ReceiveItem> receiveItemList = new ArrayList<>(transferDeclareDetailList.size());
        //查询报关单包含的订单信息
        List<String> soIdList = transferDeclareDetailList.stream().map(TransferDeclareDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIdList);
        List<PackageForecastDTO.ExportViewDTO> exportViewDTOS = packageForecastFeign.listPackageForecastBySoIdList(soIdList);
        String platform = "";
        if(StringUtils.isNotBlank(transferDeclareEntity.getDeliveryLogisticsSupplierId())){
            LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthService.getAuthBySupplierId(transferDeclareEntity.getDeliveryLogisticsSupplierId());
            if(Objects.nonNull(authDTO)){
                platform = authDTO.getLogisticsPlatform();
            }
        }
        //下单
        for (TransferDeclareDetailEntity transferDeclareDetailEntity : transferDeclareDetailList) {
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(e -> e.getId().equals(transferDeclareDetailEntity.getSoId())).findFirst().orElse(null);
            PackageForecastDTO.ExportViewDTO packageForecastDTO = exportViewDTOS.stream().filter(e -> e.getSoId().equals(transferDeclareDetailEntity.getSoId())).findFirst().orElse(new PackageForecastDTO.ExportViewDTO());
            if (Objects.nonNull(soB2cEntity) && StringUtils.isNotEmpty(soB2cEntity.getShippingOrderNo())){
                BigDecimal maxWeight = BigDecimal.ONE;
                if (transferDeclareDetailEntity.getWeightUnit().equals("g") && transferDeclareDetailEntity.getPackageWeight().compareTo(BigDecimal.ZERO) != 0) {
                    maxWeight = transferDeclareDetailEntity.getPackageWeight().divide(BigDecimal.valueOf(1000));
                }
                if(LogisticsPlatformEnum.ALI_EXPRESS.getCode().equals(platform) && StringUtils.isBlank(packageForecastDTO.getTransportNo())){
                    throw new ServiceException("物流物流中转报关必须要有4PX单号");
                }
                TransferLogisticsCreateInboundReq.ReceiveItem receiveItem = TransferLogisticsCreateInboundReq.ReceiveItem.builder()
                        .orderCode(soB2cEntity.getShippingOrderNo())
                        .packNum(StringUtils.isBlank(packageForecastDTO.getTransportNo())?packageForecastDTO.getCode():packageForecastDTO.getTransportNo())
                        .grossWeight(maxWeight)
                        .build();
                receiveItemList.add(receiveItem);
            }
        }
        if(CollectionUtils.isEmpty(receiveItemList)){
            throw new ServiceException(ApiError.ERROR_TRANSFER_DECLARE_DETAIL_NOT_EXIST);
        }
        //查询授权信息
        TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", transferDeclareEntity.getTransferLogisticsSupplierId());
        if (ObjectUtil.isEmpty(authEntity)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
        }
        TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
        if (Objects.isNull(service)){
            resultDTOList.add(BatchResultDTO.fail(transferDeclareEntity.getId(), transferDeclareEntity.getCode(), "未开发平台【" + LogisticsPlatformEnum.getByName(authEntity.getLogisticsPlatform()).getName() + "】报关功能"));
            return resultDTOList;
        }
        //计算入库预报客户单号
        String referenceCode = getReferenceCode();
        TransferLogisticsCreateInboundReq request = TransferLogisticsCreateInboundReq.builder()
                .referenceCode(referenceCode)
                .isDelivery(true)
                .packQty(qtyDTO.getQty())
                .grossWeight(receiveItemList.stream().map(TransferLogisticsCreateInboundReq.ReceiveItem::getGrossWeight).reduce(BigDecimal::add).orElse(BigDecimal.ZERO))
                .receivingStatus("2")
                .receiveItemList(receiveItemList)
                .build();
        try {
            //下单
            log.info("入库预报参数请求：{}", JSONObject.toJSON(request));
            ApiResult<String> result = service.createInbound(request, authEntity.getId());
            log.info("入库预报参数响应：{}", JSONObject.toJSON(result));
            transferDeclareEntity.setInstockRefCode(referenceCode);
            transferDeclareEntity.setInstockForecastAsnCode(result.getData());
            transferDeclareEntity.setTotalQty(qtyDTO.getQty());
            if (result.getCode() == 200) {
                //拿到第三方订单号，用于给订单赋值第三方平台发货单号
                transferDeclareEntity.setInstockForecastStatus(InstockForecastStatusEnum.UPLOAD_SUCCESS.getCode());
                transferDeclareEntity.setInstockForecastRemark("");
                transferDeclareEntity.setInstockForecastDate(LocalDate.now());
                //上传成功
                baseMapper.updateById(transferDeclareEntity);

                //入库预报成功添加报关对账明细
                addDeclareReconciliation(transferDeclareDetailEntities);

                resultDTOList.add(BatchResultDTO.success(transferDeclareEntity.getId(), transferDeclareEntity.getCode(), "入库预报成功"));
            } else {
                String msg = String.format("入库预报失败：%s", result.getMsg());
                //上传失败
                transferDeclareEntity.setInstockForecastRemark(result.getMsg());
                transferDeclareEntity.setInstockForecastStatus(InstockForecastStatusEnum.UPLOAD_FAILURE.getCode());
                baseMapper.updateById(transferDeclareEntity);

                resultDTOList.add(BatchResultDTO.fail(transferDeclareEntity.getId(), transferDeclareEntity.getCode(), msg));
            }
        }catch (Exception e){
            transferDeclareEntity.setInstockRefCode(referenceCode);
            transferDeclareEntity.setInstockForecastAsnCode("");
            transferDeclareEntity.setTotalQty(qtyDTO.getQty());
            String msg = String.format("入库预报失败：%s", e.getMessage());
            //上传失败
            transferDeclareEntity.setInstockForecastRemark(e.getMessage());
            transferDeclareEntity.setInstockForecastStatus(InstockForecastStatusEnum.UPLOAD_FAILURE.getCode());
            baseMapper.updateById(transferDeclareEntity);


            resultDTOList.add(BatchResultDTO.fail(transferDeclareEntity.getId(), transferDeclareEntity.getCode(), e.getMessage()));
        }


        return resultDTOList;
    }

    /**
     * @description: 新增报关对账明细数据
     * @author Will
     * @date: 2024/3/26 17:19
     * @param transferDeclareDetailList
     */
    private void addDeclareReconciliation (List<TransferDeclareDetailEntity> transferDeclareDetailList) {
        if (CollectionUtil.isEmpty(transferDeclareDetailList)) {
            return;
        }
        List<TmsB2cDeclareReconciliationDetailDTO.AddDTO> addDetailList = new ArrayList<>();
        for (TransferDeclareDetailEntity transferDeclareDetailEntity : transferDeclareDetailList) {
            TmsB2cDeclareReconciliationDetailDTO.AddDTO addDTO = new TmsB2cDeclareReconciliationDetailDTO.AddDTO();
            addDTO.setSourceDetailId(transferDeclareDetailEntity.getId());
            addDTO.setSourceType(SourceTypeEnum.TRANSFER_DECLARE.getCode());
            addDetailList.add(addDTO);
        }
        if (CollectionUtil.isEmpty(addDetailList)) {
            return;
        }
        tmsB2cDeclareReconciliationDetailService.add(addDetailList);
    }

    private String getReferenceCode() {
        StringBuilder stringBuffer = new StringBuilder();
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String format = localDateTime.format(formatter);
        stringBuffer.append("深圳市优篮子科技有限公司+").append(format);
        Integer count = this.lambdaQuery().likeRight(TransferDeclareEntity::getInstockRefCode,stringBuffer.toString()).count();
        if (Objects.isNull(count)){
            stringBuffer.append(StringUtils.leftPad("1",4, "0"));
        }else {
            count = count + 1;
            stringBuffer.append(StringUtils.leftPad(count.toString(),4, "0"));
        }
        return stringBuffer.toString();
    }

    @Override
    public void declareAutoGenerationJob() {
        LocalTime localTime = LocalTime.now();
        //报关设置信息
        List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView = transferDeclareGenerationSettingService.forcastSettingView();
        XxlJobHelper.log("====查询报关设置信息信息，date={}====", JSONUtil.toJsonStr(forcastSettingView));
        //截单设置信息
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> deadlineSettingView = transferDeclareDeadlineSettingService.view();
        XxlJobHelper.log("====查询截单设置信息信息，date={}====", JSONUtil.toJsonStr(deadlineSettingView));
        for (TransferDeclareDeadlineSettingDTO.ViewDTO deadlineSetting : deadlineSettingView) {
            //生效时间
            LocalTime generateTime = deadlineSetting.getGenerateTime();
            XxlJobHelper.log("====系统当前时={}：分={}，接单设置时={}：分={}====", localTime.getHour(), localTime.getMinute(), generateTime.getHour(), generateTime.getMinute());
            if (localTime.getHour() == generateTime.getHour() && generateTime.getMinute() == localTime.getMinute()) {

                //如果当前时间等于生效时间，根据报关设置生成报关单
                List<TransferDeclareGenerationSettingDTO.ViewDTO> viewDTOList = forcastSettingView.stream().filter(req -> deadlineSetting.getTransferLogisticsSupplierIdList().contains(req.getTransferLogisticsSupplierId())).collect(Collectors.toList());
                List<TransferDeclareDTO.AddDTO> addDTOList = soB2cFeign.generateTransferDeclareView(viewDTOList);
                XxlJobHelper.log("====根据报关设置生成报关单，组装新增入参TransferDeclareDTO.AddDTO={}====", JSONUtil.toJsonStr(addDTOList));
                for (TransferDeclareDTO.AddDTO addDTO : addDTOList) {
                    addDTO.setGenerateTime(generateTime);
                    BaseResultDTO.AddDTO add = this.add(addDTO);
                    if (StringUtils.isNotBlank(add.getId())) {
                        //更新订单中转状态
                        List<String> soIds = addDTO.getDetailList().stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
//                        soB2cFeign.updateTransferStatusBatch(soIds, TransferStatusEnum.ALREADY.getCode());
                    }
                }
                XxlJobHelper.log("====新增成功====");
            }
        }
    }

    @Override
    public TransferDeclareEntity getBySoId(String soId) {
        return baseMapper.getBySoId(soId);
    }

    @Override
    public Boolean checkExistTransferLogisticsSupplier(String supplierId) {
        if (StringUtils.isBlank(supplierId)) {
            return Boolean.FALSE;
        }
        TransferDeclareEntity entity = lambdaQuery().eq(TransferDeclareEntity::getTransferLogisticsSupplierId, supplierId).last(SqlConstants.LIMIT_1).one();
        if (ObjectUtil.isNotEmpty(entity)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    @Override
    public Boolean updateUploadStatus(String id, String status) {
        return lambdaUpdate().eq(TransferDeclareEntity::getId, id).set(TransferDeclareEntity::getUploadStatus, status).update();
    }

    @Override
    public void getOrderByCodeJob() {
        List<TransferDeclareDetailEntity> detailEntities = transferDeclareDetailService.listWaitSyncTransferStatus();
        List<String> ids = detailEntities.stream().map(req -> req.getMainId()).distinct().collect(Collectors.toList());
        List<TransferDeclareEntity> transferDeclareEntities = this.listByIds(ids);
        List<String> transferLogisticsSupplierIds = transferDeclareEntities.stream().map(req -> req.getTransferLogisticsSupplierId()).distinct().collect(Collectors.toList());

        //查询授权信息
        List<TransferLogisticsAuthEntity> transferLogisticsAuthEntities = transferLogisticsAuthService.listByMainIds(transferLogisticsSupplierIds);
        for (TransferDeclareDetailEntity detailEntity : detailEntities) {
            TransferDeclareEntity transferDeclareEntity = transferDeclareEntities.stream().filter(req -> detailEntity.getMainId().equals(req.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(transferDeclareEntity)) {
                continue;
            }
            TransferLogisticsAuthEntity authEntity = transferLogisticsAuthEntities.stream().filter(req -> transferDeclareEntity.getTransferLogisticsSupplierId().equals(req.getMainId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(authEntity)) {
                continue;
            }
            TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
            ApiResult<TransferLogisticsOrderDTO> result = service.getOrderByCode(detailEntity.getSoCode(), authEntity.getId());
            if (result.getCode() == 200) {
                transferDeclareDetailService.updateTransferStatus(detailEntity.getId(), result.getData().getOrderStatusEnum().getCode());
            }
        }
    }

    @Override
    public TransferDeclareDTO.ShippingOrderDTO b2cOrderForecast(TransferDeclareDTO.B2cOrderForecastDTO b2cOrderForecastDTO) {
        SoB2cEntity soB2cEntity = b2cOrderForecastDTO.getSoB2cEntity();
        SoB2cLogisticsEntity soB2cLogisticsEntity = b2cOrderForecastDTO.getSoB2cLogisticsEntity();
        SoB2cReceiverEntity soB2cReceiverEntity = b2cOrderForecastDTO.getSoB2cReceiverEntity();
        List<SplitSkuDTO> transferDeclareProductDTOList = b2cOrderForecastDTO.getTransferDeclareProductDTOList();
        ShopInfoEntity shopInfoEntity = b2cOrderForecastDTO.getShopInfoEntity();
        try {
            //查询授权信息
            TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", soB2cLogisticsEntity.getTransferLogisticsSupplierId());
            if (ObjectUtil.isEmpty(authEntity)) {
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"未找到物流商授权信息",SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
            TransferLogisticsChannelEntity transferLogisticsChannelEntity = transferLogisticsChannelService.getById(soB2cLogisticsEntity.getTransferLogisticsChannelId());
            if(Objects.isNull(transferLogisticsChannelEntity)){
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"未找到中转渠道",SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
            //下单
            TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
            if (Objects.isNull(service)){
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"未开发平台【" + LogisticsPlatformEnum.getByName(authEntity.getLogisticsPlatform()).getName() + "】报关功能",SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
            LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(soB2cLogisticsEntity.getLogisticsChannelId());
            if(Objects.isNull(logisticsChannelEntity)){
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),"未找到物流渠道",SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
            String trackingNumber = logisticsChannelEntity.getDeclareCodeType().equals(DeclareCodeTypeEnum.TRANSPORT_NO.getCode())?soB2cLogisticsEntity.getCode():soB2cLogisticsEntity.getTrackNo();
            if(StringUtil.isBlank(trackingNumber)){
                String codeType = EnumMessage.getNameByCode(DeclareCodeTypeEnum.class,logisticsChannelEntity.getDeclareCodeType());
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),CharSequenceUtil.format("{}为空",codeType),SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
            List<TransferLogisticsCreateOrderReq.ProductDetail> productDetails = TransferDeclareConverter.INSTANCE.transferDeclareProductConvert(transferDeclareProductDTOList);
            //组装SDK需要的下报关单单信息
            TransferLogisticsCreateOrderReq orderReq = TransferLogisticsCreateOrderReq.builder()
                    .trackingNumber(trackingNumber)
                    .country(soB2cReceiverEntity.getCountry())
                    .shippingCode(transferLogisticsChannelEntity.getCode())
                    .name(soB2cReceiverEntity.getReceiverName())
                    .referenceNo(soB2cEntity.getCode())
                    .deliveryAddress(soB2cReceiverEntity.getFirstAddress())
                    .streetAddress(soB2cReceiverEntity.getFirstAddress())
                    .streetAddress2(soB2cReceiverEntity.getSecondAddress())
                    .state(soB2cReceiverEntity.getProvinceName())
                    .city(soB2cReceiverEntity.getCityName())
                    .postcode(soB2cReceiverEntity.getPostCode())
                    .phone(soB2cReceiverEntity.getReceiverTelNumber())
                    .orderStatus("2")
                    .iossNo(shopInfoEntity.getIossTaxNo())
                    .serialNo("")
                    .grossWeight(soB2cLogisticsEntity.getWeight())
                    .buyInsurance(0)
                    .productDetailList(productDetails)
                    .build();
            try {
                //下单
                ApiResult<String> result = service.createOrder(orderReq, authEntity.getId());
                if (result.getCode() == 200) {
                    //拿到第三方订单号，用于给订单赋值第三方平台发货单号
                    //给订单赋值第三方平台发货单号 同时删除异常标识
                    return TransferDeclareDTO.ShippingOrderDTO.success(soB2cEntity.getId(),soB2cEntity.getCode(),result.getData());
                } else {
                    String msg = String.format("订单预报失败：%s",result.getMsg());
                    return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),msg,SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
                }
            }catch (Exception e){
                String msg = String.format("订单预报失败：%s",e.getMessage());
                return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(),msg,SoB2cErrorTypeEnum.ORDER_FORECAST.getCode());
            }
        }catch (Exception e){
            log.error("b2c订单预报异常",e);
            return TransferDeclareDTO.ShippingOrderDTO.fail(soB2cEntity.getId(),soB2cEntity.getCode(), ExceptionUtil.getSimpleMessage(e));
        }
    }

    @Override
    public ApiResult<String> cancelOrderForecast(TransferDeclareDTO.CancelOrderForecastDTO cancelOrderForecastDTO) {
        //查询授权信息
        TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", cancelOrderForecastDTO.getTransferLogisticsSupplierId());
        if (ObjectUtil.isEmpty(authEntity)) {
            return ApiResult.error("未找到物流商授权信息");
        }
        TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
        if (Objects.isNull(service)){
            return ApiResult.error(CharSequenceUtil.format("{}平台不支持API取消",authEntity.getLogisticsPlatform()));
        }
        return service.cancelOrder(cancelOrderForecastDTO.getTransferCancelOrderReq(),authEntity.getId());
    }

    @Override
    public PagingVO<TransferDeclareDTO.ExportListDTO> exportTransferDeclare(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        Page<TransferDeclareDTO.ExportListDTO> page = baseMapper.listExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            fileExportList(page.getRecords());
        }
        return new PagingVO<>(page);
    }

    private void fillOne(TransferDeclareDTO.ViewDTO data, List<TransferDeclareDetailEntity> transferDeclareDetailEntities) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }
        List<String> soIdList = transferDeclareDetailEntities.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockEntities = soOutstockFeign.listBySoIds(soIdList);

        //明细信息
        List<TransferDeclareDetailDTO.ViewDTO> detailList = BeanMapper.copyList(transferDeclareDetailEntities, TransferDeclareDetailDTO.ViewDTO.class);
        for (TransferDeclareDetailDTO.ViewDTO viewDTO : detailList) {
            //出库状态中文
            SoOutstockEntity soOutstockEntity = soOutstockEntities.stream()
                    .filter(req -> req.getSoId().equals(viewDTO.getSoId())
                            && ApproveStatusEnum.APPROVE.getStatus().equals(req.getApproveStatus().getStatus()))
                    .findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soOutstockEntity)) {
                viewDTO.setOutstockStatusName(TransferOutstockStatusEnum.OUTSTOCK.getName());
            } else {
                viewDTO.setOutstockStatusName(TransferOutstockStatusEnum.UN_OUTSTOCK.getName());
            }
            //中转状态中文
            viewDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(viewDTO.getTransferStatus()));
            //上传状态（订单）中文
            viewDTO.setOrderUploadStatusName(TransferDeclareUploadStatusEnum.getName(viewDTO.getOrderUploadStatus()));
        }
        //明细
        data.setDetailList(detailList);
    }

    /**
     * tab页状态处理
     * @Author Luo_WG
     * @Date 2024/1/20 16:35
     * @param params
     * @return void
     **/
    private void handleTableParam(TransferDeclareDTO.PagingParamDTO params) {
        List<String> uploadStatusList = params.getUploadStatusList();
        List<String> transferStatusList = params.getTransferStatusList();
        List<String> instockForecastStatusList = params.getInstockForecastStatusList();
        //待上传
        if (TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(params.getTabFlag())) {
            instockForecastStatusList.add(InstockForecastStatusEnum.WAIT_UPLOAD.getCode());
        }
        //上传失败
        if (TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(params.getTabFlag())) {
            instockForecastStatusList.add(InstockForecastStatusEnum.UPLOAD_FAILURE.getCode());
        }
        //物流商未出库
        if (TransferDeclareTabFlagEnum.LOGISTICS_UN_OUTSTOCK.getCode().equals(params.getTabFlag())) {
            transferStatusList.add(TransferLogisticsStatusEnum.DELETED.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.DRAFT.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.UNUSUAL.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.CONFIRMED.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.SUBMITTED.getCode());
        }
        //物流商已出库
        if (TransferDeclareTabFlagEnum.LOGISTICS_OUTSTOCK.getCode().equals(params.getTabFlag())) {
            transferStatusList.add(TransferLogisticsStatusEnum.OUTSTOCK.getCode());
            transferStatusList.add(TransferLogisticsStatusEnum.SIGNED.getCode());
        }

        //入库预报状态
        if (CollUtil.isNotEmpty(instockForecastStatusList)) {
            params.setInstockForecastStatusList(instockForecastStatusList);
        }

        //中转状态
        if (CollUtil.isNotEmpty(transferStatusList)) {
            params.setTransferStatusList(transferStatusList);
        }
    }

    /**
     * 分页列表字段处理
     *
     * @param dataList
     * @param params
     */
    private void fillList(List<TransferDeclareDTO.ListDTO> dataList, TransferDeclareDTO.PagingParamDTO params) {
        if (CollectionUtils.isEmpty(dataList)){
            return;
        }
        //主表记录ids
        List<String> declareIds = dataList.stream().map(TransferDeclareDTO.ListDTO::getId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = transferDeclareDetailService.listByCondition(declareIds,params);
        List<String> soIdList = dataList.stream().map(TransferDeclareDTO.ListDTO::getSoId).distinct().collect(Collectors.toList());
        soIdList.addAll(transferDeclareDetailEntities.stream().map(TransferDeclareDetailEntity::getSoId).collect(Collectors.toList()));
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIdList);
        List<PackageForecastDetailEntity> packageForecastDetailEntityList = CollectionUtils.isNotEmpty(soIdList)?FeignQuery.create(PackageForecastDetailEntity.class).in(PackageForecastDetailEntity::getSoId,soIdList).list():new ArrayList<>();
        List<String> packageForecastIds = packageForecastDetailEntityList.stream().map(PackageForecastDetailEntity::getMainId).distinct().collect(Collectors.toList());
        List<PackageForecastEntity> packageForecastEntityList = CollectionUtils.isNotEmpty(packageForecastIds)?FeignQuery.create(PackageForecastEntity.class).in(PackageForecastEntity::getId,packageForecastIds).list():new ArrayList<>();
        for (TransferDeclareDTO.ListDTO listDTO : dataList){
            List<TransferDeclareDetailEntity> detailEntityList = transferDeclareDetailEntities.stream().filter(e -> e.getMainId().equals(listDTO.getId())).collect(Collectors.toList());

            //如果明细有移除需要根据明细上传状态修改主表上传状态
            List<String> orderUploadStatusList = detailEntityList.stream().map(TransferDeclareDetailEntity::getOrderUploadStatus).distinct().collect(Collectors.toList());

                if (orderUploadStatusList.contains(TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode())) {
                    //如果明细包含失败，主单据改为上传失败
                    listDTO.setUploadOrderStatus(TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode());
                    listDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getName());

                } else if (orderUploadStatusList.contains(TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode())) {
                    //如果明细包含待上传，主单据改为上传失败
                    listDTO.setUploadOrderStatus(TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode());
                    listDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getName());

                } else {
                    //上传成功
                    listDTO.setUploadOrderStatus(TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode());
                    listDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getName());
                }

                //出库状态中文
                SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                        .filter(req -> req.getId().equals(listDTO.getSoId())
                                && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                        .findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                    listDTO.setOutstockStatusName(TransferOutstockStatusEnum.OUTSTOCK.getName());
                } else {
                    listDTO.setOutstockStatusName(TransferOutstockStatusEnum.UN_OUTSTOCK.getName());
                }
                //中转状态中文
                listDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(listDTO.getTransferStatus()));
                //上传状态（批次）中文
                listDTO.setUploadBatchStatusName(TransferDeclareUploadStatusEnum.getName(listDTO.getUploadBatchStatus()));
                //上传状态（订单）中文
                listDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.getName(listDTO.getUploadOrderStatus()));
                //入库预报状态
                listDTO.setInstockForecastStatusName(InstockForecastStatusEnum.getName(listDTO.getInstockForecastStatus()));
                //组包单号
            PackageForecastDetailEntity mainPackageForecastDetailEntity = packageForecastDetailEntityList.stream().filter(v -> v.getSoCode().equals(listDTO.getSoCode())).findFirst().orElse(new PackageForecastDetailEntity());
            PackageForecastEntity mainPackageForecastEntity = packageForecastEntityList.stream().filter(v -> v.getId().equals(mainPackageForecastDetailEntity.getMainId())).findFirst().orElse(new PackageForecastEntity());
            listDTO.setPackageForecastCode(mainPackageForecastEntity.getCode());
            detailEntityList.forEach(transferDeclareDetailEntity -> {
                SoB2cEntity detailSoB2cEntity = soB2cEntityList.stream()
                        .filter(req -> req.getId().equals(transferDeclareDetailEntity.getSoId())
                                && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                        .findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(detailSoB2cEntity)) {
                    transferDeclareDetailEntity.setOutstockStatusName(TransferOutstockStatusEnum.OUTSTOCK.getName());
                } else {
                    transferDeclareDetailEntity.setOutstockStatusName(TransferOutstockStatusEnum.UN_OUTSTOCK.getName());
                }
                transferDeclareDetailEntity.setOrderUploadStatusName(TransferDeclareUploadStatusEnum.getName(transferDeclareDetailEntity.getOrderUploadStatus()));
                transferDeclareDetailEntity.setTransferStatusName(TransferLogisticsStatusEnum.getName(transferDeclareDetailEntity.getTransferStatus()));
                PackageForecastDetailEntity packageForecastDetailEntity = packageForecastDetailEntityList.stream().filter(v -> v.getSoId().equals(transferDeclareDetailEntity.getSoId())).findFirst().orElse(new PackageForecastDetailEntity());
                PackageForecastEntity packageForecastEntity = packageForecastEntityList.stream().filter(v -> v.getId().equals(packageForecastDetailEntity.getMainId())).findFirst().orElse(new PackageForecastEntity());
                transferDeclareDetailEntity.setPackageForecastCode(packageForecastEntity.getCode());
                transferDeclareDetailEntity.setPlatformOrderCode(Objects.nonNull(detailSoB2cEntity) ? detailSoB2cEntity.getPlatformCode() : "");
            });
            listDTO.setDetailEntityList(detailEntityList);
        }
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(TransferDeclareEntity transferDeclareEntity) {
        //发货物流商名称
        LogisticsSupplierEntity logisticsSupplierEntity = logisticsSupplierService.getById(transferDeclareEntity.getDeliveryLogisticsSupplierId());
        if (ObjectUtil.isNotEmpty(logisticsSupplierEntity)) {
            transferDeclareEntity.setDeliveryLogisticsSupplierName(logisticsSupplierEntity.getSupplierName());
        }

        if (StringUtils.isBlank(transferDeclareEntity.getTransferLogisticsSupplierId())) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
        }
        //中转物流商名称
        TransferLogisticsSupplierEntity transferLogisticsSupplierEntity = transferLogisticsSupplierService.getById(transferDeclareEntity.getTransferLogisticsSupplierId());
        if (ObjectUtil.isNotEmpty(transferLogisticsSupplierEntity)) {
            transferDeclareEntity.setTransferLogisticsSupplierName(transferLogisticsSupplierEntity.getSupplierName());
        }
        //中转物流渠道名称
        TransferLogisticsChannelEntity transferLogisticsChannelEntity = transferLogisticsChannelService.getById(transferDeclareEntity.getTransferChannelId());
        if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity)) {
            transferDeclareEntity.setTransferChannelName(transferLogisticsChannelEntity.getName());
        }

    }
}
