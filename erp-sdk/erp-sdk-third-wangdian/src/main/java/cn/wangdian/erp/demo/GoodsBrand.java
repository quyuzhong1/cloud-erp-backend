package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.goods.GoodsBrandApi;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBrandSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsBrandSearchResponse;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class GoodsBrand
{
	public static void main(String[] args) throws WdtErpException
	{
		Client client = DefaultClient.get("wdterp30", "http://127.0.0.1:30000/", "xyx_api",
				"e8866c1681dacc9488a19c89991791b8:8f5800dab84b7a8d614e92cba739032c");
		GoodsBrandApi goodsBrandApi = ApiFactory.get(client, GoodsBrandApi.class);
		queryGoodsBrand(goodsBrandApi);
	}

	private static void queryGoodsBrand(GoodsBrandApi api) throws WdtErpException
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		GoodsBrandSearchRequest request = new GoodsBrandSearchRequest();

		Date now = new Date();
		request.setEndTime(DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss"));
		request.setStartTime(DateFormatUtils.format(DateUtils.addDays(now, -30), "yyyy-MM-dd HH:mm:ss"));

		GoodsBrandSearchResponse response = api.search(request, pager);

		Integer total = response.getTotal();
		if (null == total || pageSize >= total)
		{
			System.out.println("处理数据");
			return;
		}

		int totalPage = (total % pageSize == 0 ? total / pageSize : total / pageSize + 1) - 1;
		pager.setCalcTotal(false); //后续翻页不需要计算总条数, 可以大大减少请求时间
		for (int i = totalPage; i >= 0; i--)// 从后向前翻页
		{
			pager.setPageNo(i);
			System.out.print("pager: page_size:" + pageSize + "  page_no: " + i + "  ");
			response = api.search(request, pager);
			System.out.println("处理数据");
		}
	}
}
