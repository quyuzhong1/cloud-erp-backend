package com.sdk.wangdian.sdk.api.setting.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;

@Data
public class ShopQueryResponse
{
	@SerializedName("total_count")
	private Integer total;
	@SerializedName("details")
	private List<ShopDto> shopDtoList ;

	@Data
	public static class ShopDto
	{
		private Short shopId ;
		private String shopName ;
		private String shopNo ;
		private Short platformId ;
		private Byte subPlatformId ;
		private String contact ;
		private String province ;
		private String city ;
		private String district ;
		private String address ;
		private String telno ;
		private String mobile ;
		private String zip ;
		private String email ;
		private String remark ;
		private String prop1 ;
		private String prop2 ;
		private String website ;
		@SerializedName("is_disabled")
		private Boolean disabled ;
		private String groupId ;
		private Byte authState ;
		private String authTime ;
		private String reExpireTime ;
		private String modified ;
		private String expireTime ;
		private String created ;
	}

}
