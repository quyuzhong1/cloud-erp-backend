package com.erp.server.oms.sdk.invoice;

import com.sdk.third.tf.dto.GetDanfeResponseDTO;
import com.sdk.third.tf.dto.GetDanfeDTO;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class NfeInvoiceServiceTest {

    @Test
    public void buildSimpleDanfeRequestUse150By100() throws Exception {
        GetDanfeDTO result = invokeBuildSimpleDanfeRequest("invoice-uuid");

        Assert.assertEquals("invoice-uuid", result.getUuid());
        Assert.assertEquals(Integer.valueOf(150), result.getAltura());
        Assert.assertEquals(Integer.valueOf(100), result.getLargura());
    }

    @Test
    public void resolveDanfePdfUrlPreferSimpleDanfe() throws Exception {
        GetDanfeResponseDTO.GetDanfeDataDTO dataDTO = new GetDanfeResponseDTO.GetDanfeDataDTO();
        dataDTO.setDanfe("https://example.com/full.pdf");
        dataDTO.setDanfeSimples("https://example.com/simple.pdf");

        String result = invokeResolveDanfePdfUrl(dataDTO);

        Assert.assertEquals("https://example.com/simple.pdf", result);
    }

    @Test
    public void resolveDanfePdfUrlFallbackToFullDanfe() throws Exception {
        GetDanfeResponseDTO.GetDanfeDataDTO dataDTO = new GetDanfeResponseDTO.GetDanfeDataDTO();
        dataDTO.setDanfe("https://example.com/full.pdf");

        String result = invokeResolveDanfePdfUrl(dataDTO);

        Assert.assertEquals("https://example.com/full.pdf", result);
    }

    private GetDanfeDTO invokeBuildSimpleDanfeRequest(String uuid) throws Exception {
        Method method = NfeInvoiceService.class.getDeclaredMethod("buildSimpleDanfeRequest", String.class);
        method.setAccessible(true);
        return (GetDanfeDTO) method.invoke(null, uuid);
    }

    private String invokeResolveDanfePdfUrl(GetDanfeResponseDTO.GetDanfeDataDTO dataDTO) throws Exception {
        Method method = NfeInvoiceService.class.getDeclaredMethod("resolveDanfePdfUrl", GetDanfeResponseDTO.GetDanfeDataDTO.class);
        method.setAccessible(true);
        return (String) method.invoke(null, dataDTO);
    }
}
