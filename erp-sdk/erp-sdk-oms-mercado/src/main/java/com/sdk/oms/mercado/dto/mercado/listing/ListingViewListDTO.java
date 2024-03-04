package com.sdk.oms.mercado.dto.mercado.listing;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ListingViewListDTO {
    private List<ListingViewDTO> list;
}
