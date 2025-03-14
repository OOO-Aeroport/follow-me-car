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

    public void handleNewPlane(int planeId, long orderId) {
        FollowMeCar car = database.getAvailableCar();
        if (car == null) {
            System.out.println("No available FollowMeCar.");
            return;
        }

        car.setAvailable(false);
        car.setDestination(Destination.PLANE);
        car.setCurrentPlane(planeId);
        while (!requestInitialPosition()) {
        }

        car.setCurrentPosition(initialPosition);

        List<Integer> routeToPlane = requestRouteToPlaneOnRunway(car.getCurrentPosition(), planeId);
        car.setCurrentDestinationPoint(routeToPlane.getLast());
        car.setCurrentPlanePosition(routeToPlane.getLast());
        followRoute(car, routeToPlane);

        int destination = requestPlaneParkingSpot(planeId);
        List<Integer> routeToParkingSpot = requestRouteForParkingSpot(car.getCurrentPosition(), destination);
        routeToParkingSpot.add(routeToParkingSpot.getLast() - 1);
        car.setCurrentDestinationPoint(routeToParkingSpot.getLast());
        car.setDestination(Destination.GATE);
        routeToParkingSpot.remove(0);
        followRoute(car, routeToParkingSpot);

//        sendTransportationEndStatus();
        sendUnoSuccess(orderId);

    }


    private void sendUnoSuccess(long orderId) {
        String url = "http://26.53.143.176/uno/api/v1/order/successReport/"+ orderId +"/follow-me";
        restTemplate.getForObject(url, Void.class);
    }

    private List<Integer> requestRouteForParkingSpot(int currentPosition, int destination) {
        String url = "http://26.21.3.228:5555/dispatcher/plane/follow-me/" + currentPosition + "/" + destination;
        return restTemplate.getForObject(url, List.class);
    }

    private Integer requestPlaneParkingSpot(int planeId) {
        String url = "http://26.125.155.211:5555/follow-me/" + planeId; //todo url для получения перрона у самолета
        String object = restTemplate.getForObject(url, String.class);
        return Integer.parseInt(object);
    }

    private boolean requestInitialPosition() {
        String url = "http://26.21.3.228:5555/dispatcher/garage/follow_me";
        return restTemplate.getForObject(url, Boolean.class);
    }


    private List<Integer> requestRouteToPlaneOnRunway(int startPosition, int planeId) {
        String url = "http://26.21.3.228:5555/dispatcher/plane/runway/" + startPosition + "/" + planeId;
        return restTemplate.getForObject(url, List.class);
    }


    private void followRoute(FollowMeCar car, List<Integer> route) {
        for (int point : route) {
            int attempts = 0;
            while (attempts < 5) {
                boolean requestPermissionToMove = car.getDestination() == Destination.GATE? requestPermissionToMoveToGate(car.getCurrentPosition(), point, car.getCurrentPlanePosition(), car.getCurrentPlane()): requestPermissionToMove(car.getCurrentPosition(), point);
                if (requestPermissionToMove) {
                    if (car.getDestination() == Destination.GATE) {
                        car.setCurrentPlanePosition(car.getCurrentPosition());
                    }
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

    private boolean requestPermissionToMoveToGate(int initialPosition, int targetPosition, int planePosition, int planeId) {
        String url = "http://26.21.3.228:5555/dispatcher/plane/follow-me/permission/" + initialPosition +"/" + targetPosition +"/" + planePosition;
        boolean canGo = restTemplate.getForObject(url, Boolean.class);
        if (canGo) {
            sendPlaneNewPosition(initialPosition, planeId);
            return true;
        }
        return false;
    }

    private void sendPlaneNewPosition(int initialPosition, int planeId) {
        String url = "http://26.125.155.211:5555/update_location/" + planeId; //todo URL самолета, который принимает свою новую позицию
        restTemplate.postForEntity(url, initialPosition, Void.class);
    }

    private boolean requestPermissionToMove(int initialPosition, int targetPosition) {
        String url = "http://26.21.3.228:5555/dispatcher/point/" + initialPosition + "/" + targetPosition;
        return restTemplate.getForObject(url, Boolean.class);
    }
}