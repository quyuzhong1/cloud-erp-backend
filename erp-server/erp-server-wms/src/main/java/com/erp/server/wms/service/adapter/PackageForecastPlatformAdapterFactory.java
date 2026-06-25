package com.erp.server.wms.service.adapter;

import com.common.core.exception.ServiceException;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.PackageForecastDetailService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 组包预报平台适配器工厂。
 */
@Component
public class PackageForecastPlatformAdapterFactory {

    private final Map<String, PackageForecastPlatformAdapter> adapterMap;

    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    public PackageForecastPlatformAdapterFactory(List<PackageForecastPlatformAdapter> adapterList) {
        adapterMap = CollectionUtils.emptyIfNull(adapterList).stream()
                .collect(Collectors.toMap(PackageForecastPlatformAdapter::platform, Function.identity(), (left, right) -> {
                    throw new IllegalStateException("重复的平台组包预报适配器: " + left.platform());
                }));
    }

    public Optional<PackageForecastPlatformAdapter> getByPlatform(String platform) {
        return Optional.ofNullable(adapterMap.get(platform));
    }

    public Optional<PackageForecastPlatformAdapter> getByForecastIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Optional.empty();
        }
        List<PackageForecastDetailEntity> detailList = packageForecastDetailService.listDbByMainIds(ids);
        if (CollectionUtils.isEmpty(detailList)) {
            return Optional.empty();
        }
        List<String> soIds = detailList.stream()
                .map(PackageForecastDetailEntity::getSoId)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(soIds)) {
            return Optional.empty();
        }
        List<SoB2cEntity> soList = soB2cFeign.listByIds(soIds);
        if (CollectionUtils.isEmpty(soList)) {
            return Optional.empty();
        }
        if (soList.size() != soIds.size()) {
            throw new ServiceException("组包预报单销售订单数据不完整");
        }
        List<String> platformList = soList.stream()
                .map(SoB2cEntity::getDictPlatform)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        if (platformList.size() > 1) {
            throw new ServiceException("组包预报单明细数据平台不一致");
        }
        if (CollectionUtils.isEmpty(platformList)) {
            return Optional.empty();
        }
        return getByPlatform(platformList.get(0));
    }

    public Collection<PackageForecastPlatformAdapter> listAdapters() {
        return adapterMap.values();
    }
}
