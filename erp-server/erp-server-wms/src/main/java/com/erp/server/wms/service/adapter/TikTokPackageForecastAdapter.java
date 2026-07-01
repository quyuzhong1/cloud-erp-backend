package com.erp.server.wms.service.adapter;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.utils.PdfUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.dto.WmsAttachmentDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.server.wms.service.WmsAttachmentService;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackageGroupsBean;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackagePramDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.CombinePackageViewDTO;
import com.sdk.oms.tiktok.service.TikTokPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * TikTok 组包预报适配器。
 */
@Slf4j
@Component
public class TikTokPackageForecastAdapter extends AbstractPackageForecastPlatformAdapter {

    @Resource
    private TikTokPackageService tikTokPackageService;

    @Resource
    private WmsAttachmentService wmsAttachmentService;

    private static final String UPLOAD_FAILURE_MESSAGE = "上传失败，请稍后重试或联系管理员处理";
    private static final String CANCEL_FAILURE_MESSAGE = "取消上传失败，请稍后重试或联系管理员处理";
    private static final String CANCEL_LOCAL_UPDATE_FAILURE_MESSAGE = "平台已取消，本地状态更新失败，请人工核对";

    @Override
    public String platform() {
        return PlatformDictEnum.TIK_TOK.getCode();
    }

