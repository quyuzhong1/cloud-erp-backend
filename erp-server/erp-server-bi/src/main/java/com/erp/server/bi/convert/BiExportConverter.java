package com.erp.server.bi.convert;

import com.erp.model.bi.dto.*;
import com.erp.model.bi.dto.excel.*;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName BiExportConverter
 * @description:
 * @date 2023年12月14日
 * @version: 1.0
 */
@Mapper(builder = @Builder(disableBuilder = true))
public interface BiExportConverter {
    BiExportConverter INSTANCE = Mappers.getMapper(BiExportConverter.class);

    @Mappings({
            @Mapping(target = "createTime", source = "createTime" , qualifiedByName="toStrByDate"),
    })
    TargetStaffSettingExportExcelDTO exportUserTargetStaff(BiTargetStaffSettingDTO.PagingViewDTO pagingViewDTO);
    List<TargetStaffSettingExportExcelDTO> exportUserTargetStaff(List<BiTargetStaffSettingDTO.PagingViewDTO> list);
    @Mappings({
            @Mapping(target = "createTime", source = "createTime" , qualifiedByName="toStrByDate"),
    })
    TargetShopSettingExportExcelDTO exportShopTargetStaff(BiTargetShopSettingDTO.PagingViewDTO pagingViewDTO);
    List<TargetShopSettingExportExcelDTO> exportShopTargetStaff(List<BiTargetShopSettingDTO.PagingViewDTO> list);
    @Mappings({
            @Mapping(target = "createTime", source = "createTime" , qualifiedByName="toStrByDate"),
    })
    TargetSkuSettingExportExcelDTO exportSkuTargetStaff(BiTargetSkuSettingDTO.PagingViewDTO pagingViewDTO);
    List<TargetSkuSettingExportExcelDTO> exportSkuTargetStaff(List<BiTargetSkuSettingDTO.PagingViewDTO> list);
    @Mappings({
            @Mapping(target = "createTime", source = "createTime" , qualifiedByName="toStrByDate"),
    })
    TargetCategorySettingExportExcelDTO exportCategoryTargetStaff(BiTargetCategorySettingDTO.PagingViewDTO pagingViewDTO);
    List<TargetCategorySettingExportExcelDTO> exportCategoryTargetStaff(List<BiTargetCategorySettingDTO.PagingViewDTO> list);
    @Mappings({
            @Mapping(target = "createTime", source = "createTime" , qualifiedByName="toStrByDate"),
    })
    TargetNewProductSettingExportExcelDTO exportNewProductTargetStaff(BiTargetNewProductSettingDTO.PagingViewDTO pagingViewDTO);
    List<TargetNewProductSettingExportExcelDTO> exportNewProductTargetStaff(List<BiTargetNewProductSettingDTO.PagingViewDTO> list);

    /**
     * 时间转换类
     * @param localDateTime
     * @return
     */
    @Named("toStrByDate")
    default String toStrByDate(LocalDateTime localDateTime) {
        if(Objects.isNull(localDateTime)){
            return null;
        }
        // 定义日期时间格式化器
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 格式化为字符串
        return localDateTime.format(formatter);
    }
}
