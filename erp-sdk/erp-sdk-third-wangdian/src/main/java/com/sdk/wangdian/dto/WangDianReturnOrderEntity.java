package com.sdk.wangdian.dto;

import com.common.business.dto.CleanBaseDTO;
import com.common.business.dto.JobTaskDTO;
import com.common.business.enums.PlatformDictEnum;
import com.sdk.wangdian.sdk.api.wms.stockin.dto.RefundStockinResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Setter
@NoArgsConstructor
public class WangDianReturnOrderEntity extends CleanBaseDTO {

    private RefundStockinResponse.OrderInfoDto orderInfoDto;


    public WangDianReturnOrderEntity(RefundStockinResponse.OrderInfoDto orderInfoDto, JobTaskDTO dto) {
        this.orderInfoDto = orderInfoDto;
        this.setIsClean(0);
        this.setPlatform(PlatformDictEnum.WDT.getCode());
        this.setUniqueId(orderInfoDto.getStockinId());
        this.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
        this.setLastPushTime(dto.getNextTime().toString());
    }
}
