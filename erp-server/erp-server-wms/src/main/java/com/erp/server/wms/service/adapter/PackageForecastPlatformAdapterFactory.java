package com.erp.server.wms.service.adapter;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

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

    public PackageForecastPlatformAdapterFactory(List<PackageForecastPlatformAdapter> adapterList) {
        adapterMap = CollectionUtils.emptyIfNull(adapterList).stream()
                .collect(Collectors.toMap(PackageForecastPlatformAdapter::platform, Function.identity(), (left, right) -> left));
    }

    public Optional<PackageForecastPlatformAdapter> getByPlatform(String platform) {
        return Optional.ofNullable(adapterMap.get(platform));
    }

    public Optional<PackageForecastPlatformAdapter> getByForecastIds(List<String> ids) {
        return adapterMap.values().stream()
                .filter(adapter -> adapter.isForecast(ids))
                .findFirst();
    }

    public Collection<PackageForecastPlatformAdapter> listAdapters() {
        return adapterMap.values();
    }
}
