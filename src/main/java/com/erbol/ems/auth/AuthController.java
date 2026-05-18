package com.erbol.ems.auth;

import com.erbol.ems.common.exception.EmailAlreadyUsedException;
import com.erbol.ems.user.UserService;
import com.erbol.ems.user.UserType;
import com.erbol.ems.user.dto.UserRegisterDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("userRegisterDto", new UserRegisterDto());
        model.addAttribute("roles", new UserType[]{UserType.ORGANIZER, UserType.ATTENDEE});
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute UserRegisterDto dto,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles",
                    new UserType[]{UserType.ORGANIZER, UserType.ATTENDEE});
            return "register";
        }

        try {
            userService.register(dto);
        } catch (EmailAlreadyUsedException ex) {
            bindingResult.rejectValue("email", "email.exists", ex.getMessage());
            model.addAttribute("roles",
                    new UserType[]{UserType.ORGANIZER, UserType.ATTENDEE});
            return "register";
        }

        return "redirect:/login?registered=true";
    }
}