package com.nhnacademy.front.organization.controller;

import com.nhnacademy.front.global.dto.PageResponse;
import com.nhnacademy.front.organization.client.InvitationApiClient;
import com.nhnacademy.front.organization.dto.InvitationStatus;
import com.nhnacademy.front.organization.dto.request.InvitationCreateRequest;
import com.nhnacademy.front.organization.dto.request.InvitationSearchRequest;
import com.nhnacademy.front.organization.dto.response.InvitationSearchResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/organizations/me/invitations")
public class InvitationController {

    private final InvitationApiClient invitationApiClient;

    @GetMapping
    public String invitationList(@ModelAttribute InvitationSearchRequest request,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "10") int size,
                                 Model model) {
        PageResponse<InvitationSearchResponse> invitations = invitationApiClient.getInvitations(request, page, size);

        model.addAttribute("invitations", invitations);
        model.addAttribute("invitationStatuses", InvitationStatus.values());
        model.addAttribute("invitationCreateRequest", new InvitationCreateRequest());

        return "organization/invitation-list";
    }

    @PostMapping
    public String createInvitation(@Valid @ModelAttribute("invitationCreateRequest") InvitationCreateRequest request,
                                   BindingResult bindingResult,
                                   Model model) {
        if (bindingResult.hasErrors()) {
            PageResponse<InvitationSearchResponse> invitations = invitationApiClient.getInvitations(new InvitationSearchRequest(null, null), 0, 10);

            model.addAttribute("invitations", invitations);
            model.addAttribute("invitationStatuses", InvitationStatus.values());

            return "organization/invitation-list";
        }

        invitationApiClient.createInvitation(request);

        return "redirect:/organizations/me/invitations";
    }

    @PostMapping("/{invitation-id}/resend")
    public String resendInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        invitationApiClient.resendInvitation(invitationId);

        return "redirect:/organizations/me/invitations";
    }

    @PostMapping("/{invitation-id}/cancel")
    public String cancelInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        invitationApiClient.cancelInvitation(invitationId);

        return "redirect:/organizations/me/invitations";
    }

    @PostMapping("/{invitation-id}/reissue")
    public String reissueInvitation(@PathVariable(name = "invitation-id") Long invitationId) {
        invitationApiClient.reissueInvitation(invitationId);

        return "redirect:/organizations/me/invitations";
    }
}
