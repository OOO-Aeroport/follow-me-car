package ru.vesolyydrug;

import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<FollowMeCar> cars;

    public Database() {
        cars = new ArrayList<>();
        cars.add(new FollowMeCar(1, 0));
        cars.add(new FollowMeCar(2, 0));
        cars.add(new FollowMeCar(3, 0));
        cars.add(new FollowMeCar(4, 0));
        cars.add(new FollowMeCar(5, 0));
        cars.add(new FollowMeCar(6, 0));
        cars.add(new FollowMeCar(7, 0));
        cars.add(new FollowMeCar(8, 0));
    }

    public FollowMeCar getAvailableCar() {
        for (FollowMeCar car : cars) {
            if (car.isAvailable()) {
                return car;
            }
        }
        return null;
    }
}
