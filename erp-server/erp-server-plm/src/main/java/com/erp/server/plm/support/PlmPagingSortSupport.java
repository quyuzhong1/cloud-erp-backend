package com.erp.server.plm.support;

import com.common.business.dto.base.SortParamDTO;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页排序字段白名单校验。
 */
public final class PlmPagingSortSupport {

    private static final Map<String, String> FORBIDDEN_WORD_SORT_FIELDS;

    private static final Map<String, String> FORBIDDEN_WORD_CHECK_SORT_FIELDS;

    static {
        Map<String, String> forbiddenWordFields = new HashMap<>(8);
        forbiddenWordFields.put("forbiddenWord", "cpfw.forbidden_word");
        forbiddenWordFields.put("disabled", "cpfw.disabled");
        forbiddenWordFields.put("createUserName", "cpfw.create_user_name");
        forbiddenWordFields.put("createTime", "cpfw.create_time");
        forbiddenWordFields.put("updateTime", "cpfw.update_time");
        FORBIDDEN_WORD_SORT_FIELDS = Collections.unmodifiableMap(forbiddenWordFields);

        Map<String, String> checkFields = new HashMap<>(8);
        checkFields.put("reportName", "pfwc.report_name");
        checkFields.put("status", "pfwc.status");
        checkFields.put("finishTime", "pfwc.finish_time");
        checkFields.put("totalCount", "pfwc.total_count");
        checkFields.put("hitCount", "pfwc.hit_count");
        checkFields.put("createUserName", "pfwc.create_user_name");
        checkFields.put("createTime", "pfwc.create_time");
        FORBIDDEN_WORD_CHECK_SORT_FIELDS = Collections.unmodifiableMap(checkFields);
    }

    private PlmPagingSortSupport() {
    }

    public static void sanitizeForbiddenWordSort(List<SortParamDTO> sortList) {
        sanitizeSort(sortList, FORBIDDEN_WORD_SORT_FIELDS);
    }

    public static void sanitizeForbiddenWordCheckSort(List<SortParamDTO> sortList) {
        sanitizeSort(sortList, FORBIDDEN_WORD_CHECK_SORT_FIELDS);
    }

    private static void sanitizeSort(List<SortParamDTO> sortList, Map<String, String> allowedFields) {
        if (CollectionUtils.isEmpty(sortList)) {
            return;
        }
        for (SortParamDTO item : sortList) {
            if (item == null || StringUtils.isBlank(item.getField())) {
                throw new ServiceException("非法排序字段");
            }
            String column = allowedFields.get(item.getField());
            if (StringUtils.isBlank(column)) {
                throw new ServiceException("非法排序字段");
            }
            item.setField(column);
        }
    }
}
