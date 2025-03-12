package com.erp.server.oms.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WdtTikTokOrderProd {
	public static void main(String[] args) {
		Date startTime = DateUtil.parse("2024-10-01 00:00:00");
		String logFilePath = "TestAmazonOrderProd.log"; // 日志文件路径
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) { // true 表示追加模式
			while (startTime.before(new Date())) {
				HttpRequest createPost = HttpUtil.createPost("https://erp.ulanzi.cn:9000/api/dmp/dmpInout/doOutputTask");
				Map<String, String> headers = new HashMap<>();
				headers.put("Authorization", "eyJhbGciOiJIUzI1NiJ9.eyJ1aWQiOiIxNjUwMDQ3NTQ1OTA2MzAyOTc4IiwidXNlcl9rZXkiOiIxNjUwMDQ3NTQ1OTA2MzAyOTc4IiwidXNlck5hbWUiOiLnvZfnu7TliJoiLCJpYXQiOjE3NDA1NDIxNTMsImV4cCI6MTc0MTE0Njk1M30.AtXN-3UPoZyPAFwswCMSyPfcBw9_l38aAG2AcZPWEoI");
				createPost.addHeaders(headers);
				String body = "{\r\n" +
						"  \"cfgOutputId\": \"1818200664778495831\",\r\n" +
						"  \"queryParams\":[{\r\n" +
						"    \"type\": \"GE\",\r\n" +
						"    \"name\" : \"platform_create_time\",\r\n" +
						"    \"value\" : \"{startTime}\"\r\n" +
						"  },{\r\n" +
						"    \"type\": \"LE\",\r\n" +
						"    \"name\" : \"platform_create_time\",\r\n" +
						"    \"value\" : \"{endTime}\"\r\n" +
						"  },{\r\n" +
						"    \"type\": \"EQ\",\r\n" +
						"    \"name\": \"source_system\",\r\n" +
						"    \"value\": \"TikTok\"\r\n" +
						"  }]\r\n" +
						"}";
				body = body.replace("{startTime}", DateUtil.formatDateTime(startTime));
				startTime = DateUtil.offsetHour(startTime, 12);
				body = body.replace("{endTime}", DateUtil.formatDateTime(startTime));
				createPost.body(body);

				HttpResponse execute = createPost.execute();
				String logEntry;
				if (execute.getStatus() != 200) {
					logEntry = "失败：" + body + "\n";
				} else {
					logEntry = "成功：" + body + "\n";
				}

				// 写入日志文件
				writer.write(logEntry);
				writer.flush();
				System.out.print(logEntry); // 打印到控制台
			}
		} catch (IOException e) {
			System.err.println("日志写入失败：" + e.getMessage());
		}
	}
}
