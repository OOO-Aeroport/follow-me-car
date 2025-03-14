package ru.vesolyydrug;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow-me")
public class FollowMeController {
    private final FollowMeService followMeService;

    @Autowired
    public FollowMeController(FollowMeService followMeService) {
        this.followMeService = followMeService;
    }

    @PostMapping("/handle-new-plane/{plane-id}")
    public String handleNewPlane(@PathVariable("plane-id") int planeId) {
        followMeService.handleNewPlane(planeId);
        return "FollowMeCar is handling the new plane: " + planeId;
    }
}
