package ru.vesolyydrug;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FollowMeService {
    private final RestTemplate restTemplate;

    List<FollowMeCar> cars = new ArrayList<>();

    public FollowMeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void handleNewPlane(int planeId, int orderId) {

        FollowMeCar followMeCar = new FollowMeCar(orderId, planeId);

        cars.add(followMeCar);

        followMeOrder(followMeCar);

    }

    public void followMeOrder(FollowMeCar car)  {
        leaveGarage(car);

        takeANap(70);

        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Нужно подъехать к самолету.");

        car.setRoutePoints(requestRouteToPlaneOnRunway(car.getCurrentPosition(), car.getCurrentPlaneId()));
        followTheRoute(car, "garageToPlaneOnRunway");

        takeANap(300);

        car.setCurrentPlanePosition(requestCurrentPlanePos(car.getCurrentPlaneId()));


        Integer destination = requestPlaneParkingSpot(car.getCurrentPlaneId());

        car.setCurrentDestinationPoint(destination);

        car.setRoutePoints(requestRouteForParkingSpot(car.getCurrentPosition(), car.getCurrentDestinationPoint()));

        followTheRoute(car, "runwayToPerron");

        sendUnoSuccess(car.getOrderId());
        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Заказ выполнен. Нужно ехать к гаражу.");
        car.setRoutePoints(requestRouteToGarage(car.getCurrentPosition()));
        followTheRoute(car, "garage");
        takeANap(210);
        requestDeleteCarFromMap(car.getCurrentPosition());
        cars.remove(car);
        System.out.println("Машина заехала в гараж");
    }

    private int requestCurrentPlanePos(int planeId) {
        String url = "http://26.125.155.211:5555/current-point/" + planeId;
        String template = restTemplate.getForObject(url, String.class);
        return Integer.parseInt(template);
    }

    public void leaveGarage(FollowMeCar car) {
        while (true) {
            if (requestInitialPosition()) {
                System.out.println("planeId: " + car.getCurrentPlaneId() + ". Получено разрешение на выезд из гаража.");
                car.setCurrentPosition(297);
                break;
            }
        }
    }

    public void followTheRoute(FollowMeCar car, String destination)  {
        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Едем по маршруту. Пункт назначения: " + destination);

        while (!car.getRoute().isEmpty()) {
            takeANap(35);
            proceedToThePoint(car, destination);
        }
    }

    public void takeANap(Integer modelSecsToWait)  {
        waitFor(modelSecsToWait);
    }

    private void waitFor(Integer modelSecsToWait) {
        restTemplate.getForObject("http://26.228.200.110:5555/dep-board/api/v1/time/timeout?timeout=" + modelSecsToWait, Void.class);
    }

    public void proceedToThePoint(FollowMeCar car, String destination) {
        Integer targetPoint = car.getRoute().peek();

        if (destination.equals("runwayToPerron")) {
            if (requestPermissionToMoveToGate(car.getCurrentPosition(), targetPoint, car.getCurrentPlanePosition(), car.getCurrentPlaneId())) {
                car.setCurrentPlanePosition(car.getCurrentPosition());
                sendPlaneNewPosition(car.getCurrentPlanePosition(), car.getCurrentPlaneId());
                car.setCurrentPosition(targetPoint);
                car.setProceedingToPointFails(0);
                car.getRoute().remove();
            }

        } else if (requestPermissionToMove(car.getCurrentPosition(), targetPoint)) {
             System.out.println("planeId: " + car.getCurrentPlaneId() + ". currentPoint: " + car.getCurrentPosition() + ", targetPoint: " + targetPoint);
            car.setCurrentPosition(targetPoint);
            car.setProceedingToPointFails(0);
            car.getRoute().remove();
        } else {
            car.incrementProceedingToPointFails();
            System.out.println("Попытки: " + car.getProceedingToPointFails());
            if (car.getProceedingToPointFails() >= 5) {
                System.out.println("planeId: " + car.getCurrentPlaneId() + ". Машина попала в пробку. Перестроим маршрут.");
                switch (destination) {
                    case "garageToPlaneOnRunway" -> {
                        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Едем по перестроенному маршруту. Пункт назначения: " + destination);
                        car.setRoutePoints(requestRouteToPlaneOnRunway(car.getCurrentPosition(), car.getCurrentPlaneId()));
                    }
                    case "runwayToPerron" -> {
                        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Едем по перестроенному маршруту. Пункт назначения: " + destination);
                        car.setRoutePoints(requestRouteForParkingSpot(car.getCurrentPosition(), car.getCurrentDestinationPoint()));
                    }
                    case "garage" -> {
                        System.out.println("planeId: " + car.getCurrentPlaneId() + ". Едем по перестроенному маршруту. Пункт назначения: " + destination);
                        car.setRoutePoints(requestRouteToGarage(car.getCurrentPosition()));
                    }
                }
            }
        }
    }

    private void sendUnoSuccess(long orderId) {
        String url = "http://26.53.143.176:5555/uno/api/v1/order/successReport/" + orderId + "/follow-me";
        restTemplate.postForEntity(url, "", Void.class);
    }

    private List<Integer> requestRouteForParkingSpot(int currentPosition, int destination) {
        String url = "http://26.34.23.177:5555/dispatcher/plane/follow-me/" + currentPosition + "/" + destination;
        return restTemplate.getForObject(url, List.class);
    }

    private Integer requestPlaneParkingSpot(int planeId) {
        String url = "http://26.125.155.211:5555/follow-me/" + planeId;
        String object = restTemplate.getForObject(url, String.class);
        return Integer.parseInt(object);
    }

    private boolean requestInitialPosition() {
        String url = "http://26.34.23.177:5555/dispatcher/garage/follow_me";
        return restTemplate.getForObject(url, Boolean.class);
    }


    private List<Integer> requestRouteToPlaneOnRunway(int startPosition, int planeId) {
        String url = "http://26.34.23.177:5555/dispatcher/plane/runway/" + startPosition + "/" + planeId;
        return restTemplate.getForObject(url, List.class);
    }

    private List<Integer> requestRouteToGarage(int startPosition) {
        String url = "http://26.34.23.177:5555/dispatcher/" + startPosition + "/garage";
        return restTemplate.getForObject(url, List.class);
    }

    private void requestDeleteCarFromMap(int position) {
        String url = "http://26.34.23.177:5555/dispatcher/garage/free/" + position;
        restTemplate.delete(url);
    }

    private boolean requestPermissionToMoveToGate(int initialPosition, int targetPosition, int planePosition, int planeId) {
        String url = "http://26.34.23.177:5555/dispatcher/plane/follow-me/permission/" + initialPosition + "/" + targetPosition + "/" + planePosition;
        boolean canGo = restTemplate.getForObject(url, Boolean.class);
        if (canGo) {
            sendPlaneNewPosition(initialPosition, planeId);
            return true;
        }
        return false;
    }

    private void sendPlaneNewPosition(int initialPosition, int planeId) {
        String url = "http://26.125.155.211:5555/update_location/" + planeId;
        restTemplate.postForEntity(url, initialPosition, Void.class);
    }

    private boolean requestPermissionToMove(int initialPosition, int targetPosition) {
        String url = "http://26.34.23.177:5555/dispatcher/point/" + initialPosition + "/" + targetPosition;
        return restTemplate.getForObject(url, Boolean.class);
    }
}