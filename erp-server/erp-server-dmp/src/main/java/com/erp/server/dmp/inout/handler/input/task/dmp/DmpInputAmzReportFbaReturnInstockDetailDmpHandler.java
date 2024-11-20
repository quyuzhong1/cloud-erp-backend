package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputRowIndexMongoHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 * @author Administrator
 *
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportFbaReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler{

	public static final String THIRD_DETAIL_ID = "thirdDetailId";

	@Override
	protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
		log.debug("DmpInputAmzReportFbaReturnInstockDetailDmpHandler afterConvertData 处理");
		String md5 = dmpInputMongoEntity.getOrDefault(DmpInputRowIndexMongoHandler.ROW_INDEX_UNIQUE_MD5, "").toString();
		dmpInputMongoEntity.put(THIRD_DETAIL_ID, md5);
		return Collections.singletonList(dmpInputMongoEntity);
	}
}
