package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountApiClient accountApiClient;
    private final PasswordFormValidator passwordFormValidator;

    @Value("${cookie.secure:false}")
    private boolean secure;

    @GetMapping("/mypage")
    public String info(
            Model model
    ) {

        AccountInfoResponse response = accountApiClient.getAccountInfo();
        model.addAttribute("accountInfoResponse", response);

        return "account/account-info";
    }

    @PutMapping("/mypage")
    public String changeName(
            @Valid @ModelAttribute UpdateAccountNameRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/mypage";
        }

        accountApiClient.changeName(request);
        redirectAttributes.addFlashAttribute("successMessage", "회원정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

    @GetMapping("/mypage/change-password")
    public String password(Model model) {
        model.addAttribute("changePasswordForm", new ChangePasswordFormRequest());
        return "account/change-password";
    }

    @PutMapping("/mypage/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordFormRequest request,
            BindingResult bindingResult
    ) {
        passwordFormValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            return "account/change-password";
        }

        accountApiClient.changePassword(new ChangeOwnPasswordRequest(
                request.currentPassword(),
                request.newPassword()
        ));
        return "redirect:/mypage";
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "account/withdraw";
    }

    @DeleteMapping("/withdraw")
    public String deleteAccount(
            @Valid @RequestBody WithdrawAccountRequest request,
            BindingResult bindingResult,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/withdraw";
        }

        accountApiClient.withdraw(request);

        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/login";
    }

}
