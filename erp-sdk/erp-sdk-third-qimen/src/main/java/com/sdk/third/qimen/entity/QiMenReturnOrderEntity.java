package com.sdk.third.qimen.entity;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.qimencloud.api.scene3ldsmu02o9.response.WdtWmsStockinRefundQuerywithdetailResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 
 * @date 2024-06-11
 * @author tanmujin
 */
@Getter
@Setter
@NoArgsConstructor
public class QiMenReturnOrderEntity extends CleanBaseDTO {

    private WdtWmsStockinRefundQuerywithdetailResponse.Order orderInfoDto;

    public QiMenReturnOrderEntity(WdtWmsStockinRefundQuerywithdetailResponse.Order orderInfoDto, JobTaskDTO dto) {
        this.orderInfoDto = orderInfoDto;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.WDT.getCode());
        this.setUniqueId(String.valueOf(orderInfoDto.getOrderNo()));
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }
}
