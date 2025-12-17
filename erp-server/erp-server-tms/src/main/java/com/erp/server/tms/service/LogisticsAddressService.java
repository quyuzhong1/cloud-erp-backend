package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAddressDTO;
import com.erp.model.tms.enums.LogisticsAddressTypeEnum;

import java.util.List;

/**
 * <p>
 * 物流地址表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsAddressService extends SuperService<LogisticsAddressEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsAddressDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsAddressDTO.UpdateDTO dto);

    /**
     * 获取物流信息
     * @author yl
     * @date 2023-11-03 9:32
     * @param id
     * @return com.erp.model.tms.dto.LogisticsAddressDTO.ViewDTO
     */
    LogisticsAddressDTO.ViewDTO view(String id);

    /**
     * 地址分页
     * @param dto
     * @return
     */
    PagingVO<LogisticsAddressDTO.PagingViewDTO> paging(PagingDTO<LogisticsAddressDTO.PagingParamDTO> dto);

    
    /**
     * 导出
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-11-08 17:39
     */
    Boolean exportExcel(LogisticsAddressDTO.ExportDTO dto);

    /**
     * 删除物流地址
     * @author yl
     * @date 2023-11-08 18:45
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     */
    BatchResultDTO delete(String id);

    /**
     *根据类型获取地址信息
     *@parms type
     *@return 
     *@author yl
     *@date 2023-11-13
     */
    List<LogisticsAddressDTO.ListDTO> listByType(String type);


    /**
     * 根据渠道和 类型获取到地址信息
     *
     * @param type
     * @return
     */
    List<LogisticsAddressEntity> listByTypeAndShopId(LogisticsAddressTypeEnum type, String shopId);

    /**  根据渠道id 和店铺id  获取地址
     * @description
     * @param
     * @author Lambda
     * @return
     * @create 2024-01-04 14:39
     */
    List<LogisticsAddressEntity> listByChannelIdAndShopId(String channelId, String shopId);

    /**
     * 批量保存或更新 同步物流商物流地址
     * @param list
     */
    void batchSaveOrUpdateLogisticsAddress(List<LogisticsAddressEntity> list);

    /**
     * 根据类型和店铺获取地址列表
     * @param dto
     * @return
     */
    List<LogisticsAddressDTO.ListDTO> listAddressByType(LogisticsAddressDTO.AddressByTypeDTO dto);

    PagingVO<LogisticsAddressDTO.PagingViewDTO> exportLogisticsAddress(PagingDTO<LogisticsAddressDTO.ExportDTO> dto);

    PagingVO<LogisticsAddressDTO.ListDTO> pagingSelect(PagingDTO<LogisticsAddressDTO.SelectDTO> dto);
}
