package com.erp.server.wms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.PdfUtil;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.PackageStatusEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.oms.enums.TransferStatusEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.entity.TransferDeclareDetailEntity;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.PackageForecastDetailDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.tms.feign.ForecastFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.tms.feign.TransferDeclareFeign;
import com.erp.server.wms.convert.PackageForecastConverter;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.*;
import com.erp.server.wms.service.adapter.AliExpressPackageForecastAdapter;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapter;
import com.erp.server.wms.service.adapter.PackageForecastPlatformAdapterFactory;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyShippingProviderReq;
import com.sdk.oms.tiktok.dto.tiktok.fully.TikTokFullyShippingProviderResp;
import com.sdk.oms.tiktok.dto.tiktok.order.FullyDeliveryOrderDTO;
import com.sdk.oms.tiktok.service.TikTokFullService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    @Resource
    private ForecastFeign forecastFeign;

    @Resource
    private LogisticsFeign logisticsFeign;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;

    @Resource
    private TransferDeclareFeign transferDeclareFeign;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    @Resource
    private TikTokFullService tikTokFullService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private PackageForecastPlatformAdapterFactory packageForecastPlatformAdapterFactory;

    @Resource
    private AliExpressPackageForecastAdapter aliExpressPackageForecastAdapter;

    @Resource
    @Lazy
    private PackageForecastService service;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据单号为【{}】", UserContext.getDefaultLoginUser().getUserName(), "组包预报单", packageForecastEntity.getCode());
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
        if (Objects.isNull(entity)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
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
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
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
                SoB2cEntity sourceSoB2cEntity = soB2cEntityList.stream()
                        .filter(req -> req.getId().equals(packageForecastDetailEntity.getSoId()))
                        .findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(sourceSoB2cEntity)) {
                    pagingDetailViewDTO.setPlatformOrderCode(sourceSoB2cEntity.getPlatformCode());
                }
                SoB2cEntity soB2cEntity = soB2cEntityList.stream()
                        .filter(req -> req.getId().equals(packageForecastDetailEntity.getSoId())
                                && SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(req.getBillStatus()))
                        .findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cEntity)) {
                    pagingDetailViewDTO.setOutstockStatusName("已出库");
                } else {
                    pagingDetailViewDTO.setOutstockStatusName("未出库");
                }
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
                if (CharSequenceUtil.isBlank(trackNo)) {
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
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
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
        if (CharSequenceUtil.isNotBlank(handoverNo) || CharSequenceUtil.isNotBlank(platformPackageNo)) {
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO delete(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
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
    public BatchResultDTO cancel(String id) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        String successCode = PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode();
        if(PackageUploadStatusEnum.CANCEL.getCode().equals(entity.getUploadStatus())){
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "已取消上传");
        }
        if (!successCode.equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功可操作");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        String logisticsPlatform = authDTO.getLogisticsPlatform();
        PackageForecastPlatformAdapter adapter = packageForecastPlatformAdapterFactory.getByPlatform(logisticsPlatform)
                .orElseThrow(() -> new ServiceException(platformName(logisticsPlatform) + "平台尚未对接取消上传"));
        return adapter.cancel(Collections.singletonList(id)).get(0);

    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        Optional<PackageForecastPlatformAdapter> adapterOptional = packageForecastPlatformAdapterFactory.getByForecastIds(ids);
        if (adapterOptional.isPresent()) {
            return adapterOptional.get().cancel(ids);
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = this.cancel(id);
            } catch (Exception e) {
                log.error("组包预报单取消失败===>{}", e.getMessage());
                PackageForecastEntity entity = this.getById(id);
                if (Objects.isNull(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 取消失败");
                } else {
                    cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                }
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS;
    }

    /**
     * 获取速卖通交接单上传基础信息
     *
     * @return
     */
    @Override
    public PackageForecastDTO.AlExpressHandoverBaseDTO getAlExpressHandoverBase(String logisticsPlatform, String shopId) {
        return aliExpressPackageForecastAdapter.getAlExpressHandoverBase(logisticsPlatform, shopId);
    }

    @Override
    public List<PackageForecastEntity> getAliExpressHandoverList(DateTime dateTime) {
        return aliExpressPackageForecastAdapter.listSyncTrackingStatus(dateTime);
    }

    @Async
    public void syncAliExpressInfo(PackageForecastEntity packageForecastEntity){
        aliExpressPackageForecastAdapter.syncTrackingStatus(packageForecastEntity);
    }

    @Override
    public void queryAliExpressInfo(PackageForecastEntity packageForecastEntity) {
        aliExpressPackageForecastAdapter.syncTrackingStatus(packageForecastEntity);
    }



    @Override
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        Optional<PackageForecastPlatformAdapter> adapterOptional = packageForecastPlatformAdapterFactory.getByPlatform(dto.getDeliveryPlatform());
        if (adapterOptional.isPresent()) {
            return adapterOptional.get().upload(dto);
        }
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO uploadResult;
            try {
                uploadResult = upload(id, dto.getCollectMode(), dto.getCollectAddressId());
            } catch (Exception e) {
                log.error("组包预报上传失败===>{}", e);
                PackageForecastEntity entity = this.getById(id);
                if (Objects.isNull(entity)) {
                    uploadResult = BatchResultDTO.fail(id, id, "组包预报单不存在, 上传失败");
                } else {
                    uploadResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
                }
            }
            resultDTOS.add(uploadResult);
        }
        return resultDTOS;
    }

    @Override
    public BatchResultDTO upload(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        //待上传
        String wait = PackageUploadStatusEnum.WAIT.getCode();
        //上传失败
        String failure = PackageUploadStatusEnum.UPLOAD_FAILURE.getCode();
        //已取消
        String cancel = PackageUploadStatusEnum.CANCEL.getCode();
        List<String> uploadStatusList = Arrays.asList(wait, failure, cancel);
        //上传状态
        String uploadStatus = entity.getUploadStatus();
        if (!uploadStatusList.contains(uploadStatus)) {
            throw new ServiceException("仅待上传/上传失败/已取消可操作");
        }
        //物流商
        String supplierId = entity.getLogisticsSupplierId();
        LogisticsSupplierDTO.AuthDTO authDTO = logisticsAuthFeign.getAuthBySupplierId(supplierId);
        if (Objects.isNull(authDTO)) {
            throw new ServiceException("物流商不存在");
        }
        try {
            String logisticsPlatform = authDTO.getLogisticsPlatform();
            PackageForecastPlatformAdapter adapter = packageForecastPlatformAdapterFactory.getByPlatform(logisticsPlatform)
                    .orElseThrow(() -> new ServiceException(platformName(logisticsPlatform) + "平台尚未对接上传"));
            PackageForecastDTO.UploadDTO dto = new PackageForecastDTO.UploadDTO();
            dto.setIds(Collections.singletonList(id));
            dto.setDeliveryPlatform(logisticsPlatform);
            dto.setCollectMode(collectMode);
            dto.setCollectAddressId(collectAddressId);
            return adapter.upload(dto).get(0);
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
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
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
            PackageForecastPlatformAdapter adapter = packageForecastPlatformAdapterFactory.getByPlatform(logisticsPlatform)
                    .orElseThrow(() -> new ServiceException(platformName(logisticsPlatform) + "平台尚未对接打印"));
            base64 = adapter.print(id);
        } catch (Exception e) {
            log.error("打印失败>>>>>>>{}", e);
            throw new ServiceException(e.getMessage());
        }
        if (CharSequenceUtil.isNotBlank(base64)) {
            entity.setPrintStatus(PackagePrintStatusEnum.ALREADY.getCode());
            this.updateById(entity);
        } else {
            throw new ServiceException("打印失败");
        }
        return base64;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO forecast(String id, String transferLogisticsSupplierId, String transferLogisticsChannelId) {
        PackageForecastEntity entity = this.getById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
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
//            return BatchResultDTO.fail(entity.getId(), entity.getCode(), CharSequenceUtil.format("{}已中转不可重复中转",alreadyTransferList));
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
            if (CharSequenceUtil.isBlank(packageForecastEntity.getLogisticsSupplierId())) {
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
                if(CharSequenceUtil.isBlank(soB2cLogisticsEntities.getTransferLogisticsChannelId())){
                    errorSoList.add(detail.getSoCode());
                }
            }
            if(CollectionUtils.isNotEmpty(errorSoList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), CharSequenceUtil.format("中转物流商和中转渠道为空,销售订单:{}", errorSoList)));
                continue;
            }
            //是否有销售订单已生成中转报关详情
            List<String> soCodes = detailList.stream().map(PackageForecastDetailEntity::getSoCode).collect(Collectors.toList());
            List<TransferDeclareDetailEntity> transferDeclareDetailEntityList = allTransferDeclareDetailEntityList.stream().filter(v->soCodes.contains(v.getSoCode())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(transferDeclareDetailEntityList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), CharSequenceUtil.format("销售订单已生成中转报关详情,销售订单:{}", transferDeclareDetailEntityList.stream().map(TransferDeclareDetailEntity::getSoCode).collect(Collectors.toList()))));
                continue;
            }
            //校验销售订单是否已成功预报
            List<String> passTransferStatus = Arrays.asList(TransferStatusEnum.NOT.getCode(),TransferStatusEnum.SUCCESS.getCode());
            List<SoB2cEntity> notPassSoB2cList = allSoB2cEntityList.stream().filter(v->soIds.contains(v.getId()) && !passTransferStatus.contains(v.getTransferStatus())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(notPassSoB2cList)){
                batchResultDTOList.add(BatchResultDTO.fail(packageForecastEntity.getId(), packageForecastEntity.getCode(), CharSequenceUtil.format("销售订单未成功预报,销售订单:{}", notPassSoB2cList.stream().map(SoB2cEntity::getCode).collect(Collectors.toList()))));
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
        List<TransferDeclareDTO.AddDTO> addDTOList = new ArrayList<>(instockForcastMergeDTOList.size());
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
            addDTOList.add(addDTO);
        }
        transferDeclareFeign.batchAdd(addDTOList);
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
        List<String> soIds = list.stream()
                .map(PackageForecastDTO.ExportViewDTO::getSoId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = CollectionUtils.isNotEmpty(soIds) ? soB2cFeign.listByIds(soIds) : Collections.emptyList();
        Map<String, SoB2cEntity> soB2cEntityMap = soB2cEntityList.stream()
                .collect(Collectors.toMap(SoB2cEntity::getId, item -> item, (oldValue, newValue) -> oldValue));
        for (PackageForecastDTO.ExportViewDTO item : list) {
            SoB2cEntity soB2cEntity = soB2cEntityMap.get(item.getSoId());
            if (Objects.nonNull(soB2cEntity)) {
                item.setPlatformOrderCode(soB2cEntity.getPlatformCode());
            }
            String uploadStatus = item.getUploadStatus();
            String uploadStatusName = PackageUploadStatusEnum.getName(uploadStatus);
            item.setUploadStatusName(uploadStatusName);
            String printStatus = item.getPrintStatus();
            String printStatusName = PackagePrintStatusEnum.getName(printStatus);
            item.setPrintStatusName(printStatusName);
            //跟踪单号
            String trackNo = item.getTrackNo();
            String minPackageTransportNo = item.getMinPackageTransportNo();
            if (CharSequenceUtil.isBlank(trackNo)) {
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
            if (Objects.nonNull(weight) && CharSequenceUtil.isNotBlank(weightUnit)){
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
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
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

        String msg = CharSequenceUtil.format("用户【{}】通过【{}】触发单据编号【{}】的自动发货功能", UserContext.getDefaultLoginUser().getUserName(), "组包称重", deliveryEntity.getCode());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SO_B2C_DELIVERY.getCode(), deliveryEntity.getId(), "组包称重");

        //修改订单状态已发货
        SoB2cDTO.UpdateDeliveryTimeDTO updateDeliveryTimeDTO = new SoB2cDTO.UpdateDeliveryTimeDTO();
        updateDeliveryTimeDTO.setSoB2cIds(Collections.singletonList(soId));
        updateDeliveryTimeDTO.setSoDeliveryDTOList(Collections.singletonList(new SoB2cDTO.SoDeliveryDTO(soId, deliveryEntity.getCode())));
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
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        List<PackageForecastDetailEntity> forecastDetailEntityList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(forecastDetailEntityList)){
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单明细");
        }
        List<String> soIds = forecastDetailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soB2cEntityList)) {
            throw new ServiceException(ApiError.SO_B2C_NOT_FOUND);
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
            throw new ServiceException(ApiError.FILE_EXPORT_DATA_EMPTY);
        }
        //处理分页数据
        fillExportPaging(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean uploadFileDTO(List<PackageForecastDTO.UploadFileDTO> uploadFileDTOList) {
        if(CollectionUtils.isEmpty(uploadFileDTOList)){
            return true;
        }
        List<String> ids = uploadFileDTOList.stream().map(PackageForecastDTO.UploadFileDTO::getId).collect(Collectors.toList());
        List<PackageForecastEntity> packageForecastEntityList = this.listByIds(ids);

        List<String> supplierIds = packageForecastEntityList.stream().map(PackageForecastEntity::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierDTO.AuthDTO> authDTOList = logisticsAuthFeign.listAuthBySupplierId(supplierIds);
        wmsAttachmentService.batchRemoveAttachment(ids);
        List<WmsAttachmentEntity> wmsAttachmentEntities = new ArrayList<>();
        List<PackageForecastEntity> updateList = new ArrayList<>();
        for (PackageForecastDTO.UploadFileDTO fileDTO : uploadFileDTOList) {
            PackageForecastEntity entity = packageForecastEntityList.stream().filter(v -> v.getId().equals(fileDTO.getId())).findFirst().orElse(null);
            if (Objects.isNull(entity)) {
                throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
            }
            //物流商
            LogisticsSupplierDTO.AuthDTO authDTO = authDTOList.stream().filter(v -> v.getMainId().equals(entity.getLogisticsSupplierId())).findFirst().orElse(null);
            if (Objects.isNull(authDTO)) {
                throw new ServiceException("物流商不存在");
            }
            String logisticsPlatform = authDTO.getLogisticsPlatform();
            if (logisticsPlatform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())) {
                throw new ServiceException("速卖通不支持上传文件");
            }
            entity.setTransportNo(fileDTO.getTransportNo());
            updateList.add(entity);
            WmsAttachmentEntity wmsAttachmentEntity = new WmsAttachmentEntity();
            wmsAttachmentEntity.setAttachUrl(fileDTO.getAttachDTO().getAttachUrl());
            wmsAttachmentEntity.setAttachName(fileDTO.getAttachDTO().getAttachName());
            wmsAttachmentEntity.setBusinessId(entity.getId());
            wmsAttachmentEntity.setType(PackageForecastEntity.PACKAGE_FORECAST);
            wmsAttachmentEntities.add(wmsAttachmentEntity);

        }
        wmsAttachmentService.saveBatch(wmsAttachmentEntities);
        this.updateBatchById(updateList);
        return true;
    }

    @Override
    public List<PackageForecastDTO.UploadFileViewDTO> uploadLabelView(List<String> ids) {
        List<PackageForecastEntity> packageForecastEntityList = this.listByIds(ids);
        List<PackageForecastDTO.UploadFileViewDTO> uploadFileViewDTOList = BeanUtil.copyToList(packageForecastEntityList, PackageForecastDTO.UploadFileViewDTO.class);
        List<String> businessIds = packageForecastEntityList.stream().map(PackageForecastEntity::getId).collect(Collectors.toList());
        List<WmsAttachmentDTO.UpdateDTO> attachmentList = wmsAttachmentService.getByBusinessIds(businessIds);
        uploadFileViewDTOList.forEach(v->{
            WmsAttachmentDTO.UpdateDTO updateDTO = attachmentList.stream().filter(t->t.getBusinessId().equals(v.getId())).findFirst().orElse(new WmsAttachmentDTO.UpdateDTO());
            AttachDTO attachDTO = new AttachDTO();
            attachDTO.setAttachName(updateDTO.getAttachName());
            attachDTO.setAttachUrl(updateDTO.getAttachUrl());
            v.setAttachDTO(attachDTO);
        });
        return uploadFileViewDTOList;
    }

    @Override
    public void batchPrint(List<String> ids, HttpServletResponse response) {
        List<PackageForecastEntity> packageForecastEntityList = this.listByIds(ids);
        List<String> errorCodeList = new ArrayList<>();
        List<String> base64List = new ArrayList<>();
        for (PackageForecastEntity entity : packageForecastEntityList) {
            String base64 = this.print(entity.getId());
            if(StringUtils.isEmpty(base64)){
                errorCodeList.add(entity.getCode());
            }else{
                base64List.add(base64);
            }
        }
        if(CollectionUtils.isNotEmpty(errorCodeList)){
            throw new ServiceException("组包预报批量打印失败,单号:{},未上传标签",errorCodeList);
        }
        if(CollectionUtils.isNotEmpty(base64List)){
            try {
                String newMergePdfBase64 = PdfUtil.getNewMergePdfBase64(base64List);

                // 设置响应头，告诉浏览器返回的是一个 PDF 文件
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline; filename=\"filename.pdf\""); // 设置 PDF 的显示方式和文件名
                BASE64Decoder decoder = new BASE64Decoder();
                try (OutputStream out = response.getOutputStream()) {
                    // 将 Base64 编码的字符串解码为字节数组
                    byte[] pdfBytes = decoder.decodeBuffer(newMergePdfBase64);
                    // 将字节数组写入到响应输出流中
                    out.write(pdfBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                log.error("组包预报批量打印打印失败>>>>>>>", e);
                throw new ServiceException(e.getMessage());
            }
        }
    }

    @Override
    public List<DictBasicDTO.DropDownDTO> getLogisticsType(String dictPlatform, String shopId) {
        if(!PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(dictPlatform)){
            return new ArrayList<>();
        }
//        tikTokFullService.getLogisticsType()
        return Collections.emptyList();
    }

    @Override
    public String getDeliveryPlatform(List<String> ids) {
        List<PackageForecastDetailEntity> allDetailList = packageForecastDetailService.listDbByMainIds(ids);
        if(CollectionUtils.isEmpty(allDetailList)){
            throw new ServiceException("组包预报单明细数据为空");
        }
        List<String> allSoIdList = allDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> allSoB2cEntityList = soB2cFeign.listByIds(allSoIdList);
        if(allSoB2cEntityList.stream().map(SoB2cEntity::getDictPlatform).distinct().count() > 1){
            throw new ServiceException("组包预报单明细数据平台不一致");
        }
        String platform = allSoB2cEntityList.get(0).getDictPlatform();
        if(!platform.equals(PlatformDictEnum.TIK_TOK_FULLY.getCode())
                && !platform.equals(PlatformDictEnum.ALI_EXPRESS.getCode())
                && !platform.equals(PlatformDictEnum.TIK_TOK.getCode())
                && !platform.equals(PlatformDictEnum.SHOPEE.getCode())){
            throw new ServiceException("非tiktok,tiktok全托管，速卖通，Shopee平台无需上传");
        }
        return platform;
    }

    @Override
    public List<DictBasicDTO.DropDownDTO> getLogisticType(String platform) {
        return dictBasicService.listByType("logisticType-"+platform, null);
    }

    @Override
    public PackageForecastDTO.ShippingProviderDTO searchShippingProvider(PackageForecastDTO.SearchShippingProviderDTO dto) {
        List<PackageForecastDetailEntity> allDetailList = packageForecastDetailService.listDbByMainIds(dto.getIds());
        if(CollectionUtils.isEmpty(allDetailList)){
            throw new ServiceException("组包预报单明细数据为空");
        }
        List<String> allSoIdList = allDetailList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> allSoB2cEntityList = soB2cFeign.listByIds(allSoIdList);
        List<SoB2cLogisticsEntity> allSoB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(allSoIdList);
        if(allSoB2cLogisticsEntities.size() > 50){
            throw new ServiceException("超过50个订单不可合并下单");
        }
        if(allSoB2cEntityList.stream().map(SoB2cEntity::getShopId).distinct().count()>1){
            throw new ServiceException("组包预报单明细数据店铺不一致");
        }
        allSoB2cLogisticsEntities.forEach(v->{
            if(StringUtils.isBlank(v.getCode())){
                SoB2cEntity soB2cEntity = allSoB2cEntityList.stream().filter(t->t.getId().equals(v.getMainId())).findFirst().orElse(new SoB2cEntity());
                throw new ServiceException("销售订单:{}未获取送货单号", soB2cEntity.getCode());
            }
        });
        List<String> deliveryCodes = allSoB2cLogisticsEntities.stream().map(SoB2cLogisticsEntity::getCode).collect(Collectors.toList());
        TikTokFullyShippingProviderReq tikTokFullyShippingProviderReq = buildReq(dto, deliveryCodes);
        TikTokFullyShippingProviderResp tikTokFullyShippingProviderResp = tikTokFullService.searchShippingProvider(allSoB2cEntityList.get(0).getShopId(),tikTokFullyShippingProviderReq);
        TikTokFullyShippingProviderResp.DataDTO data = tikTokFullyShippingProviderResp.getData();
        return buildTikTokSearchShipping(data);
    }

    @Override
    public BatchResultDTO uploadTikTokFully(PackageForecastDTO.UploadDTO dto) {
        PackageForecastPlatformAdapter adapter = packageForecastPlatformAdapterFactory.getByPlatform(PlatformDictEnum.TIK_TOK_FULLY.getCode())
                .orElseThrow(() -> new ServiceException("TikTok全托管平台尚未对接上传"));
        dto.setDeliveryPlatform(PlatformDictEnum.TIK_TOK_FULLY.getCode());
        return adapter.upload(dto).get(0);
    }

    private String platformName(String platform) {
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(platform);
        return Objects.nonNull(platformDictEnum) ? platformDictEnum.getName() : platform;
    }

    @Override
    public List<BatchResultDTO> confirmDelivery(List<String> ids) {
        List<PackageForecastEntity> packageForecastEntityList = this.listByIds(ids);
        List<PackageForecastDetailEntity> allPackageForecastDetailEntityList = packageForecastDetailService.listDbByMainIds(ids);
        List<String> allSoIds = allPackageForecastDetailEntityList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
        List<SoB2cEntity> allSoB2cEntityList = soB2cFeign.listByIds(allSoIds);
        List<SoB2cLogisticsEntity> allSoB2cLogisticsEntities = soB2cFeign.listSoB2cLogisticsByMainIdList(allSoIds);

        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<PackageForecastEntity> updateList = new ArrayList<>();
        for (PackageForecastEntity packageForecast : packageForecastEntityList) {
            List<PackageForecastDetailEntity> packageForecastDetailEntityList = allPackageForecastDetailEntityList.stream().filter(v->v.getMainId().equals(packageForecast.getId())).collect(Collectors.toList());
            List<String> soIds = packageForecastDetailEntityList.stream().map(PackageForecastDetailEntity::getSoId).collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntities = allSoB2cEntityList.stream().filter(v->soIds.contains(v.getId())).collect(Collectors.toList());
            List<SoB2cLogisticsEntity> soB2cLogisticsEntities = allSoB2cLogisticsEntities.stream().filter(v->soIds.contains(v.getMainId())).collect(Collectors.toList());
            if(soB2cEntities.stream().anyMatch(v->!v.getDictPlatform().equals(PlatformDictEnum.TIK_TOK_FULLY.getCode()))){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), "非TIKTOK全托管平台不支持确认发货"));
                continue;
            }
            if(!packageForecast.getCollectMode().equals(PackageForecastCollectModeEnum.SELF_SEND.getCode())){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), "非商家自配不支持确认发货"));
                continue;
            }
            if(!packageForecast.getUploadStatus().equals(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode())){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), "非上传成功不支持确认发货"));
                continue;
            }
            List<String> deliveryCodes = soB2cLogisticsEntities.stream().map(SoB2cLogisticsEntity::getCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
            if(CollectionUtils.isEmpty(deliveryCodes)){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), "发货单号为空"));
                continue;
            }
            List<FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO> deliveryOrdersDTOList = new ArrayList<>();
            try {
                deliveryOrdersDTOList= tikTokFullService.listDeliveryOrderByParam(soB2cEntities.get(0).getShopId(),deliveryCodes);
            }catch (Exception e){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), e.getMessage()));
                continue;
            }
            //tiktok全托管接口确认发货只能单个
            List<String> errorCodeList = new ArrayList<>();
            String errorMsg = "";
            for (SoB2cLogisticsEntity soB2cLogisticsEntity : soB2cLogisticsEntities) {
                SoB2cEntity soB2cEntity = allSoB2cEntityList.stream().filter(v->v.getId().equals(soB2cLogisticsEntity.getMainId())).findFirst().orElse(new SoB2cEntity());
                FullyDeliveryOrderDTO.DataDTO.DeliveryOrdersDTO deliveryOrdersDTO = deliveryOrdersDTOList.stream().filter(v->v.getCode().equals(soB2cLogisticsEntity.getCode())).findFirst().orElse(null);
                if(Objects.isNull(deliveryOrdersDTO)){
                    errorCodeList.add(soB2cEntity.getCode());
                    continue;
                }
                if(!deliveryOrdersDTO.getStatus().equals("WAIT_DELIVERY")){
                    continue;
                }
                try {
                    tikTokFullService.confirmDelivery(soB2cEntity.getShopId(),deliveryOrdersDTO.getCode());
                }catch (Exception e){
                    errorCodeList.add(soB2cEntity.getCode());
                    errorMsg = errorMsg + CharSequenceUtil.format("单号:{}确认发货失败,{};", soB2cEntity.getCode(), e.getMessage());
                }
            }

            if(StringUtils.isNotBlank(errorMsg)){
                packageForecast.setRemark(errorMsg);
                updateList.add(packageForecast);
            }
            if(CollectionUtils.isNotEmpty(errorCodeList)){
                resultDTOS.add(BatchResultDTO.fail(packageForecast.getId(), packageForecast.getCode(), CharSequenceUtil.format("单号:{}确认发货失败,{}", errorCodeList, errorMsg)));
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            service.updateBatchById(updateList);
        }
        return resultDTOS;
    }

    private PackageForecastDTO.ShippingProviderDTO buildTikTokSearchShipping(TikTokFullyShippingProviderResp.DataDTO data) {
        /**
         * 封装返回数据
         */
        PackageForecastDTO.ShippingProviderDTO shippingProviderDTO = new PackageForecastDTO.ShippingProviderDTO();
        if(CollectionUtils.isNotEmpty(data.getShippingProviders())){
            List<PackageForecastDTO.ProvidersAndCollectDTO> list = new ArrayList<>();
            //服务商
            for (TikTokFullyShippingProviderResp.DataDTO.ShippingProvidersDTO shippingProvider : data.getShippingProviders()) {
                PackageForecastDTO.ProvidersAndCollectDTO providersAndCollectDTO = new PackageForecastDTO.ProvidersAndCollectDTO();
                providersAndCollectDTO.setProviderCode(shippingProvider.getProviderCode());
                providersAndCollectDTO.setProviderName(shippingProvider.getProviderName());
                List<PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO> collectDataDTOList = new ArrayList<>();
                //揽收日期
                shippingProvider.getReserveDatas().forEach(v->{
                    PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO collectDataDTO = new PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO();
                    LocalDateTime collectDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(v.getShipTime() + "")), ZoneId.systemDefault());
                    collectDataDTO.setCollectDate(collectDateTime.toLocalDate());
                    collectDataDTO.setCanReserve(v.getCanReserve());
                    List<PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO.CollectTimeDTO> collectTimeDTOList = new ArrayList<>();
                    //揽收时间
                    v.getReserveSegments().forEach(t->{
                        PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO.CollectTimeDTO collectTimeDTO = new PackageForecastDTO.ProvidersAndCollectDTO.CollectDataDTO.CollectTimeDTO();
                        LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(t.getStartTime() + "")), ZoneId.systemDefault());
                        LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(t.getEndTime() + "")), ZoneId.systemDefault());
                        collectTimeDTO.setStartTime(startTime);
                        collectTimeDTO.setEndTime(endTime);
                        collectTimeDTO.setCanReserve(t.getCanReserve());
                        collectTimeDTOList.add(collectTimeDTO);
                    });
                    collectDataDTO.setCollectTimeDTOList(collectTimeDTOList);
                    collectDataDTOList.add(collectDataDTO);
                });
                providersAndCollectDTO.setCollectDataList(collectDataDTOList);
                list.add(providersAndCollectDTO);
            }
            shippingProviderDTO.setShippingProviderList(list);
        }
        if(CollectionUtils.isNotEmpty(data.getReserveArrivedTimes())){
            List<PackageForecastDTO.ReserveArrivedTimesDTO> reserveArrivedTimes = new ArrayList<>();
            for (TikTokFullyShippingProviderResp.DataDTO.ReserveArrivedTimesDTO reserveArrivedTime : data.getReserveArrivedTimes()) {
                PackageForecastDTO.ReserveArrivedTimesDTO reserveArrivedTimesDTO = new PackageForecastDTO.ReserveArrivedTimesDTO();
                LocalDateTime collectDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(reserveArrivedTime.getArrivedTime() + "")), ZoneId.systemDefault());
                reserveArrivedTimesDTO.setArrivedTime(collectDateTime.toLocalDate());
                reserveArrivedTimesDTO.setCanReserve(reserveArrivedTime.getCanReserve());
                reserveArrivedTimes.add(reserveArrivedTimesDTO);
            }
            shippingProviderDTO.setReserveArrivedTimes(reserveArrivedTimes);
        }
        return shippingProviderDTO;
    }

    private TikTokFullyShippingProviderReq buildReq(PackageForecastDTO.SearchShippingProviderDTO dto, List<String> deliveryCodes) {
        TikTokFullyShippingProviderReq tikTokFullyShippingProviderReq = new TikTokFullyShippingProviderReq();
        tikTokFullyShippingProviderReq.setDeliveryOption(dto.getDeliveryOption());
        if(dto.getCollectMode().equals(PackageForecastCollectModeEnum.SELF_SEND.getCode())){
            tikTokFullyShippingProviderReq.setDeliveryMode("SELF_DELIVERY");
        }else{
            tikTokFullyShippingProviderReq.setDeliveryMode("PLATFORM_DELIVERY");
        }
        tikTokFullyShippingProviderReq.setSenderContactId(dto.getAddressId());
        tikTokFullyShippingProviderReq.setDeliveryOrderCodes(deliveryCodes);
        tikTokFullyShippingProviderReq.setTotalWeight(new TikTokFullyShippingProviderReq.TotalWeightDTO(String.valueOf(dto.getWeight()),"GRAM"));
        return tikTokFullyShippingProviderReq;
    }
}
