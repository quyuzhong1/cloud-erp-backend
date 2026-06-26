package com.erp.server.tms.service.support;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TmsAsyncTaskBatchIdPagesTest {

    @Test
    public void pageDistinctIdsFromStart() {
        assertEquals(Arrays.asList("a", "b"),
            TmsAsyncTaskBatchIdPages.pageDistinctIds(Arrays.asList("a", "b", "c"), "", 2));
    }

    @Test
    public void pageDistinctIdsAfterLastId() {
        assertEquals(Collections.singletonList("c"),
            TmsAsyncTaskBatchIdPages.pageDistinctIds(Arrays.asList("a", "b", "c"), "b", 2));
    }

    @Test
    public void pageDistinctIdsReturnsEmptyWhenLastIdMissing() {
        assertTrue(TmsAsyncTaskBatchIdPages.pageDistinctIds(Arrays.asList("a", "b"), "z", 2).isEmpty());
    }

    @Test
    public void selectedIdProviderReturnsNullForBlankIds() {
        assertEquals(null, TmsAsyncTaskBatchIdPages.selectedIdProvider(null));
        assertEquals(null, TmsAsyncTaskBatchIdPages.selectedIdProvider(Collections.emptyList()));
    }
}
