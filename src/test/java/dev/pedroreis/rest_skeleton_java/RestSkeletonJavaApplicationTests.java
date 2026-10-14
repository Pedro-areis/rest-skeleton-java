package dev.pedroreis.rest_skeleton_java;

import dev.pedroreis.rest_skeleton_java.support.PostgresContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresContainerConfig.class)
class RestSkeletonJavaApplicationTests {

	@Test
	void contextLoads() {
	}

}
