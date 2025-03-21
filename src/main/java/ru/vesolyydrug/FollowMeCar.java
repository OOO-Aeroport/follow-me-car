package ru.vesolyydrug;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FollowMeCar {
    private int carId;
    private int orderId;
    private int currentPosition;
    private boolean isAvailable;
    private Destination destination;
    private int currentPlaneId;
    private int currentDestinationPoint;

    private Queue<Integer> route = new LinkedList<>();

    private int currentPlanePosition;

    private int proceedingToPointFails;

    public FollowMeCar(int orderId, int currentPlaneId) {
        this.orderId = orderId;
        this.currentPlaneId = currentPlaneId;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public int getCurrentPlaneId() {
        return currentPlaneId;
    }

    public void setCurrentPlaneId(int currentPlaneId) {
        this.currentPlaneId = currentPlaneId;
    }

    public int getCurrentDestinationPoint() {
        return currentDestinationPoint;
    }

    public void setCurrentDestinationPoint(int currentDestinationPoint) {
        this.currentDestinationPoint = currentDestinationPoint;
    }

    public Queue<Integer> getRoute() {
        return route;
    }

    public void setRoute(Queue<Integer> route) {
        this.route = route;
    }

    public int getCurrentPlanePosition() {
        return currentPlanePosition;
    }

    public void setCurrentPlanePosition(int currentPlanePosition) {
        this.currentPlanePosition = currentPlanePosition;
    }

    public int getProceedingToPointFails() {
        return proceedingToPointFails;
    }

    public void setProceedingToPointFails(int proceedingToPointFails) {
        this.proceedingToPointFails = proceedingToPointFails;
    }

    public void incrementProceedingToPointFails() {
        proceedingToPointFails++;
    }

    public void setRoutePoints(List<Integer> route) {
        this.route.clear();
        this.route.addAll(route);
        System.out.print("Маршрут:");
        for (Integer el : this.route) {
            System.out.print(" " + el);
        }
        System.out.println();
    }
}
