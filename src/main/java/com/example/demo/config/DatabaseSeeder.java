package com.example.demo.config;

import com.example.demo.model.Car;
import com.example.demo.repository.CarRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {
  @Autowired
  private CarRepository carRepository;

  @Autowired
  private Environment env;

  @Override
  public void run(String... args) {
    boolean seedDatabase = Boolean.parseBoolean(env.getProperty("seed.database", "false"));

    if (seedDatabase) {
      List<Car> cars = List.of(
              new Car( "Toyota", "Camry", "AUTOMATIC", 2022, 50.0, true, "https://images.unsplash.com/photo-1664287721774-13da4b108b18?q=80&w=2942&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"),
              new Car( "Honda", "Civic", "MANUAL", 2021, 45.0, true, "https://images.unsplash.com/photo-1594070319944-7c0cbebb6f58?q=80&w=2900&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"),
              new Car( "Ford", "Mustang", "AUTOMATIC", 2020, 80.0, true, "https://images.unsplash.com/photo-1655628266959-12ec3f839a46?q=80&w=2942&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"),
              new Car( "Tesla", "Cyber Truck", "AUTOMATIC", 2023, 85.0, true, "https://images.pexels.com/photos/28468181/pexels-photo-28468181/free-photo-of-tesla-cybertruck-with-dog-and-crate-in-nature.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
              new Car( "BMW", "3 Series", "AUTOMATIC", 2019, 35.0, true, "https://images.pexels.com/photos/28522339/pexels-photo-28522339/free-photo-of-blue-bmw-3-series-parked-in-arlington-texas.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
              new Car( "Volkswagen", "Golf", "AUTOMATIC", 2021, 19.0, true, "https://images.pexels.com/photos/14776877/pexels-photo-14776877.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
              new Car( "Mercedes", "Benz", "MANUAL", 2023, 60.0, true, "https://images.pexels.com/photos/2365572/pexels-photo-2365572.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
              new Car( "Audi", "A3", "AUTOMATIC", 2024, 80.0, true, "https://images.pexels.com/photos/27833024/pexels-photo-27833024/free-photo-of-the-audi-a3-is-parked-on-a-dirt-road.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2"),
              new Car( "Lamborghini", "Aventador", "AUTOMATIC", 2024, 100.0, true, "https://images.pexels.com/photos/2127733/pexels-photo-2127733.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2")
      );

      carRepository.saveAll(cars);
      log.info("✅ Database seeded with {} sample car records.", cars.size());
    }
  }
}

