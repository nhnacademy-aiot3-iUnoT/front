package com.nhnacademy.front.account.controller;

import com.nhnacademy.front.account.client.AccountApiClient;
import com.nhnacademy.front.account.dto.request.ChangeOwnPasswordRequest;
import com.nhnacademy.front.account.dto.request.ChangePasswordFormRequest;
import com.nhnacademy.front.account.dto.request.ReactivationConfirmRequest;
import com.nhnacademy.front.account.dto.request.UpdateAccountNameRequest;
import com.nhnacademy.front.account.dto.request.WithdrawAccountRequest;
import com.nhnacademy.front.account.dto.response.AccountInfoResponse;
import com.nhnacademy.front.account.validator.PasswordFormValidator;
import com.nhnacademy.front.auth.service.AuthSessionService;
import com.nhnacademy.front.global.error.ApiException;
import com.nhnacademy.front.global.error.ErrorCode;
import com.nhnacademy.front.organization.client.DepartmentApiClient;
import com.nhnacademy.front.organization.client.OrganizationMemberApiClient;
import com.nhnacademy.front.organization.dto.response.DepartmentListResponse;
import com.nhnacademy.front.organization.dto.response.OrganizationMemberRoleResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountApiClient accountApiClient;
    private final DepartmentApiClient departmentApiClient;
    private final OrganizationMemberApiClient memberApiClient;
    private final PasswordFormValidator passwordFormValidator;
    private final AuthSessionService authSessionService;

    @GetMapping("/mypage")
    public String info(Model model) {
        populateAccountInfoModel(model);
        return "account/account-info";
    }

    @GetMapping("/reactivation")
    public String reactivation(
            @RequestParam(required = false) String token,
            Model model
    ) {
        if (!model.containsAttribute("reactivationConfirmRequest")) {
            model.addAttribute(
                    "reactivationConfirmRequest",
                    new ReactivationConfirmRequest(token == null ? "" : token)
            );
        }

        return "account/reactivation";
    }

    @PostMapping("/reactivation/verification")
    public String requestReactivationVerification(
            RedirectAttributes redirectAttributes
    ) {
        try {
            accountApiClient.requestReactivationVerification();
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "인증 메일 발송 요청을 접수했습니다. 이메일을 확인해주세요."
            );
        } catch (ApiException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    reactivationErrorMessage(exception)
            );
        }

        return "redirect:/reactivation";
    }

    @PostMapping("/reactivation/confirm")
    public String confirmReactivation(
            @Valid @ModelAttribute("reactivationConfirmRequest") ReactivationConfirmRequest request,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest httpRequest,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "account/reactivation";
        }

        try {
            accountApiClient.confirmReactivation(request);
        } catch (ApiException exception) {
            model.addAttribute("errorMessage", reactivationErrorMessage(exception));
            return "account/reactivation";
        }

        authSessionService.revoke(httpRequest, response);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                "계정이 재활성화되었습니다. 다시 로그인해주세요."
        );

        return "redirect:/login?reactivated";
    }

    @PutMapping("/mypage")
    public String changeName(
            @Valid @ModelAttribute("nameRequest") UpdateAccountNameRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            populateAccountInfoModel(model);
            return "account/account-info";
        }

        accountApiClient.changeName(request);
        redirectAttributes.addFlashAttribute("successMessage", "회원정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

    private void populateAccountInfoModel(Model model) {
        AccountInfoResponse response = accountApiClient.getAccountInfo();
        OrganizationMemberRoleResponse memberRole = memberApiClient.getRole();
        List<DepartmentListResponse> departments = departmentApiClient.getMyDepartments();

        model.addAttribute("accountInfoResponse", response);
        model.addAttribute("departments", departments);
        model.addAttribute("memberRole", memberRole);

        if (!model.containsAttribute("nameRequest")) {
            model.addAttribute("nameRequest", new UpdateAccountNameRequest(response.name()));
        }
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
            HttpServletRequest httpRequest,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            return "redirect:/withdraw";
        }

        accountApiClient.withdraw(request);

        authSessionService.revoke(httpRequest, response);

        return "redirect:/login";
    }

    private String reactivationErrorMessage(ApiException exception) {
        if (exception.getErrorCode() == ErrorCode.A009) {
            return "인증 링크가 유효하지 않거나 만료되었습니다. 인증 메일을 다시 요청해주세요.";
        }

        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? "계정 재활성화 요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요."
                : exception.getMessage();
    }
}
