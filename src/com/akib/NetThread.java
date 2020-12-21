package com.akib;

import data.Car;
import data.Loader;
import data.TransferImg;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class NetThread extends Thread{
    private Socket socket;

    public NetThread(Socket socket) throws IOException {
        this.socket = socket;
    }

    private static WritableImage clone(Image image) {
        int height = (int) image.getHeight();
        int width = (int) image.getWidth();
        WritableImage writableImage = new WritableImage(width,height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        if (pixelWriter == null) {
            throw new IllegalStateException("IMAGE_PIXEL_READER_NOT_AVAILABLE");
        }

        final PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            throw new IllegalStateException("IMAGE_PIXEL_READER_NOT_AVAILABLE");
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, color);
            }
        }
        return writableImage;
    }

    @Override
    public void run() {
        String clientMessage="default";
        System.out.println("client connected");
        try(ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream input = new ObjectInputStream(socket.getInputStream())){
            clientMessage =(String) input.readObject();
            System.out.println(clientMessage);

            switch(clientMessage){
                case "-get":{
                    //send the default image first
                    Path defaultImgPath = Paths.get("src\\resources\\img\\defaultImg.png");
                    Image defaultImg = new Image(Files.newInputStream(defaultImgPath));
                    TransferImg transfer = new TransferImg();
                    WritableImage transferImage = clone(defaultImg);
                    transfer.setImg(transferImage);
                    output.writeObject(transfer);
                    output.flush();

                    for (Car car: Loader.getInstance().getCarList().getCars()){
                        //writer.write(car)
                        output.writeObject(car);
                        int stock = Loader.getInstance().getCarList().getStock(car);
                        output.writeInt(stock);
                        Path carImgPath = Paths.get(car.getImageLoc());
                        if (defaultImgPath.toAbsolutePath().compareTo(carImgPath.toAbsolutePath()) != 0){
                            Image img = new Image(Files.newInputStream(Paths.get(car.getImageLoc())));
                            WritableImage image = clone(img);
                            TransferImg tr = new TransferImg();
                            tr.setImg(image);
                            output.writeObject(tr);
                        }
                        output.flush();
                    }
                    break;
                }
                case "-updateStock": {
                    Car car =(Car) input.readObject();
                    int amount = input.readInt();
                    boolean result = Loader.getInstance().updateStock(car.getRegistrationNumber(), amount);
                    output.writeBoolean(result);
                    output.flush();
                    break;
                }
                case "-delete":{
                    Car car = (Car) input.readObject();
                    boolean result = Loader.getInstance().deleteCar(car.getRegistrationNumber());
                    output.writeBoolean(result);
                    output.flush();
                    break;
                }
                case "-edit":{
                    Car previous = (Car) input.readObject();
                    Car present = (Car) input.readObject();
                    TransferImg img = (TransferImg) input.readObject();
                    WritableImage image = img.getImg();
                    boolean result = Loader.getInstance().edit(previous,present,image);
                    output.writeBoolean(result);
                    output.flush();
                    break;
                }
                case "-add":{
                    Car car = (Car) input.readObject();
                    TransferImg img = (TransferImg) input.readObject();
                    WritableImage image = img.getImg();
                    boolean result = Loader.getInstance().addCar(car,image);
                    System.out.println(result);
                    output.writeBoolean(result);
                    output.flush();
                    break;
                }
            }
        }
        catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
    }

}
