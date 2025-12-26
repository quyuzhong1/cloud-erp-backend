
package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * 旺店通B2C销售订单相应类
 *
 * @author jack
 * @date 2025-12-10
 */
@Data
public class CreateSoB2cResponse {

	/**
	 * 更新订单数
	 */
	private Integer chgCount;
	/**
	 * 新增订单数
	 */
	private Integer newCount;

	/**
	 * 错误信息为空，表示全部创建成功
	 */
	private List<ErrorInfo> errorList;


	@ToString
	@Data
	public static class ErrorInfo {

		/**
		 * 单号
		 */
		private String no;
		/**
		 * 错误信息
		 */
		private String error;
	}

}