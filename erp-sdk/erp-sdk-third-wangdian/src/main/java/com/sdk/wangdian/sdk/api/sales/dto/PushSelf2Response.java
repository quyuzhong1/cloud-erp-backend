package com.sdk.wangdian.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;
import com.sdk.wangdian.sdk.api.ErrorList;
import com.sdk.wangdian.sdk.api.Result;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
public class PushSelf2Response {

	@SerializedName("chg_count")
	private Integer chgCount;
	@SerializedName("new_count")
	private Integer newCount;
	@SerializedName("error_list")
	private List<Error> errorList;

	/**
	 *
	 *
	 *
	 * */
	@Getter
	@Setter
	public static class Error{
		@SerializedName("error")
		private String error;
		@SerializedName("no")
		private String no;
	}

}
