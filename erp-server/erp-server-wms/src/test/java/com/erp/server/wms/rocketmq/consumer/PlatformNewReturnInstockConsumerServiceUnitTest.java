package com.erp.server.wms.rocketmq.consumer;

import com.common.business.dto.PlatformReturnInstockDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.erp.model.oms.dto.SkuMappingDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

/**
 * B2C 参考单号匹配拆分逻辑单元测试：验证就近出库单匹配与订单明细兜底匹配均透传不良品标识。
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformNewReturnInstockConsumerServiceUnitTest {

    private static final String SO_ID = "so-b2c-001";
    private static final String SO_DETAIL_ID = "so-detail-001";
    private static final String OUTSTOCK_ID = "outstock-001";
    private static final String PLATFORM_SKU = "PLAT-SKU-001";
    private static final String ERP_SKU_ID = "erp-sku-001";
    private static final String ERP_SKU_NO = "ERP-SKU-001";
    private static final String WAREHOUSE_ID = "wh-001";

    @InjectMocks
    private PlatformNewReturnInstockConsumerService consumerService;

    @Mock
    private SoB2cFeign soB2cFeign;

    @Mock
    private SoOutstockService soOutstockService;

    @Mock
    private SoOutstockDetailService soOutstockDetailService;

    private WarehouseEntity warehouseEntity;
    private SoB2cEntity soB2cEntity;
    private List<SoB2cDetailEntity> soDetailEntityList;

    @Before
    public void setUp() {
        warehouseEntity = new WarehouseEntity();
        warehouseEntity.setId(WAREHOUSE_ID);
        warehouseEntity.setName("测试仓");

        soB2cEntity = new SoB2cEntity();
        soB2cEntity.setId(SO_ID);
        soB2cEntity.setCode("SO-B2C-001");

        SoB2cDetailEntity soDetail = new SoB2cDetailEntity();
        soDetail.setId(SO_DETAIL_ID);
        soDetail.setSkuId(ERP_SKU_ID);
        soDetail.setSkuNo(ERP_SKU_NO);
        soDetail.setPlatformSkuNo(PLATFORM_SKU);
        soDetailEntityList = Collections.singletonList(soDetail);

        when(soB2cFeign.listDetailByMainIds(Collections.singletonList(SO_ID))).thenReturn(soDetailEntityList);
    }

    /**
     * 就近出库单匹配成功时应透传平台推送的不良品标识。
     */
    @Test
    public void buildPlatformSoReturnInstockDetailSplit_nearestOutstock_setsDefectiveProductFlag() throws Exception {
        mockNearestOutstockMatch();

        PlatformReturnInstockDTO dto = buildDto(Boolean.TRUE);
        Object splitResult = invokeBuildPlatformSoReturnInstockDetailSplit(dto, Collections.emptyMap());
        List<SoReturnInstockDetailEntity> matchedList = getMatchedList(splitResult);

        Assert.assertEquals(1, matchedList.size());
        SoReturnInstockDetailEntity detailEntity = matchedList.get(0);
        Assert.assertEquals("outstock-sku-001", detailEntity.getSkuId());
        Assert.assertTrue(Boolean.TRUE.equals(detailEntity.getDefectiveProductFlag()));
    }

    /**
     * 未匹配就近出库单、按订单明细兜底匹配时应透传平台推送的不良品标识。
     */
    @Test
    public void buildPlatformSoReturnInstockDetailSplit_orderDetailFallback_setsDefectiveProductFlag() throws Exception {
        when(soOutstockService.listBySoIds(anyList())).thenReturn(Collections.emptyList());

        PlatformReturnInstockDTO dto = buildDto(Boolean.TRUE);
        Object splitResult = invokeBuildPlatformSoReturnInstockDetailSplit(dto, Collections.emptyMap());
        List<SoReturnInstockDetailEntity> matchedList = getMatchedList(splitResult);

        Assert.assertEquals(1, matchedList.size());
        SoReturnInstockDetailEntity detailEntity = matchedList.get(0);
        Assert.assertEquals(ERP_SKU_ID, detailEntity.getSkuId());
        Assert.assertEquals(ERP_SKU_NO, detailEntity.getSkuNo());
        Assert.assertTrue(Boolean.TRUE.equals(detailEntity.getDefectiveProductFlag()));
    }

    /**
     * 非 split 版 buildFallbackDetailBySoB2c 兜底路径同样应透传不良品标识。
     */
    @Test
    public void buildFallbackDetailBySoB2c_setsDefectiveProductFlag() throws Exception {
        PlatformReturnInstockDTO dto = buildDto(Boolean.TRUE);
        PlatformReturnInstockDTO.Detail detail = dto.getProductDetailList().get(0);

        SoReturnInstockDetailEntity detailEntity = invokeBuildFallbackDetailBySoB2c(
                detail, soDetailEntityList, warehouseEntity, dto);

        Assert.assertEquals(ERP_SKU_ID, detailEntity.getSkuId());
        Assert.assertTrue(Boolean.TRUE.equals(detailEntity.getDefectiveProductFlag()));
    }

    private void mockNearestOutstockMatch() {
        SoOutstockEntity outstockEntity = new SoOutstockEntity();
        outstockEntity.setId(OUTSTOCK_ID);
        // 此处赋值没问题，因为该字段是枚举
        outstockEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
        outstockEntity.setInvalidStatus(Boolean.FALSE);
        outstockEntity.setBillDate(LocalDate.now());
        when(soOutstockService.listBySoIds(anyList())).thenReturn(Collections.singletonList(outstockEntity));

        SoOutstockDetailEntity outstockDetail = new SoOutstockDetailEntity();
        outstockDetail.setMainId(OUTSTOCK_ID);
        outstockDetail.setSoDetailId(SO_DETAIL_ID);
        outstockDetail.setSkuId("outstock-sku-001");
        outstockDetail.setSkuNo("OUT-SKU-001");
        when(soOutstockDetailService.listByMainIds(anyList())).thenReturn(Collections.singletonList(outstockDetail));
    }

    private PlatformReturnInstockDTO buildDto(Boolean defectiveProductFlag) {
        PlatformReturnInstockDTO dto = new PlatformReturnInstockDTO();
        dto.setPlatformOrderNo("PO-001");
        dto.setPutawayTime(LocalDateTime.now());
        dto.setReason("测试");
        dto.setReturnType("claim");

        PlatformReturnInstockDTO.Detail detail = new PlatformReturnInstockDTO.Detail();
        detail.setProductSku(PLATFORM_SKU);
        detail.setMustQty(1);
        detail.setReceiveQty(1);
        detail.setRealQty(1);
        detail.setDefectiveProductFlag(defectiveProductFlag);
        dto.setProductDetailList(Collections.singletonList(detail));
        return dto;
    }

    private Object invokeBuildPlatformSoReturnInstockDetailSplit(PlatformReturnInstockDTO dto,
                                                                 Map<String, SkuMappingDTO.MappingSkuViewDTO> skuMappingMap) throws Exception {
        Method method = PlatformNewReturnInstockConsumerService.class.getDeclaredMethod(
                "buildPlatformSoReturnInstockDetailSplit",
                PlatformReturnInstockDTO.class,
                WarehouseEntity.class,
                SoB2cEntity.class,
                Map.class
        );
        method.setAccessible(true);
        return method.invoke(consumerService, dto, warehouseEntity, soB2cEntity, skuMappingMap);
    }

    private SoReturnInstockDetailEntity invokeBuildFallbackDetailBySoB2c(PlatformReturnInstockDTO.Detail detail,
                                                                         List<SoB2cDetailEntity> soDetails,
                                                                         WarehouseEntity warehouse,
                                                                         PlatformReturnInstockDTO dto) throws Exception {
        Method method = PlatformNewReturnInstockConsumerService.class.getDeclaredMethod(
                "buildFallbackDetailBySoB2c",
                PlatformReturnInstockDTO.Detail.class,
                List.class,
                WarehouseEntity.class,
                PlatformReturnInstockDTO.class
        );
        method.setAccessible(true);
        return (SoReturnInstockDetailEntity) method.invoke(consumerService, detail, soDetails, warehouse, dto);
    }

    @SuppressWarnings("unchecked")
    private List<SoReturnInstockDetailEntity> getMatchedList(Object splitResult) throws Exception {
        Field matchedListField = splitResult.getClass().getDeclaredField("matchedList");
        matchedListField.setAccessible(true);
        return (List<SoReturnInstockDetailEntity>) matchedListField.get(splitResult);
    }
}
