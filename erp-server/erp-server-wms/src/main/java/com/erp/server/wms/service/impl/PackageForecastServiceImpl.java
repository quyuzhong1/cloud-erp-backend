package com.erp.server.wms.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.constant.PackageForecastConstant;
import com.erp.server.wms.convert.PackageForecastConverter;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.*;
import com.erp.tms.aliexpress.api.IopResponse;
import com.erp.tms.aliexpress.model.handover.*;
import com.erp.tms.aliexpress.model.handover.request.CancelRequest;
import com.erp.tms.aliexpress.model.handover.request.CommitRequest;
import com.erp.tms.aliexpress.model.handover.request.HandoverQueryRequest;
import com.erp.tms.aliexpress.model.handover.request.PdfRequest;
import com.erp.tms.aliexpress.model.handover.response.BaseResponse;
import com.erp.tms.aliexpress.model.handover.response.HandoverCommitResult;
import com.erp.tms.aliexpress.model.handover.response.HandoverQueryResponse;
import com.erp.tms.aliexpress.model.handover.response.PdfResponse;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.model.order.response.ErrorResponse;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_PACKAGE_FORECAST;

/**
 * <p>
 * 组包预报表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2024-01-26
 */
@Slf4j
@Service
public class PackageForecastServiceImpl extends SuperServiceImpl<PackageForecastMapper, PackageForecastEntity> implements PackageForecastService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private PackageForecastDetailService packageForecastDetailService;

    @Autowired
    private ForecastFeign forecastFeign;

    @Autowired
    private LogisticsFeign logisticsFeign;

    @Autowired
    private LogisticsAuthFeign logisticsAuthFeign;

    @Autowired
    private TransferDeclareFeign transferDeclareFeign;

    @Autowired
    private SoB2cFeign soB2cFeign;

    @Autowired
    private DmpTaskFeign dmpTaskFeign;

    @Autowired
    private ShopInfoFeign shopInfoFeign;

    @Autowired
    private AliExpressHandoverService aliExpressHandoverService;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource(name ="packAsyncExecutor")
    private ThreadPoolTaskExecutor packAsyncExecutor;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(PackageForecastDTO.AddDTO addDTO) {
        PackageForecastEntity packageForecastEntity = new PackageForecastEntity();
        BeanMapperUtils.copy(addDTO, packageForecastEntity);
        // 数据处理
        handleData(packageForecastEntity);

        String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_ZB);
        packageForecastEntity.setCode(code);
        boolean save = super.save(packageForecastEntity);
        if (!save) {
            throw new ServiceException("组包预报单保存失败");
        }
        String id = packageForecastEntity.getId();
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "组包预报单", packageForecastEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.PACKAGE_FORECAST.getCode(), id, "新增操作");
        packageForecastDetailService.add(id, addDTO.getDetailList());

        return new BaseResultDTO.AddDTO(packageForecastEntity.getId(), code);
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(PackageForecastDTO.UpdateDTO updateDTO) {
        PackageForecastEntity entity = super.getById(updateDTO.getId());
        Optional.ofNullable(entity).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单"));
        entity.setBillDate(updateDTO.getBillDate());
        packageForecastDetailService.update(entity, updateDTO.getDetailIdList());
        boolean save = super.updateById(entity);
        if (!save) {
            throw new ServiceException("组包预报单保存失败");
        }
        return save;
    }

    /**
     * tab 列表
     *
     * @param
     * @return
     */
    @Override
    public List<PackageForecastDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<PackageForecastDTO.TabListDTO> resultList = new ArrayList<>(5);
        List<PackageForecastDTO.TabListDTO> tabListList = baseMapper.tabList();
        PackageForecastDTO.TabListDTO all = new PackageForecastDTO.TabListDTO();
        all.setTabFlag("all");
        all.setTabName("全部");
        Integer allCount = tabListList.stream().mapToInt(PackageForecastDTO.TabListDTO::getCount).sum();
        all.setCount(allCount);
        resultList.add(all);
        PackageUploadStatusEnum cancel = PackageUploadStatusEnum.CANCEL;
        for (PackageUploadStatusEnum item : PackageUploadStatusEnum.values()) {
            if (!cancel.equals(item)) {
                PackageForecastDTO.TabListDTO tabDTO = new PackageForecastDTO.TabListDTO();
                String tabCode = item.getCode();
                tabDTO.setTabFlag(item.getCode());
                tabDTO.setTabName(item.getName());
                Integer count = tabListList.stream().filter(t -> tabCode.equals(t.getTabFlag())).
                        map(PackageForecastDTO.TabListDTO::getCount).findFirst().orElse(0);
                tabDTO.setCount(count);
                resultList.add(tabDTO);
            }
        }
        return resultList;
    }

    @Override
    public PagingVO<PackageForecastDTO.PagingViewDTO> paging(PagingDTO<PackageForecastDTO.PagingParamDTO> dto) {
        PackageForecastDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params);
        List<PackageForecastDTO.PagingViewDTO> list = pageData.getRecords();
        if(CollectionUtils.isEmpty(list)){
            return new PagingVO<>();
        }
        //处理分页数据
        fillPaging(list);
        //设置分页信息
        PagingVO pagingVO = new PagingVO<>(pageData);
        return pagingVO;

    }

    private void fillPaging(List<PackageForecastDTO.PagingViewDTO> list) {
        List<String> ids = list.stream().map(PackageForecastDTO.PagingViewDTO::getId).distinct().collect(Collectors.toList());
        List<PackageForecastDetailEntity> allDetailEntityList = packageForecastDetailService.listDbByMainIds(ids);
        List<String> soIds = allDetailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoOutstockEntity> soOutstockList = soOutstockService.listBySoIds(soIds);
        List<String> soCodes = allDetailEntityList.stream().map(PackageForecastDetailEntity::getSoCode).distinct().collect(Collectors.toList());
        List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = transferDeclareFeign.listBySoCodeList(soCodes);
        List<String> transferIds = transferDeclareDetailEntityList.stream().map(TransferDeclareDetailEntity::getMainId).collect(Collectors.toList());
        List<TransferDeclareEntity> transferDeclareEntityList = CollectionUtils.isNotEmpty(transferIds)?FeignQuery.create(TransferDeclareEntity.class).in(TransferDeclareEntity::getId,transferIds).list():new ArrayList<>();

        for (PackageForecastDTO.PagingViewDTO pagingViewDTO : list) {
            List<PackageForecastDetailEntity> detailEntityList = allDetailEntityList.stream().filter(v->v.getMainId().equals(pagingViewDTO.getId())).collect(Collectors.toList());
            List<PackageForecastDTO.PagingDetailViewDTO> detailViewDTOList = new ArrayList<>();
            for (PackageForecastDetailEntity packageForecastDetailEntity : detailEntityList) {
                PackageForecastDTO.PagingDetailViewDTO pagingDetailViewDTO = new PackageForecastDTO.PagingDetailViewDTO();
                pagingDetailViewDTO.setDetailId(packageForecastDetailEntity.getId());
                pagingDetailViewDTO.setSoId(packageForecastDetailEntity.getSoId());
                pagingDetailViewDTO.setSoCode(packageForecastDetailEntity.getSoCode());
                pagingDetailViewDTO.setLogisticsChannelId(packageForecastDetailEntity.getLogisticsChannelId());
                pagingDetailViewDTO.setLogisticsChannelName(packageForecastDetailEntity.getLogisticsChannelName());
                pagingDetailViewDTO.setTrackNo(packageForecastDetailEntity.getTrackNo());
                pagingDetailViewDTO.setMinPackageTransportNo(packageForecastDetailEntity.getTransportNo());
                pagingDetailViewDTO.setWeight(packageForecastDetailEntity.getWeight());
                pagingDetailViewDTO.setWeightUnit(packageForecastDetailEntity.getWeightUnit());
                pagingDetailViewDTO.setMinPackageHandoverStatus(packageForecastDetailEntity.getHandoverStatus());
                ApproveStatusEnum approveStatus = soOutstockList.stream().filter(s -> s.getSoId().equals(packageForecastDetailEntity.getSoId())).
                        map(SoOutstockEntity::getApproveStatus).findFirst().orElse(ApproveStatusEnum.WAIT_SUBMIT);
                pagingDetailViewDTO.setOutstockStatusName(ApproveStatusEnum.APPROVE.equals(approveStatus)?"已出库":"未出库");
                detailViewDTOList.add(pagingDetailViewDTO);
            }
            pagingViewDTO.setDetailViewDTOList(detailViewDTOList);
        }
        for (PackageForecastDTO.PagingViewDTO item : list) {
            String soCode = CollectionUtils.isNotEmpty(item.getDetailViewDTOList())?item.getDetailViewDTOList().get(0).getSoCode():"";
            TransferDeclareDetailEntity transferDeclareDetailEntity = transferDeclareDetailEntityList.stream().filter(v->v.getSoCode().equals(soCode)).findFirst().orElse(new TransferDeclareDetailEntity());
            TransferDeclareEntity transferDeclareEntity = transferDeclareEntityList.stream().filter(v->v.getId().equals(transferDeclareDetailEntity.getMainId())).findFirst().orElse(new TransferDeclareEntity());
            item.setTransferDeclareCode(transferDeclareEntity.getCode());
            String uploadStatus = item.getUploadStatus();
            String uploadStatusName = PackageUploadStatusEnum.getName(uploadStatus);
            item.setUploadStatusName(uploadStatusName);
            String printStatus = item.getPrintStatus();
            String printStatusName = PackagePrintStatusEnum.getName(printStatus);
            item.setPrintStatusName(printStatusName);
            for(PackageForecastDTO.PagingDetailViewDTO detail : item.getDetailViewDTOList()){
                //跟踪单号
                String trackNo = detail.getTrackNo();
                String minPackageTransportNo = detail.getMinPackageTransportNo();
                if (StringUtils.isBlank(trackNo)) {
                    trackNo = minPackageTransportNo;
                }
                String subHandoverStatus = detail.getMinPackageHandoverStatus();
                String subHandoverStatusName= HandoverSubStatusEnum.getByCode(subHandoverStatus);
                detail.setMinPackageHandoverStatusName(subHandoverStatusName);
                detail.setTrackNo(trackNo);
                BigDecimal weight = detail.getWeight();
                String weightUnit = detail.getWeightUnit();
                String weightStr = weight + weightUnit;
                detail.setWeightStr(weightStr);
            }
            String handoverStatus = item.getHandoverStatus();
            String handoverStatusName= HandoverStatusEnum.getByCode(handoverStatus);
            item.setHandoverStatusName(handoverStatusName);

            //第三方交接单号
            String handoverNo = item.getHandoverNo();
            //第三方组包号
            String platformPackageNo = item.getPlatformPackageNo();
            String platformNo = handoverNo + "/" + platformPackageNo;
            item.setPlatformNo(platformNo);
            BigDecimal totalPackageWeight = item.getTotalPackageWeight();
            String totalPackageWeightUnit = item.getTotalPackageWeightUnit();
            String totalPackageWeightStr = totalPackageWeight + totalPackageWeightUnit;
            item.setTotalPackageWeightStr(totalPackageWeightStr);
        }
    }


    @Override
    public PackageForecastDTO.ViewDTO view(String id) {
        PackageForecastDTO.ViewDTO viewDTO = new PackageForecastDTO.ViewDTO();
        PackageForecastEntity packageForecast = this.getById(id);
        if (Objects.isNull(packageForecast)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        BeanMapperUtils.copy(packageForecast, viewDTO);
        String uploadStatus = packageForecast.getUploadStatus();
        viewDTO.setUploadStatusName(PackageUploadStatusEnum.getName(uploadStatus));
        String printStatus = packageForecast.getPrintStatus();
        viewDTO.setPrintStatusName(PackagePrintStatusEnum.getName(printStatus));
        String collectMode = packageForecast.getCollectMode();
        String collectModeName= PackageForecastCollectModeEnum.getName(collectMode);
        viewDTO.setCollectModeName(collectModeName);
        String handoverStatus = packageForecast.getHandoverStatus();
        String handoverStatusName= HandoverStatusEnum.getByCode(handoverStatus);
        viewDTO.setHandoverStatusName(handoverStatusName);
        //交接单号
        String handoverNo = packageForecast.getHandoverNo();
        String platformPackageNo = packageForecast.getPlatformPackageNo();
        String platformNo="";
        if (StringUtils.isNotBlank(handoverNo) || StringUtils.isNotBlank(platformPackageNo)) {
            platformNo=handoverNo+"/"+platformPackageNo;
        }
        viewDTO.setPlatformNo(platformNo);
        //获取详情
        List<PackageForecastDetailDTO.ViewDTO> detailList = packageForecastDetailService.listDetailViewByMainId(id);
        viewDTO.setDetailList(detailList);
        return viewDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        String successCode = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        if (successCode.equals(entity.getUploadStatus())) {
            throw new ServiceException("已上传成功,无法删除");
        }
        this.removeById(id);
        packageForecastDetailService.removeByMainId(id,entity.getLogisticsSupplierId());
        return BatchResultDTO.success(entity.getId(), entity.getCode(), OperationTypeEnum.DELETE);

    }


    /**
     * 取消上传
     *
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO cancel(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        String successCode = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        if (!successCode.equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功可操作");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        String logisticsPlatform = authDTO.getLogisticsPlatform();
        try {
            //如果这里是速卖通的话就 对接平台
            if (logisticsPlatform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())) {
                aliExpressCancel(logisticsPlatform, entity);
            }
            entity.setUploadStatus(PackageUploadStatusEnum.CANCEL.getCode());
            this.updateById(entity);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传");
        } catch (Exception e) {
            entity.setRemark("取消失败原因:" + e.getMessage());
            this.updateById(entity);
            log.error("取消上传失败>>>>{}", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "取消上传");
        }

    }


    /**
     * 速卖通取消上传
     *
     * @param logisticsPlatform
     * @param entity
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    public void aliExpressCancel(String logisticsPlatform, PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)){
            throw new ServiceException("销售订单店铺未找到");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = this.getAlExpressHandoverBase(logisticsPlatform, shopIds.get(0));
//        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform);
        CancelRequest cancelRequest = CancelRequest.builder().
                userInfo(base.getUserInfo()).client(base.getClient()).
                handoverContentId(Long.valueOf(entity.getPlatformPackageNo())).
                build();
        try {
            IopResponse iopResponse = aliExpressHandoverService.cancel(base.getAuthMap(), cancelRequest);
            BaseResult baseResult = JSONObject.parseObject(iopResponse.getBody(), BaseResult.class);
            ErrorResponse errorResponse = baseResult.getErrorResponse();
            //表示失败了
            if (Objects.nonNull(errorResponse)) {
                throw new ServiceException(errorResponse.getSubMsg());
            }
        } catch (ApiException e) {
            log.error("取消上传交接单失败>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }


    }

    /**
     * 获取速卖通交接单上传基础信息
     *
     * @return
     */
    @Override
    public PackageForecastDTO.AlExpressHandoverBaseDTO getAlExpressHandoverBase(String logisticsPlatform, String shopId) {

        PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBaseDTO = new PackageForecastDTO.AlExpressHandoverBaseDTO();
        CfgAppClientDTO.FindDTO findDTO = new CfgAppClientDTO.FindDTO();
        AppClientEnum appClientEnum = AppClientEnum.ALI_EXPRESS_TOKEN;
        findDTO.setBusinessType(appClientEnum.getBusinessType());
        findDTO.setDictPlatform(logisticsPlatform);
        findDTO.setPlatformType(appClientEnum.getPlatformType());
        CfgAppClientEntity cfgAppClient = dmpTaskFeign.getCfgAppClient(findDTO);
        ApiResult<List<ShopAuthEntity>> shopAuthResult = shopInfoFeign.getAuthShopByPlatformType(logisticsPlatform);
        if (!shopAuthResult.isSuccess()) {
            throw new ServiceException("获取店铺token失败");
        }
        String sellerIdFlag = PackageForecastConstant.SELLER_ID;
        List<ShopAuthEntity> shopAuthList = shopAuthResult.getData().stream().filter(s -> s.getExtendData().contains(sellerIdFlag) && shopId.equals(s.getShopId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopAuthList)) {
            throw new ServiceException("获取店铺速卖通卖家id失败");
        }
        if (Objects.isNull(cfgAppClient)) {
            throw new ServiceException("未找到对应平台");
        }
        ShopAuthEntity shopAuthEntity = shopAuthList.get(0);
        Map<String, String> authMap = new HashMap<>(4);
        authMap.put("clientId", cfgAppClient.getClientId());
        authMap.put("clientSecret", cfgAppClient.getClientSecret());
        authMap.put("token", shopAuthEntity.getToken());
        JSONObject jsonObject = JSONObject.parseObject(shopAuthEntity.getExtendData());
        //买家id
        String sellerId = jsonObject.getString("sellerId");
        String client = PackageForecastConstant.CLIENT;
        UserInfo userInfo = UserInfo.builder().topUserKey(sellerId).build();
        alExpressHandoverBaseDTO.setClient(client);
        alExpressHandoverBaseDTO.setAuthMap(authMap);
        alExpressHandoverBaseDTO.setUserInfo(userInfo);
        alExpressHandoverBaseDTO.setLocale("zh_CN");
        return alExpressHandoverBaseDTO;
    }

    @Override
    public List<PackageForecastEntity> getAliExpressHandoverList(DateTime dateTime) {
        return baseMapper.getAliExpressHandoverList(dateTime);
    }

    @Override
    public void queryAliExpressInfo(PackageForecastEntity packageForecastEntity) {

        PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBase = null;
        try {
            List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(packageForecastEntity.getId());
            List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
            List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(shopIds)){
                throw new ServiceException("销售订单店铺未找到");
            }
            alExpressHandoverBase = this.getAlExpressHandoverBase(LogisticsPlatformEnum.ALI_EXPRESS.getCode(), shopIds.get(0));
        }catch (Exception e){
            log.error("syncPackageForecastInfo error : {}", e.getMessage());
        }
        if (Objects.isNull(alExpressHandoverBase)){
            return;
        }
        HandoverQueryRequest handoverQueryRequest = HandoverQueryRequest.builder()
                .client(alExpressHandoverBase.getClient())
                .locale("zh_CN")
                .orderCode(packageForecastEntity.getHandoverNo())
                .userInfo(alExpressHandoverBase.getUserInfo())
                .build();
        try {
            IopResponse response = aliExpressHandoverService.queryContent(alExpressHandoverBase.getAuthMap(), handoverQueryRequest);
            if (StringUtils.isEmpty(response.getBody())) {
                return;
            }
            BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
            if (StringUtils.isEmpty(baseResponse.getResult())) {
                return;
            }
            BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
            if (StringUtils.isEmpty(baseResult.getData())) {
                return;
            }
            HandoverQueryResponse queryResponse = JSONObject.parseObject(baseResult.getData(), HandoverQueryResponse.class);
            packageForecastEntity.setHandoverStatus(queryResponse.getStatus());
            packageForecastEntity.setTransportNo(queryResponse.getTrackingNumber());
            baseMapper.updateById(packageForecastEntity);
            //更新明细表
            List<ParcelOrder> parcelOrderList = queryResponse.getParcelOrderList();
            if (CollectionUtils.isEmpty(parcelOrderList)) {
                return;
            }
            parcelOrderList.forEach(parcelOrder -> {
                packageForecastDetailService.updateStatusByOrderCode(parcelOrder.getOrderCode(), parcelOrder.getStatus());
            });
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO upload(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        //待上传
        String wait = PackageUploadStatusEnum.WAIT.getCode();
        //上传失败
        String failure = PackageUploadStatusEnum.UPLOAD_FAILURE.getCode();
        List<String> uploadStatusList = Arrays.asList(wait, failure);
        //上传状态
        String uploadStatus = entity.getUploadStatus();
        if (!uploadStatusList.contains(uploadStatus)) {
            throw new ServiceException("仅待上传/上传失败可操作");
        }
        //物流地址
        LogisticsAddressEntity addressEntity = logisticsFeign.getLogisticsAddressById(collectAddressId);
        if (Objects.isNull(addressEntity)) {
            throw new ServiceException("揽收地址不存在");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        if (Objects.isNull(authDTO)) {
            throw new ServiceException("物流商不存在");
        }
        try {
            String addressName = addressEntity.getName();
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            entity.setCollectMode(collectMode);
            entity.setCollectAddressId(collectAddressId);
            entity.setCollectAddress(addressName);
            String logisticsPlatform = authDTO.getLogisticsPlatform();
            //如果这里是速卖通的话就 对接平台
            if (logisticsPlatform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())) {
                addBigPackage(logisticsPlatform, entity, addressEntity);
                //针对待揽收状态  异步拉取速卖通的数据
                CompletableFuture.runAsync(() -> {
                    this.queryAliExpressInfo(entity);
                }, packAsyncExecutor);
                this.updateById(entity);
                return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
            }else{
                entity.setUploadStatus(failure);
                entity.setRemark("上传失败:" + PlatformDictEnum.getByCode(logisticsPlatform).getName()+"平台尚未对接上传");
                this.updateById(entity);
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败" + PlatformDictEnum.getByCode(logisticsPlatform).getName()+"平台尚未对接上传");
            }
        } catch (Exception e) {
            entity.setUploadStatus(failure);
            entity.setRemark("上传失败:" + e.getMessage());
            this.updateById(entity);
            log.error("组包预报上传失败>>>>>{}", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败" + e.getMessage());
        }


    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        //上传成功
        String uploadSuccess = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        //上传状态
        String uploadStatus = entity.getUploadStatus();
        if (!uploadSuccess.equals(uploadStatus)) {
            throw new ServiceException("仅上传成功后可操作");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        if (Objects.isNull(authDTO)) {
            throw new ServiceException("物流商不存在");
        }
        String logisticsPlatform = authDTO.getLogisticsPlatform();
        String base64 = "";
        try {
            //如果这里是速卖通的话就 对接平台
            if (logisticsPlatform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())) {
                base64 = aliExpressPrint(logisticsPlatform, entity);
            }
        } catch (Exception e) {
            log.error("打印失败>>>>>>>{}", e);
        }
        if (StringUtils.isNotBlank(base64)) {
            entity.setPrintStatus(PackagePrintStatusEnum.CANCEL.getCode());
            this.updateById(entity);
        } else {
            throw new ServiceException("打印失败");
        }
        return base64;
    }


    /**
     * 速卖通打印
     *
     * @param logisticsPlatform
     * @param entity
     */
    public String aliExpressPrint(String logisticsPlatform, PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)){
            throw new ServiceException("销售订单店铺未找到");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform, shopIds.get(0));
        PdfRequest pdfRequest = PdfRequest.builder()
                .client(base.getClient())
                .handoverContentId(Long.valueOf(entity.getPlatformPackageNo()))
                .locale("zh_CN")
                .type(1)
                .userInfo(base.getUserInfo())
                .build();
        try {
            IopResponse response = aliExpressHandoverService.getPdf(base.getAuthMap(), pdfRequest);
            BaseResponse baseResponse = JSONObject.parseObject(response.getBody(), BaseResponse.class);
            BaseResult baseResult = JSONObject.parseObject(baseResponse.getResult(), BaseResult.class);
            if (StringUtils.isNotEmpty(baseResult.getErrorMsg()) || StringUtils.isEmpty(baseResult.getData())) {
                throw new ServiceException(baseResult.getErrorMsg());
            }
            PdfResponse pdfResponse = JSONObject.parseObject(baseResult.getData(), PdfResponse.class);
            String prefix = "data:application/pdf;base64,";
            String base64Str = prefix + pdfResponse.getBody();
            return base64Str;
        } catch (ApiException e) {
            log.error("速卖通打印失败>>>{}", e);
            throw new ServiceException(e.getMessage());
        }

    }

    /**
     * 速卖通组包
     *
     * @param logisticsPlatform
     * @param entity
     * @param logisticsAddress
     */
    public void addBigPackage(String logisticsPlatform, PackageForecastEntity entity, LogisticsAddressEntity logisticsAddress) {
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)){
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1){
            throw new ServiceException("速卖通不支持多店铺组包预报");
        }
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform, shopIds.get(0));

        List<String> soIdList = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        Map<String, String> authMap = base.getAuthMap();
        //根据销售订单获取销售物流信息
        List<SoB2cLogisticsEntity> soB2cLogisticsList = soB2cFeign.listSoB2cLogisticsByMainIdList(soIdList);
        List<String> orderCodeList = soB2cLogisticsList.stream().map(SoB2cLogisticsEntity::getCode).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        if (orderCodeList.size() != forecastDetailList.size()) {
            throw new ServiceException("未获取到小包第三方交易号");
        }
        List<SellerParcelOrder> sellerParcelOrderList = new ArrayList<>(1);
        String topUserKey = base.getUserInfo().getTopUserKey();
        packageForecastDetailService.updateBatchById(forecastDetailList);
        SellerParcelOrder parcelOrder = new SellerParcelOrder();
        parcelOrder.setSellerId(topUserKey);
        parcelOrder.setOrderCodeList(orderCodeList);
        sellerParcelOrderList.add(parcelOrder);

        //揽收地址基础信息
        AddressBase addressBase = PackageForecastConverter.INSTANCE.convertAddressBase(logisticsAddress);
        //揽收地址信息
        AddressInfo addressInfo = PackageForecastConverter.INSTANCE.convertAddressInfo(logisticsAddress);
        addressInfo.setAddress(addressBase);
        /**
         * 要创建交接单的小包编码集合
         */
        String type = PackageForecastConstant.CAINIAO_PICKUP;
        String collectMode = entity.getCollectMode();
        String selfSend = PackageForecastCollectModeEnum.SELF_SEND.getCode();
        if (selfSend.equals(collectMode)) {
            type = PackageForecastConstant.SELF_SEND;
        }
        String client = PackageForecastConstant.CLIENT;
        CommitRequest commitRequest = CommitRequest.builder().pickInfo(addressInfo).
                skipInvalidParcel(Boolean.FALSE).
                orderCodeList(orderCodeList).
                handoverOrderId("").
                appointmentType("bigbag").
                weight(entity.getTotalPackageWeight().setScale(0, RoundingMode.HALF_UP)).
                weightUnit(entity.getWeightUnit()).userInfo(base.getUserInfo()).
                sellerParcelOrderList(sellerParcelOrderList).
                type(type).client(client).locale(base.getLocale()).build();
        try {
            IopResponse iopResponse = aliExpressHandoverService.commit(authMap, commitRequest);
            BaseResult baseResult = JSONObject.parseObject(iopResponse.getBody(), BaseResult.class);
            if (Objects.nonNull(baseResult.getErrorResponse())) {
                throw new ServiceException(baseResult.getErrorResponse().getSubMsg());
            }
            HandoverCommitResult handoverCommitResult = JSONObject.parseObject(baseResult.getResult(), HandoverCommitResult.class);
            if (Objects.nonNull(handoverCommitResult) && handoverCommitResult.getSuccess()) {
                entity.setHandoverNo(handoverCommitResult.getResponse().getHandoverContentCode());
                entity.setPlatformPackageNo(String.valueOf(handoverCommitResult.getResponse().getHandoverContentId()));
                entity.setRemark("");
            }
        } catch (ApiException e) {
            log.error("创建交接单失败>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }


    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public BatchResultDTO forecast(String id, String transferLogisticsSupplierId, String transferLogisticsChannelId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        List<String> uploadStatusList = new ArrayList<>(2);
        uploadStatusList.add(PackageUploadStatusEnum.NOT.getCode());
        uploadStatusList.add(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
        String uploadStatus = entity.getUploadStatus();
        if (!uploadStatusList.contains(uploadStatus)) {
            throw new ServiceException("只有无需上传和上传成功的组包 才能中转报关");
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainId(id);
        List<String> soIdList = detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        //校验订单中转状态 （先去掉校验，因为历史数据问题）
//        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIdList);
//        List<String> alreadyTransferList = soB2cEntityList.stream().filter(v->TransferStatusEnum.ALREADY.getCode().equals(v.getTransferStatus())).map(SoB2cEntity::getCode).collect(Collectors.toList());
//        if(CollectionUtils.isNotEmpty(alreadyTransferList)){
//            return BatchResultDTO.fail(entity.getId(), entity.getCode(), StrUtil.format("{}已中转不可重复中转",alreadyTransferList));
//        }

        TransferDeclareDTO.AddDTO addDTO = new TransferDeclareDTO.AddDTO();
        addDTO.setTransferLogisticsSupplierId(transferLogisticsSupplierId);
        addDTO.setTransferChannelId(transferLogisticsChannelId);
        addDTO.setDeliveryLogisticsSupplierId(entity.getLogisticsSupplierId());
        addDTO.setGenerateTime(LocalTime.now());
        List<TransferDeclareDetailDTO.AddDTO> addDetailList = PackageForecastConverter.INSTANCE.convertDeclareDetail(detailList);
        addDTO.setDetailList(addDetailList);
        BaseResultDTO.AddDTO result = transferDeclareFeign.add(addDTO);
        this.updateById(entity);
        return BatchResultDTO.success(entity.getId(), entity.getCode(), "中转报关");

    }

    @Override
    public List<BatchResultDTO> instockForcast(List<String> ids) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<PackageForecastEntity> allEntityList = this.listByIds(ids);
        List<PackageForecastDetailEntity> allDetailList = packageForecastDetailService.listDbByMainIds(ids);
        //校验数据 并将相同物流商，相同中转报关商，相同中转渠道合并
        List<String> allSoIdList = allDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cLogisticsEntity> allSoB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(allSoIdList);
        List<PackageForecastDTO.InstockForcastMergeDTO> instockForcastMergeDTOList = new ArrayList<>();
        List<String> allSoCodeList = allDetailList.stream().map(PackageForecastDetailEntity::getSoCode).collect(Collectors.toList());
        List<TransferDeclareDetailEntity> allTransferDeclareDetailEntityList = transferDeclareFeign.listBySoCodeList(allSoCodeList);
        List<SoB2cEntity> allSoB2cEntityList = soB2cFeign.listByIds(allSoIdList);
        for (PackageForecastEntity packageForecastEntity : allEntityList) {
            String uploadStatus = packageForecastEntity.getUploadStatus();
            List<String> uploadStatusList = new ArrayList<>(2);
            uploadStatusList.add(PackageUploadStatusEnum.NOT.getCode());
            uploadStatusList.add(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            if (!uploadStatusList.contains(uploadStatus)) {
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), "只有无需上传和上传成功的组包 才能入库预报"));
                continue;
            }
            if (StringUtils.isBlank(packageForecastEntity.getLogisticsSupplierId())) {
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), "物流商为空"));
                continue;
            }
            List<PackageForecastDetailEntity> detailList = allDetailList.stream().filter(v->v.getMainId().equals(packageForecastEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(detailList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), "明细数据为空"));
                continue;
            }
            List<String> soIds = detailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
            List<String> errorSoList = new ArrayList<>();
            List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = allSoB2cLogisticsEntities.stream().filter(v->soIds.contains(v.getMainId())).collect(Collectors.toList());
            //校验中转物流商和中转渠道
            for (PackageForecastDetailEntity detail : detailList) {
                SoB2cLogisticsEntity soB2cLogisticsEntities = allSoB2cLogisticsEntities.stream().filter(v->v.getMainId().equals(detail.getSoId())).findFirst().orElse(new SoB2cLogisticsEntity());
                if(StringUtils.isBlank(soB2cLogisticsEntities.getTransferLogisticsChannelId()) || StringUtils.isBlank(soB2cLogisticsEntities.getTransferLogisticsChannelId())){
                    errorSoList.add(detail.getSoCode());
                }
            }
            if(CollectionUtils.isNotEmpty(errorSoList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), StrUtil.format("中转物流商和中转渠道为空,销售订单:{}", errorSoList)));
                continue;
            }
            //是否有销售订单已生成中转报关详情
            List<String> soCodes = detailList.stream().map(PackageForecastDetailEntity::getSoCode).collect(Collectors.toList());
            List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = allTransferDeclareDetailEntityList.stream().filter(v->soCodes.contains(v.getSoCode())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(transferDeclareDetailEntityList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), StrUtil.format("销售订单已生成中转报关详情,销售订单:{}", transferDeclareDetailEntityList.stream().map(TransferDeclareDetailEntity::getSoCode).collect(Collectors.toList()))));
                continue;
            }
            //校验销售订单是否已成功预报
            List<String> passTransferStatus = Arrays.asList(TransferStatusEnum.NOT.getCode(),TransferStatusEnum.SUCCESS.getCode());
            List<SoB2cEntity> notPassSoB2cList = allSoB2cEntityList.stream().filter(v->soIds.contains(v.getId()) && !passTransferStatus.contains(v.getTransferStatus())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notPassSoB2cList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), StrUtil.format("销售订单未成功预报,销售订单:{}", notPassSoB2cList.stream().map(SoB2cEntity::getCode).collect(Collectors.toList()))));
                continue;
            }

            PackageForecastDTO.InstockForcastMergeDTO currentMergeDTO = new PackageForecastDTO.InstockForcastMergeDTO(packageForecastEntity.getLogisticsSupplierId(),soB2cLogisticsEntityList.get(0).getTransferLogisticsSupplierId(),soB2cLogisticsEntityList.get(0).getTransferLogisticsChannelId(),detailList);
            PackageForecastDTO.InstockForcastMergeDTO instockForcastMergeDTO = instockForcastMergeDTOList.stream().filter(v->v.equals(currentMergeDTO)).findFirst().orElse(null);
            if(Objects.isNull(instockForcastMergeDTO)){
                instockForcastMergeDTOList.add(currentMergeDTO);
            }else{
                instockForcastMergeDTO.getDetailEntityList().addAll(currentMergeDTO.getDetailEntityList());
            }
        }

        for (PackageForecastDTO.InstockForcastMergeDTO instockForcastMergeDTO : instockForcastMergeDTOList) {
            TransferDeclareDTO.AddDTO addDTO = new TransferDeclareDTO.AddDTO();
            addDTO.setTransferLogisticsSupplierId(instockForcastMergeDTO.getTransferLogisticsSupplierId());
            addDTO.setTransferChannelId(instockForcastMergeDTO.getTransferLogisticsChannelId());
            addDTO.setDeliveryLogisticsSupplierId(instockForcastMergeDTO.getLogisticsSupplierId());
            addDTO.setGenerateTime(LocalTime.now());
            List<TransferDeclareDetailDTO.AddDTO> addDetailList = PackageForecastConverter.INSTANCE.convertDeclareDetail(instockForcastMergeDTO.getDetailEntityList());
            addDTO.setDetailList(addDetailList);
            addDTO.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            addDTO.getDetailList().forEach(v->v.setOrderUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode()));
            transferDeclareFeign.add(addDTO);
        }
        return batchResultDTOList;
    }

    @Override
    public Boolean exportExcel(PackageForecastDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("组包预报列表", EXPORT_WMS_PACKAGE_FORECAST.getCode(), dto);
        return Boolean.TRUE;

    }


    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillExportPaging(List<PackageForecastDTO.ExportViewDTO> list) {
        for (PackageForecastDTO.ExportViewDTO item : list) {
            String uploadStatus = item.getUploadStatus();
            String uploadStatusName = PackageUploadStatusEnum.getName(uploadStatus);
            item.setUploadStatusName(uploadStatusName);
            String printStatus = item.getPrintStatus();
            String printStatusName = PackagePrintStatusEnum.getName(printStatus);
            item.setPrintStatusName(printStatusName);
            //跟踪单号
            String trackNo = item.getTrackNo();
            String minPackageTransportNo = item.getMinPackageTransportNo();
            if (StringUtils.isBlank(trackNo)) {
                trackNo = minPackageTransportNo;
            }
            String handoverStatus = item.getHandoverStatus();
            String handoverStatusName= HandoverStatusEnum.getByCode(handoverStatus);
            item.setHandoverStatusName(handoverStatusName);

            String subHandoverStatus = item.getMinPackageHandoverStatus();
            String subHandoverStatusName= HandoverSubStatusEnum.getByCode(subHandoverStatus);
            item.setMinPackageHandoverStatusName(subHandoverStatusName);

            item.setTrackNo(trackNo);
            //第三方交接单号
            String handoverNo = item.getHandoverNo();
            //第三方组包号
            String platformPackageNo = item.getPlatformPackageNo();
            String platformNo = handoverNo + "/" + platformPackageNo;
            item.setPlatformNo(platformNo);
            BigDecimal totalPackageWeight = item.getTotalPackageWeight();
            String totalPackageWeightUnit = item.getTotalPackageWeightUnit();
            String totalPackageWeightStr = totalPackageWeight + totalPackageWeightUnit;
            item.setTotalPackageWeightStr(totalPackageWeightStr);
            BigDecimal weight = item.getWeight();
            String weightUnit = item.getWeightUnit();
            if (Objects.nonNull(weight) && StrUtil.isNotBlank(weightUnit)){
                String weightStr = weight + weightUnit;
                item.setWeightStr(weightStr);
            }else {
                item.setWeightStr("");
            }

        }
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(PackageForecastEntity entity) {
        String logisticsSupplierId = entity.getLogisticsSupplierId();
        SettingForecastDTO.FindByLogisticsSupplierDTO dto = new SettingForecastDTO.FindByLogisticsSupplierDTO();
        dto.setOrderTime(LocalDateTime.now());
        dto.setLogisticsSupplierId(logisticsSupplierId);
        SettingForecastDTO.ForecastStatusDTO forecastStatus = forecastFeign.getByLogisticsSupplier(dto);
        String uploadStatus = PackageUploadStatusEnum.NOT.getCode();
        if (Objects.nonNull(forecastStatus)) {
            String packageStatus = forecastStatus.getPackageStatus();
            //表示要组包啊
            if (!PackageStatusEnum.NOT.getCode().equals(packageStatus)) {
                uploadStatus = PackageUploadStatusEnum.WAIT.getCode();
            }
        }
        entity.setUploadStatus(uploadStatus);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void handleMergePackageDeliveryOther(String soId, SoB2cDeliveryEntity deliveryEntity) {
        //将发货状态更新为已发货
        deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.SHIPPED.getCode());
        //获取一个当前时间当作发货时间
        LocalDateTime deliveryTime = LocalDateTime.now();
        deliveryEntity.setDeliveryTime(deliveryTime);

        //将发货状态更新为已发货
        if (!soB2cDeliveryService.updateById(deliveryEntity)) {
            throw new ServiceException("发货单更新失败");
        }

        String msg = StrUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", UserContext.getDefaultLoginUser().getUserName(), "组包称重", deliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), deliveryEntity.getId(), "组包称重");

        //修改订单状态已发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Arrays.asList(soId));
        updateDeliveryTimeDTO.setSoDeliveryDTOList(Arrays.asList(new SoB2cDTO.SoDeliveryDTO(soId, deliveryEntity.getCode())));
        updateDeliveryTimeDTO.setStatus(SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
        updateDeliveryTimeDTO.setDeliveryTime(deliveryTime);
        deliveryEntity.setShipmentMark(ShipmentMarkTypeEnum.AUTO.getCode());
        soB2cFeign.updateSoB2cStatusAndDeliveryTime(updateDeliveryTimeDTO);
    }

    /**
     * 根据组包id获取揽收地址
     * @param ids
     * @return
     */
    @Override
    public List<LogisticsAddressDTO.ListDTO> listAddressByForecastIds(List<String> ids) {
        List<PackageForecastEntity> packageForecastEntityList = this.getBaseMapper().selectBatchIds(ids);
        if (CollectionUtils.isEmpty(packageForecastEntityList)) {
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单");
        }
        List<PackageForecastDetailEntity> forecastDetailEntityList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(forecastDetailEntityList)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "组包预报单明细");
        }
        List<String> soIds = forecastDetailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_NOT_EXIST);
        }
        List<String> shopIds = soB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)){
            throw new ServiceException("销售订单店铺未找到");
        }
        LogisticsAddressDTO.AddressByTypeDTO dto = LogisticsAddressDTO.AddressByTypeDTO.builder()
                .type(LogisticsAddressTypeEnum.COLLECT.getCode())
                .shopIds(shopIds)
                .build();
        return logisticsFeign.listAddressByType(dto);
    }

    @Override
    public PagingVO<PackageForecastDTO.ExportViewDTO> exportPackageForecast(PagingDTO<PackageForecastDTO.ExportDTO> dto) {
        Page<PackageForecastDTO.ExportViewDTO> page = baseMapper.listExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()),dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //处理分页数据
        fillExportPaging(page.getRecords());
        return new PagingVO<>(page);
    }
}
