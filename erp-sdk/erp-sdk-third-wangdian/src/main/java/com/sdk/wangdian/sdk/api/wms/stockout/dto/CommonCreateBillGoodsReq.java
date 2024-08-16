package com.sdk.wangdian.sdk.api.wms.stockout.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonCreateBillGoodsReq {

	/**
	 * skuNo
	 */
	private String specNo;
	/**
	 * 数量
	 */
	private BigDecimal num;
	/**
	 * 备注
	 */
	private String remark;
	/**
	 * 仓位
	 */
	private String positionNo;
}
