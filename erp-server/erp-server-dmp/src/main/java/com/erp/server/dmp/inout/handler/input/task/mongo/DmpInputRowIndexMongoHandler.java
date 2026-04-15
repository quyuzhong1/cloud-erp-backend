package com.erp.server.dmp.inout.handler.input.task.mongo;

import com.erp.model.dmp.enums.DmpInputTaskFileContentTypeEnum;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * dmp输入任务mongo转换处理器, 无法判断的重复记录, 根据唯一键组合添加出现索引index号
 * @author Administrator
 *
 */
@Service
@Scope("prototype")
public class DmpInputRowIndexMongoHandler extends DmpInputBaseMongoHandler{

	public static final String INDEX_NUMBER = "indexNumber";

	public static final String UNIQUE_FIELD_SET_INDEX_KEY = "uniqueFieldSetIndexKey";

	public static final String ROW_INDEX_UNIQUE_MD5 = "rowIndexUniqueMd5";
	public static final String STR = "|";

    /**
     * 唯一键配置必须添加indexNumber
     */
	@Override
	protected List<Map<String, Object>> getDataList(DmpInputTaskFileContentTypeEnum contentType , List<String> resultList) {
		List<Map<String, Object>> dataList = super.getDataList(contentType, resultList);

		Map<String, Integer> uniqueFieldSetIndexMap = new HashMap<>();

		for (Map<String, Object> respMap : dataList) {
			StringBuilder uniqueFieldSetIndexKeyBuilder = new StringBuilder();
			uniqueFieldSetIndexKeyBuilder.append(nextLevelId).append(STR);
			for (String uniqueField : uniqueFieldSet) {
                // 原始key移除生成的indexNumber和uniqueFieldSetIndexKey，避免重复添加到唯一键组合中
                // 唯一键配置必须添加indexNumber
                if (INDEX_NUMBER.equalsIgnoreCase(uniqueField)) {
                    continue;
                }
				String value = respMap.getOrDefault(uniqueField, "").toString();
				uniqueFieldSetIndexKeyBuilder
						.append(value)
						.append(STR);
			}
			String uniqueFieldSetIndexKey = uniqueFieldSetIndexKeyBuilder.toString();
			Integer indexNumber = uniqueFieldSetIndexMap.getOrDefault(uniqueFieldSetIndexKey, 0);
			// 当前行号索引
			respMap.put(INDEX_NUMBER, indexNumber);
			respMap.put(UNIQUE_FIELD_SET_INDEX_KEY, uniqueFieldSetIndexKey);

			String rowIndexUniqueMd5 = uniqueFieldSetIndexKeyBuilder.append(indexNumber).toString();
			String upperCaseMd5 = DigestUtils.md5Hex(rowIndexUniqueMd5.getBytes(StandardCharsets.UTF_8)).toLowerCase();
			respMap.put(ROW_INDEX_UNIQUE_MD5, upperCaseMd5);
			// 累计次数
			uniqueFieldSetIndexMap.put(uniqueFieldSetIndexKey, indexNumber + 1);
		}
		return dataList;
	}
}
