package data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CarList {
    private final HashMap<Car,Integer> cars;

    public CarList(){
        this.cars = new HashMap<>();
    }

    public boolean addCar(Car car,int stock){
        if (cars.containsKey(car)){
            //no need to proceed the car is already in the list
            return false;
        }
        this.cars.put(car,stock);
        return true;
    }

    public boolean deleteCar(String registrationNumber){
        Car newCar = this.searchCar(registrationNumber);
        if (newCar != null){
            this.cars.remove(newCar);
            return true;
        }
        return false;
    }

    private Car searchCar(String registrationNumber){
        for (Car c: this.cars.keySet()){
            if (c.getRegistrationNumber().equalsIgnoreCase(registrationNumber)){
                return c;
            }
        }
        return null;
    }

    public int getStock(Car car){
        if (cars.containsKey(car)){
            return cars.get(car);
        }
        return -1;
    }

    public List<Car> getCars() {
        return new ArrayList<>(cars.keySet());
    }

    public boolean updateStock(String registration,int amount) {
        Car car = this.searchCar(registration);
        int previous = this.cars.get(car);
        if (previous<amount){
            //can't not enough amount to sell
            return false;
        }

        this.cars.put(car,previous - amount);
        return true;
    }

    public boolean addStock(String registrationNumber, int amount) {
        Car car = this.searchCar( registrationNumber);
        if (amount>0) {
            if (car != null) {
                int previous = cars.get(car);
                cars.put(car, previous + amount);
                return true;
            }
        }
        return false;
    }
}
