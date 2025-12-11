package de.jdufner.webscraper.picturechoice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PictureChoiceController {

    @GetMapping("/picture-choice")
    public String greeting(Model model) {
        model.addAttribute("picture1", "");
        model.addAttribute("picture2", "");
        return "picture-choice";
    }

}
