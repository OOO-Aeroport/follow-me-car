package ru.vesolyydrug;

import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/get-all-cars")
    public List<FollowMeCar> getAllCars() {
        return followMeService.getCars();
    }
}
