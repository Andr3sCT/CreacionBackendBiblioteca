package apx.school.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
		System.out.println("El programa está corriendo.");
		System.out.println("" +
				"             _.-;;-._\n" +
				"      '-..-'|   ||   |\n" +
				"      '-..-'|_.-;;-._|\n" +
				"      '-..-'|   ||   |\n" +
				"      '-..-'|_.-''-._|");
	}

}
