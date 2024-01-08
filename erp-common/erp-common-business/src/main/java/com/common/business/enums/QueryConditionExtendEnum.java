package com.common.business.enums;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@ToString
public enum QueryConditionExtendEnum {

    GT("scm:purchaseOrder:paging","pod.arrival_status", "case when subString(#{params.sourceCode},1,2) != 'PL' then po.source_code ILIKE concat('%',#{params.sourceCode}::text,'%')\n" +
            "else EXISTS ( select pa.id from purchase_application pa left join purchase_application_ref_po parp on parp.is_deleted = false and pa.id = parp.purchase_application_id\n" +
            " where parp.purchase_order_id = po.id and pa.code ILIKE concat('%',#{params.sourceCode}::text,'%')) end"),
    ;


    private final String code;

    private final String field;

    private final String sql;

    QueryConditionExtendEnum(String code, String field, String sql) {
        this.code = code;
        this.field = field;
        this.sql = sql;
    }

}
