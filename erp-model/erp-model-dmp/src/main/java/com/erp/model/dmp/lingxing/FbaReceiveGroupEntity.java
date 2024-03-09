package com.erp.model.dmp.lingxing;

import cn.hutool.core.util.StrUtil;
import com.common.business.dto.CleanBaseDTO;
import com.erp.model.dmp.enums.PlatformEnum;
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
     * 领星店铺ID
     */
    private String sid;

    /**
     * ERP店铺ID
     */
    private String shopId;

    /**
     * 同组的货件ID
     */
    private String fbaShipmentId;

    /**
     * 请求的签收日期
     */
    private LocalDate requestReceiveDate;

    /**
     * 当天同组的货件明细
     */
    private List<FbaReceiveDetailEntity> detailList;

    public static FbaReceiveGroupEntity init(Map.Entry<String, List<FbaReceiveDetailEntity>> entry, LocalDate receiveDate, String sid, String shopId) {
        FbaReceiveGroupEntity result = new FbaReceiveGroupEntity();
        result.setSid(sid);
        result.setShopId(shopId);
        result.setFbaShipmentId(entry.getKey());
        result.setRequestReceiveDate(receiveDate);
        result.setDetailList(entry.getValue());
        result.setPlatform(PlatformEnum.LINGXING.getName());
        result.setUniqueId(StrUtil.format("{}_{}_{}", entry.getKey(), receiveDate.toString(), sid));
        return result;
    }


}
