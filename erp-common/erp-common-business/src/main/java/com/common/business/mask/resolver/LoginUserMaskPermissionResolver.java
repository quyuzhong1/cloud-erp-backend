package com.common.business.mask.resolver;

import com.common.business.mask.MaskPermissionResolver;
import com.common.business.vo.LoginUser;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 默认权限解析实现：直接读 {@link LoginUser#getPermissionList()}
 *
 * <p>仅在以下场景"刚好"能工作：</p>
 * <ul>
 *   <li>单元测试中手动 {@code user.setPermissionList(List.of("xxx"))}</li>
 *   <li>网关本身（{@code LoginUser} 还没被 {@code simpleLoginUser} 压缩前）</li>
 *   <li>业务服务自定义 Filter 把 permissionList 重新塞回 LoginUser 的场景</li>
 * </ul>
 *
 * <p>项目里业务服务下游通常 {@code permissionList=null}，此时本实现返回空集合
 * → MaskCore 无法豁免任何字段 → 所有用户看脱敏。
 * <b>业务服务请引入 {@code erp-rpc-sys}，由 {@code FeignMaskPermissionResolver}
 * 自动覆盖本实现</b>。</p>
 *
 * @author cloud-erp
 */
@Component
public class LoginUserMaskPermissionResolver implements MaskPermissionResolver {

    @Override
    public Set<String> resolve(LoginUser user) {
        if (user == null) {
            return Collections.emptySet();
        }
        List<String> list = user.getPermissionList();
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(list);
    }
}
