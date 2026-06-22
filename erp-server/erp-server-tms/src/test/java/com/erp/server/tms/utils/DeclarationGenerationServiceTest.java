package com.erp.server.tms.utils;

import com.erp.model.tms.dto.TmsDeclareBillDTO;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DeclarationGenerationServiceTest {

    private final DeclarationGenerationService service = new DeclarationGenerationService();

    @Test
    public void shouldKeepSameBoxInSingleDeclarationWhenExceeding48Rows() {
        String sourceId = "SRC001";
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            sourceDetails.add(buildSourceDetail(sourceId, "BOX-1", "SKU-" + i, "HS001"));
        }

        List<TmsDeclareBillDTO.MergeDeclareBillDTO> bills = service.generateMergeBillDetails(sourceDetails, false, true);

        Assert.assertEquals(1, bills.size());
        assertNoBoxSplitAcrossBills(bills);
    }

    @Test
    public void shouldSplitDifferentBoxesWithin48RowLimit() {
        String sourceId = "SRC001";
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails = new ArrayList<>();
        for (int boxIndex = 1; boxIndex <= 2; boxIndex++) {
            for (int skuIndex = 0; skuIndex < 25; skuIndex++) {
                sourceDetails.add(buildSourceDetail(sourceId, "BOX-" + boxIndex, "SKU-" + boxIndex + "-" + skuIndex, "HS001"));
            }
        }

        List<TmsDeclareBillDTO.MergeDeclareBillDTO> bills = service.generateMergeBillDetails(sourceDetails, false, true);

        Assert.assertEquals(2, bills.size());
        Assert.assertEquals(25, bills.get(0).getDeclareBillList().size());
        Assert.assertEquals(25, bills.get(1).getDeclareBillList().size());
        assertNoBoxSplitAcrossBills(bills);
    }

    @Test
    public void shouldPackTwoSmallBoxesIntoOneDeclaration() {
        String sourceId = "SRC001";
        List<TmsDeclareBillDTO.SourceDeliveryDetailDTO> sourceDetails = new ArrayList<>();
        for (int boxIndex = 1; boxIndex <= 2; boxIndex++) {
            for (int skuIndex = 0; skuIndex < 24; skuIndex++) {
                sourceDetails.add(buildSourceDetail(sourceId, "BOX-" + boxIndex, "SKU-" + boxIndex + "-" + skuIndex, "HS001"));
            }
        }

        List<TmsDeclareBillDTO.MergeDeclareBillDTO> bills = service.generateMergeBillDetails(sourceDetails, false, true);

        Assert.assertEquals(1, bills.size());
        Assert.assertEquals(48, bills.get(0).getDeclareBillList().size());
        assertNoBoxSplitAcrossBills(bills);
    }

    private TmsDeclareBillDTO.SourceDeliveryDetailDTO buildSourceDetail(String sourceId, String boxNo, String skuId, String hsCode) {
        TmsDeclareBillDTO.SourceDeliveryDetailDTO detail = new TmsDeclareBillDTO.SourceDeliveryDetailDTO();
        detail.setSourceId(sourceId);
        detail.setBoxNo(boxNo);
        detail.setSkuId(skuId);
        detail.setSkuNo(skuId);
        detail.setHsCode(hsCode);
        detail.setProductNameCn("测试品名");
        detail.setDeclareElement("要素");
        detail.setUnit("个");
        detail.setDeclareCurrency("USD");
        detail.setUnitPrice(new BigDecimal("20"));
        detail.setQty(2);
        detail.setCountryId("US");
        return detail;
    }

    private void assertNoBoxSplitAcrossBills(List<TmsDeclareBillDTO.MergeDeclareBillDTO> bills) {
        Set<String> seenBoxKeys = new HashSet<>();
        for (int billIndex = 0; billIndex < bills.size(); billIndex++) {
            TmsDeclareBillDTO.MergeDeclareBillDTO bill = bills.get(billIndex);
            Set<String> boxKeysInBill = bill.getDeclareBillList().stream()
                    .flatMap(detail -> detail.getSourceDeliveryDetailList().stream())
                    .map(source -> source.getSourceId() + "|" + source.getBoxNo())
                    .collect(Collectors.toSet());
            for (String boxKey : boxKeysInBill) {
                if (seenBoxKeys.contains(boxKey)) {
                    Assert.fail("Box " + boxKey + " appears in multiple declarations");
                }
                seenBoxKeys.add(boxKey);
            }
            for (String boxKey : boxKeysInBill) {
                long billCount = 0;
                for (TmsDeclareBillDTO.MergeDeclareBillDTO otherBill : bills) {
                    boolean containsBox = otherBill.getDeclareBillList().stream()
                            .flatMap(detail -> detail.getSourceDeliveryDetailList().stream())
                            .anyMatch(source -> boxKey.equals(source.getSourceId() + "|" + source.getBoxNo()));
                    if (containsBox) {
                        billCount++;
                    }
                }
                Assert.assertEquals("Box " + boxKey + " must stay in exactly one declaration", 1, billCount);
            }
        }
    }
}
