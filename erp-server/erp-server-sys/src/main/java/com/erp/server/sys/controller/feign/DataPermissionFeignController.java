package com.erp.server.sys.controller.feign;

import com.common.business.dto.UserRequestPermissionsDTO;
import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.DataPermissionContextDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.server.sys.service.AuthUserShopService;
import com.erp.server.sys.service.AuthUserWarehouseService;
import com.erp.server.sys.service.SysRoleUserService;
import com.erp.server.sys.service.SysUserInfoService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据权限聚合 Feign 端点
 *
 * <p>串行调用 sys 内部 4 个 service，组装出 {@link DataPermissionContextDTO}，
 * 替代 {@code DataPermissionAspect} 历史上散打 5 次 Feign 的实现。</p>
 *
 * <p>不引入并发：</p>
 * <ul>
 *   <li>4 次 DB 查询单 Pod 内串行总耗时通常 &lt; 30ms，已经远小于一次跨服务 Feign 来回</li>
 *   <li>并发要引线程池，调度成本 + 上下文切换 + 异常合并都额外引入风险，得不偿失</li>
 *   <li>真正的收益来自业务侧 60s 本地缓存命中（{@code FeignDataPermissionContextResolver}），首次未命中时多 15ms 完全可接受</li>
 * </ul>
 *
 * <p>各子项查询失败时<b>不抛异常</b>，置成空集合返回，让业务侧 aspect 按"无该维度数据权限"处理，
 * 避免因为某次 sys DB 抖动直接 5xx 导致整个请求挂掉。</p>
 *
 * @author cloud-erp
 */
@Slf4j
@RestController
@RequestMapping("feign/dataPermission")
public class DataPermissionFeignController extends BaseController {

    @Resource
    private SysUserInfoService sysUserInfoService;

    @Resource
    private SysRoleUserService sysRoleUserService;

    @Resource
    private AuthUserShopService authUserShopService;

    @Resource
    private AuthUserWarehouseService authUserWarehouseService;

    @PostMapping("/getDataPermissionContext")
    public DataPermissionContextDTO getDataPermissionContext(@RequestBody String userId) {
        if (StringUtils.isBlank(userId)) {
            return DataPermissionContextDTO.empty();
        }
        long start = System.currentTimeMillis();

        List<UserRequestPermissionsDTO> permissionsList = safelyList(
                () -> sysUserInfoService.getRequestPermissionsList(userId), "permissions", userId);
        List<String> roleIdList = safelyList(
                () -> sysRoleUserService.findRoleIdsByUid(userId), "roleId", userId);
        List<String> depUserList = safelyList(
                () -> sysUserInfoService.getDepUserList(userId), "depUser", userId);
        List<SysUserDTO.ShopDTO> shopUserList = safelyList(
                () -> authUserShopService.getShopUserList(userId), "shop", userId);
        List<SysUserDTO.WarehouseDTO> warehouseUserList = safelyList(
                () -> authUserWarehouseService.getWarehouseUserList(userId), "warehouse", userId);

        DataPermissionContextDTO ctx = DataPermissionContextDTO.builder()
                .permissionsList(permissionsList)
                .roleIdList(roleIdList)
                .depUserList(depUserList)
                .shopUserList(shopUserList)
                .warehouseUserList(warehouseUserList)
                .build();

        long cost = System.currentTimeMillis() - start;
        if (cost > 200) {
            log.warn("DataPermissionFeign getDataPermissionContext slow, uid={}, costMs={}, "
                            + "permSize={}, roleSize={}, depUserSize={}, shopSize={}, warehouseSize={}",
                    userId, cost, permissionsList.size(), roleIdList.size(), depUserList.size(),
                    shopUserList.size(), warehouseUserList.size());
        }
        return ctx;
    }

    /**
     * 子项查询的统一容错包装：任何异常都吞掉，记 warn 并返回空集合。
     *
     * <p>设计意图：单个维度的 DB 异常不应该让整个聚合调用挂掉。最坏情况是该维度按"无权限"
     * 处理（业务上看不到对应数据），比 5xx 中断业务请求好得多。</p>
     */
    private <T> List<T> safelyList(SafeSupplier<List<T>> supplier, String dimension, String userId) {
        try {
            List<T> result = supplier.get();
            return result == null ? Collections.emptyList() : result;
        } catch (Throwable e) {
            log.warn("DataPermissionFeign sub-query failed, dimension={}, uid={}, msg={}",
                    dimension, userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    @FunctionalInterface
    private interface SafeSupplier<T> {
        T get() throws Exception;
    }
}
