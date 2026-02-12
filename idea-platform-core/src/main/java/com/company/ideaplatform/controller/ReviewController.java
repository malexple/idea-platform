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

    /**
     * Страница управления статусом (для идей в VOTING и далее)
     */
    @GetMapping("/{number}/workflow")
    public String workflowPage(
            @PathVariable String number,
            @CurrentUser UserDetails userDetails,
            Model model) {

        User user = userService.findByEmail(userDetails.getUsername());
        IdeaViewDto idea = ideaService.getIdeaByNumber(number, user.getId());

        model.addAttribute("idea", idea);
        return "review/workflow";
    }

    /**
     * Взять в работу (VOTING → IN_PROGRESS)
     */
    @PostMapping("/{number}/to-progress")
    public String toInProgress(
            @PathVariable String number,
            @RequestParam String jiraLink,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            ideaService.advanceToInProgress(number, jiraLink, user);
            redirectAttributes.addFlashAttribute("success", "Идея взята в работу");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ideas/" + number;
    }

    /**
     * Перевести в пилот (IN_PROGRESS → PILOT)
     */
    @PostMapping("/{number}/to-pilot")
    public String toPilot(
            @PathVariable String number,
            @RequestParam(required = false) String comment,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            ideaService.advanceToPilot(number, comment, user);
            redirectAttributes.addFlashAttribute("success", "Идея переведена в пилот");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ideas/" + number;
    }

    /**
     * Завершить (PILOT/IN_PROGRESS → IMPLEMENTED)
     */
    @PostMapping("/{number}/complete")
    public String complete(
            @PathVariable String number,
            @RequestParam String actualEffect,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            ideaService.completeIdea(number, actualEffect, user);
            redirectAttributes.addFlashAttribute("success", "Идея успешно внедрена!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ideas/" + number;
    }

    /**
     * Отклонить или отложить
     */
    @PostMapping("/{number}/reject")
    public String reject(
            @PathVariable String number,
            @RequestParam IdeaStatus status,
            @RequestParam(required = false) String comment,
            @CurrentUser UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(userDetails.getUsername());
            ideaService.rejectOrPostpone(number, status, comment, user);
            redirectAttributes.addFlashAttribute("success", "Статус изменён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/ideas/" + number;
    }

}
