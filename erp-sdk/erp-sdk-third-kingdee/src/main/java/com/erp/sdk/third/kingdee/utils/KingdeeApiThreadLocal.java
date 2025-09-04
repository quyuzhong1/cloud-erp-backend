package com.erp.sdk.third.kingdee.utils;

import java.util.ArrayDeque;
import java.util.Deque;

import cn.hutool.core.lang.Pair;

public class KingdeeApiThreadLocal {
    private KingdeeApiThreadLocal() {
    	
    }
    private static final ThreadLocal<Deque<Pair<Boolean, KingdeeApiUtils>>> STACK = ThreadLocal.withInitial(ArrayDeque::new);

    public static KingdeeApiUtils get() {
        Deque<Pair<Boolean, KingdeeApiUtils>> stack = STACK.get();
        if (!stack.isEmpty()) {
            return stack.peek().getValue();
        }
        return null;
    }

    public static void set(KingdeePushModuleEnum kingdeePushModuleEnum) {
        boolean newFormId = false;
        KingdeeApiUtils kingdeeApiUtils = get();
        if (kingdeeApiUtils == null || !kingdeePushModuleEnum.getCode().equals(kingdeeApiUtils.getFormId())) {
            newFormId = true;
            kingdeeApiUtils = KingdeeApiUtilsPool.getKingdeeApiUtils(kingdeePushModuleEnum.getCode());
        }
        STACK.get().push(Pair.of(newFormId, kingdeeApiUtils));
    }

    public static void clear() {
        Deque<Pair<Boolean, KingdeeApiUtils>> stack = STACK.get();
        if (!stack.isEmpty()) {
            Pair<Boolean, KingdeeApiUtils> top = stack.pop();
            if (top.getKey()) {
                KingdeeApiUtilsPool.returnKingdeeApiUtils(top.getValue());
            }
            if (stack.isEmpty()) {
            	STACK.remove();
            }
        }
    }
}