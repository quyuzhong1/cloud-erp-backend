package com.erp.model.plm.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ZipTaskResultDTO {
    private final AtomicInteger total = new AtomicInteger();
    private final AtomicInteger success = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();

    private final Map<String, List<String>> successUrls = new ConcurrentHashMap<>();
    private final Map<String, List<String>> failedNames = new ConcurrentHashMap<>();

    public void incrementTotal() {
        total.incrementAndGet();
    }

    public void incrementSuccess(String skuId, String fileUrl) {
        success.incrementAndGet();
        if (StringUtils.isNotBlank(skuId) && StringUtils.isNotBlank(fileUrl)) {
            successUrls.computeIfAbsent(skuId, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(fileUrl); // synchronizedList 本身保证了线程安全
        }
    }

    public void incrementFailed(String skuNo, String fileName) {
        failed.incrementAndGet();
        if (StringUtils.isNotBlank(skuNo) && StringUtils.isNotBlank(fileName)) {
            failedNames.computeIfAbsent(skuNo, k -> Collections.synchronizedList(new ArrayList<>()))
                    .add(fileName); // synchronizedList 本身保证了线程安全
        }
    }

    public int getTotal() {
        return total.get();
    }

    public int getSuccess() {
        return success.get();
    }

    public int getFailed() {
        return failed.get();
    }

    public Map<String, List<String>> getSuccessFiles() {
        return successUrls;
    }
    public Map<String, List<String>> getFailFiles() {
        return failedNames;
    }

    @Override
    public String toString() {
        return String.format("Total: %d, Success: %d, Failed: %d", getTotal(), getSuccess(), getFailed());
    }
}
