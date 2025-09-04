package com.erp.sdk.third.kingdee.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class KingdeeApiThreadLocal {
    private KingdeeApiThreadLocal() {
    	
    }
    private static final ThreadLocal<Deque<KingdeeApiUtils>> SUSPENDED =
            ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<KingdeeApiUtils> CURRENT = new ThreadLocal<>();
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    public static KingdeeApiUtils get() {
        return CURRENT.get();
    }

    public static void set(KingdeeApiUtils apiUtils) {
        CURRENT.set(apiUtils);
    }

    public static void suspendAndReplace(KingdeeApiUtils newApiUtils) {
        KingdeeApiUtils current = CURRENT.get();
        if (current != null) {
            SUSPENDED.get().push(current);
        }
        CURRENT.set(newApiUtils);
    }

    public static void resumePrevious() {
        KingdeeApiUtils old = CURRENT.get();
        if (old != null) {
            KingdeeApiUtilsPool.returnKingdeeApiUtils(old);
        }
        Deque<KingdeeApiUtils> stack = SUSPENDED.get();
        if (!stack.isEmpty()) {
            CURRENT.set(stack.pop());
        } else {
            CURRENT.remove();
        }
    }

    public static void enter() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void exit() {
        int d = DEPTH.get() - 1;
        DEPTH.set(d);
        if (d == 0) {
            clear();
        }
    }

    private static void clear() {
        CURRENT.remove();
        SUSPENDED.remove();
        DEPTH.remove();
    }
    
}