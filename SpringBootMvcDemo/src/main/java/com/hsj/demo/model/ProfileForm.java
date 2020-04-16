package com.hsj.demo.model;


import com.hsj.demo.date.PastLocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sergei
 * @create 2020-04-15
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileForm {
    @Size(min = 2,message = "Please specify a valid twitter handle")
    private String twitterHandle;
    @Email
    @NotEmpty
    private String email;
    @NotNull
    @PastLocalDate
    private LocalDate birthDate;
    @NotEmpty
    private List<String> tastes = new ArrayList<>();
}