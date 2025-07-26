package com.erp.model.plm.dto;

import com.erp.model.plm.entity.PlmAttachmentEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ZipTaskResultDTO {
    private final AtomicInteger total = new AtomicInteger();
    private final AtomicInteger success = new AtomicInteger();
    private final AtomicInteger failed = new AtomicInteger();
    private final List<String> failedFiles = Collections.synchronizedList(new ArrayList<>());

    public void incrementTotal() {
        total.incrementAndGet();
    }

    public void incrementSuccess() {
        success.incrementAndGet();
    }

    public void incrementFailed(String fileName) {
        failed.incrementAndGet();
        failedFiles.add(fileName);
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

    public List<String> getFailedFiles() {
        return failedFiles;
    }

    @Override
    public String toString() {
        return String.format("Total: %d, Success: %d, Failed: %d", getTotal(), getSuccess(), getFailed());
    }
}
