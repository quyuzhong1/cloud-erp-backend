package com.erp.server.dmp.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapValueDTO;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API字段映射
 * @author Will
 * @version 1.0
 * @date 2023/1/11 11:47
 */
@RestController
@RequestMapping("dmp/cfgApiFieldMap")
public class CfgApiFieldMapController extends BaseController {

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private KingdeeProductDetailService kingdeeProductDetailService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult<PagingVO<CfgApiFieldMapVO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgApiFieldMapVO>> queryByPage(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<CfgApiFieldMapVO> pagingVO = cfgApiFieldMapService.paging(dto);
        return success(pagingVO);
    }


   /**
    * 新增
    * @author Will
    * @date: 2023/1/11 12:13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        Boolean flag = this.cfgApiFieldMapService.insert(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 批量新增
     * @author Will
     * @date: 2023/1/11 12:13
     * @param list
     * @return ApiResult
     */
    @PostMapping("/batchAdd")
    public ApiResult batchAdd(@RequestBody @Validated List<CfgApiFieldMapDTO> list) {
        Boolean flag = this.cfgApiFieldMapService.batchAdd(list);
        return flag == true ? success() : failure();
    }


    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 12:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated CfgApiFieldMapDTO dto) {
        this.cfgApiFieldMapService.update(dto);
        return success();
    }

    /**
     * 删除
     * @author Will
     * @date: 2023/1/11 12:19
     * @param ids
     * @return ApiResult
     */
    @PostMapping("/batchDelete")
    public ApiResult batchDelete(@RequestBody @Validated List<String> ids){
        this.cfgApiFieldMapService.batchDelete(ids);
        return success();
    }

    /**
     * 查询单条数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param id
     * @return ApiResult
     */
    @GetMapping("/getById")
    public ApiResult getById(@Param("id") String id){
        CfgApiFieldMapDTO dto = this.cfgApiFieldMapService.getCfgApiFieldMapById(id);
        return success(dto);
    }

    /**
     * 查询明细数据
     * @author Will
     * @date: 2023/1/11 12:23
     * @param fieldMapId
     * @return ApiResult
     */
    @GetMapping("/listDetails")
    public ApiResult listDetails(@Param("fieldMapId") String fieldMapId){
       List<CfgApiFieldMapValueDTO> list = this.cfgApiFieldMapService.listDetails(fieldMapId);
        return success(list);
    }

    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 12:13
     * @return ApiResult
     */
    @GetMapping("/pushProductDetail")
    public ApiResult pushProductDetail() {
        Map<String,Object> map = new HashMap<>();
        //id
        map.put("id","444");
        //sku
        map.put("skuNo","OJOHNFIDJFI");
        //sku
        map.put("name","pppp");
        //spu
        map.put("spuNo","pppp");
        //产品功能描述
        map.put("functionDesc","pppp");
        //属性
        map.put("property","7777");
        //单位
        map.put("unitName","Pcs");
        //一级分类名称
        map.put("oneLevelCategory","");
        //二级分类名称
        map.put("secondLevelCategory","7777");
        //产品经理
        map.put("chargeName","777");
        //销售信息
        //上市时间
        map.put("listingTime","23");
        //物流信息
        //报关中文名
        map.put("declareChineseName","23");
        //报关英文名
        map.put("declareEnglishName","23");
        //报关申报价
        map.put("declarePrice","23");
        //产品属性（是否带电）
        map.put("productProperty_electric","23");
        //产品属性（是否带磁）
        map.put("productProperty_magnetism","23");
        //海关编码
        map.put("customsCode","23");
        //申报要素
        map.put("declareElement","23");
        //毛重
        map.put("grossWeight", 22);
        //净重
        map.put("netWeight", 22);
        //产品尺寸
        //产品尺寸-长(cm)
        map.put("productSize_length",  333333333);
        //产品尺寸-宽(cm)
        map.put("productSize_width",  2233333);
        //产品尺寸-高(cm)
        map.put("productSize_height",  224444);
        //单箱数量
        map.put("boxQty", 22);
        //单箱重量
        map.put("boxWeight", "22");
        //单箱尺寸
        //产品尺寸-长(cm)
        map.put("boxSize_length", "22");
        //产品尺寸-宽(cm)
        map.put("boxSize_width", "22");
        //产品尺寸-高(cm)
        map.put("boxSize_height", "22");
        //实际不含税成本
        map.put("actualNoTaxCost","22");
        //实际含税成本
        map.put("actualTaxCost", "22");
        map.put("moq","22");
        //采购员
        map.put("purchaseUser","7777");
        map.put("mainSupplier","7777");
        this.kingdeeProductDetailService.pushProductDetail(map);
        return success();
    }


}
