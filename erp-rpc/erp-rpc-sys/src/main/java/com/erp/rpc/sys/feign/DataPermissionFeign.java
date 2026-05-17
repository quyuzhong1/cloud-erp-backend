package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.dto.DataPermissionContextDTO;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 数据权限聚合 Feign：一次 RPC 拿到 {@link DataPermissionContextDTO} 所需的全部 5 类数据
 *
 * <p>用于替代 {@code DataPermissionAspect} 历史上每请求散打 5 次 sys Feign 的实现，
 * 配合业务侧 60s 本地 TTL 缓存（{@code FeignDataPermissionContextResolver}），
 * 把对 sys 的 QPS 降到原来 1% 以内。</p>
 *
 * <p>独立成新 Feign（不塞进 {@link SysUserFeign} / {@link AuthDataFeign}）的原因：</p>
 * <ul>
 *   <li>聚合端点是<b>新增能力</b>，老 5 个端点继续保留向后兼容，不动它们的语义和路径</li>
 *   <li>contextId 隔离，避免和 SysUserFeign / AuthDataFeign 的 ribbon / circuit-breaker 配置互相干扰</li>
 *   <li>方便后续单独加 hystrix fallback / 单独打点</li>
 * </ul>
 *
 * @author cloud-erp
 */
@FeignClient(name = "erp-sys", contextId = "dataPermissionFeign", configuration = {FeignErrorDecoder.class})
public interface DataPermissionFeign {

    /**
     * 一次返回数据权限解析所需的全部上下文（5 类聚合）
     *
     * <p>sys 侧实现见 {@code DataPermissionFeignController#getDataPermissionContext}，
     * 串行调用现有 5 个内部 service 后组装返回，不破坏现有 service 的边界。</p>
     *
     * @param userId 用户 id，必填
     * @return 聚合上下文；任一子项为空时仍返回非 null 的空 List，便于上游免判空
     */
    @PostMapping("feign/dataPermission/getDataPermissionContext")
    DataPermissionContextDTO getDataPermissionContext(@RequestBody String userId);
}
