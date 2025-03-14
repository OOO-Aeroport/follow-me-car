package ru.vesolyydrug;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class FollowMeService {
    private final Database database;
    private final RestTemplate restTemplate;

    private final int initialPosition = 297;

    public FollowMeService(Database database, RestTemplate restTemplate) {
        this.database = database;
        this.restTemplate = restTemplate;
    }

    public void handleNewPlane(int planeId) {
        FollowMeCar car = database.getAvailableCar(); //todo Сделать так -> если машинки нет, создаем её и кладем в БД, когда их 3 больше не создаем а отправляем существующие у которых статус не in-progress
        if (car == null) {
            System.out.println("No available FollowMeCar.");
            return;
        }

        car.setAvailable(false);
        car.setDestination(Destination.PLANE);

        while (!requestInitialPosition()) {
        }

        car.setCurrentPosition(initialPosition);

        List<Integer> routeToPlane = requestRouteToPlane(car.getCurrentPosition(), planeId);
        followRoute(car, routeToPlane);

    }

    private boolean requestInitialPosition() {
        String url = "http://localhost:8080/dispatcher/garage/follow-me";
        return restTemplate.getForObject(url, Boolean.class);
    }

    private List<Integer> requestRoute(int startPosition, int targetPosition) {
        String url = "http://localhost:8081/dispatcher/route?start=" + startPosition + "&target=" + targetPosition;
        return restTemplate.getForObject(url, List.class);
    }

    private List<Integer> requestRouteToPlane(int startPosition, int planeId) {
        String url = "http://localhost:8081/dispatcher/plane/" + startPosition + "/" + planeId;
        return restTemplate.getForObject(url, List.class);
    }

    private int getPlanePosition(String planeId) {
        String url = "http://localhost:8080/ground-service/plane-position/" + planeId;
        return restTemplate.getForObject(url, Integer.class);
    }

    private int getServicePosition(String planeId) {
        String url = "http://localhost:8080/ground-service/service-position/" + planeId;
        return restTemplate.getForObject(url, Integer.class);
    }

    private void followRoute(FollowMeCar car, List<Integer> route) {
        for (int point : route) {
            int attempts = 0;
            while (attempts < 5) {
                if (requestPermissionToMove(car.getCurrentPosition(), point)) {
                    car.setCurrentPosition(point);
                    System.out.println("FollowMeCar " + car.getId() + " moved to position " + point);
                    break;
                } else {
                    attempts++;
                    try {
                        Thread.sleep(2000); // Ждем 2 секунды перед повторным запросом
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (attempts == 5) {
                System.out.println("FollowMeCar " + car.getId() + " could not move to point " + point + ". Requesting new route.");

                if (car.getDestination() == Destination.PLANE) {
                    route = requestRouteToPlane(car.getCurrentPosition(), car.getCurrentPlane());
                }

                followRoute(car, route);
                break;
            }
        }
    }

    private boolean requestPermissionToMove(int initialPosition, int targetPosition) {
        String url = "http://localhost:8081/dispatcher/point/" + initialPosition + "/" + targetPosition;
        return restTemplate.getForObject(url, Boolean.class);
    }
}