    @Override
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        TikTokForecastContext context = buildContext(dto.getIds());
        for (String id : dto.getIds()) {
            try {
                PackageForecastEntity entity = getForecastOrThrow(id, context);
                resultDTOS.add(uploadOne(entity, dto.getCollectMode(), dto.getCollectAddressId(), context));
            } catch (Exception e) {
                log.error("组包预报上传失败>>>>>", e);
                PackageForecastEntity entity = context.getEntityMap().get(id);
                if (Objects.isNull(entity)) {
                    resultDTOS.add(BatchResultDTO.fail(id, id, "组包预报单不存在, 上传失败"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), uploadFailureMessage(e)));
                }
            }
        }
        return resultDTOS;
    }

    private BatchResultDTO uploadOne(PackageForecastEntity entity, String collectMode, String collectAddressId,
                                     TikTokForecastContext context) {
        validateUploadable(entity);
        try {
            entity.setCollectMode(collectMode);
            entity.setCollectAddressId(collectAddressId);
            String newPackageId = tikTokMergePackage(entity, context);
            entity.setHandoverNo(newPackageId);
            entity.setPlatformPackageNo(newPackageId);
            entity.setPlatformNo(buildPlatformNo(entity.getHandoverNo(), entity.getPlatformPackageNo()));
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            updateForecastOrThrow(entity);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
        } catch (Exception e) {
            if (StringUtils.isNotBlank(entity.getHandoverNo()) || StringUtils.isNotBlank(entity.getPlatformPackageNo())) {
                log.error("TikTok组包平台已成功但本地更新失败, id: {}, code: {}", entity.getId(), entity.getCode(), e);
                return BatchResultDTO.fail(entity.getId(), entity.getCode(), "平台已组包，本地状态更新失败，请人工核对");
            }
            String failureMessage = uploadFailureMessage(e);
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
            entity.setRemark(failureMessage);
            try {
                updateForecastOrThrow(entity);
            } catch (Exception updateException) {
                log.error("TikTok组包预报上传失败后更新失败状态失败, id: {}, code: {}", entity.getId(), entity.getCode(), updateException);
            }
            log.error("组包预报上传失败>>>>>", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), failureMessage);
        }
    }

    private String uploadFailureMessage(Exception e) {
        if (e instanceof ServiceException && StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        }
        return UPLOAD_FAILURE_MESSAGE;
    }

    private String cancelFailureMessage(Exception e) {
        if (e instanceof ServiceException && StringUtils.isNotBlank(e.getMessage())) {
            return e.getMessage();
        }
        return CANCEL_FAILURE_MESSAGE;
    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validatePrintable(entity);
        String base64 = tikTokPrint(entity);
        if (StringUtils.isBlank(base64)) {
            throw new ServiceException("打印失败");
        }
        return base64;
    }

    @Override
    public List<BatchResultDTO> cancel(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        TikTokForecastContext context = buildContext(ids);
        for (String id : ids) {
            PackageForecastEntity entity = null;
            boolean platformCanceled = false;
            try {
                entity = getForecastOrThrow(id, context);
                if (isCanceled(entity)) {
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已取消上传"));
                    continue;
                }
                validateUploaded(entity);
                tikTokCancel(entity, context);
                platformCanceled = true;
                resetAfterCancel(entity);
                updateForecastOrThrow(entity);
                resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
            } catch (Exception e) {
                if (platformCanceled && Objects.nonNull(entity)) {
                    log.error("TikTok组包平台已取消但本地更新失败, id: {}, code: {}", entity.getId(), entity.getCode(), e);
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), CANCEL_LOCAL_UPDATE_FAILURE_MESSAGE));
                    continue;
                }
                log.error("取消上传失败>>>>", e);
                String failureMessage = cancelFailureMessage(e);
                if (Objects.nonNull(entity)) {
                    entity.setRemark(failureMessage);
                    try {
                        updateForecastOrThrow(entity);
                    } catch (Exception updateException) {
                        log.error("TikTok组包预报取消失败后更新失败原因失败, id: {}, code: {}", entity.getId(), entity.getCode(), updateException);
                    }
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), failureMessage));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, id, failureMessage));
                }
            }
        }
        return resultDTOS;
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        // TikTok 普通组包暂无独立状态同步链路。
    }

    private void tikTokCancel(PackageForecastEntity entity, TikTokForecastContext context) {
        List<PackageForecastDetailEntity> detailEntityList = getForecastDetails(entity, context);
        List<String> soIds = getSoIds(detailEntityList);
        List<SoB2cEntity> soB2cEntityList = getSoList(soIds, context);
        List<String> shopIds = soB2cEntityList.stream()
                .map(SoB2cEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("TikTok不支持多店铺取消组包");
        }
        String packageId = entity.getPlatformPackageNo();
        if (StringUtils.isBlank(packageId)) {
            throw new ServiceException("TikTok包裹号为空");
        }
        List<String> orderIds = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).collect(Collectors.toList());
        tikTokPackageService.uncombinePackage(shopIds.get(0), packageId, orderIds);
    }

    private String tikTokMergePackage(PackageForecastEntity entity, TikTokForecastContext context) {
        List<PackageForecastDetailEntity> detailEntityList = getForecastDetails(entity, context);
        List<String> soIds = getSoIds(detailEntityList);
        List<SoB2cEntity> soB2cEntityList = getSoList(soIds, context);
        List<String> shopIds = soB2cEntityList.stream()
                .map(SoB2cEntity::getShopId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopIds)) {
            throw new ServiceException("销售订单店铺未找到");
        }
        if (shopIds.size() > 1) {
            throw new ServiceException("TikTok不支持多店铺组包预报");
        }
        List<SoB2cDetailEntity> soB2cDetailEntityList = getSoDetailList(soIds, context);
        List<String> packageIds = soB2cDetailEntityList.stream()
                .map(SoB2cDetailEntity::getPlatformPackageId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(packageIds)) {
            throw new ServiceException("TikTok包裹号为空");
        }
        if (packageIds.size() > 1) {
            throw new ServiceException("TikTok不支持一个组包预报单关联多个平台包裹号");
        }
        String packageId = packageIds.get(0);
        List<String> orderIds = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).collect(Collectors.toList());
        CombinePackagePramDTO combinePackagePramDTO = new CombinePackagePramDTO();
        List<CombinePackageGroupsBean> combinePackageGroupsBeanList = new ArrayList<>();
        CombinePackageGroupsBean combinePackageGroupsBean = new CombinePackageGroupsBean();
        combinePackageGroupsBean.setId(packageId);
        combinePackageGroupsBean.setOrderIds(orderIds);
        combinePackageGroupsBeanList.add(combinePackageGroupsBean);
        combinePackagePramDTO.setCombinablePackages(combinePackageGroupsBeanList);
        CombinePackageViewDTO combinePackageViewDTO = tikTokPackageService.combinePackage(shopIds.get(0), combinePackagePramDTO);
        if (combinePackageViewDTO.getCode() != 0) {
            throw new ServiceException("TIKTOK组包失败：" + combinePackageViewDTO.getMessage());
        }
        if (combinePackageViewDTO.getData() == null
                || CollectionUtils.isEmpty(combinePackageViewDTO.getData().getPackages())) {
            throw new ServiceException("TIKTOK组包失败：未返回有效包裹信息");
        }
        String newPackageId = combinePackageViewDTO.getData().getPackages().get(0).getId();
        if (StringUtils.isBlank(newPackageId)) {
            throw new ServiceException("TIKTOK组包失败：未返回有效包裹号");
        }
        return newPackageId;
    }

    private TikTokForecastContext buildContext(List<String> ids) {
        TikTokForecastContext context = new TikTokForecastContext();
        if (CollectionUtils.isEmpty(ids)) {
            return context;
        }
        List<PackageForecastEntity> entityList = packageForecastMapper.selectBatchIds(ids);
        if (CollectionUtils.isNotEmpty(entityList)) {
            context.setEntityMap(entityList.stream()
                    .collect(Collectors.toMap(PackageForecastEntity::getId, Function.identity(), (left, right) -> left)));
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            return context;
        }
        context.setDetailMap(detailList.stream().collect(Collectors.groupingBy(PackageForecastDetailEntity::getMainId)));
        List<String> soIds = getSoIds(detailList);
        if (CollectionUtils.isEmpty(soIds)) {
            return context;
        }
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isNotEmpty(soList)) {
            context.setSoMap(soList.stream()
                    .collect(Collectors.toMap(SoB2cEntity::getId, Function.identity(), (left, right) -> left)));
        }
        List<SoB2cDetailEntity> soDetailList = soB2cFeign.listDetailByMainIds(soIds);
        if (CollectionUtils.isNotEmpty(soDetailList)) {
            context.setSoDetailMap(soDetailList.stream().collect(Collectors.groupingBy(SoB2cDetailEntity::getMainId)));
        }
        return context;
    }

    private void validateOrderPlatform(List<SoB2cEntity> soList) {
        boolean hasWrongPlatform = soList.stream()
                .anyMatch(entity -> !PlatformDictEnum.TIK_TOK.getCode().equals(entity.getDictPlatform()));
        if (hasWrongPlatform) {
            throw new ServiceException("仅TikTok普通平台订单可操作");
        }
    }

    private PackageForecastEntity getForecastOrThrow(String id, TikTokForecastContext context) {
        PackageForecastEntity entity = context.getEntityMap().get(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException("组包预报单不存在");
        }
        return entity;
    }

    private List<PackageForecastDetailEntity> getForecastDetails(PackageForecastEntity entity, TikTokForecastContext context) {
        List<PackageForecastDetailEntity> detailEntityList = context.getDetailMap().get(entity.getId());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            throw new ServiceException("组包预报单明细未找到");
        }
        return detailEntityList;
    }

    private List<String> getSoIds(List<PackageForecastDetailEntity> detailEntityList) {
        return detailEntityList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<SoB2cEntity> getSoList(List<String> soIds, TikTokForecastContext context) {
        if (CollectionUtils.isEmpty(soIds)) {
            throw new ServiceException("销售订单未找到");
        }
        List<SoB2cEntity> soList = soIds.stream()
                .map(id -> context.getSoMap().get(id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soList)) {
            throw new ServiceException("销售订单未找到");
        }
        if (soList.size() != soIds.size()) {
            throw new ServiceException("组包预报单销售订单数据不完整");
        }
        validateOrderPlatform(soList);
        return soList;
    }

    private List<SoB2cDetailEntity> getSoDetailList(List<String> soIds, TikTokForecastContext context) {
        return soIds.stream()
                .map(id -> context.getSoDetailMap().getOrDefault(id, Collections.emptyList()))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private String tikTokPrint(PackageForecastEntity entity) {
        List<WmsAttachmentDTO.UpdateDTO> updateDTOS = wmsAttachmentService.getByBusinessIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(updateDTOS)) {
            // 保持平台适配器返回空串，外层 print 统一转成“打印失败”，避免适配器直接改变接口兼容行为。
            return "";
        }
        WmsAttachmentDTO.UpdateDTO updateDTO = updateDTOS.get(0);
        String url = updateDTO.getAttachUrl();
        try (InputStream inputStream = FastDFSClientUtil.getInputStream(url)) {
            String base64 = PdfUtil.base64ForPdf(inputStream);
            return "data:application/pdf;base64," + base64;
        } catch (Exception e) {
            throw new ServiceException("打印失败");
        }
    }

    private String buildPlatformNo(String handoverNo, String platformPackageNo) {
        if (StringUtils.isBlank(handoverNo) && StringUtils.isBlank(platformPackageNo)) {
            return "";
        }
        return StringUtils.defaultString(handoverNo) + "/" + StringUtils.defaultString(platformPackageNo);
    }

    private static class TikTokForecastContext {

        private Map<String, PackageForecastEntity> entityMap = new HashMap<>();
        private Map<String, List<PackageForecastDetailEntity>> detailMap = new HashMap<>();
        private Map<String, SoB2cEntity> soMap = new HashMap<>();
        private Map<String, List<SoB2cDetailEntity>> soDetailMap = new HashMap<>();

        public Map<String, PackageForecastEntity> getEntityMap() {
            return entityMap;
        }

        public void setEntityMap(Map<String, PackageForecastEntity> entityMap) {
            this.entityMap = entityMap;
        }

        public Map<String, List<PackageForecastDetailEntity>> getDetailMap() {
            return detailMap;
        }

        public void setDetailMap(Map<String, List<PackageForecastDetailEntity>> detailMap) {
            this.detailMap = detailMap;
        }

        public Map<String, SoB2cEntity> getSoMap() {
            return soMap;
        }

        public void setSoMap(Map<String, SoB2cEntity> soMap) {
            this.soMap = soMap;
        }

        public Map<String, List<SoB2cDetailEntity>> getSoDetailMap() {
            return soDetailMap;
        }

        public void setSoDetailMap(Map<String, List<SoB2cDetailEntity>> soDetailMap) {
            this.soDetailMap = soDetailMap;
        }
    }
}
