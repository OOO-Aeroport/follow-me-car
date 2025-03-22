package ru.vesolyydrug;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follow-me")
public class FollowMeController {
    private final FollowMeService followMeService;

    public FollowMeController(FollowMeService followMeService) {
        this.followMeService = followMeService;
    }

    @GetMapping("/handle-new-plane/{plane-id}/{order-id}")
    public String handleNewPlane(@PathVariable("plane-id") int planeId, @PathVariable("order-id") int orderId) {
        followMeService.handleNewPlane(planeId, orderId);
        return "FollowMeCar обрабатывает новый самолет: " + planeId;
    }
}
