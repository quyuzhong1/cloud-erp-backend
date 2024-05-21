package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.api.sales.LogisticsSyncAPI;
import cn.wangdian.erp.sdk.api.sales.dto.*;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogisticsSync
{
	public static void main(String[] args)
	{
		Gson gson = new GsonBuilder().create();
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://192.168.1.41:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");

		LogisticsSyncAPI logisticsSyncApi = ApiFactory.get(client, LogisticsSyncAPI.class);
		LogisticsSyncGetResponse response = get(logisticsSyncApi);

		if (response.getTotalCount() <= 0)
		{
			System.out.println("No eligible results!");
			return;
		}

		List<LogisticsSyncUpdateDto> updateRequest = new ArrayList<>();
		for (LogisticsSyncGetResponse.LogisticsSyncGetDto dto : response.getLogisticsSyncGetDtos())
		{
			String oids = dto.getOids();
			if ("!!!!".equals(StringUtils.substring(oids, 0, 4)))
				oids = GetSpecialOids(logisticsSyncApi, dto.getTradeId(), dto.getPlatformId(), dto.getTid(),
						dto.getOids());

			// do something

			if (!dto.getOids().equals(oids))
				updateRequest.add(new LogisticsSyncUpdateDto(dto.getSyncId(), LogisticsSyncUpdateDto.SYNC_RETRY, ""));
		}

		List<LogisticsSyncUpdateDto> request = Arrays.asList(new LogisticsSyncUpdateDto(
				response.getLogisticsSyncGetDtos().get(0).getSyncId(), LogisticsSyncUpdateDto.SYNC_SUCCESS, ""));
		LogisticsSyncUpdateResponse updateResponse = logisticsSyncApi.update(request);

		if (updateResponse.getErrorMessages() == null || updateResponse.getErrorMessages().size() == 0)
			System.out.println("logistics sync success.");
	}

	private static LogisticsSyncGetResponse get(LogisticsSyncAPI logisticsSyncApi)
	{
		LogisticsSyncGetRequest request = new LogisticsSyncGetRequest();
		// request.setTid("tid-D9kO2OooUy");
		request.setOwnPlatform(true);
		request.setLogisticsNo("klj2384920938");
		// request.setPartSync(true);
		// request.setShopNo("test");

		LogisticsSyncGetResponse response = logisticsSyncApi.get(request, new Pager(10, 0, true));
		System.out.println(response.getLogisticsSyncGetDtos().get(0).getLogisticsNo());

		return response;
	}

	private static String GetSpecialOids(LogisticsSyncAPI logisticsSyncApi, Integer tradeId, Byte platformId,
			String tid, String oids)
	{
		LogisticsSyncSpecialOidsGetResponse response = logisticsSyncApi.getSpecialOids(tradeId, platformId, tid, oids);
		System.out.println("special oids:" + response.getOids());
		return response.getOids();
	}
}
