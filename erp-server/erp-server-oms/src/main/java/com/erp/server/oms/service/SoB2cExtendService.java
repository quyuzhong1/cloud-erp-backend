package com.erp.server.oms.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.PlatformOrderDTO;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cExtendEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cExtendDTO;

import java.util.List;

/**
 * <p>
 * 销售订单-tiktok全托管属性表 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-03-24
 */
public interface SoB2cExtendService extends SuperService<SoB2cExtendEntity> {

    /**
     * 新增
     *
     * @param dto
     * @param soB2cEntity
     * @return
     * @author zdy
     * @date: 2025-03-24
     */
    BaseResultDTO.AddDTO add(SoB2cExtendDTO.AddDTO dto, SoB2cEntity soB2cEntity);

    /**
    * 修改
    * @author zdy
    * @date: 2025-03-24
    * @param dto
    * @return
    */
    Boolean update(SoB2cExtendDTO.UpdateDTO dto,SoB2cEntity soB2cEntity);

    /**
     * 获取无军区的订单
     * @param query
     * @return
     */
    IPage<SoB2cExtendEntity> pagePartitionIsNull(Page query);

    /**
     * 根据上级id获取扩展记录
     * @param id
     * @return
     */
    SoB2cExtendEntity getByMainId(String id);

    /**
     * 订单超时警告查询
     * @param offsetMinutes 单位：分钟 获取当前时间和预警时间区间内的销售订单列表
     * @return
     */
    List<FullyManagedDTO.WarningDTO> fullyManagedOrderMsgWarning(Integer offsetMinutes);

    void saveOrUpdateEntity(PlatformOrderDTO dto, SoB2cEntity mainEntity);

    List<SoB2cExtendEntity> listByMainIds(List<String> ids);
}
