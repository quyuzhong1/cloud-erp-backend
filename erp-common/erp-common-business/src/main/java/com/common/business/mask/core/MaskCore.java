package com.common.business.mask.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.common.business.mask.MaskContext;
import com.common.business.mask.MaskHandler;
import com.common.business.mask.MaskPermissionEvaluator;
import com.common.business.mask.MaskPermissionResolver;
import com.common.business.mask.MaskScan;
import com.common.business.mask.MaskStrategy;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;

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
 *   <li>方法级权限作为字段默认权限传入，字段级权限优先覆盖</li>
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
     * 权限解析 SPI。{@code erp-common-business} 默认提供 {@code LoginUserMaskPermissionResolver}
     * （直接读 {@link LoginUser#getPermissionList()}）；业务侧引入 {@code erp-rpc-sys}
     * 时由 {@code FeignMaskPermissionResolver} 通过 {@code @Primary} 覆盖，
     * 自动改为 Feign 现查 + Caffeine 本地缓存 + Pub/Sub 失效。
     *
     * <p>required=false 是为了兼容极端场景（连默认实现都没扫描到）下回退到老逻辑。</p>
     */
    @Autowired(required = false)
    private MaskPermissionResolver permissionResolver;

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
        process(returnValue, "");
    }

    /**
     * 入口：按方法级默认权限处理 Controller 返回值。
     */
    public void process(Object returnValue, String methodPermission) {
        if (returnValue == null) {
            return;
        }
        if (registry == null) {
            throw new ServiceException("响应数据脱敏组件不可用");
        }
        ensureDispatch();
        LoginUser user = UserContext.getLoginUser();
        Set<String> permissionSet = toPermissionSet(user);
        walk(returnValue, user, permissionSet, methodPermission, new IdentityHashMap<>(64));
    }

    /**
     * 解析当前用户的"看明文"权限码集合，供 walk 路径 O(1) contains。
     *
     * <p>优先链：</p>
     * <ol>
     *   <li>{@link #permissionResolver} != null（项目里 99% 走这条）：调 resolver
     *       —— 若 Feign 实现命中本地缓存，开销 < 1μs；未命中则 Feign 现查 sys 一次。</li>
     *   <li>{@code permissionResolver == null}（极端兜底）：退回 {@link LoginUser#getPermissionList()}，
     *       维持本类历史行为，便于单测和老代码迁移。</li>
     * </ol>
     *
     * <p>返回结果保证非空。Resolver 异常时按无权限处理，避免权限解析失败时放行明文。</p>
     */
    private Set<String> toPermissionSet(LoginUser user) {
        if (permissionResolver != null) {
            try {
                Set<String> resolved = permissionResolver.resolve(user);
                return resolved == null ? Collections.emptySet() : resolved;
            } catch (Exception e) {
                log.warn("MaskCore permissionResolver threw, treat as no permission, msg={}", e.getMessage());
                return Collections.emptySet();
            }
        }
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
    private void walk(Object obj, LoginUser user, Set<String> permissionSet, String methodPermission,
                      IdentityHashMap<Object, Boolean> seen) {
        if (obj == null) {
            return;
        }
        if (seen.put(obj, Boolean.TRUE) != null) {
            return;
        }
        if (obj instanceof ApiResult) {
            walk(((ApiResult<?>) obj).getData(), user, permissionSet, methodPermission, seen);
            return;
        }
        if (obj instanceof PagingVO) {
            List<?> list = ((PagingVO<?>) obj).getList();
            if (list != null) {
                for (Object e : list) {
                    walk(e, user, permissionSet, methodPermission, seen);
                }
            }
            return;
        }
        if (obj instanceof IPage) {
            List<?> records = ((IPage<?>) obj).getRecords();
            if (records != null) {
                for (Object e : records) {
                    walk(e, user, permissionSet, methodPermission, seen);
                }
            }
            return;
        }
        if (obj instanceof Collection) {
            for (Object e : (Collection<?>) obj) {
                walk(e, user, permissionSet, methodPermission, seen);
            }
            return;
        }
        if (obj instanceof Map) {
            for (Object v : ((Map<?, ?>) obj).values()) {
                walk(v, user, permissionSet, methodPermission, seen);
            }
            return;
        }
        if (obj.getClass().isArray()) {
            Class<?> comp = obj.getClass().getComponentType();
            if (!comp.isPrimitive()) {
                Object[] arr = (Object[]) obj;
                for (Object e : arr) {
                    walk(e, user, permissionSet, methodPermission, seen);
                }
            }
            return;
        }
        walkPojo(obj, user, permissionSet, methodPermission, seen);
    }

    /**
     * 处理一个 POJO：拿元数据 → 遍历每个 MaskFieldDescriptor → 脱敏或递归
     */
    private void walkPojo(Object pojo, LoginUser user, Set<String> permissionSet, String methodPermission,
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
                throw new ServiceException(e, "响应数据脱敏失败");
            }

            // 字段级豁免：①权限码命中 ②MaskPermissionEvaluator 扩展点命中（任一即看明文）
            // 容器字段（无策略）仍继续递归
            boolean fieldGranted = fd.hasStrategy()
                    && isFieldGranted(user, permissionSet, pojo, fd, methodPermission);

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
                        throw new ServiceException(e, "响应数据脱敏失败");
                    }
                }
                value = newValue;
            }

            if (fd.isRecursive() && fd.isContainer() && value != null) {
                walk(value, user, permissionSet, methodPermission, seen);
            }
        }
    }

    /**
     * 字段豁免链（按优先级短路）：
     * <ol>
     *   <li>超管 → 全字段一律明文。common-business 默认认 {@code LoginUser.isSupper=true}，
     *       业务服务引入 erp-rpc-sys 后由 {@code FeignMaskPermissionResolver} 对齐数据权限的
     *       {@code roleId=1} 口径</li>
     *   <li>当前用户 permissionList 命中 {@code fd.getPermission()} → 通过</li>
     *   <li>{@link MaskPermissionEvaluator} 扩展点任一返回 true → 通过</li>
     * </ol>
     *
     * <p>无 evaluator Bean 时第 3 步跳过；evaluator 抛异常被吞掉并按"未豁免"处理，不影响响应链路。</p>
     */
    private boolean isFieldGranted(LoginUser user, Set<String> permissionSet,
                                   Object pojo, MaskFieldDescriptor fd, String methodPermission) {
        if (isSuperAdmin(user)) {
            return true;
        }
        String effectivePermission = fd.getPermission() == null || fd.getPermission().isEmpty()
                ? methodPermission : fd.getPermission();
        if (hasFieldPermission(permissionSet, effectivePermission)) {
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
            } catch (Exception e) {
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
            throw new ServiceException("响应数据脱敏配置异常");
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
     *   <li>当前用户为超级管理员。common-business 默认认 {@code LoginUser.isSupper == true}，
     *       业务服务引入 erp-rpc-sys 后对齐数据权限的 {@code roleId=1} 口径</li>
     * </ul>
     *
     * <p>方法级 permission 不能在此处整段早退，否则会绕过字段级权限覆盖；
     * 它在字段判权时仅作为未配置字段权限的默认值。</p>
     */
    public boolean shouldSkip(MaskScan scan) {
        if (scan != null && scan.disabled()) {
            return true;
        }
        LoginUser user = UserContext.getLoginUser();
        if (user == null) {
            return false;
        }
        if (isSuperAdmin(user)) {
            return true;
        }
        return false;
    }

    private boolean isSuperAdmin(LoginUser user) {
        if (user == null) {
            return false;
        }
        if (permissionResolver != null) {
            try {
                return permissionResolver.isSuperAdmin(user);
            } catch (Exception e) {
                log.warn("MaskCore permissionResolver.isSuperAdmin threw, fallback to LoginUser.isSupper, msg={}",
                        e.getMessage());
            }
        }
        return Boolean.TRUE.equals(user.getIsSupper());
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
        walkPojo(pojo, user, toPermissionSet(user), "", new IdentityHashMap<>());
    }

    /**
     * 仅用于单元测试：判断字段是否为 transient/static/final
     */
    static boolean isSkippable(Field field) {
        int mod = field.getModifiers();
        return Modifier.isStatic(mod) || Modifier.isFinal(mod) || Modifier.isTransient(mod);
    }
}
