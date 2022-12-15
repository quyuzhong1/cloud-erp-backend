package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpShopInfoChangeDTO;
import com.erp.model.bi.dto.DmpShopInfoDeptChangeDTO;
import com.erp.model.bi.dto.DmpShopInfoShowDTO;
import com.erp.model.bi.dto.DmpShopInfoSearchDTO;
import com.erp.model.dmp.dto.DmpShopInfoDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 14:43
 */
public interface DmpShopInfoService extends IService<DmpShopInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 14:56
     * @param dto
     * @return PagingVO<DmpShopInfoDTO>
     */
    PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto);

    /**
     * @description: 根据id查询店铺数据
     * @author Will
     * @date: 2022/12/15 16:34
     * @param id
     * @return DmpShopInfoDTO
     */
    DmpShopInfoDTO getDmpShopInfoById(String id);
    /**
     * @description: 新增
     * @author Will
     * @date: 2022/12/15 13:58
     * @param dto
     * @return Boolean
     */
    Boolean addDmpShopInfo(DmpShopInfoDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2022/12/15 13:58
     * @param dto
     * @return Boolean
     */
    Boolean updateDmpShopInfo(DmpShopInfoDTO dto);
    /**
     * @description: 变更负责人
     * @author Will
     * @date: 2022/12/15 13:59
     * @param dto
     * @return Boolean
     */
    Boolean changeChargeName(DmpShopInfoChangeDTO dto);
    /**
     * @description: 变更部门
     * @author Will
     * @date: 2022/12/15 13:59
     * @param dto
     * @return Boolean
     */
    Boolean changeDept(DmpShopInfoDeptChangeDTO dto);

    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 16:55
     * @param dto
     * @param response

     */
    void exportExcel(DmpShopInfoSearchDTO dto, HttpServletResponse response);
}
