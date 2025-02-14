package com.card.Credit_card.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
public class SignUpDto {
    @Email(message = "Enter a valid email address.")
    @NotBlank(message = "Email id is mandatory.")
    private String emailId;
    @NotBlank(message = "Password is mandatory.")
    private String password;
    @NotBlank(message = "First name is mandatory.")
    private String firstName;
    private String lastName;
    @NotBlank(message = "Mobile number is mandatory.")
    private String mobileNumber;
}
