package com.sdk.wms.iml.dto.response;

import com.common.business.dto.CleanBaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImlOutboundResp extends CleanBaseDTO {

    private String orderNo;
}
