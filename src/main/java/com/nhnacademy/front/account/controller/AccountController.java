package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountPasswordRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import com.nhnacademy.front.global.security.AccessTokenCookieManager;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountApiClient accountApiClient;
    private final PasswordFormValidator passwordFormValidator;
    private final AccessTokenCookieManager cookieManager;

    @GetMapping("/mypage")
    public String info(
            Model model
    ) {

        AccountInfoResponse response = accountApiClient.getAccountInfo();
        model.addAttribute("accountInfoResponse", response);

        return "account/account_info";
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
        return "account/change_password";
    }

    @PutMapping("/mypage/change-password")
    public String changePassword(
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordFormRequest request,
            BindingResult bindingResult
    ) {
        passwordFormValidator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            return "account/change_password";
        }

        accountApiClient.changePassword(new UpdateAccountPasswordRequest(request.newPassword()));
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

        cookieManager.delete(response);

        return "redirect:/login";
    }

}
