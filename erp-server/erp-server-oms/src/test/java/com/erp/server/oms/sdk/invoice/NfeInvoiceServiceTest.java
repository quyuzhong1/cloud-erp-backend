package com.erp.server.oms.sdk.invoice;

import com.sdk.third.tf.dto.GetDanfeResponseDTO;
import com.sdk.third.tf.dto.GetDanfeDTO;
import org.junit.Assert;
import org.junit.Test;

public class NfeInvoiceServiceTest {

    @Test
    public void buildSimpleDanfeRequestUse150By100() {
        GetDanfeDTO result = NfeInvoiceService.buildSimpleDanfeRequest("invoice-uuid");

        Assert.assertEquals("invoice-uuid", result.getUuid());
        Assert.assertEquals(Integer.valueOf(150), result.getAltura());
        Assert.assertEquals(Integer.valueOf(100), result.getLargura());
    }

    @Test
    public void resolveDanfePdfUrlPreferSimpleDanfe() {
        GetDanfeResponseDTO.GetDanfeDataDTO dataDTO = new GetDanfeResponseDTO.GetDanfeDataDTO();
        dataDTO.setDanfe("https://example.com/full.pdf");
        dataDTO.setDanfeSimples("https://example.com/simple.pdf");

        String result = NfeInvoiceService.resolveDanfePdfUrl(dataDTO);

        Assert.assertEquals("https://example.com/simple.pdf", result);
    }

    @Test
    public void resolveDanfePdfUrlFallbackToFullDanfe() {
        GetDanfeResponseDTO.GetDanfeDataDTO dataDTO = new GetDanfeResponseDTO.GetDanfeDataDTO();
        dataDTO.setDanfe("https://example.com/full.pdf");

        String result = NfeInvoiceService.resolveDanfePdfUrl(dataDTO);

        Assert.assertEquals("https://example.com/full.pdf", result);
    }
}
