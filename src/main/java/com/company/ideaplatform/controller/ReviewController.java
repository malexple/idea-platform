package com.company.ideaplatform.controller;

import com.company.ideaplatform.config.CurrentUser;
import com.company.ideaplatform.dto.IdeaViewDto;
import com.company.ideaplatform.dto.ReviewDecisionDto;
import com.company.ideaplatform.entity.User;
import com.company.ideaplatform.entity.enums.IdeaStatus;
import com.company.ideaplatform.service.IdeaService;
import com.company.ideaplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/review")
@PreAuthorize("hasAnyRole('REVIEWER', 'ADMIN')")
@RequiredArgsConstructor
public class ReviewController {

    private final IdeaService ideaService;
    private final UserService userService;

    @GetMapping
    public String reviewQueue(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @CurrentUser UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        Page<IdeaViewDto> ideas = ideaService.getIdeasForReview(pageRequest, user.getId());
        model.addAttribute("ideas", ideas);

        return "review/queue";
    }

    @GetMapping("/{number}")
    public String reviewIdea(
            @PathVariable String number,
            @CurrentUser UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        IdeaViewDto idea = ideaService.getIdeaByNumber(number, user.getId());

        model.addAttribute("idea", idea);
        model.addAttribute("reviewForm", new ReviewDecisionDto());
        model.addAttribute("decisions", new IdeaStatus[]{
                IdeaStatus.APPROVED,
                IdeaStatus.REJECTED,
                IdeaStatus.POSTPONED,
                IdeaStatus.DUPLICATE
        });

        return "review/detail";
    }

    @PostMapping("/{number}/decision")
    public String submitDecision(
            @PathVariable String number,
            @Valid @ModelAttribute("reviewForm") ReviewDecisionDto reviewForm,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User reviewer = userService.findByEmail(userDetails.getUsername());
        ideaService.processReviewDecision(number, reviewer, reviewForm);

        redirectAttributes.addFlashAttribute("success", "Решение сохранено");

        return "redirect:/review";
    }

    @PostMapping("/{number}/remove-duplicate")
    public String removeFromDuplicates(
            @PathVariable String number,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());
        ideaService.removeFromDuplicates(number, user);

        redirectAttributes.addFlashAttribute("success", "Заявка убрана из дубликатов");

        return "redirect:/ideas/" + number;
    }
}
