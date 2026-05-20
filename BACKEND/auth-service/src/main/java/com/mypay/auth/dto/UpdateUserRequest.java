package com.mypay.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating an authenticated user's profile.
 *
 * All fields are optional. Only non-null fields will be applied to the underlying
 * {@link com.mypay.auth.entity.User} entity. Email uniqueness is enforced at the
 * service layer; password changes are NOT handled through this DTO.
 */
@Data
public class UpdateUserRequest {

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Size(min = 1, max = 100, message = "Nickname must be between 1 and 100 characters")
    private String userNickname;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    @Pattern(regexp = "^$|^[+0-9 ()-]{6,20}$", message = "Phone format is invalid")
    private String phone;
}
