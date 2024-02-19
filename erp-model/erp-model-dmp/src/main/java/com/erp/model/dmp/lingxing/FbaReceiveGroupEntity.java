package com.erp.model.dmp.lingxing;

import com.common.business.dto.CleanBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class FbaReceiveGroupEntity extends CleanBaseDTO {
    /**
     * 同组的货件ID
     */
    private String fbaShipmentId;

    /**
     * 签收日期
     */
    private LocalDate receiveDate;

    /**
     * 当天同组的货件明细
     */
    private List<FbaReceiveDetailEntity> detailList;

}
