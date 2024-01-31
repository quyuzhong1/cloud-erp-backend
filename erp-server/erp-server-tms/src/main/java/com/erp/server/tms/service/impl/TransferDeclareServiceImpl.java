package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDeadlineSettingDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.dto.TransferDeclareGenerationSettingDTO;
import com.erp.model.tms.dto.transfer.TransferLogisticsCreateOrderReq;
import com.erp.model.tms.dto.transfer.TransferLogisticsOrderDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.TransferDeclareTabFlagEnum;
import com.erp.model.tms.enums.TransferDeclareUploadStatusEnum;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import com.erp.model.tms.enums.TransferOutstockStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.tms.convert.TransferDeclareConverter;
import com.erp.server.tms.handler.TransferLogisticsRegistry;
import com.erp.server.tms.mapper.TransferDeclareMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;
    @Autowired
    private DocNoGenHelper docNoGenHelper;
    @Autowired
    private TransferDeclareDetailService transferDeclareDetailService;
    @Autowired
    private MultipleOptionService multipleOptionService;
    @Autowired
    private TransferDeclareGenerationSettingService transferDeclareGenerationSettingService;
    @Autowired
    private TransferDeclareDeadlineSettingService transferDeclareDeadlineSettingService;
    @Autowired
    private LogisticsSupplierService logisticsSupplierService;
    @Autowired
    private TransferLogisticsSupplierService transferLogisticsSupplierService;
    @Autowired
    private TransferLogisticsChannelService transferLogisticsChannelService;
    @Autowired
    private SoB2cFeign soB2cFeign;
    @Autowired
    private TransferLogisticsRegistry transferLogisticsRegistry;
    @Autowired
    private TransferLogisticsAuthService transferLogisticsAuthService;
    @Autowired
    private LogisticsChannelService logisticsChannelService;
    @Autowired
    private ShopInfoFeign shopInfoFeign;
    @Autowired
    private TransferDeclareProductService transferDeclareProductService;

    @Override
    public PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        //列表Tab查询状态处理
        handleTableParam(pagingParamDTO.getParams());

        IPage<TransferDeclareDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
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
                transferDeclareEntity.setPlanTransferDate(LocalDate.now().plusDays(1));
            } else {
                transferDeclareEntity.setPlanTransferDate(LocalDate.now());
            }
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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "中转报关单" , transferDeclareEntity.getCode());
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
        String msg = StrUtil.format("用户【{}】编辑单号为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), transferDeclareEntity.getCode(), "中转报关单");
        operateLogService.addModuleOperateLogByObj(old, transferDeclareEntity, ModuleTypeEnum.TRANSFER_DECLARE.getCode(), transferDeclareEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public TransferDeclareEntity checkExistByChannelIds(List<String> ids) {
        return lambdaQuery().in(TransferDeclareEntity::getTransferChannelId, ids).last("LIMIT 1").one();
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
    public Boolean delete(List<String> ids) {
        if (CollectionUtil.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_98004);
        }

        //上传成功不能删除
        List<TransferDeclareEntity> transferDeclareEntities = this.listByIds(ids);
        long count = transferDeclareEntities.stream().filter(req -> TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(req.getUploadStatus())).count();
        if (count > 0) {
            throw new ServiceException(ApiError.UPLOAD_SUCCESS_NOT_DELETE);
        }

        return this.removeByIds(ids);
    }

    @Override
    public Boolean exportExcel(TransferDeclareDTO.PagingParamDTO dto, HttpServletResponse response) {
        List<TransferDeclareDTO.ListDTO> list = baseMapper.listExportExcel(dto);
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }
        fillList(list);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/transferDeclare.xlsx";
        String name = "中转报关单导出";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            throw new ServiceException(ApiError.ERROR_1015);
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public List<BatchResultDTO> upload(String id) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        List<TransferDeclareDTO.ShippingOrderDTO> shippingOrderDTOList = new ArrayList<>();
        TransferDeclareEntity transferDeclareEntity = this.getById(id);

        if (TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(transferDeclareEntity.getUploadStatus())) {
            throw new ServiceException(ApiError.UPLOAD_SUCCESS_NOT_UPLOAD);
        }

        //查询单据需要上传的订单（待上传，上传失败）状态的订单
        List<TransferDeclareDetailEntity> transferDeclareDetailEntities = transferDeclareDetailService.listByMainIds(Arrays.asList(id));
        List<TransferDeclareDetailEntity> transferDeclareDetailList = transferDeclareDetailEntities.stream()
                .filter(req -> TransferDeclareUploadStatusEnum.WAIT_UPLOAD.getCode().equals(req.getOrderUploadStatus())
                        || TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode().equals(req.getOrderUploadStatus())
        ).collect(Collectors.toList());

        //查询报关单包含的订单信息
        List<String> soIdList = transferDeclareDetailList.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(soIdList);

        //订单物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);

        //订单客户信息
        List<SoB2cReceiverEntity> soB2cReceiverEntities = soB2cFeign.listSoB2cReceiverByMainIdList(soIdList);

        //店铺信息
        List<String> shopIds = soB2cEntities.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoEntities = shopInfoFeign.listShopInfoByIds(shopIds);

        //查询授权信息
        TransferLogisticsAuthEntity authEntity = transferLogisticsAuthService.getByMainId("", transferDeclareEntity.getTransferLogisticsSupplierId());
        if (ObjectUtil.isEmpty(authEntity)) {
            throw new ServiceException(ApiError.ERROR_LOGISTICS_CHANNEL_NOT_AUTU_EXIST);
        }

        //查询物流渠道
        List<String> logisticsChannelIds = transferDeclareDetailList.stream().map(req -> req.getLogisticsChannelId()).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> logisticsChannelEntities = logisticsChannelService.listByIds(logisticsChannelIds);

        //查询中转物流渠道
        TransferLogisticsChannelEntity transferLogisticsChannelEntity = transferLogisticsChannelService.getById(transferDeclareEntity.getTransferChannelId());

        //拆分的订单产品信息
        List<TransferDeclareProductEntity> transferDeclareProductEntities = transferDeclareProductService.listByDeclareIds(Arrays.asList(id));

        //下单
        for (TransferDeclareDetailEntity transferDeclareDetailEntity : transferDeclareDetailList) {
            TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
            if (Objects.isNull(service)){
                resultDTOList.add(BatchResultDTO.fail(transferDeclareDetailEntity.getId(), transferDeclareDetailEntity.getSoCode(), "未开发平台【" + LogisticsPlatformEnum.getByName(authEntity.getLogisticsPlatform()).getName() + "】报关功能"));
                continue;
            }
            //订单信息
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> transferDeclareDetailEntity.getSoId().equals(req.getId())).findFirst().orElse(new SoB2cEntity());

            //订单买家信息
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverEntities.stream().filter(req -> transferDeclareDetailEntity.getSoId().equals(req.getMainId())).findFirst().orElse(new SoB2cReceiverEntity());

            //订单物流信息
            SoB2cLogisticsEntity logisticsEntity = soB2cLogisticsEntities.stream().filter(req -> transferDeclareDetailEntity.getSoId().equals(req.getMainId())).findFirst().orElse(new SoB2cLogisticsEntity());

            //物流渠道信息
            LogisticsChannelEntity logisticsChannelEntity = logisticsChannelEntities.stream().filter(req -> transferDeclareDetailEntity.getLogisticsChannelId().equals(req.getId())).findFirst().orElse(new LogisticsChannelEntity());

            //店铺信息
            ShopInfoEntity shopInfoEntity = shopInfoEntities.stream().filter(req -> transferDeclareDetailEntity.getLogisticsChannelId().equals(req.getId())).findFirst().orElse(new ShopInfoEntity());

            //组装SDK需要的下单详情信息
            List<TransferDeclareProductEntity> declareProductEntityList = transferDeclareProductEntities.stream().filter(req -> transferDeclareDetailEntity.getId().equals(req.getDeclareDetailId())).collect(Collectors.toList());
            List<TransferLogisticsCreateOrderReq.ProductDetail> productDetails = TransferDeclareConverter.INSTANCE.declareProductEntityToCreateOrderReq(declareProductEntityList);

            String shippingCode = "";
            if (ObjectUtil.isNotEmpty(transferLogisticsChannelEntity)) {
                shippingCode = transferLogisticsChannelEntity.getCode();
            }

            //组装SDK需要的下报关单单信息
            TransferLogisticsCreateOrderReq orderReq = TransferLogisticsCreateOrderReq.builder()
                    .trackingNumber(logisticsEntity.getCode())
                    .country(soB2cReceiverEntity.getCountry())
                    .shippingCode(shippingCode)
                    .name(soB2cReceiverEntity.getReceiverName())
                    .referenceNo(soB2cEntity.getPlatformCode())
                    .deliveryAddress(soB2cReceiverEntity.getFullAddress())
                    .streetAddress(soB2cReceiverEntity.getFullAddress())
                    .state(soB2cReceiverEntity.getDistrictName())
                    .city(soB2cReceiverEntity.getCityName())
                    .postcode(soB2cReceiverEntity.getPostCode())
                    .phone(soB2cReceiverEntity.getTelNumber())
                    .orderStatus("2")
                    .iossNo(logisticsChannelEntity.getIsIossPrepay() ? shopInfoEntity.getIossTaxNo() : "")
                    .serialNo(soB2cEntity.getCode())
                    .grossWeight(transferDeclareDetailEntity.getPackageWeight())
                    .buyInsurance(logisticsChannelEntity.getIsApiInsurance() ? 1 : 0)
                    .productDetailList(productDetails)
                    .build();

            //下单
            ApiResult<String> result = service.createOrder(orderReq, authEntity.getId());

            if (result.getCode() == 200) {
                //拿到第三方订单号，用于给订单赋值第三方平台发货单号
                TransferDeclareDTO.ShippingOrderDTO shippingOrderDTO = new TransferDeclareDTO.ShippingOrderDTO();
                shippingOrderDTO.setSoId(soB2cEntity.getId());
                shippingOrderDTO.setShippingOrderNo(result.getData());
                shippingOrderDTOList.add(shippingOrderDTO);

                //上传成功
                transferDeclareDetailService.updateOrderUploadStatus(transferDeclareDetailEntity.getId(), TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode(), result.getData(), "");
                resultDTOList.add(BatchResultDTO.success(transferDeclareDetailEntity.getId(), transferDeclareDetailEntity.getSoCode(), "上传成功"));
                continue;
            } else {
                //上传失败
                transferDeclareDetailService.updateOrderUploadStatus(transferDeclareDetailEntity.getId(), TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode(), "", result.getMsg());
                resultDTOList.add(BatchResultDTO.fail(transferDeclareDetailEntity.getId(), transferDeclareDetailEntity.getSoCode(), "上传失败：" + result.getMsg() + ""));
                continue;
            }
        }

        //给订单赋值第三方平台发货单号
        soB2cFeign.updateShippingOrderNo(shippingOrderDTOList);

        //如果上传数量等于成功数量，修改主单据上传状态为成功
        long count = resultDTOList.stream().filter(req -> req.getSuccess()).count();
        if (transferDeclareDetailList.size() == count) {
            this.updateUploadStatus(id, TransferDeclareUploadStatusEnum.UPLOAD_SUCCESS.getCode());
        } else {
            this.updateUploadStatus(id, TransferDeclareUploadStatusEnum.UPLOAD_FAILURE.getCode());
        }

        return resultDTOList;
    }

    @Override
    public void declareAutoGenerationJob() {
        LocalTime localTime = LocalTime.now();
        //报关设置信息
        List<TransferDeclareGenerationSettingDTO.ViewDTO> forcastSettingView = transferDeclareGenerationSettingService.forcastSettingView();

        //截单设置信息
        List<TransferDeclareDeadlineSettingDTO.ViewDTO> deadlineSettingView = transferDeclareDeadlineSettingService.view();
        for (TransferDeclareDeadlineSettingDTO.ViewDTO deadlineSetting : deadlineSettingView) {
            //生效时间
            LocalTime generateTime = deadlineSetting.getGenerateTime();
            if (localTime.getHour() != generateTime.getHour() && generateTime.getMinute() != localTime.getMinute()) {
                continue;
            }

            //如果当前时间等于生效时间，根据报关设置生成报关单
            TransferDeclareGenerationSettingDTO.ViewDTO viewDTO = forcastSettingView.stream().filter(req -> deadlineSetting.getTransferLogisticsSupplierIdList().contains(req.getTransferLogisticsSupplierId())).findFirst().orElse(null);
            List<TransferDeclareDTO.AddDTO> addDTOList = soB2cFeign.generateTransferDeclareView(viewDTO);
            for (TransferDeclareDTO.AddDTO addDTO : addDTOList) {
                addDTO.setGenerateTime(generateTime);
                BaseResultDTO.AddDTO add = this.add(addDTO);
                if (StringUtils.isNotBlank(add.getId())) {
                    //更新订单中转状态
                    List<String> soIds = addDTO.getDetailList().stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
                    soB2cFeign.updateTransferStatusBatch(soIds, TransferStatusEnum.ALREADY.getCode());
                }

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
        TransferDeclareEntity entity = lambdaQuery().eq(TransferDeclareEntity::getTransferLogisticsSupplierId, supplierId).last("LIMIT 1").one();
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
            TransferLogisticsAuthEntity authEntity = transferLogisticsAuthEntities.stream().filter(req -> detailEntity.getMainId().equals(req.getMainId())).findFirst().orElse(null);
            TransferLogisticsService service = transferLogisticsRegistry.getHandler(authEntity.getLogisticsPlatform());
            ApiResult<TransferLogisticsOrderDTO> result = service.getOrderByCode(detailEntity.getShippingOrderNo(), authEntity.getId());
            if (result.getCode() == 200) {
                transferDeclareDetailService.updateTransferStatus(detailEntity.getId(), result.getData().getOrderStatusEnum().getCode());
            }
        }
    }

    private void fillOne(TransferDeclareDTO.ViewDTO data, List<TransferDeclareDetailEntity> transferDeclareDetailEntities) {
        if (ObjectUtil.isEmpty(data)) {
            return;
        }

        //明细信息
        List<TransferDeclareDetailDTO.ViewDTO> detailList = BeanMapper.copyList(transferDeclareDetailEntities, TransferDeclareDetailDTO.ViewDTO.class);
        for (TransferDeclareDetailDTO.ViewDTO viewDTO : detailList) {
            //出库状态中文
            viewDTO.setOutstockStatusName(TransferOutstockStatusEnum.getName(viewDTO.getOutstockStatus()));
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
        //待上传
        if (TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode().equals(params.getTabFlag())) {
            uploadStatusList.add(TransferDeclareTabFlagEnum.WAIT_UPLOAD.getCode());
        }
        //上传失败
        if (TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode().equals(params.getTabFlag())) {
            uploadStatusList.add(TransferDeclareTabFlagEnum.UPLOAD_FAILURE.getCode());
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

        //上传状态
        if (CollectionUtil.isNotEmpty(uploadStatusList)) {
            params.setUploadStatusList(uploadStatusList);
        }

        //中转状态
        if (CollectionUtil.isNotEmpty(transferStatusList)) {
            params.setTransferStatusList(transferStatusList);
        }
    }

    /**
     * 分页列表字段处理
     * @param dateList
     */
    private void fillList(List<TransferDeclareDTO.ListDTO> dateList) {
        for (TransferDeclareDTO.ListDTO listDTO : dateList) {
            //出库状态中文
            listDTO.setOutstockStatusName(TransferOutstockStatusEnum.getName(listDTO.getOutstockStatus()));
            //中转状态中文
            listDTO.setTransferStatusName(TransferLogisticsStatusEnum.getName(listDTO.getTransferStatus()));
            //上传状态（批次）中文
            listDTO.setUploadBatchStatusName(TransferDeclareUploadStatusEnum.getName(listDTO.getUploadBatchStatus()));
            //上传状态（订单）中文
            listDTO.setUploadOrderStatusName(TransferDeclareUploadStatusEnum.getName(listDTO.getUploadOrderStatus()));
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
