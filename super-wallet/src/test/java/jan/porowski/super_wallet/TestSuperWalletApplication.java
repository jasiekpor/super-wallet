package jan.porowski.super_wallet;

import org.springframework.boot.SpringApplication;

public class TestSuperWalletApplication {

	public static void main(String[] args) {
		SpringApplication.from(SuperWalletApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
