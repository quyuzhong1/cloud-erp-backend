package com.erp.server.wms.service.impl;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.CfgAppClientDTO;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.erp.model.dmp.enums.AppClientEnum;
import com.erp.model.oms.entity.ShopAuthEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.*;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.constant.PackageForecastConstant;
import com.erp.server.wms.convert.PackageForecastConverter;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.CommonService;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
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
import com.erp.tms.aliexpress.model.order.request.QueryOrderRequest;
import com.erp.tms.aliexpress.model.order.response.BaseResult;
import com.erp.tms.aliexpress.model.order.response.ErrorResponse;
import com.erp.tms.aliexpress.model.order.response.QueryResponse;
import com.erp.tms.aliexpress.model.order.response.QueryResult;
import com.erp.tms.aliexpress.service.AliExpressHandoverService;
import com.erp.tms.aliexpress.service.AliExpressShipperService;
import com.erp.tms.aliexpress.util.ApiException;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private CommonService commonService;

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

    @Autowired
    private AliExpressShipperService aliExpressShipperService;


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
        String msg = StrUtil.format("用户【{}】新增【{}】单据单号为【{}】", commonService.getUserInfo().getUserName(), "组包预报单", packageForecastEntity.getCode());
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
    public List<PackageForecastDTO.TabListDTO> tabList() {
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
        String uploadStatus = "";
        if (!"all".equals(params.getTabFlag())) {
            uploadStatus = params.getTabFlag();
        }
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.paging(query, params, uploadStatus);
        List<PackageForecastDTO.PagingViewDTO> list = pageData.getRecords();
        //处理分页数据
        fillPaging(list);
        return new PagingVO<>(pageData);

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
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform);
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
    public PackageForecastDTO.AlExpressHandoverBaseDTO getAlExpressHandoverBase(String logisticsPlatform) {

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
        List<ShopAuthEntity> shopAuthList = shopAuthResult.getData().stream().filter(s -> s.getExtendData().contains(sellerIdFlag)).collect(Collectors.toList());
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
    public void queryAliExpressInfo(PackageForecastEntity packageForecastEntity, PackageForecastDTO.AlExpressHandoverBaseDTO alExpressHandoverBase) {
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
                this.updateById(entity);
                return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
            }else{
                entity.setUploadStatus(failure);
                entity.setRemark("上传失败:" + PlatformDictEnum.getByCode(logisticsPlatform).getName()+"平台尚未对接上传");
                this.updateById(entity);
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败");
            }
        } catch (Exception e) {
            entity.setUploadStatus(failure);
            entity.setRemark("上传失败:" + e.getMessage());
            this.updateById(entity);
            log.error("组包预报上传失败>>>>>{}", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败");
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
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform);
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
        PackageForecastDTO.AlExpressHandoverBaseDTO base = getAlExpressHandoverBase(logisticsPlatform);
        List<PackageForecastDetailEntity> forecastDetailList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIdList = forecastDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        Map<String, String> authMap = base.getAuthMap();
        //销售订单
        List<SoB2cEntity> soB2cList = soB2cFeign.listByIds(soIdList);
        for (SoB2cEntity soB2c : soB2cList) {
            String tradeOrderId = soB2c.getSourceId();
            PackageForecastDetailEntity detailEntity = forecastDetailList.stream().filter(d -> d.getSoId().equals(soB2c.getId())).
                    findFirst().orElse(null);
            String transportNo = detailEntity.getTransportNo();
            if (StringUtils.isNotBlank(tradeOrderId) && Objects.nonNull(detailEntity) && StringUtils.isBlank(detailEntity.getSourceCode())) {
                QueryOrderRequest queryOrderRequest=QueryOrderRequest.builder()
                        .trade_order_id(tradeOrderId)
                        .current_page(1)
                        .page_size(20)
                        .build();
                try {
                    BaseResult iopResponse = aliExpressShipperService.queryLogisticsOrder(authMap, queryOrderRequest);
                    ErrorResponse errorResponse = iopResponse.getErrorResponse();
                    if(Objects.nonNull(errorResponse)){
                        continue;
                    }
                    QueryResponse queryResponse = JSONObject.parseObject(iopResponse.getResult(), QueryResponse.class);
                    //成功
                    if (Objects.nonNull(queryResponse) && queryResponse.getSuccess()) {
                        List<QueryResult> responseList = queryResponse.getResultList();
                        String outOrderCode= responseList.stream().filter(r->r.getLogistics_order_id().equals(transportNo)).
                               map(QueryResult::getOut_order_code).findFirst().orElse("");
                       detailEntity.setSourceCode(outOrderCode);
                    }
                } catch (ApiException e) {
                 log.error("查询物流单信息失败,tradeOrderId:{}，错误信息:{}",tradeOrderId,e);
                }
            }
        }



        List<SellerParcelOrder> sellerParcelOrderList = new ArrayList<>(1);
        String topUserKey = base.getUserInfo().getTopUserKey();
        packageForecastDetailService.updateBatchById(forecastDetailList);
        SellerParcelOrder parcelOrder = new SellerParcelOrder();
        parcelOrder.setSellerId(topUserKey);
        List<String> orderCodeList =forecastDetailList.stream().map(PackageForecastDetailEntity::getSourceCode).collect(Collectors.toList());
        if (orderCodeList.size() != forecastDetailList.size()) {
            throw new ServiceException("未获取到小包第三方交易号");
        }
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
        List<String> sourceCodeList = forecastDetailList.stream().map(PackageForecastDetailEntity::getSourceCode).collect(Collectors.toList());
        String type = PackageForecastConstant.CAINIAO_PICKUP;
        String collectMode = entity.getCollectMode();
        String selfSend = PackageForecastCollectModeEnum.SELF_SEND.getCode();
        if (selfSend.equals(collectMode)) {
            type = PackageForecastConstant.SELF_SEND;
        }
        String client = PackageForecastConstant.CLIENT;
        CommitRequest commitRequest = CommitRequest.builder().pickInfo(addressInfo).
                skipInvalidParcel(Boolean.FALSE).
                orderCodeList(sourceCodeList).
                handoverOrderId("").
                appointmentType("bigbag").
                weight(entity.getTotalPackageWeight().setScale(0)).
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
    public Boolean exportExcel(PackageForecastDTO.ExportDTO dto, HttpServletResponse response) {
        String uploadStatus = "";
        if (!"all".equals(dto.getTabFlag())) {
            uploadStatus = dto.getTabFlag();
        }
        List<PackageForecastDTO.PagingViewDTO> list = baseMapper.listExcel(dto, uploadStatus);
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //处理分页数据
        fillPaging(list);

        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/packageForecast.xlsx";
        String name = "组包预报列表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("组包预报导出出错 {}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;

    }


    /**
     * 填充分页数据
     *
     * @param list
     */
    private void fillPaging(List<PackageForecastDTO.PagingViewDTO> list) {
        for (PackageForecastDTO.PagingViewDTO item : list) {
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
            String weightStr = weight + weightUnit;
            item.setWeightStr(weightStr);
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
}
