package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.bi.vo.ShopDropDownVO;
import com.erp.model.bi.vo.ShopSiteVO;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.BiShopInfoEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/14 14:43
 */
public interface BiShopInfoService extends IService<BiShopInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 14:56
     * @param dto
     * @return PagingVO<DmpShopInfoDTO>
     */
    PagingVO<DmpShopInfoShowDTO> paging(PagingDTO<DmpShopInfoSearchDTO> dto);

    /**
     * 查询所有店铺
     * @Author Luo_WG
     * @Date 2022/12/26 15:15
     * @return com.erp.common.vo.PagingVO<com.erp.model.bi.dto.DmpShopInfoShowDTO>
     **/
    List<BiShopInfoEntity> shopList();

    /**
     * @description: 根据id查询店铺数据
     * @author Will
     * @date: 2022/12/15 16:34
     * @param id
     * @return BiShopInfoDTO
     */
    BiShopInfoDTO getDmpShopInfoById(String id);
    /**
     * @description: 新增
     * @author Will
     * @date: 2022/12/15 13:58
     * @param dto
     * @return Boolean
     */
    Boolean addDmpShopInfo(BiShopInfoDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2022/12/15 13:58
     * @param dto
     * @return Boolean
     */
    Boolean updateDmpShopInfo(BiShopInfoDTO dto);
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
    /**
     * @description: 根据平台、站点、店铺名称查询店铺是否存在
     * @author Will
     * @date: 2022/12/16 10:35
     * @param platform 平台
     * @param site 站点
     * @param shopName 店铺名称
     * @return Integer
     */
    Integer getDmpShopInfoByParam(String platform, String site, String shopName);


    /**
     * 店铺站点分类
     * @author yl
     * @date 2022-12-28 17:57
     * @param
     * @return java.util.List<com.erp.model.bi.vo.ShopCategoryVO>
     */
    List<ShopSiteVO> getShopCategoryList();


    /**
     * 根据 shopno 获取对应的店铺信息
     * @author yl
     * @date 2023-04-18 10:14
     * @param shopNoList
     * @return java.util.List<com.erp.model.dmp.entity.DmpShopInfoEntity>
     */
    List<BiShopInfoEntity> getByShopNoList(List<String> shopNoList);

    /**
     * 根据店铺名查询数据
     * @param shopNameList
     * @return
     */
    List<BiShopInfoEntity> listByNames(List<String> shopNameList);

    /**
     * 获取店铺下拉列表
     * @param status
     * @return
     */
    List<ShopDropDownVO.ShopDropDownNameVO> listShopDropDown(Integer status);

    /**
     * 获取站点列表
     * @return
     */
    List<ShopDropDownVO.ShopDropDownNameVO> listSiteDropDown();

    /**
     * 获取店铺信息 根据 非空的店铺标识
     *@parms
     *@return
     *@author yl
     *@date 2023-11-21
     */
    List<BiShopInfoEntity> listByStoreSign();
}
