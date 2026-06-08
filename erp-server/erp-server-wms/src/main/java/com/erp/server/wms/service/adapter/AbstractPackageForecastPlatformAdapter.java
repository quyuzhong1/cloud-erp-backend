package com.erp.server.wms.service.adapter;

import com.common.business.enums.PlatformDictEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.model.wms.entity.PackageForecastEntity;
import com.erp.model.wms.enums.PackageUploadStatusEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.PackageForecastMapper;
import com.erp.server.wms.service.PackageForecastDetailService;
import org.apache.commons.collections4.CollectionUtils;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 组包预报平台适配器公共逻辑。
 */
public abstract class AbstractPackageForecastPlatformAdapter implements PackageForecastPlatformAdapter {

    @Resource
    protected PackageForecastMapper packageForecastMapper;

    @Resource
    protected PackageForecastDetailService packageForecastDetailService;

    @Resource
    protected SoB2cFeign soB2cFeign;

    @Override
    public boolean isForecast(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return false;
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            return false;
        }
        List<String> soIds = detailList.stream().map(PackageForecastDetailEntity::getSoId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            return false;
        }
        long platformCount = soList.stream().map(SoB2cEntity::getDictPlatform).distinct().count();
        if (platformCount > 1 && soList.stream().anyMatch(item -> platform().equals(item.getDictPlatform()))) {
            throw new ServiceException("组包预报单明细数据平台不一致");
        }
        return soList.stream().allMatch(item -> platform().equals(item.getDictPlatform()));
    }

    protected PackageForecastEntity getForecastOrThrow(String id) {
        PackageForecastEntity entity = packageForecastMapper.selectById(id);
        if (Objects.isNull(entity)) {
            throw new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "组包预报单");
        }
        return entity;
    }

    protected void validateUploadable(PackageForecastEntity entity) {
        List<String> uploadStatusList = Arrays.asList(
                PackageUploadStatusEnum.WAIT.getCode(),
                PackageUploadStatusEnum.UPLOAD_FAILURE.getCode(),
                PackageUploadStatusEnum.CANCEL.getCode()
        );
        if (!uploadStatusList.contains(entity.getUploadStatus())) {
            throw new ServiceException("仅待上传/上传失败/已取消可操作");
        }
    }

    protected boolean isCanceled(PackageForecastEntity entity) {
        return PackageUploadStatusEnum.CANCEL.getCode().equals(entity.getUploadStatus());
    }

    protected void validateUploaded(PackageForecastEntity entity) {
        if (!PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功可操作");
        }
    }

    protected void validatePrintable(PackageForecastEntity entity) {
        if (!PackageUploadStatusEnum.UPLOAD_SUCCESS.getCode().equals(entity.getUploadStatus())) {
            throw new ServiceException("仅上传成功后可操作");
        }
    }

    protected void resetAfterCancel(PackageForecastEntity entity) {
        entity.setUploadStatus(PackageUploadStatusEnum.CANCEL.getCode());
        entity.setHandoverStatus("");
        entity.setTransportNo("");
        entity.setHandoverNo("");
        entity.setRemark("");
        entity.setPlatformPackageNo("");
    }

    protected String platformName(String platform) {
        PlatformDictEnum platformDictEnum = PlatformDictEnum.getByCode(platform);
        return Objects.nonNull(platformDictEnum) ? platformDictEnum.getName() : platform;
    }
}
