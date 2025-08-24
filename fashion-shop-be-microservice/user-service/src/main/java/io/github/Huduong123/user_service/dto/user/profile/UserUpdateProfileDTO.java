package io.github.Huduong123.user_service.dto.user.profile;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateProfileDTO {
    @NotBlank(message = "email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = "^[\\w.+\\-]+@gmail\\.com$", message = "Email phải đúng định dạng @gmail.com")
    private String email;
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 6, message = "Họ và tên phải trên 5 ký tự")
    private String fullname;
    @NotBlank(message = "phone không được để trống")
    @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại phải đúng định dạng 0xxxxxxxxx")
    private String phone;
    @NotBlank(message = "Giới tính không được để trống")
    private String gender;
    @NotNull(message = "ngày sinh không được để trống")
    @JsonProperty("birth_day")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate birthday;
}
