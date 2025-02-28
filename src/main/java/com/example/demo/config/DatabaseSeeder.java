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

    if (seedDatabase && carRepository.count() == 0) {
      List<Car> cars = List.of(
              new Car( "Toyota", "Camry", "AUTOMATIC", 2022, 50.0, true, "https://images.unsplash.com/photo-1664287721774-13da4b108b18?q=80&w=2942&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"),
              new Car( "Honda", "Civic", "MANUAL", 2021, 45.0, true, "https://images.unsplash.com/photo-1594070319944-7c0cbebb6f58?q=80&w=2900&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D"),
              new Car( "Ford", "Mustang", "AUTOMATIC", 2020, 80.0, true, "https://images.unsplash.com/photo-1655628266959-12ec3f839a46?q=80&w=2942&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D")
      );

      carRepository.saveAll(cars);
      log.info("✅ Database seeded with {} sample car records.", cars.size());
    }
  }
}

