package com.erp.model.wms.dto.third;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author liuruipeng
 * @date 2023年11月17日 10:22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class ThirdWarehouseQueryFbaOutboundReq extends ThirdWarehouseAuth{

    //第三方仓的出库单号
    @NotEmpty(message = "发货单号不能为空")
    private List<String> erpOrderCodeList;

}
