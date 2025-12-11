package de.jdufner.webscraper.picturechoice;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PictureChoiceController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PictureChoiceController.class);

    @NonNull
    private final PictureSelector pictureSelector;

    public PictureChoiceController(@NonNull PictureSelector pictureSelector) {
        this.pictureSelector = pictureSelector;
    }

    @GetMapping("/picture-choice")
    public String pictureChoice(Model model) {
        Picture[] pictures = pictureSelector.selectTwoRandomPictures();
        LOGGER.debug("Selected picture1: {}, {}", pictures[0], pictures[1]);
        model.addAttribute("picture1", pictures[0].htmlFileName());
        model.addAttribute("picture2", pictures[1].htmlFileName());
        return "picture-choice";
    }

}
