package com.erp.server.wms.controller.api;


import cn.hutool.core.util.StrUtil;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationAreaTypeEnum;
import com.erp.model.wms.enums.WarehouseLocationStatusEnum;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.server.wms.service.WarehouseLocationService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import java.util.List;

/**
 * 仓库仓位分区
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
@AllArgsConstructor
@RestController
@RequestMapping("/warehouseLocation")
public class WarehouseLocationController extends BaseController {

    private final WarehouseLocationService warehouseLocationService;

    /**
     * 获取仓位下拉列表
     * @return
     */
    @PostMapping(value = "/select")
    public ApiResult<List<WarehouseLocationDTO.LocationListDTO>> select(@RequestParam(value = "warehouseId")String warehouseId) {
        return success(warehouseLocationService.select(warehouseId));
    }

    /**
     * 初始化部分仓位数据
     * @return
     */
    @PostMapping(value = "/init")
    public ApiResult<Void> init(@RequestParam(value = "warehouseId")String warehouseId,
                                @RequestParam(value = "prefix")String prefix) {

        // 新增分区
        WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.AREA.getCode());
        warehouseLocationEntity.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        warehouseLocationEntity.setName("暂存区");
        warehouseLocationEntity.setStatus("");
        warehouseLocationService.save(warehouseLocationEntity);
        String areaId = warehouseLocationEntity.getId();

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("");
        warehouseLocationEntity.setName("空仓位");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        // 新增分区
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.AREA.getCode());
        warehouseLocationEntity.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        warehouseLocationEntity.setName("暂存区");
        warehouseLocationEntity.setStatus("");
        warehouseLocationService.save(warehouseLocationEntity);


        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode(StrUtil.format("{}000001", prefix));
        warehouseLocationEntity.setName(warehouseLocationEntity.getCode());
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode(StrUtil.format("{}000002", prefix));
        warehouseLocationEntity.setName(warehouseLocationEntity.getCode());
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode(StrUtil.format("{}000003", prefix));
        warehouseLocationEntity.setName(warehouseLocationEntity.getCode());
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode(StrUtil.format("{}000004", prefix));
        warehouseLocationEntity.setName(warehouseLocationEntity.getCode());
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode(StrUtil.format("{}000005", prefix));
        warehouseLocationEntity.setName(warehouseLocationEntity.getCode());
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);
        return success();
    }

    /**
     * 初始化部分仓位数据
     * @return
     */
    @PostMapping(value = "/initUat1")
    public ApiResult<Void> initUat(@RequestParam(value = "warehouseId")String warehouseId) {

        // 新增分区
        WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.AREA.getCode());
        warehouseLocationEntity.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        warehouseLocationEntity.setName("暂存区");
        warehouseLocationEntity.setStatus("");
        warehouseLocationService.save(warehouseLocationEntity);
        String areaId = warehouseLocationEntity.getId();

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("");
        warehouseLocationEntity.setName("空仓位");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00001");
        warehouseLocationEntity.setName("劲捷");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00011");
        warehouseLocationEntity.setName("万欣数码");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00121");
        warehouseLocationEntity.setName("中山加宝");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00003");
        warehouseLocationEntity.setName("中山帆影");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00119");
        warehouseLocationEntity.setName("伟成创");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00004");
        warehouseLocationEntity.setName("中山品创");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00002");
        warehouseLocationEntity.setName("中山芯思");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00122");
        warehouseLocationEntity.setName("爱果");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00013");
        warehouseLocationEntity.setName("中山市科曼");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00022");
        warehouseLocationEntity.setName("济美瑞光");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00123");
        warehouseLocationEntity.setName("恩阳电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00026");
        warehouseLocationEntity.setName("日晖达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00015");
        warehouseLocationEntity.setName("鸿喆鑫");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00124");
        warehouseLocationEntity.setName("风之传");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00125");
        warehouseLocationEntity.setName("优至胜");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00027");
        warehouseLocationEntity.setName("精格电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00024");
        warehouseLocationEntity.setName("长冈金属");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00160");
        warehouseLocationEntity.setName("罗德");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00008");
        warehouseLocationEntity.setName("特达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00016");
        warehouseLocationEntity.setName("朗诗格");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00140");
        warehouseLocationEntity.setName("敏智创新");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00144");
        warehouseLocationEntity.setName("小隼");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00019");
        warehouseLocationEntity.setName("励扬");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00257");
        warehouseLocationEntity.setName("豪华联合");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00270");
        warehouseLocationEntity.setName("稳得福");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00296");
        warehouseLocationEntity.setName("稳润达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00309");
        warehouseLocationEntity.setName("福鑫");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00319");
        warehouseLocationEntity.setName("优篮子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00141");
        warehouseLocationEntity.setName("哥芝米");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00310");
        warehouseLocationEntity.setName("艺星");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00382");
        warehouseLocationEntity.setName("腾赛");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00405");
        warehouseLocationEntity.setName("坦途");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00365");
        warehouseLocationEntity.setName("影越");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00413");
        warehouseLocationEntity.setName("万信达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00350");
        warehouseLocationEntity.setName("兴键电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00421");
        warehouseLocationEntity.setName("京新模具");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00025");
        warehouseLocationEntity.setName("小天创意");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00408");
        warehouseLocationEntity.setName("锦澜视听");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00409");
        warehouseLocationEntity.setName("狼锄");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00410");
        warehouseLocationEntity.setName("吉图");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("VEN00366");
        warehouseLocationEntity.setName("奈斯德科技");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        return success();
    }

    /**
     * 初始化部分仓位数据
     * @return
     */
    @PostMapping(value = "/initUat2")
    public ApiResult<Void> initUat2(@RequestParam(value = "warehouseId")String warehouseId) {

        // 新增分区
        WarehouseLocationEntity warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.AREA.getCode());
        warehouseLocationEntity.setCode(WarehouseLocationAreaTypeEnum.PICK.getCode());
        warehouseLocationEntity.setName("暂存区");
        warehouseLocationEntity.setStatus("");
        warehouseLocationService.save(warehouseLocationEntity);
        String areaId = warehouseLocationEntity.getId();

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("");
        warehouseLocationEntity.setName("空仓位");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        // 新增仓位
        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00001");
        warehouseLocationEntity.setName("C-劲捷");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00011");
        warehouseLocationEntity.setName("C-万欣数码");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00121");
        warehouseLocationEntity.setName("C-中山加宝");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00003");
        warehouseLocationEntity.setName("C-中山帆影");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00119");
        warehouseLocationEntity.setName("C-伟成创");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00004");
        warehouseLocationEntity.setName("C-中山品创");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00002");
        warehouseLocationEntity.setName("C-中山芯思");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00122");
        warehouseLocationEntity.setName("C-爱果");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00013");
        warehouseLocationEntity.setName("C-中山市科曼");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00022");
        warehouseLocationEntity.setName("C-济美瑞光");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00123");
        warehouseLocationEntity.setName("C-恩阳电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00026");
        warehouseLocationEntity.setName("C-日晖达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00015");
        warehouseLocationEntity.setName("C-鸿喆鑫");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00124");
        warehouseLocationEntity.setName("C-风之传");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00125");
        warehouseLocationEntity.setName("C-优至胜");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00027");
        warehouseLocationEntity.setName("C-精格电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00024");
        warehouseLocationEntity.setName("C-长冈金属");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00160");
        warehouseLocationEntity.setName("C-罗德");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00008");
        warehouseLocationEntity.setName("C-特达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00016");
        warehouseLocationEntity.setName("C-朗诗格");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00140");
        warehouseLocationEntity.setName("C-敏智创新");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00144");
        warehouseLocationEntity.setName("C-小隼");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00019");
        warehouseLocationEntity.setName("C-励扬");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00257");
        warehouseLocationEntity.setName("C-豪华联合");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00270");
        warehouseLocationEntity.setName("C-稳得福");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00296");
        warehouseLocationEntity.setName("C-稳润达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00309");
        warehouseLocationEntity.setName("C-福鑫");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00319");
        warehouseLocationEntity.setName("C-优篮子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00141");
        warehouseLocationEntity.setName("C-哥芝米");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00310");
        warehouseLocationEntity.setName("C-艺星");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00382");
        warehouseLocationEntity.setName("C-腾赛");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00405");
        warehouseLocationEntity.setName("C-坦途");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00365");
        warehouseLocationEntity.setName("C-影越");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00413");
        warehouseLocationEntity.setName("C-万信达");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00350");
        warehouseLocationEntity.setName("C-兴键电子");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00421");
        warehouseLocationEntity.setName("C-京新模具");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00025");
        warehouseLocationEntity.setName("C-小天创意");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00408");
        warehouseLocationEntity.setName("C-锦澜视听");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00409");
        warehouseLocationEntity.setName("C-狼锄");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00410");
        warehouseLocationEntity.setName("C-吉图");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        warehouseLocationEntity = new WarehouseLocationEntity();
        warehouseLocationEntity.setWarehouseId(warehouseId);
        warehouseLocationEntity.setType(WarehouseLocationTypeEnum.LOCATION.getCode());
        warehouseLocationEntity.setCode("C-VEN00366");
        warehouseLocationEntity.setName("C-奈斯德科技");
        warehouseLocationEntity.setStatus(WarehouseLocationStatusEnum.IDLE.getCode());
        warehouseLocationEntity.setParentId(areaId);
        warehouseLocationService.save(warehouseLocationEntity);

        return success();
    }


}
