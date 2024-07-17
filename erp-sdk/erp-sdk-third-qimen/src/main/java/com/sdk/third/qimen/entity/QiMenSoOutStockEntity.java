package com.sdk.third.qimen.entity;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockoutSalesQuerywithdetailResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 从奇门拿到的销售出库单原始数据
 * @date 2024-06-07
 * @author tanmujin
 */
@Getter
@Setter
@NoArgsConstructor
public class QiMenSoOutStockEntity extends CleanBaseDTO {

    private WdtWmsStockoutSalesQuerywithdetailResponse.Order orderInfoDto;

    public QiMenSoOutStockEntity(WdtWmsStockoutSalesQuerywithdetailResponse.Order orderInfoDto, JobTaskDTO dto) {
        this.orderInfoDto = orderInfoDto;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.QI_MEN.getCode());
        this.setUniqueId(orderInfoDto.getOrderNo());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }
}
