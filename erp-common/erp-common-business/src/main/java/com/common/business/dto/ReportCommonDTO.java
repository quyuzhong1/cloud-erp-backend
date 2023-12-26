package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
public class ReportCommonDTO {

	private static final long serialVersionUID = -5473867468292852654L;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ReportDTO {
		/**
		 * 报表名称
		 */
		private String reportName;
		/**
		 * 参数id
		 */
		private String prtid;
		/**
		 * 单号
		 */
		private String billNo;
		/**
		 * 参数id
		 */
		private String id;
		/**
		 * 导出操作人
		 */
		private String operator;
	}


}
