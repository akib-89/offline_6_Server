package data;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Loader {
    private final static Loader instance = new Loader();
    private final static String filename = "src//resources//cars.txt";
    private final static Path defaultImgPath = Paths.get("src\\resources\\img\\defaultImg.png");

    private final CarList carList;

    private Loader() {
        this.carList = new CarList();
    }

    public static Loader getInstance() {
        return instance;
    }

    public CarList getCarList() {
        return carList;
    }

    public synchronized void read(){
        try(BufferedReader reader = Files.newBufferedReader(Paths.get(filename))){
            String line;
            while ((line = reader.readLine()) != null){
                String [] fields = line.split(",");
                String registrationNumber = fields[0];
                int yearMade = Integer.parseInt(fields[1]);
                String [] colors = new String[3];
                System.arraycopy(fields, 2, colors, 0, 3);
                String manufacture = fields[5];
                String model = fields[6];
                double price = Double.parseDouble(fields[7]);
                String imgLoc = fields[8];
                int stock = Integer.parseInt(fields[9]);
                Car car = new Car(registrationNumber,yearMade,colors,manufacture,model,price);
                car.setImageLoc(imgLoc);
                carList.addCar(car,stock);
            }
            System.out.println("successful");
        }catch (IOException e){

            e.printStackTrace();
        }

    }
    public synchronized void write(){
        try(BufferedWriter writer = Files.newBufferedWriter(Paths.get(filename))) {
            for (Car car : carList.getCars()) {
                writer.write(car.toString() +"," + carList.getStock(car));
                writer.newLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public synchronized boolean updateStock(String registration,int amount) {
        boolean b = carList.updateStock(registration,amount);
        this.write();
        return b;
    }

    public synchronized boolean deleteCar(String registration) {
        boolean b = carList.deleteCar(registration);
        this.write();
        return b;
    }

    public synchronized boolean edit(Car prev, Car car, WritableImage image) {
        //car has garbage value in location
        if (prev == null || car == null || image == null){
            return false;
        }

        if (!carList.getCars().contains(prev)){
            return false;
        }

        int stock = carList.getStock(prev);
        Path prevPath = Paths.get(prev.getImageLoc());
        if (!prevPath.endsWith(defaultImgPath)){
            try{
                Files.deleteIfExists(prevPath);
            }catch (IOException e){
                System.out.println("error in deleting previous file from database");
            }
        }
        boolean result = false;
        File file = new File("src\\resources\\img\\"+car.getRegistrationNumber()+".png");
        RenderedImage ri = SwingFXUtils.fromFXImage(image,null);
        try{
            result = ImageIO.write(ri,"png",file);
            car.setImageLoc("src\\resources\\img\\"+car.getRegistrationNumber()+".png");
        }catch (IOException e){
            e.printStackTrace();
        }
        if (result) {
            prev.setImageLoc(car.getImageLoc());
            prev.setColors(car.getColors());
            prev.setPrice(car.getPrice());
            System.out.println(prev);
            System.out.println(car);
            carList.deleteCar(prev.getRegistrationNumber());
            carList.addCar(prev, stock);
        }
        this.write();
        return  result;
    }

    public boolean addCar(Car car, WritableImage image) {
        if (car == null || image == null){
            return false;
        }
        if (carList.getCars().contains(car)){
            System.out.println("contains");
            return false;
        }else{
            System.out.println("not contains");
        }
        boolean result = false;
        File file = new File("src\\resources\\img\\"+car.getRegistrationNumber()+".png");
        RenderedImage ri = SwingFXUtils.fromFXImage(image,null);
        try{
            result = ImageIO.write(ri,"png",file);
            car.setImageLoc("src\\resources\\img\\"+car.getRegistrationNumber()+".png");
        }catch (IOException e){
            e.printStackTrace();
        }
        carList.addCar(car,0);
        this.write();
        return result;
    }

    public boolean addStock(String registrationNumber, int amount) {
        boolean result =  carList.addStock(registrationNumber,amount);
        this.write();
        return result;
    }
}
