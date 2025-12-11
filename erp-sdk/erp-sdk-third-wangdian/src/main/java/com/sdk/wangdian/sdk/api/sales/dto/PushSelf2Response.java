package com.sdk.wangdian.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;
import com.sdk.wangdian.sdk.api.ErrorList;
import com.sdk.wangdian.sdk.api.Result;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PushSelf2Response {

	@SerializedName("status")
	private Integer status;
	@SerializedName("message")
	private String message;
	@SerializedName("data")
	private ErrorData data;


	/**
	 *
	 *
	 *
	 * */
	@Data
	public static class ErrorData{
		@SerializedName("chg_count")
		private Integer chgCount;
		@SerializedName("new_count")
		private Integer newCount;
		@SerializedName("error_list")
		private List<Error> errorList;
	}

	/**
	 *
	 *
	 *
	 * */
	@Data
	public static class Error{
		@SerializedName("error")
		private String error;
		@SerializedName("no")
		private String no;
	}

}
