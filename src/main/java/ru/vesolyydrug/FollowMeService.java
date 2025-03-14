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

        List<Integer> routeToPlane = requestRouteToPlaneOnRunway(car.getCurrentPosition(), planeId);
        car.setCurrentDestinationPoint(routeToPlane.getLast());
        followRoute(car, routeToPlane);

        int destination = requestPlaneParkingSpot();
        List<Integer> routeToParkingSpot = requestRouteForParkingSpot(car.getCurrentPosition(), destination);
        car.setCurrentDestinationPoint(routeToParkingSpot.getLast());
        car.setDestination(Destination.GATE);
        followRoute(car, routeToParkingSpot);

        sendTransportationEndStatus();
        sendUnoSuccess();

    }

    private List<Integer> requestRouteForParkingSpot(int currentPosition, int destination) {
        String url = "http://localhost:8080/dispatcher/plane/follow-me/" + currentPosition + "/" + destination;
        return restTemplate.getForObject(url, List.class);
    }

    private Integer requestPlaneParkingSpot() {
        String url = ""; //todo url для получения перрона у самолета
        return restTemplate.getForObject(url, Integer.class);
    }

    private boolean requestInitialPosition() {
        String url = "http://localhost:8080/dispatcher/garage/follow_me";
        return restTemplate.getForObject(url, Boolean.class);
    }

    private List<Integer> requestRoute(int startPosition, int targetPosition) {
        String url = "http://localhost:8080/dispatcher/route?start=" + startPosition + "&target=" + targetPosition;
        return restTemplate.getForObject(url, List.class);
    }

    private List<Integer> requestRouteToPlaneOnRunway(int startPosition, int planeId) {
        String url = "http://localhost:8080/dispatcher/plane/runway/" + startPosition + "/" + planeId;
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
                boolean requestPermissionToMove = car.getDestination() == Destination.GATE? requestPermissionToMoveToGate(car.getCurrentPosition(), point, car.getCurrentPlane()): requestPermissionToMove(car.getCurrentPosition(), point);
                if (requestPermissionToMove) {
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
                    route = requestRouteToPlaneOnRunway(car.getCurrentPosition(), car.getCurrentPlane());
                } else if (car.getDestination() == Destination.GARAGE) {

                } else if (car.getDestination() == Destination.GATE) {
                    route = requestRouteForParkingSpot(car.getCurrentPosition(), car.getCurrentDestinationPoint());
                }

                followRoute(car, route);
                break;
            }
        }
    }

    private boolean requestPermissionToMoveToGate(int initialPosition, int targetPosition, int planePosition) {
        String url = "http://localhost:8080/ground-service/plane/follow-me/permission/" + initialPosition +"/" + targetPosition +"/" + planePosition;
        boolean canGo = restTemplate.getForObject(url, Boolean.class);
        if (canGo) {
            sendPlaneNewPosition(initialPosition);
        }
        return false;
    }

    private void sendPlaneNewPosition(int initialPosition) {
        String url = ""; //todo URL самолета, который принимает свою новую позицию
        restTemplate.postForEntity(url, initialPosition, Void.class);
    }

    private boolean requestPermissionToMove(int initialPosition, int targetPosition) {
        String url = "http://localhost:8080/dispatcher/point/" + initialPosition + "/" + targetPosition;
        return restTemplate.getForObject(url, Boolean.class);
    }
}