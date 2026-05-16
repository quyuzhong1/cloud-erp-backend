package com.common.business.mask.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskPermissionEvaluator;
import com.common.business.mask.MaskScan;
import com.common.business.mask.MaskStrategy;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏引擎核心：递归扫描容器、按字段分发到 {@link MaskHandler}
 *
 * <p>容器解包覆盖 {@link ApiResult} / {@link IPage} / {@link PagingVO} /
 * {@link Collection} / {@link Map} / 数组 / 嵌套对象，与 {@code DictCore} 完全一致。</p>
 *
 * <p>性能要点：</p>
 * <ul>
 *   <li>类元数据由 {@link MaskClassDescriptorRegistry} 提供，命中 {@link MaskClassDescriptor#NO_MASK} 直接 O(1) 返回</li>
 *   <li>字段反射 {@code Field.setAccessible(true)} 已在元数据扫描时设置，热路径无开销</li>
 *   <li>用 {@link IdentityHashMap} 跟踪已处理对象，避免循环引用导致栈溢出</li>
 *   <li>权限豁免在切面层早退；本类总是按全脱执行，不感知权限</li>
 * </ul>
 *
 * @author cloud-erp
 */
@Slf4j
@Component
public class MaskCore {

    @Autowired(required = false)
    private List<MaskHandler> handlerBeans;

    @Autowired(required = false)
    private MaskClassDescriptorRegistry registry;

    /**
     * 字段脱敏豁免扩展点。无 Bean 时本字段为 null，{@link #isFieldGranted} 完全等同
     * 「仅按权限码判定」的原有行为；有 Bean 时多走一次 OR 判断
     */
    @Autowired(required = false)
    private List<MaskPermissionEvaluator> permissionEvaluators;

    /**
     * MaskStrategy -> MaskHandler 调度表（启动时一次性构建）
     */
    private volatile Map<MaskStrategy, MaskHandler> dispatch;

    /**
     * 入口：处理 Controller 返回值。原对象就地修改（与 DictCore 一致）
     *
     * <p>性能优化：</p>
     * <ul>
     *   <li>{@code user} 从 {@link UserContext} 拿一次后传递，避免 {@link #walkPojo} 对每个 POJO 重复取 ThreadLocal；</li>
     *   <li><b>{@code permissionList} 转 {@link HashSet} 一次</b>：{@link LoginUser#getPermissionList()} 是
     *       {@link List}，原 {@code contains} 是 O(P) 线性扫；P=200 + 1000 条 × 5 mask 字段 = 100w 次字符串
     *       比较。Set 化后 O(1)，本路径整体提速 5-10x；</li>
     *   <li>{@link IdentityHashMap} 初始容量 64，覆盖大多数列表型返回的对象树规模，减少 resize。</li>
     * </ul>
     */
    public void process(Object returnValue) {
        if (returnValue == null || registry == null) {
            return;
        }
        ensureDispatch();
        try {
            LoginUser user = UserContext.getLoginUser();
            Set<String> permissionSet = toPermissionSet(user);
            walk(returnValue, user, permissionSet, new IdentityHashMap<>(64));
        } catch (Throwable e) {
            log.warn("MaskCore process failed, return value will be returned without mask, msg={}", e.getMessage());
        }
    }

    /**
     * 把 {@link LoginUser#getPermissionList()} 转换成 {@link HashSet}，供 walk 路径 O(1) contains。
     * user 或 permissionList 为空时返回 {@link Collections#emptySet()}。
     */
    private static Set<String> toPermissionSet(LoginUser user) {
        if (user == null || user.getPermissionList() == null || user.getPermissionList().isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(user.getPermissionList());
    }

    private void ensureDispatch() {
        if (dispatch != null) {
            return;
        }
        synchronized (this) {
            if (dispatch != null) {
                return;
            }
            EnumMap<MaskStrategy, MaskHandler> map = new EnumMap<>(MaskStrategy.class);
            if (handlerBeans != null) {
                for (MaskHandler h : handlerBeans) {
                    if (h.strategy() != null) {
                        map.put(h.strategy(), h);
                    }
                }
            }
            dispatch = map;
        }
    }

    /**
     * 递归遍历入口：根据对象类型解包到具体的元素，再走 {@link #walkPojo} 处理 POJO
     */
    private void walk(Object obj, LoginUser user, Set<String> permissionSet,
                      IdentityHashMap<Object, Boolean> seen) {
        if (obj == null) {
            return;
        }
        if (seen.put(obj, Boolean.TRUE) != null) {
            return;
        }
        if (obj instanceof ApiResult) {
            walk(((ApiResult<?>) obj).getData(), user, permissionSet, seen);
            return;
        }
        if (obj instanceof PagingVO) {
            List<?> list = ((PagingVO<?>) obj).getList();
            if (list != null) {
                for (Object e : list) {
                    walk(e, user, permissionSet, seen);
                }
            }
            return;
        }
        if (obj instanceof IPage) {
            List<?> records = ((IPage<?>) obj).getRecords();
            if (records != null) {
                for (Object e : records) {
                    walk(e, user, permissionSet, seen);
                }
            }
            return;
        }
        if (obj instanceof Collection) {
            for (Object e : (Collection<?>) obj) {
                walk(e, user, permissionSet, seen);
            }
            return;
        }
        if (obj instanceof Map) {
            for (Object v : ((Map<?, ?>) obj).values()) {
                walk(v, user, permissionSet, seen);
            }
            return;
        }
        if (obj.getClass().isArray()) {
            Class<?> comp = obj.getClass().getComponentType();
            if (!comp.isPrimitive()) {
                Object[] arr = (Object[]) obj;
                for (Object e : arr) {
                    walk(e, user, permissionSet, seen);
                }
            }
            return;
        }
        walkPojo(obj, user, permissionSet, seen);
    }

    /**
     * 处理一个 POJO：拿元数据 → 遍历每个 MaskFieldDescriptor → 脱敏或递归
     */
    private void walkPojo(Object pojo, LoginUser user, Set<String> permissionSet,
                          IdentityHashMap<Object, Boolean> seen) {
        Class<?> clazz = pojo.getClass();
        MaskClassDescriptor descriptor = registry.of(clazz);
        if (descriptor.isEmpty()) {
            return;
        }
        for (MaskFieldDescriptor fd : descriptor.getFields()) {
            Field field = fd.getField();
            Object value;
            try {
                value = field.get(pojo);
            } catch (IllegalAccessException e) {
                continue;
            }

            // 字段级豁免：①权限码命中 ②MaskPermissionEvaluator 扩展点命中（任一即看明文）
            // 容器字段（无策略）仍继续递归
            boolean fieldGranted = fd.hasStrategy() && isFieldGranted(user, permissionSet, pojo, fd);

            if (fd.hasStrategy() && !fieldGranted) {
                Object newValue = applyStrategy(pojo, fd, value);
                // hideWhenMasked=true 表示"不可见"语义：脱敏后再置 null 整字段不返回
                if (fd.isHideWhenMasked()) {
                    newValue = null;
                }
                if (newValue != value) {
                    try {
                        field.set(pojo, newValue);
                    } catch (Exception e) {
                        log.debug("MaskCore set field failed, class={}, field={}, msg={}",
                                clazz.getName(), field.getName(), e.getMessage());
                    }
                }
                value = newValue;
            }

            if (fd.isRecursive() && fd.isContainer() && value != null) {
                walk(value, user, permissionSet, seen);
            }
        }
    }

    /**
     * 字段豁免链：权限码命中 → 直接通过；否则交给 {@link MaskPermissionEvaluator} 扩展点判断。
     *
     * <p>无 evaluator Bean 时退化为原有"仅看权限码"行为；多个 evaluator 中任一返回 true 即豁免。
     * evaluator 抛异常被吞掉并按"未豁免"处理，不影响响应链路。</p>
     */
    private boolean isFieldGranted(LoginUser user, Set<String> permissionSet,
                                   Object pojo, MaskFieldDescriptor fd) {
        if (hasFieldPermission(permissionSet, fd.getPermission())) {
            return true;
        }
        if (permissionEvaluators == null || permissionEvaluators.isEmpty()) {
            return false;
        }
        for (MaskPermissionEvaluator evaluator : permissionEvaluators) {
            try {
                if (evaluator.shouldShowPlain(user, pojo, fd)) {
                    return true;
                }
            } catch (Throwable e) {
                log.debug("MaskPermissionEvaluator threw, treat as not bypassed, evaluator={}, msg={}",
                        evaluator.getClass().getName(), e.getMessage());
            }
        }
        return false;
    }

    /**
     * 用预先转好的 {@link Set} 做 O(1) contains，替代 {@code List.contains} 的 O(P) 线性扫
     */
    private static boolean hasFieldPermission(Set<String> permissionSet, String permission) {
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        return !permissionSet.isEmpty() && permissionSet.contains(permission);
    }

    private Object applyStrategy(Object owner, MaskFieldDescriptor fd, Object value) {
        if (value == null && fd.isKeepEmpty()) {
            return null;
        }
        MaskHandler handler = dispatch.get(fd.getStrategy());
        if (handler == null) {
            log.warn("MaskCore no handler for strategy={}, field={}, classPath={}",
                    fd.getStrategy(), fd.getField().getName(), owner.getClass().getName());
            return value;
        }
        MaskContext ctx = MaskContext.builder()
                .owner(owner)
                .fieldName(fd.getField().getName())
                .classPath(owner.getClass().getName())
                .regex(fd.getRegex())
                .replacement(fd.getReplacement())
                .keepEmpty(fd.isKeepEmpty())
                .build();
        return handler.handle(value, ctx);
    }

    /**
     * 决定本次请求是否完全跳过脱敏（在切面层调用，避免引擎做无效遍历）
     *
     * <p>跳过条件（任一命中即跳过）：</p>
     * <ul>
     *   <li>方法 @MaskScan(disabled=true)</li>
     *   <li>当前用户为超级管理员（{@code LoginUser.isSupper == true}）</li>
     *   <li>方法 @MaskScan(permission) 不为空 且 当前用户拥有该权限码</li>
     * </ul>
     */
    public boolean shouldSkip(MaskScan scan) {
        if (scan != null && scan.disabled()) {
            return true;
        }
        LoginUser user = UserContext.getLoginUser();
        if (user == null) {
            return false;
        }
        if (Boolean.TRUE.equals(user.getIsSupper())) {
            return true;
        }
        if (scan != null) {
            String permission = scan.permission();
            if (permission != null && !permission.isEmpty()
                    && user.getPermissionList() != null
                    && user.getPermissionList().contains(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 仅用于单元测试：跳过 walk 直接拿到调度表
     */
    Map<MaskStrategy, MaskHandler> dispatchForTest() {
        ensureDispatch();
        return dispatch;
    }

    /**
     * 仅用于单元测试：暴露 walkPojo
     */
    void walkPojoForTest(Object pojo) {
        ensureDispatch();
        LoginUser user = UserContext.getLoginUser();
        walkPojo(pojo, user, toPermissionSet(user), new IdentityHashMap<>());
    }

    /**
     * 仅用于单元测试：判断字段是否为 transient/static/final
     */
    static boolean isSkippable(Field field) {
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod);
    }
}
