package com.erp.server.sys.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.mask.cache.CfgMaskFieldFullCacheDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.CfgMaskFieldDTO;
import com.erp.model.sys.entity.CfgMaskFieldEntity;

/**
 * 字段脱敏配置 服务接口
 *
 * @author cloud-erp
 */
public interface CfgMaskFieldService extends SuperService<CfgMaskFieldEntity> {

    /**
     * 分页查询
     */
    PagingVO<CfgMaskFieldDTO.ListDTO> paging(PagingDTO<CfgMaskFieldDTO.SearchParamDTO> dto);

    /**
     * 新增；提交后自动延迟双删 Redis 缓存
     */
    Boolean add(CfgMaskFieldDTO.AddDTO dto);

    /**
     * 修改；提交后自动延迟双删 Redis 缓存
     */
    Boolean update(CfgMaskFieldDTO.UpdateDTO dto);

    /**
     * 批量删除（逻辑删除）；提交后自动延迟双删 Redis 缓存
     */
    Boolean delete(BaseIdsDTO.IdsDTO dto);

    /**
     * 拉取全量未禁用配置（Feign 暴露给业务节点 Redis miss 兜底）
     */
    CfgMaskFieldFullCacheDTO listAllForCache();

    /**
     * 强制刷新：按当前表数据重建 FullCache 并写入 Redis
     */
    Boolean publishFullCache();
}
