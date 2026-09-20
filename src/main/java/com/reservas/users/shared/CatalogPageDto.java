package com.reservas.users.shared;

import com.reservas.commons.dto.CatalogDto;
import com.reservas.commons.dto.PageMetadataDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CatalogPageDto {

	private List<CatalogDto> content;
	private PageMetadataDto page;
}
