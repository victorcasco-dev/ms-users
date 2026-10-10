package com.reservas.users.dto.request;

import com.reservas.commons.dto.UserTypeDto;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchRequest {

	@Min(0)
	private Integer page;

	@Min(1)
	private Integer size;

	private String q;
	private String firstName;
	private String lastName;
	private String email;
	private UserTypeDto type;
	private Boolean active;
}
