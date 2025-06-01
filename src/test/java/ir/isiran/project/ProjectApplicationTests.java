package ir.isiran.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import ir.isiran.project.service.CamundaService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ProjectApplicationTests {

	@MockBean
	private CamundaService camundaService;

	@Test
	void contextLoads() {
	}

}
