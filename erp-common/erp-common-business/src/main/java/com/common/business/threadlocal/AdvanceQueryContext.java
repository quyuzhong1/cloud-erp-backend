package com.common.business.threadlocal;

import com.common.business.dto.AdvanceQueryDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liuruipeng
 * 高级查询上下文
 */
public class AdvanceQueryContext {

    /**
     * 存放调用第三方接口返回JSON
     */
    private static final ThreadLocal<List<AdvanceQueryDTO>> queryList = ThreadLocal.withInitial(ArrayList::new);

    private static final ThreadLocal<String> compareCode = new ThreadLocal<>();

    public static void addQuery(AdvanceQueryDTO dto) {queryList.get().add(dto);}

    public static List<AdvanceQueryDTO> getQueryList() { return queryList.get();}

    public static void setCompareCode(String code) {compareCode.set(code);}

    public static String getCompareCode() {return compareCode.get();}

    public static void remove() {
        queryList.remove();
        compareCode.remove();
    }
}
