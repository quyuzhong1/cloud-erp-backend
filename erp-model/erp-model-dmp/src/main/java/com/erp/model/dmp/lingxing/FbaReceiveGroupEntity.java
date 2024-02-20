package com.erp.model.dmp.lingxing;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.CleanBaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
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

    public static FbaReceiveGroupEntity init(Map.Entry<String, List<FbaReceiveDetailEntity>> entry, LocalDate receiveDate) {
        FbaReceiveGroupEntity result = new FbaReceiveGroupEntity();
        result.setFbaShipmentId(entry.getKey());
        result.setReceiveDate(receiveDate);
        result.setDetailList(entry.getValue());
        result.setUniqueId(StrUtil.format("{}_{}", entry.getKey(), receiveDate.toString()));
        return result;
    }


}
