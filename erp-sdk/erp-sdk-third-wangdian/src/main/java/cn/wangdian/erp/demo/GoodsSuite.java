package cn.wangdian.erp.demo;

import cn.wangdian.erp.sdk.Client;
import cn.wangdian.erp.sdk.Pager;
import cn.wangdian.erp.sdk.WdtErpException;
import cn.wangdian.erp.sdk.api.goods.GoodsSuiteAPI;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteSearchRequest;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteSearchResponse;
import cn.wangdian.erp.sdk.api.goods.dto.GoodsSuiteUploadRequest;
import cn.wangdian.erp.sdk.impl.ApiFactory;
import cn.wangdian.erp.sdk.impl.DefaultClient;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

public class GoodsSuite
{
	public static void main(String[] args) throws WdtErpException
	{
		// Client client = DefaultClient.get("wdtapi3", "test", "test");
		Client client = DefaultClient.get("wdterp30", "http://192.168.1.41:30000/", "POS",
				"c1fbdd70132de9300d23a05a5f63e150:cc169a95acdaa9ffaed4e9d59f93efaa");
		GoodsSuiteAPI suiteApi = ApiFactory.get(client, GoodsSuiteAPI.class);

//		GoodsSuiteUploadTest(suiteApi);
		GoodsSuiteSearchTest(suiteApi);
	}

	private static void GoodsSuiteUploadTest(GoodsSuiteAPI suiteApi)
	{
		String randomSuffix = RandomStringUtils.randomAlphanumeric(4);

		GoodsSuiteUploadRequest.Suite suite = new GoodsSuiteUploadRequest.Suite();
		suite.setSuiteNo("suite_" + randomSuffix);
		suite.setSuiteName("sname_" + randomSuffix);
		suite.setRetailPrice(new BigDecimal("20"));

		GoodsSuiteUploadRequest.Detail detail1 = new GoodsSuiteUploadRequest.Detail();
		detail1.setSpecNo("AM02T038W031");
		detail1.setNum(BigDecimal.ONE);
		detail1.setPrice(BigDecimal.ONE);
		detail1.setRatio(BigDecimal.ONE);//被忽略
		detail1.setFixedPrice(true);

		GoodsSuiteUploadRequest.Detail detail2 = new GoodsSuiteUploadRequest.Detail();
		detail2.setSpecNo("AM02T046W042");
		detail2.setNum(new BigDecimal("2"));
		detail2.setPrice(new BigDecimal(-1));//自动价格?
		detail2.setFixedPrice(true);

		GoodsSuiteUploadRequest.Detail detail3 = new GoodsSuiteUploadRequest.Detail();
		detail3.setSpecNo("AM02L499Z011");
		detail3.setNum(new BigDecimal("2"));
		detail3.setPrice(BigDecimal.ZERO);//价格为0
		detail3.setFixedPrice(true);

		// 浮动
		GoodsSuiteUploadRequest.Detail detail4 = new GoodsSuiteUploadRequest.Detail();
		detail4.setSpecNo("AM02L499Z012");
		detail4.setNum(new BigDecimal("2"));
		detail4.setPrice(BigDecimal.ZERO);
		detail4.setFixedPrice(false);
		detail4.setRatio(new BigDecimal("-1"));

		GoodsSuiteUploadRequest.Detail detail5 = new GoodsSuiteUploadRequest.Detail();
		detail5.setSpecNo("AM02T038W032");
		detail5.setNum(new BigDecimal("2"));
		detail5.setPrice(BigDecimal.ZERO);//价格为0
		detail5.setFixedPrice(false);
		detail5.setRatio(BigDecimal.ZERO);

		GoodsSuiteUploadRequest.Detail detail6 = new GoodsSuiteUploadRequest.Detail();
		detail6.setSpecNo("AM02T046W041");
		detail6.setNum(new BigDecimal("2"));
		detail6.setPrice(BigDecimal.ZERO);//价格为0
		detail6.setFixedPrice(false);
		detail6.setRatio(new BigDecimal("-1"));

		List<GoodsSuiteUploadRequest.Detail> detailList = Arrays
				.asList(detail1, detail2, detail3, detail4, detail5, detail6);

		try
		{
			Integer response = suiteApi.upload(suite, detailList);
		}
		catch (WdtErpException e)
		{
			e.printStackTrace();
		}
	}

	private static void GoodsSuiteSearchTest(GoodsSuiteAPI api) throws WdtErpException
	{
		int pageSize = 5;
		Pager pager = new Pager(pageSize, 0, true);
		GoodsSuiteSearchRequest request = new GoodsSuiteSearchRequest();

		Date now = new Date();
		request.setEndTime(DateFormatUtils.format(now, "yyyy-MM-dd HH:mm:ss"));
		request.setStartTime(DateFormatUtils.format(DateUtils.addDays(now, -30), "yyyy-MM-dd HH:mm:ss"));

		GoodsSuiteSearchResponse response = api.search(request, pager);

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
