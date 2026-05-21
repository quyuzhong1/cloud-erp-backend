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
import com.erp.model.wms.enums.PackagePrintStatusEnum;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.server.wms.service.WmsAttachmentService;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackageGroupsBean;
import com.sdk.oms.tiktok.dto.tiktok.packages.CombinePackagePramDTO;
import com.sdk.oms.tiktok.dto.tiktok.split.CombinePackageViewDTO;
import com.sdk.oms.tiktok.service.TikTokPackageService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

    @Override
    public String platform() {
        return PlatformDictEnum.TIK_TOK.getCode();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> upload(PackageForecastDTO.UploadDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultDTOS.add(uploadOne(id, dto.getCollectMode(), dto.getCollectAddressId()));
            } catch (Exception e) {
                log.error("组包预报上传失败>>>>>", e);
                PackageForecastEntity entity = packageForecastMapper.selectById(id);
                if (Objects.isNull(entity)) {
                    resultDTOS.add(BatchResultDTO.fail(id, id, "组包预报单不存在, 上传失败"));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
                }
            }
        }
        return resultDTOS;
    }

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public BatchResultDTO uploadOne(String id, String collectMode, String collectAddressId) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validateUploadable(entity);
        try {
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            entity.setCollectMode(collectMode);
            entity.setCollectAddressId(collectAddressId);
            String newPackageId = tikTokMergePackage(entity);
            entity.setHandoverNo(newPackageId);
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode());
            packageForecastMapper.updateById(entity);
            return BatchResultDTO.success(entity.getId(), entity.getCode(), "上传成功");
        } catch (Exception e) {
            entity.setUploadStatus(PackageUploadStatusEnum.UPLOAD_FAILURE.getCode());
            entity.setRemark("上传失败:" + e.getMessage());
            packageForecastMapper.updateById(entity);
            log.error("组包预报上传失败>>>>>", e);
            return BatchResultDTO.fail(entity.getId(), entity.getCode(), "上传失败" + e.getMessage());
        }
    }

    @Override
    public String print(String id) {
        PackageForecastEntity entity = getForecastOrThrow(id);
        validatePrintable(entity);
        String base64 = tikTokPrint(entity);
        if (StringUtils.isBlank(base64)) {
            throw new ServiceException("打印失败");
        }
        entity.setPrintStatus(PackagePrintStatusEnum.ALREADY.getCode());
        packageForecastMapper.updateById(entity);
        return base64;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public List<BatchResultDTO> cancel(List<String> ids) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        for (String id : ids) {
            PackageForecastEntity entity = null;
            try {
                entity = getForecastOrThrow(id);
                if (isCanceled(entity)) {
                    resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "已取消上传"));
                    continue;
                }
                validateUploaded(entity);
                tikTokCancel(entity);
                resetAfterCancel(entity);
                packageForecastMapper.updateById(entity);
                resultDTOS.add(BatchResultDTO.success(entity.getId(), entity.getCode(), "取消上传"));
            } catch (Exception e) {
                log.error("取消上传失败>>>>", e);
                if (Objects.nonNull(entity)) {
                    entity.setRemark("取消失败原因:" + e.getMessage());
                    packageForecastMapper.updateById(entity);
                    resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), "取消上传失败:" + e.getMessage()));
                } else {
                    resultDTOS.add(BatchResultDTO.fail(id, id, e.getMessage()));
                }
            }
        }
        return resultDTOS;
    }

    @Override
    public void syncTrackingStatus(PackageForecastEntity entity) {
        // TikTok 普通组包暂无独立状态同步链路。
    }

    private void tikTokCancel(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = detailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
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
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soIds);
        String packageId = soB2cDetailEntityList.stream()
                .map(SoB2cDetailEntity::getPlatformPackageId)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
        if (StringUtils.isBlank(packageId)) {
            throw new ServiceException("TikTok包裹号为空");
        }
        List<String> orderIds = soB2cEntityList.stream().map(SoB2cEntity::getPlatformCode).collect(Collectors.toList());
        tikTokPackageService.uncombinePackage(shopIds.get(0), entity.getPlatformPackageNo(), orderIds);
    }

    private String tikTokMergePackage(PackageForecastEntity entity) {
        List<PackageForecastDetailEntity> detailEntityList = packageForecastDetailService.listDbByMainId(entity.getId());
        List<String> soIds = detailEntityList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = soB2cFeign.listByIds(soIds);
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
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soIds);
        String packageId = soB2cDetailEntityList.stream()
                .map(SoB2cDetailEntity::getPlatformPackageId)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
        if (StringUtils.isBlank(packageId)) {
            throw new ServiceException("TikTok包裹号为空");
        }
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
            throw new ServiceException("TIKTOK组包失败，{}", combinePackageViewDTO.getMessage());
        }
        return combinePackageViewDTO.getData().getPackages().get(0).getId();
    }

    private String tikTokPrint(PackageForecastEntity entity) {
        List<WmsAttachmentDTO.UpdateDTO> updateDTOS = wmsAttachmentService.getByBusinessIds(Collections.singletonList(entity.getId()));
        if (CollectionUtils.isEmpty(updateDTOS)) {
            return "";
        }
        WmsAttachmentDTO.UpdateDTO updateDTO = updateDTOS.get(0);
        String url = updateDTO.getAttachUrl();
        try {
            InputStream inputStream = FastDFSClientUtil.getInputStream(url);
            String base64 = PdfUtil.base64ForPdf(inputStream);
            return "data:application/pdf;base64," + base64;
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }
}
