package ru.vesolyydrug;

public class FollowMeCar {
    private int id;
    private int currentPosition;
    private boolean isAvailable;
    private Destination destination;
    private int currentPlane;
    private int currentDestinationPoint;

    public FollowMeCar(int id, int currentPosition) {
        this.id = id;
        this.currentPosition = currentPosition;
        this.isAvailable = true;
    }

    public int getId() {
        return id;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int position) {
        this.currentPosition = position;
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

    public int getCurrentPlane() {
        return currentPlane;
    }

    public void setCurrentPlane(int currentPlane) {
        this.currentPlane = currentPlane;
    }

    public int getCurrentDestinationPoint() {
        return currentDestinationPoint;
    }

    public void setCurrentDestinationPoint(int currentDestinationPoint) {
        this.currentDestinationPoint = currentDestinationPoint;
    }
}
